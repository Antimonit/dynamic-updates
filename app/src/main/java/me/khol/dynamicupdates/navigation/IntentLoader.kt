package me.khol.dynamicupdates.navigation

import android.content.Context
import android.content.Intent

private fun intentTo(context: Context, className: String): Intent {
    return Intent(Intent.ACTION_VIEW).setClassName(context.packageName, className)
}

/**
 * Returns [Intent] with this as the target or null if it cannot be resolved.
 */
internal fun String.loadIntentOrNull(context: Context): Intent? {
    return try {
        Class.forName(this).run {
            intentTo(context, this@loadIntentOrNull)
        }
    } catch (e: ClassNotFoundException) {
        null
    }
}
