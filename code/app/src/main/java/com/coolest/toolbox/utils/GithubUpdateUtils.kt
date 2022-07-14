package com.coolest.toolbox.utils

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.LinearLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.coolest.toolbox.R
import com.github.kyuubiran.ezxhelper.utils.runOnMainThread
import com.google.android.material.snackbar.Snackbar
import de.robv.android.xposed.XposedBridge
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.min


/**
 * 结构:
 * 版本号Tag
 * 描述
 * 资源列表: 列表[文件名 - 下载链接 - 创建时间]
 */
class Release(
	val relTag: String,
	val relName: String,
	val relDescription: String,
	val relTime: String,
	val relAssets: List<List<String>>
)

/**
 * 有更新就弹窗, 没更新就弹SnackBar提示是最新版本
 */
fun checkUpdate(
	currentVersion: String,
	swipeRefreshLayout: SwipeRefreshLayout,
	githubUserName: String,
	githubRepoName: String
) {
	swipeRefreshLayout.isRefreshing = true
	Snackbar.make(swipeRefreshLayout.rootView, "正在检查更新...", Snackbar.LENGTH_LONG).show()
	val context = swipeRefreshLayout.rootView.context
	//开始检查更新

	thread {
		try {
			//调用github API获取json
			var githubApiResponse: String? = ""
			val client = OkHttpClient()
			val request = Request.Builder()
				.url("https://api.github.com/repos/$githubUserName/$githubRepoName/releases/latest")
				.build()
			val response = client.newCall(request).execute()
			githubApiResponse = response.body?.string()
			Log.e("github repo", "$githubApiResponse")

			//解析json获取release信息
			val releaseObjectJson = JSONObject(githubApiResponse)
			if (releaseObjectJson.has("message") && releaseObjectJson.getString("message")
					.equals("Not Found")
			) {
				//没有最新release或没有上传
				Snackbar.make(
					swipeRefreshLayout.rootView,
					"没有找到新版本, 可能是还没有上传?",
					Snackbar.LENGTH_LONG
				).show()
			} else {
				val tag = releaseObjectJson.getString("tag_name")
				if (!needToUpdate(currentVersion, tag.substring(1))) {
					Snackbar.make(swipeRefreshLayout.rootView, "已是最新版本", Snackbar.LENGTH_LONG)
						.show()
				} else {
					val name = releaseObjectJson.getString("name")
					val description = "\n${releaseObjectJson.getString("body")}"
					val relTime = adjustTime(releaseObjectJson.getString("published_at"))
					val fileList = LinkedList<LinkedList<String>>()
					//获取文件信息
					val filesJsonArray = releaseObjectJson.getJSONArray("assets")
					for (i in 0 until filesJsonArray.length()) {
						//获取单个文件信息
						val fileJsonObject = filesJsonArray.getJSONObject(i)
						val fileName = fileJsonObject.getString("name")
						val downloadUrl = fileJsonObject.getString("browser_download_url")
						val time = adjustTime(fileJsonObject.getString("updated_at"))
						val size = adjustFileSize(fileJsonObject.getLong("size"))
						val fileInfo = LinkedList<String>().apply {
							add(fileName)
							add(downloadUrl)
							add(time)
							add(size)
						}
						fileList.add(fileInfo)
					}
					val latestRelease = Release(tag, name, description, relTime, fileList)

					//从latestRelease转换为底栏弹出的更新
					//最外层的LinearLayout
					val baseLinearLayout = LinearLayout(context).apply {
						layoutParams = LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT
						)
						orientation = LinearLayout.VERTICAL
					}
					//往最外层的LinearLayout里塞小卡片
					//一个release
					val cardView = ViewUtils_Kotlin.createBigButton(
						context, null, R.color.item_card_bg,
						listOf(
							latestRelease.relName,
							latestRelease.relTag,
							latestRelease.relTime,
							latestRelease.relDescription
						),
						null,
						R.color.white
					) {//点击release卡片后弹出一个新的小卡片, 上面记载着所有的文件
						val relLinearLayout = LinearLayout(context).apply {
							layoutParams = LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT
							)
							orientation = LinearLayout.VERTICAL
						}
						//从assets列表里取出所有文件清单
						for (file in latestRelease.relAssets) {
							//拿到单个文件
							val fileCard = ViewUtils_Kotlin.createBigButton(
								context, null, R.color.item_card_bg,
								listOf(file[0], file[2], file[3]),
								null,
								R.color.white
							) {
								context.startActivity(Intent().apply {
									action = "android.intent.action.VIEW"
									data = Uri.parse(file[1])
									addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								})
							}
							relLinearLayout.addView(fileCard)
						}
						runOnMainThread {
							ViewUtils_Kotlin.getBigCardFromBottom(context, "文件", relLinearLayout)
								.show()
						}

						Unit
					}
					baseLinearLayout.addView(cardView)
					runOnMainThread {
						ViewUtils_Kotlin.getBigCardFromBottom(
							context,
							"发现新版本! 当前版本:$currentVersion",
							baseLinearLayout
						)
							.show()
					}
				}
			}
		} catch (e: Exception) {
			if (e.toString().contains("Unable to resolve host \"api.github.com\"")) {
				Snackbar.make(
					swipeRefreshLayout.rootView,
					"无法连接到GitHub服务器, 请检查网络",
					Snackbar.LENGTH_LONG
				).show()
			} else {
				e.printStackTrace()
				XposedBridge.log(e)
			}
		}
		//检查更新完成, 结束刷新动画
		runOnMainThread {
			swipeRefreshLayout.isRefreshing = false
		}
	}

