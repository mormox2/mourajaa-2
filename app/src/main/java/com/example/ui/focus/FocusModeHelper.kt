package com.example.ui.focus

import android.app.Activity
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object FocusModeHelper {
    private const val TAG = "FocusModeHelper"

    /**
     * Activates Screen Pinning (startLockTask) and Immersive Fullscreen Mode.
     */
    fun enableFocusMode(activity: Activity?) {
        if (activity == null) return
        try {
            activity.startLockTask()
            Log.d(TAG, "startLockTask succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "startLockTask failed or not permitted: ${e.message}")
        }
        setImmersive(activity, true)
    }

    /**
     * Deactivates Screen Pinning (stopLockTask) and restores system bars.
     */
    fun disableFocusMode(activity: Activity?) {
        if (activity == null) return
        try {
            activity.stopLockTask()
            Log.d(TAG, "stopLockTask succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "stopLockTask failed: ${e.message}")
        }
        setImmersive(activity, false)
    }

    /**
     * Shows or hides the system status bar and navigation bar.
     */
    fun setImmersive(activity: Activity?, enabled: Boolean) {
        val window = activity?.window ?: return
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (enabled) {
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
