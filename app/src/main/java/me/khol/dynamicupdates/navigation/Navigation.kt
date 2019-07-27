package me.khol.dynamicupdates.navigation

import android.content.Context
import android.content.Intent

object Navigation {

    private fun dynamicActivityIntent(context: Context): Intent? {
        return "me.khol.dynamic.DynamicActivity".loadIntentOrNull(context)
    }

    fun openDynamicActivity(context: Context) {
        context.startActivity(dynamicActivityIntent(context))
    }
}
