package com.keyrico.keyrisdk.confirmation

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.keyrico.keyrisdk.R
import com.keyrico.keyrisdk.databinding.KeyriDialogConfirmationBinding
import com.keyrico.keyrisdk.entity.session.Session
import com.keyrico.keyrisdk.sec.checkFakeInstance

class ConfirmationBottomDialog(
    override val session: Session,
    override val payload: String,
    blockSwizzleDetection: Boolean = false,
    override val onResult: ((Result<Unit>) -> Unit)?,
) : BaseConfirmationBottomDialog(session, payload, blockSwizzleDetection, onResult) {
    init {
        checkFakeInstance(blockSwizzleDetection)
    }

    private lateinit var binding: KeyriDialogConfirmationBinding

    private val riskAnalytics by lazy(session::riskAnalytics)
    private val mobileTemplateResponse by lazy(session::mobileTemplateResponse)
    private val authenticationDenied by lazy { riskAnalytics?.isDeny() == true }
    private val newBrowserFound by lazy { mobileTemplateResponse?.flags?.isNewBrowser == true }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = KeyriDialogConfirmationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun initUI() {
        initWidgetLocation()
        initMobileLocation()
        initWidgetAgent()
        initButtons()
        initText()
    }

    private fun initWidgetLocation() {
        with(binding) {
            val hasIssue = mobileTemplateResponse?.widget?.issue != null

            llWidgetLocation.isVisible = mobileTemplateResponse?.widget?.location != null
            tvWidgetIssue.isVisible = hasIssue

            tvWidgetLocation.text = mobileTemplateResponse?.widget?.location
            tvWidgetIssue.text = mobileTemplateResponse?.widget?.issue

            getItemColors(hasIssue)?.let {
                tvWidgetLocation.setTextColor(it.first)
                ivWidgetLocation.setColorFilter(it.first)
                tvWidgetIssue.setTextColor(it.second)
                tvWidgetIssue.setDrawableColor(it.second)
            }
        }
    }

    private fun initMobileLocation() {
        with(binding) {
            val hasIssue = mobileTemplateResponse?.mobile?.issue != null

            llMobileLocation.isVisible = mobileTemplateResponse?.mobile?.location != null
            tvMobileIssue.isVisible = hasIssue

            tvMobileLocation.text = mobileTemplateResponse?.mobile?.location
            tvMobileIssue.text = mobileTemplateResponse?.mobile?.issue

            getItemColors(hasIssue)?.let {
                tvMobileLocation.setTextColor(it.first)
                ivMobileLocation.setColorFilter(it.first)
                tvMobileIssue.setTextColor(it.second)
                tvMobileIssue.setDrawableColor(it.second)
            }
        }
    }

    private fun initWidgetAgent() {
        with(binding) {
            val hasIssue = mobileTemplateResponse?.userAgent?.issue != null

            llWidgetAgent.isVisible = mobileTemplateResponse?.userAgent?.name != null
            tvWidgetAgentIssue.isVisible = hasIssue

            tvWidgetAgent.text = mobileTemplateResponse?.userAgent?.name
            tvWidgetAgentIssue.text = mobileTemplateResponse?.userAgent?.issue

            getItemColors(hasIssue)?.let {
                tvWidgetAgent.setTextColor(it.first)
                ivWidgetAgent.setColorFilter(it.first)
                tvWidgetAgentIssue.setTextColor(it.second)
                tvWidgetAgentIssue.setDrawableColor(it.second)
            }
        }
    }

    private fun initButtons() {
        with(binding) {
            cbTrustNewBrowser.isVisible = newBrowserFound && !authenticationDenied
            llButtons.isVisible = !authenticationDenied

            cbTrustNewBrowser.setOnCheckedChangeListener { _, isChecked ->
                trustNewBrowser = isChecked
            }

            bNo.setOnClickListener {
                isAccepted = false
                dismiss()
            }

            bYes.setOnClickListener {
                isAccepted = true
                dismiss()
            }
        }
    }

    private fun initText() {
        with(binding) {
            tvMessage.isVisible = mobileTemplateResponse?.message != null

            tvTitle.text = mobileTemplateResponse?.title
            tvMessage.text = mobileTemplateResponse?.message
        }
    }

    private fun getItemColors(hasIssue: Boolean): Pair<Int, Int>? {
        return if (authenticationDenied) {
            getColor(R.color.keyri_color_red) to getColor(R.color.keyri_color_vpn_red)
        } else if (hasIssue) {
            getColor(R.color.keyri_color_orange) to getColor(R.color.keyri_color_light_orange)
        } else {
            null
        }
    }

    private fun TextView.setDrawableColor(color: Int) {
        this.compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun getColor(resId: Int): Int {
        return ContextCompat.getColor(requireContext(), resId)
    }
}
