package com.coolest.toolbox.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import de.robv.android.xposed.XposedBridge
import jp.wasabeef.glide.transformations.BlurTransformation
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.concurrent.thread


//implementation 'com.github.bumptech.glide:glide:4.9.0'
//implementation 'com.squareup.okhttp3:okhttp:4.8.1'

fun getTodayWallpaperURL(): String {
	//调用微软必应壁纸API获取json
	var imgUrl: String = ""
	var microsoftApiResponseJson: String? = ""
	val client = OkHttpClient()
	val request = Request.Builder()
		.url("https://cn.bing.com/HPImageArchive.aspx?n=1&format=js&idx=0")
		.build()
	val response = client.newCall(request).execute()
	microsoftApiResponseJson = response.body?.string()
	Log.e("okhttp", "$microsoftApiResponseJson")

	//解析json获取图片地址
	val jsonArray = JSONObject(microsoftApiResponseJson).getJSONArray("images")
	for (i in 0 until jsonArray.length()) {
		val jsonObject = jsonArray.get(i) as JSONObject
		if (jsonObject.has("url")) {
			imgUrl = jsonObject.getString("url")
		}
	}

	return "https://cn.bing.com/$imgUrl"
}

fun setBingDailyWallpaper(imageView: ImageView, context: Context, blurRadius: Int) {
	thread {
		try {
			val imgUrl = getTodayWallpaperURL()
			Log.e("okhttp", imgUrl)
			runOnMainThread {
				try {
					when {
						blurRadius < 1 -> Glide.with(context)
							.load(imgUrl)
							.into(imageView)
						else -> Glide.with(context).load(imgUrl)
							.transform(BlurTransformation(blurRadius))
							.into(imageView)
					}
//					val text = "关于  壁纸来源: Bing每日壁纸"
//					val sb = SpannableStringBuilder(text)
//					sb.setSpan(
//						AbsoluteSizeSpan(ViewUtils_Kotlin.dp2px(context, 34).toInt()),
//						0,
//						4,
//						Spannable.SPAN_INCLUSIVE_INCLUSIVE
//					)
//					sb.setSpan(
//						AbsoluteSizeSpan(ViewUtils_Kotlin.dp2px(context, 12).toInt()),
//						4,
//						text.length,
//						Spannable.SPAN_INCLUSIVE_INCLUSIVE
//					)

//					binding.toolbarLayout.title = sb
				} catch (e: java.lang.Exception) {
					e.printStackTrace()
					XposedBridge.log(e)
				}
			}
		} catch (e: Exception) {
			if (e.toString().contains("Unable to resolve host \"api.github.com\"")) {
				Snackbar.make(
					imageView.rootView,
					"无法连接到必应服务器, 请检查网络",
					Snackbar.LENGTH_LONG
				).show()
			} else {
				e.printStackTrace()
				XposedBridge.log(e)
			}
		}
	}
}