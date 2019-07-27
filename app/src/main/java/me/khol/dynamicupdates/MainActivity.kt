package me.khol.dynamicupdates

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import me.khol.dynamicupdates.navigation.Navigation

class MainActivity : BaseSplitActivity() {

    companion object {
        private const val DYNAMIC_MODULE = "dynamic"
        private const val DYNAMIC_USER_CONFIRMATION_RC = 1337
    }

    private val manager by lazy { SplitInstallManagerFactory.create(this) }

    private var sessionId: Int? = null
    private val sessionStateListener = SplitInstallStateUpdatedListener { state ->
        logTextView.append("$sessionId: ${resolveSessionState(state)}\n")
    }

    private val button by lazy { findViewById<Button>(R.id.button) }
    private val logTextView by lazy { findViewById<TextView>(R.id.log) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            if (manager.installedModules.contains(DYNAMIC_MODULE)) {
                openDynamicActivity()
            } else {
                downloadModule(DYNAMIC_MODULE)
            }
        }

        manager.registerListener(sessionStateListener)
    }

    private fun openDynamicActivity() = Navigation.openDynamicActivity(this)

    override fun onDestroy() {
        super.onDestroy()
        manager.unregisterListener(sessionStateListener)
    }

    private fun downloadModule(name: String) {
        val request = SplitInstallRequest.newBuilder().apply {
            addModule(name)
        }.build()

        manager.startInstall(request)
            .addOnSuccessListener { sessionId = it }
            .addOnFailureListener {
                logTextView.append("${resolveSessionError(it)}\n")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DYNAMIC_USER_CONFIRMATION_RC) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    // No need to handle this case, download has been started automatically.
                }
                Activity.RESULT_CANCELED -> Toast.makeText(
                    this,
                    "You must download the extension to proceed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resolveSessionState(state: SplitInstallSessionState): String =
        when (state.status()) {
            SplitInstallSessionStatus.UNKNOWN -> "Unknown"
            SplitInstallSessionStatus.PENDING -> "Pending"
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                manager.startConfirmationDialogForResult(state, this, DYNAMIC_USER_CONFIRMATION_RC)
                "Requires user confirmation"
            }
            SplitInstallSessionStatus.DOWNLOADING -> {
                val done = state.bytesDownloaded()
                val total = state.totalBytesToDownload()
                val progress = done * 100 / total
                "Downloading $progress% | $done/$total"
            }
            SplitInstallSessionStatus.DOWNLOADED -> "Downloaded"
            SplitInstallSessionStatus.INSTALLING -> "Installing"
            SplitInstallSessionStatus.INSTALLED -> {
                openDynamicActivity()
                "Installed"
            }
            SplitInstallSessionStatus.FAILED -> "Failed"
            SplitInstallSessionStatus.CANCELING -> "Canceling"
            SplitInstallSessionStatus.CANCELED -> "Canceled"
            else -> "Unknown"
        }

    private fun resolveSessionError(throwable: Throwable): String =
        if (throwable is SplitInstallException) {
            when (throwable.errorCode) {
                SplitInstallErrorCode.NO_ERROR -> "NO_ERROR"
                SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED -> "ACTIVE_SESSIONS_LIMIT_EXCEEDED"
                SplitInstallErrorCode.MODULE_UNAVAILABLE -> "MODULE_UNAVAILABLE"
                SplitInstallErrorCode.INVALID_REQUEST -> "INVALID_REQUEST"
                SplitInstallErrorCode.SESSION_NOT_FOUND -> "SESSION_NOT_FOUND"
                SplitInstallErrorCode.API_NOT_AVAILABLE -> "API_NOT_AVAILABLE"
                SplitInstallErrorCode.NETWORK_ERROR -> "NETWORK_ERROR"
                SplitInstallErrorCode.ACCESS_DENIED -> "ACCESS_DENIED"
                SplitInstallErrorCode.INCOMPATIBLE_WITH_EXISTING_SESSION -> "INCOMPATIBLE_WITH_EXISTING_SESSION"
                SplitInstallErrorCode.SERVICE_DIED -> "SERVICE_DIED"
                SplitInstallErrorCode.INSUFFICIENT_STORAGE -> "INSUFFICIENT_STORAGE"
                SplitInstallErrorCode.SPLITCOMPAT_VERIFICATION_ERROR -> "SPLITCOMPAT_VERIFICATION_ERROR"
                SplitInstallErrorCode.SPLITCOMPAT_EMULATION_ERROR -> "SPLITCOMPAT_EMULATION_ERROR"
                SplitInstallErrorCode.SPLITCOMPAT_COPY_ERROR -> "SPLITCOMPAT_COPY_ERROR"
                SplitInstallErrorCode.INTERNAL_ERROR -> "INTERNAL_ERROR"
                else -> "Unknown SplitInstall error"
            }
        } else {
            "Unknown error"
        }
}