//	ViewUtils_Kotlin.codeNotDone(swipeRefreshLayout.context)
}

fun showAllRelease(
	swipeRefreshLayout: SwipeRefreshLayout,
	title: String?,
	githubUserName: String,
	githubRepoName: String
) {
	swipeRefreshLayout.isRefreshing = true
	Snackbar.make(swipeRefreshLayout.rootView, "正在查询历史版本...", Snackbar.LENGTH_LONG).show()
	val context = swipeRefreshLayout.rootView.context
	//开始检查更新

	thread {
		try {
			val releaseList = mutableListOf<Release>()

			//调用github API获取json
			var githubApiResponse: String? = ""
			val client = OkHttpClient()
			val request = Request.Builder()
				.url("https://api.github.com/repos/$githubUserName/$githubRepoName/releases")
				.build()
			val response = client.newCall(request).execute()
			githubApiResponse = response.body?.string()
			Log.e("github repo", "$githubApiResponse")

			//解析json获取所有release并存入releaseList
			//releaseList首元素为最新release
			val responseArray = JSONArray(githubApiResponse)
			if (responseArray.length() == 0) {
				Snackbar.make(
					swipeRefreshLayout.rootView,
					"没有找到任何历史版本, 可能是还没上传?",
					Snackbar.LENGTH_LONG
				).show()
			} else {
				//所有的release
				for (i in 0 until responseArray.length()) {
					//一个release
					val relObject = responseArray.get(i) as JSONObject
					val tag = relObject.getString("tag_name")
					val name = relObject.getString("name")
					val description = "\n${relObject.getString("body")}"
					val relTime = adjustTime(relObject.getString("published_at"))
					val assetsList = LinkedList<LinkedList<String>>()
					val assetsArray = relObject.getJSONArray("assets")
					//一个release里所有文件
					for (j in 0 until assetsArray.length()) {
						//一个文件
						val assetsObj = assetsArray.get(j) as JSONObject
						val fileName = assetsObj.getString("name")
						val downloadURL = assetsObj.getString("browser_download_url")
						val time = adjustTime(assetsObj.getString("updated_at"))
						val size = adjustFileSize(assetsObj.getLong("size"))
						assetsList.add(LinkedList<String>().apply {
							add(fileName)
							add(downloadURL)
							add(time)
							add(size)
						})
					}
					releaseList.add(Release(tag, name, description, relTime, assetsList))
				}

				//从releaseList转换为底栏弹出的更新列表
				//最外层的LinearLayout
				val baseLinearLayout = LinearLayout(context).apply {
					layoutParams = LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
					)
					orientation = LinearLayout.VERTICAL
				}
				//往最外层的LinearLayout里塞小卡片
				for (release in releaseList) {
					//一个release
					val cardView = ViewUtils_Kotlin.createBigButton(
						context, null, R.color.item_card_bg,
						listOf(
							release.relName,
							release.relTag,
							release.relTime,
							release.relDescription
						),
						null,
						R.color.white
					) {//点击release卡片后弹出一个新的小卡片, 上面记载着所有的文件
						val relLinearLayout = LinearLayout(context).apply {
							layoutParams = LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT
							)
							orientation = LinearLayout.VERTICAL
						}
						//从assets列表里取出所有文件清单
						for (file in release.relAssets) {
							//拿到单个文件
							val fileCard = ViewUtils_Kotlin.createBigButton(
								context, null, R.color.item_card_bg,
								listOf(file[0], file[2], file[3]),
								null,
								R.color.white
							) {
								context.startActivity(Intent().apply {
									action = "android.intent.action.VIEW"
									data = Uri.parse(file[1])
									addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								})
							}
							relLinearLayout.addView(fileCard)
						}
						runOnMainThread {
							ViewUtils_Kotlin.getBigCardFromBottom(context, "文件", relLinearLayout)
								.show()
						}

						Unit
					}
					baseLinearLayout.addView(cardView)
				}
				runOnMainThread {
					try {
						ViewUtils_Kotlin.getBigCardFromBottom(
							context, when (title) {
								null -> "所有版本"
								else -> title
							}, baseLinearLayout
						)
							.show()
					} catch (e: Exception) {
						e.printStackTrace()
						XposedBridge.log(e)
					}
				}
			}

		} catch (e: Exception) {
			if (e.toString().contains("Unable to resolve host \"api.github.com\"")) {
				Snackbar.make(
					swipeRefreshLayout.rootView,
					"无法连接到GitHub服务器, 请检查网络",
					Snackbar.LENGTH_LONG
				).show()
			} else {
				e.printStackTrace()
				XposedBridge.log(e)
			}
		}
		//检查更新完成, 结束刷新动画
		runOnMainThread {
			swipeRefreshLayout.isRefreshing = false
		}
	}

