package io.github.twyora.douyinenhancer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.Toast
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.databinding.VerifyDialogBinding

class VerifyDialog(private val hostContext: Context) : AlertDialog.Builder(ContextThemeWrapper(hostContext, R.style.MainTheme)) {
    private val binding = VerifyDialogBinding.inflate(
        LayoutInflater.from(ContextThemeWrapper(context, R.style.MainTheme))
    )

    init {
        setView(binding.root)
        setTitle(R.string.verify_dialog_title)
        setNegativeButton(android.R.string.cancel, null)
        // just shows the positive button; click handling is set in show
        setPositiveButton(android.R.string.ok, null)
    }

    override fun show(): AlertDialog {
        val dialog = super.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val inputUrl = binding.verifyInput.text.toString()
            val valid = context.resources.getStringArray(
                R.array.valid_verification_urls
            ).any {
                inputUrl.contains(it, ignoreCase = true)
            }
            if (valid) {
                ConfigManager.moduleConfig.lastVerifiedVersion = BuildConfig.VERSION_CODE
                dialog.dismiss()

                (hostContext as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.verify_toast_success), Toast.LENGTH_SHORT).show()
                }
            } else {
                (hostContext as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.verify_toast_failure), Toast.LENGTH_SHORT).show()
                }
            }
        }
        return dialog
    }

    companion object {
        private val TAG = this::class.simpleName

        fun show(context: Context) {
            if (!shouldVerify()) {
                return
            }

            runCatching {
                (context as? Activity)?.injectModuleAppResources()
                VerifyDialog(context).show()
            }.onFailure {
                YLog.error("$TAG: failed to show verify dialog", it)
            }
        }

        fun shouldVerify(): Boolean {
            val lastVerifiedVersion = ConfigManager.moduleConfig.lastVerifiedVersion
            return !(BuildConfig.DEBUG || BuildConfig.VERSION_CODE == lastVerifiedVersion)
        }
    }
}
