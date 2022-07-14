package com.coolest.toolbox.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.coolest.toolbox.MyApplication
import com.coolest.toolbox.R
import com.coolest.toolbox.databinding.ActivityAboutBinding
import com.coolest.toolbox.utils.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.snackbar.Snackbar
import de.robv.android.xposed.XposedBridge
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread

class AboutActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAboutBinding

	var todayBingPicUrl = ""

	private var githubName = "CoolestEnoch"
	private var githubRepo = "MIUI_Tools"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityAboutBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(findViewById(R.id.toolbar))

		binding.toolbarLayout.title = "关于"

		binding.cardAuthor.setOnClickListener {
			Snackbar.make(window.decorView, "作者: Coolest Enoch", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
			try {
				Thread.sleep(100)
			} catch (e: InterruptedException) {
				e.printStackTrace()
			}
			Toast.makeText(this, "作者: Coolest Enoch", Toast.LENGTH_SHORT).show()
			startActivity(Intent().apply {
				action = "android.intent.action.VIEW"
				data = Uri.parse("https://github.com/coolestenoch")
			})
		}

		//背景获取
		thread {
			try {
				todayBingPicUrl = getTodayWallpaperURL()
			} catch (e: Exception) {
				e.printStackTrace()
				XposedBridge.log(e)
			}
		}

		//背景图
		when (MyApplication.dailyBingPaper) {
			null -> setBingDailyWallpaper(binding.aboutBgImg, this, 0)
			else -> binding.aboutBgImg.setImageBitmap(MyApplication.dailyBingPaper)
		}


		//mapOf(信息list1 to 职位1, 信息list2 to 职位2)
		//如果map里的key list里只有一个值, 那么就会被解析为github用户名并直接去github上查找这个人的信息
		//否则list格式为:{用户名:String, 个性签名:String, 头像URL, 网页URL}
		//空值不要用null, 请使用空字符串""代替

		//作者列表
		val list = mapOf(listOf("coolestenoch") to "作者")
		processDeveloperInfo(list, binding.authorListView, null, this)

		//贡献者列表
		val list2 = mapOf(listOf("mikotwa") to "", listOf("桜谷暮枫", "", "http://avatar.coolapk.com/data/002/37/46/44_avatar_middle.jpg", "http://www.coolapk.com/u/2374644") to "为修复JoyUI上的bug提供帮助")
		processDeveloperInfo(list2, binding.contributorListView, binding.contributorListTip, this)

		//下拉刷新检查更新
		binding.swipeRefresh.apply {
			setColorSchemeColors(
				resources.getColor(R.color.green),
				resources.getColor(R.color.blue),
				resources.getColor(R.color.yellow),
				resources.getColor(R.color.purple),
				resources.getColor(R.color.orange),
				resources.getColor(R.color.red),
				resources.getColor(R.color.cyan)
			)
			setOnRefreshListener {
				checkUpdate(
					packageManager.getPackageInfo(packageName, 0).versionName,
					binding.swipeRefresh,
					githubName,
					githubRepo
				)
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_about, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_update -> {
				//更新渠道设置
				getSharedPreferences("update_channel", Context.MODE_PRIVATE).apply {
					githubName = "${getString("username", "CoolestEnoch")}"
					githubRepo = "${getString("repo", "MIUI_Tools")}"
				}
				checkUpdate(
					packageManager.getPackageInfo(packageName, 0).versionName,
					binding.swipeRefresh,
					githubName,
					githubRepo
				)
			}
			R.id.release_history -> {
				//更新渠道设置
				getSharedPreferences("update_channel", Context.MODE_PRIVATE).apply {
					githubName = "${getString("username", "CoolestEnoch")}"
					githubRepo = "${getString("repo", "MIUI_Tools")}"
				}
				showAllRelease(
					binding.swipeRefresh, when (githubName) {
						"CoolestEnoch" -> "所有版本: 官方渠道"
						"Xposed-Modules-Repo" -> "所有版本: LSPosed仓库"
						else -> "所有版本: 其他渠道"
					}, githubName, githubRepo
				)
			}
			R.id.update_channel -> {
				changeUpdateChannel(this)
			}
		}
		return super.onOptionsItemSelected(item)
	}

	fun isHttpUrl(urls: String): Boolean {
		var isurl = false
		val regex = ("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
				+ "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)") //设置正则表达式
		val pat = Pattern.compile(regex.trim { it <= ' ' }) //对比
		val mat = pat.matcher(urls.trim { it <= ' ' })
		isurl = mat.matches() //判断是否匹配
		if (isurl) {
			isurl = true
		}
		return isurl
	}

	fun processDeveloperInfo(
		userInfoMap: Map<List<String>, String>?,
		developersView: ViewGroup,
		tipView: TextView?,
		context: Context
	) {
		try {
			thread {
				val tempViewList = LinkedList<MaterialCardView?>()
				if (userInfoMap != null) {
					for ((userInfo, description) in userInfoMap) {
						if (userInfo.size == 1) {
							//是个github用户名
							val username = userInfo[0]

							try {
								//调用GitHub API获取json
								var githubResponseJson: String? = ""
								var avatar_url: String? = ""
								var nickName: String? = ""
								var bio: String? = ""
								val client = OkHttpClient()
								val request = Request.Builder()
									.url("https://api.github.com/users/$username")
									.build()
								val response = client.newCall(request).execute()
								githubResponseJson = response.body?.string()
								Log.e("github", "$githubResponseJson")

								//解析json获取图片地址
								val responseJson = JSONObject(githubResponseJson)
								avatar_url = "${responseJson.get("avatar_url")}"
								nickName = "${responseJson.get("login")}"
								bio = "${responseJson.get("bio")}"

								//添加卡片
								val textList = mutableListOf("")
								textList.clear()
								if(!username.equals(""))
									textList.add(nickName)
								if(!bio.equals(""))
									textList.add(bio)
								if(!description.equals(""))
									textList.add(description)

								runOnUiThread {
									tempViewList.add(
										ViewUtils_Kotlin.createBigButton(
											context,
											avatar_url,
											R.color.item_card_bg,
											textList,
											null, R.color.white
										) {
											startActivity(Intent().apply {
												action = "android.intent.action.VIEW"
												data = Uri.parse("https://github.com/$username")
											})
										}
									)
								}
								runOnUiThread {
									developersView.removeAllViews()
									for (view in tempViewList) {
										developersView.addView(view)
									}
								}
							} catch (e: Exception) {
								if (e.toString()
										.contains("Unable to resolve host \"api.github.com\"")
								) {
									Snackbar.make(
										window.decorView,
										"无法连接到GitHub服务器, 请检查网络",
										Snackbar.LENGTH_LONG
									).show()
								} else {
									e.printStackTrace()
									XposedBridge.log(e)
								}
							}
						} else {
							//不是github用户名
							val username = userInfo[0]
							val bio = userInfo[1]
							val aviatorUrl = userInfo[2]
							val webPageUrl = userInfo[3]
							//还有一个description

							try {
								//添加卡片
								val textList = mutableListOf("")
								textList.clear()
								if(!username.equals(""))
									textList.add(username)
								if(!bio.equals(""))
									textList.add(bio)
								if(!description.equals(""))
									textList.add(description)

								runOnUiThread {
									tempViewList.add(
										ViewUtils_Kotlin.createBigButton(
											context,
											aviatorUrl,
											R.color.item_card_bg,
											textList,
											null, R.color.white
										) {
											when (TextUtils.isEmpty(webPageUrl)) {
												false -> startActivity(Intent().apply {
													action = "android.intent.action.VIEW"
													data = Uri.parse(webPageUrl)
												})
												true -> {
													Snackbar.make(
														window.decorView,
														"这个人很神秘, 没有留下可访问的页面。",
														Snackbar.LENGTH_LONG
													).show()
												}
											}
										}
									)
								}
								runOnUiThread {
									developersView.removeAllViews()
									for (view in tempViewList) {
										developersView.addView(view)
									}
								}
							} catch (e: Exception) {
								if (e.toString()
										.contains("Unable to resolve host \"api.github.com\"")
								) {
									Snackbar.make(
										window.decorView,
										"无法连接到GitHub服务器, 请检查网络",
										Snackbar.LENGTH_LONG
									).show()
								} else {
									e.printStackTrace()
									XposedBridge.log(e)
								}
							}
						}
					}
				} else {
					tipView?.text = "暂无"
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}


	private fun changeUpdateChannel(context: Context) {
		val picURL = when (todayBingPicUrl) {
			"" -> null
			else -> todayBingPicUrl
		}
		var bottomBar: MaterialDialog? = null
		runOnMainThread {
			val linearLayout = LinearLayout(context).apply {
				layoutParams = LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				)
				orientation = LinearLayout.VERTICAL
			}
			val btnOfficial = ViewUtils_Kotlin.createBigButton(
				context, R.drawable.ic_baseline_double_arrow_24,
				R.color.teal_200, listOf("官方"), MyApplication.dailyBingPaper, R.color.white
			) {
				context.getSharedPreferences("update_channel", Context.MODE_PRIVATE).edit()
					.apply {
						putString("username", "CoolestEnoch")
						putString("repo", "MIUI_Tools")
						commit()
					}
				bottomBar?.cancel()
				Unit
			}
			val btnLsp = ViewUtils_Kotlin.createBigButton(
				context, R.drawable.ic_baseline_double_arrow_24,
				R.color.teal_200, listOf("LSPosed"), MyApplication.dailyBingPaper, R.color.white
			) {
				context.getSharedPreferences("update_channel", Context.MODE_PRIVATE).edit()
					.apply {
						putString("username", "Xposed-Modules-Repo")
						putString("repo", "com.coolest.toolbox")
						commit()
					}
				bottomBar?.cancel()
				Unit
			}
			linearLayout.addView(btnOfficial)
			linearLayout.addView(btnLsp)
			bottomBar = ViewUtils_Kotlin.getBigCardFromBottom(context, "更新渠道", linearLayout)
			bottomBar?.show()
		}


	}


}