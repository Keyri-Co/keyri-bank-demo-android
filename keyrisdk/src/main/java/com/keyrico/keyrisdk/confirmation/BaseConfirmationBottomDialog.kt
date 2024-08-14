package com.keyrico.keyrisdk.confirmation

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.keyrico.keyrisdk.R
import com.keyrico.keyrisdk.entity.session.Session
import com.keyrico.keyrisdk.exception.DenialException
import com.keyrico.keyrisdk.sec.checkFakeInstance
import com.keyrico.keyrisdk.telemetry.TelemetryCodes
import com.keyrico.keyrisdk.telemetry.TelemetryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseConfirmationBottomDialog(
    protected open val session: Session,
    protected open val payload: String,
    blockSwizzleDetection: Boolean = false,
    protected open val onResult: ((Result<Unit>) -> Unit)?,
) : BottomSheetDialogFragment() {
    init {
        checkFakeInstance(blockSwizzleDetection)
    }

    internal var silentDismiss = false
    protected var isAccepted = false
    protected var trustNewBrowser = false

    abstract fun initUI()

    override fun getTheme(): Int = R.style.KeyriBottomSheetDialogTheme

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(session.riskAnalytics?.isDeny() ?: false)
        initUI()

        TelemetryManager.sendEvent(
            requireContext(),
            TelemetryCodes.CONFIRMATION_SCREEN_LAUNCHED,
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!silentDismiss) {
            activity?.lifecycleScope?.launch(Dispatchers.IO) {
                TelemetryManager.sendEvent(
                    requireContext(),
                    TelemetryCodes.CONFIRMATION_SCREEN_DISMISSED,
                )

                val result =
                    if (isAccepted) {
                        session.confirm(payload, requireContext(), trustNewBrowser)
                    } else {
                        try {
                            session.deny(payload, requireContext())

                            Result.failure(DenialException())
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }

                onResult?.invoke(result)
            }
        }

        super.onDismiss(dialog)
    }
}
