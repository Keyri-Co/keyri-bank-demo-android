package com.keyrico.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import com.keyrico.scanner.databinding.ActivityKeyriScannerAuthBinding
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

internal class ScannerAuthActivity : AppCompatActivity() {
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    private val options by lazy {
        BarcodeScannerOptions
            .Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }

    private val requestPermissionLauncher =
        (this as ComponentActivity).registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openScanner()
            }
        }

    private lateinit var binding: ActivityKeyriScannerAuthBinding

    private val viewModel by viewModels<ScannerAuthVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyriScannerAuthBinding.inflate(layoutInflater)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        initUI()
        observeViewModel()
        openScanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }

    private fun initUI() {
        val ivClose = binding.fabClose
        val topCloseMargin = ivClose.marginTop

        ViewCompat.setOnApplyWindowInsetsListener(ivClose) { view, windowInsets ->
            val topInsets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val bottomInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = topCloseMargin + topInsets
            }

            binding.vInsetPlaceholder.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = bottomInsets
            }

            windowInsets
        }

        ivClose.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect { uiState ->
                    val isLoading =
                        when (uiState) {
                            is ScannerAuthState.Authenticated -> {
                                setResult(RESULT_OK)
                                finish()

                                false
                            }

                            is ScannerAuthState.Error -> {
                                Toast
                                    .makeText(
                                        this@ScannerAuthActivity,
                                        uiState.message,
                                        Toast.LENGTH_LONG,
                                    ).show()

                                false
                            }

                            is ScannerAuthState.Loading -> true
                            is ScannerAuthState.Empty -> false
                        }

                    binding.vProgress.isVisible = isLoading
                }
            }
        }
    }

    private fun openScanner() {
        if (!hasCameraPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        bindCameraUseCases()
    }

    private fun bindCameraUseCases() {
        binding.scannerPreview.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            val lifecycleCameraController = LifecycleCameraController(this)

            lifecycleCameraController.bindToLifecycle(lifecycleOwner)

            lifecycleCameraController.cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            lifecycleCameraController.setImageAnalysisAnalyzer(
                Executors.newSingleThreadExecutor(),
                initQrAnalyzer(),
            )

            binding.scannerPreview.controller = lifecycleCameraController
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initQrAnalyzer(): ImageAnalysis.Analyzer =
        ImageAnalysis.Analyzer { imageProxy ->
            imageProxy.image
                ?.takeIf { viewModel.uiState.value is ScannerAuthState.Empty }
                ?.let {
                    val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)

                    BarcodeScanning
                        .getClient(options)
                        .process(image)
                        .addOnSuccessListener { barcodes ->
                            barcodes
                                .firstOrNull()
                                ?.takeIf { viewModel.uiState.value is ScannerAuthState.Empty }
                                ?.displayValue
                                ?.let(::processScannedData)
                        }.addOnCompleteListener {
                            imageProxy.close()
                        }
                } ?: imageProxy.close()
        }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

    private fun processScannedData(scannedData: String) {
        Log.d(KEYRI_KEY, "QR processed: $scannedData")

        try {
            processLink(scannedData.toUri())
        } catch (e: java.lang.Exception) {
            Log.d(KEYRI_KEY, "Not valid link: $scannedData")
        }
    }

    private fun processLink(data: Uri?) {
        data?.let { url ->
            val appKey = intent.getStringExtra(APP_KEY)
            val publicApiKey = intent.getStringExtra(PUBLIC_API_KEY)
            val serviceEncryptionKey = intent.getStringExtra(SERVICE_ENCRYPTION_KEY)
            val blockEmulatorDetection = intent.getBooleanExtra(BLOCK_EMULATOR_DETECTION, true)
            val blockRootDetection = intent.getBooleanExtra(BLOCK_ROOT_DETECTION, false)
            val blockDangerousAppsDetection = intent.getBooleanExtra(BLOCK_DANGEROUS_APPS_DETECTION, false)
            val blockTamperDetection = intent.getBooleanExtra(BLOCK_TAMPER_DETECTION, false)
            val blockSwizzleDetection = intent.getBooleanExtra(BLOCK_SWIZZLE_DETECTION, false)
            val publicUserId = intent.getStringExtra(PUBLIC_USER_ID)
            val payload = intent.getStringExtra(PAYLOAD)

            viewModel.easyKeyriAuth(
                this,
                url,
                requireNotNull(appKey),
                publicApiKey,
                serviceEncryptionKey,
                requireNotNull(payload),
                publicUserId,
                KeyriDetectionsConfig(
                    blockEmulatorDetection = blockEmulatorDetection,
                    blockRootDetection = blockRootDetection,
                    blockDangerousAppsDetection = blockDangerousAppsDetection,
                    blockTamperDetection = blockTamperDetection,
                    blockSwizzleDetection = blockSwizzleDetection,
                ),
            )
        } ?: Log.e(KEYRI_KEY, "Failed to process link")
    }

    companion object {
        const val APP_KEY = "APP_KEY"
        const val PUBLIC_API_KEY = "PUBLIC_API_KEY"
        const val SERVICE_ENCRYPTION_KEY = "SERVICE_ENCRYPTION_KEY"
        const val BLOCK_EMULATOR_DETECTION = "BLOCK_EMULATOR_DETECTION"
        const val BLOCK_ROOT_DETECTION = "BLOCK_ROOT_DETECTION"
        const val BLOCK_DANGEROUS_APPS_DETECTION = "BLOCK_DANGEROUS_APPS_DETECTION"
        const val BLOCK_TAMPER_DETECTION = "BLOCK_TAMPER_DETECTION"
        const val BLOCK_SWIZZLE_DETECTION = "BLOCK_SWIZZLE_DETECTION"
        const val PUBLIC_USER_ID = "PUBLIC_USER_ID"
        const val PAYLOAD = "PAYLOAD"
    }
}
