package me.khol.dynamicupdates

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat

class App : Application() {

    override fun attachBaseContext(var1: Context) {
        super.attachBaseContext(var1)
        SplitCompat.install(this)
    }
}
