package com.coolest.toolbox

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.coolest.toolbox.utils.getTodayWallpaperURL
import java.io.DataInputStream
import java.net.URL
import kotlin.concurrent.thread

class MyApplication : Application() {

	companion object {
		var dailyBingPaper: Bitmap? = null
		var appContext: Context? = null
	}

	override fun onCreate() {
		super.onCreate()

	}
}