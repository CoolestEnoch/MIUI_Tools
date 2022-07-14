package com.coolest.toolbox.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.coolest.toolbox.ui.CoolUtils
import com.github.kyuubiran.ezxhelper.utils.Log
import de.robv.android.xposed.XposedBridge
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class CoolConfigHelper {
	companion object {
		//const val configTxtFile = File(Context().externalCacheDir, "config.txt")
		private var configFileStr =
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				.toString() + "/config.txt"

		//		private var configFileStr = "/storage/emulated/0/Download/config.txt"

/*		fun initJsonConfig(): Boolean {
			try {
				//保存为json
				val obj = JSONObject().apply {
					put("miui_plus_regard_as_phone", false)
				}

				//把json转字符串并保存到本地
				val content = obj.toString()
				val file = File(configTxtFile)

				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile()
				}
				val fw = FileWriter(file.absoluteFile)
				val bw = BufferedWriter(fw)
				bw.write(content)
				bw.close()
			} catch (e: Exception) {
				e.printStackTrace()
										
				return false
			}

			return true
		}*/

		fun initJsonConfig(context: Context): Boolean {
			try {
				//保存为json
				val obj = JSONObject().apply {
					put("miui_plus_connect_pc", !CoolUtils.isPad())
					put("miui_plus_cust_pc_name_switch", false)
					put("miui_plus_pc_name", "")
					put("miui_plus_pc_tail", "")
					put("dolby_enabled", CoolUtils.isPad())
					put("harmanKardon_enabled", true)
				}
				Log.e(obj.toString())

				val myFile = File(configFileStr)
				myFile.printWriter().use { out ->
					out.println(obj.toString())
				}
			} catch (e: Exception) {
				e.printStackTrace()
				return false
			}
			return true
		}


		fun modifyJsonConfig(context: Context, key: String, value: Any): Boolean {
			try {
				//文件不存在就创建
				if (!File(configFileStr).exists()) {
					initJsonConfig(context)
				}


				//读取配置文件
				val jsonContent = StringBuilder()
				val input = FileInputStream(File(configFileStr))
				val reader = BufferedReader(InputStreamReader(input))
				reader.use {
					reader.forEachLine {
						jsonContent.append(it)
					}
				}

				//改配置
				val jsonString = jsonContent.toString()
				val obj = JSONObject(jsonString)
				obj.put(key, value)//修改json配置文件的配置项。put会覆盖旧值

				//保存文件
				val myFile = File(configFileStr)
				myFile.printWriter().use { out ->
					out.println(obj.toString())
				}

			} catch (e: Exception) {
				e.printStackTrace()
				return false
			}

			return true
		}


		fun getJsonConfig(context: Context, key: String): Any? {
			//文件不存在就创建
			val jsonContent = StringBuilder()
			try {
				val input = FileInputStream(File(configFileStr))
				val reader = BufferedReader(InputStreamReader(input))
				reader.use {
					reader.forEachLine {
						jsonContent.append(it)
					}
				}

				val jsonString = jsonContent.toString()
				val json = JSONObject(jsonString)//拿到的配置文件json object
				return json.get(key)
			} catch (e: Exception) {
				e.printStackTrace()
				return null
			}
		}
	}
}