//	ViewUtils_Kotlin.codeNotDone(swipeRefreshLayout.context)
}

fun adjustFileSize(size: Long): String {
	if (size <= 0) return "0"
	val units = arrayOf("B", "KB", "MB", "GB", "TB")
	val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
	val ret = DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble()))
		.toString() + " " + units[digitGroups]
	return "文件大小: $ret"
}

fun adjustTime(githubTime: String): String {
	val year = githubTime.substring(0, 4)
	val month = githubTime.substring(6, 7)
	val day = githubTime.substring(8, 10)
	val hour = githubTime.substring(11, 13)
	val min = githubTime.substring(14, 16)
	val sec = githubTime.substring(17, 19)

	val calendar = Calendar.getInstance()
	calendar.set(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), min.toInt(), sec.toInt())
	calendar.add(Calendar.HOUR, 8)
	return "发布于: ${calendar.get(Calendar.YEAR)}年${calendar.get(Calendar.MONTH)}月${
		calendar.get(
			Calendar.DATE
		)
	}日 ${calendar.get(Calendar.HOUR_OF_DAY)}时${calendar.get(Calendar.MINUTE)}分${
		calendar.get(
			Calendar.SECOND
		)
	}秒"


	/*return "发布于: ${year}年${month}月${day}日  ${hour}时${min}分${sec}秒"*/

	//  2022-03-20T08:24:33Z
	/*val timeStampUTC = transToTimeStamp(githubTime)
	val timeStampChn = timeStampUTC + 3600 * 8

	return "发布于: ${transToString(timeStampChn)}"*/
}


fun needToUpdate(currentVersion: String, remoteVersion: String): Boolean {
/*	val versionArray1 = currentVersion.split("\\.")
	val versionArray2 = remoteVersion.split("\\.")
	var idx = 0
	val minLength = min(versionArray1.size, versionArray2.size)
	var diff = 0
	while (idx < minLength
		&& (versionArray1[idx].length == versionArray2[idx].length)
		&& (versionArray1[idx].compareTo(versionArray2[idx]) == 0)
	) {
		++idx
	}
	when (diff) {
		0 -> versionArray1.size - versionArray2.size
		else -> diff
	}
	return diff <= 0*/

	var ret = false
	try {
		ret = currentVersion.toDouble() < remoteVersion.toDouble()
	} catch (e: java.lang.Exception) {
		e.printStackTrace()
		XposedBridge.log(e)
	}
	return ret
}