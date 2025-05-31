package com.my.raido.Utils

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

object BottomSheetManager {

    fun showSheetSafely(
        activity: FragmentActivity,
        newTag: String,
        createNewSheet: () -> BottomSheetDialogFragment,
        dismissTags: List<String> = emptyList(),
        delayAfterDismiss: Long = 200L
    ) {
        val fm = activity.supportFragmentManager

        // Dismiss previous sheets (if any)
        dismissTags.forEach { tag ->
            (fm.findFragmentByTag(tag) as? BottomSheetDialogFragment)?.dismissAllowingStateLoss()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (fm.findFragmentByTag(newTag) == null &&
                activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            ) {
                createNewSheet().show(fm, newTag)
            }
        }, delayAfterDismiss)
    }

    fun dismissSheetByTag(fm: FragmentManager, tag: String) {
        (fm.findFragmentByTag(tag) as? BottomSheetDialogFragment)?.dismissAllowingStateLoss()
    }

}