package com.coolest.toolbox.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.coolest.toolbox.MyApplication
import com.coolest.toolbox.R
import com.coolest.toolbox.databinding.ActivityMainBinding
import com.coolest.toolbox.utils.*
import com.google.android.material.snackbar.Snackbar
import java.io.DataInputStream
import java.io.File
import java.net.URL
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	//工作变量
	var rooted = false

	//运行所需的root功能按钮列表
	var rootButtonList: MutableList<View> = ArrayList()
	var xposedButtonList: MutableList<View> = ArrayList()
	private fun isXposedActivated() = false
	private fun getXposedActivatedStateString() = when (isXposedActivated()) {
		true -> "xposed已激活"
		false -> "xposed未激活"
	}

	override fun onCreate(
		savedInstanceState: Bundle?
	) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.tvXposedStatus.text = getXposedActivatedStateString()

		//初始化全局Application的变量
		thread {
			try {
				val dis = DataInputStream(URL(getTodayWallpaperURL()).openStream())
				MyApplication.dailyBingPaper = BitmapFactory.decodeStream(dis)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		/**
		 * MIUI小白条沉浸
		 * from https://dev.mi.com/console/doc/detail?pId=2229
		 */
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//		window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) //设置沉浸式状态栏，在MIUI系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
		window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。

		//布局绑定
		/**
		 * MIUI平行世界适配
		 * from https://dev.mi.com/console/doc/detail?pId=2448
		 */
		val isInMagicWindow = resources.configuration.toString().contains("miui-magic-windows")

		//root相关UI列表
		rootButtonList.apply {
			add(binding.notificationFilterB)
			add(binding.notificationIntegrateB)
			add(binding.selinuxEnforceB)
			add(binding.getEnforceB)
			add(binding.selinuxPermissiveB)
			add(binding.hideMomoB)
			add(binding.activeIceboxB)
			add(binding.btnHarmanKardon)
		}

		//xposed相关UI列表
		xposedButtonList.apply {
			add(binding.switchMiuiPlusConnectComputer)
//			add(binding.switchMiuiPlusConnectTablet)
			add(binding.swMiuiPlusPcInfo)
			add(binding.etMiuiPlusPcName)
			add(binding.etMiuiPlusPcTail)
			add(binding.switchDolby)
			add(binding.switchHarmanKardon)
		}

/*		Glide.with(this)
			.asBitmap()
			.load("https://cn.bing.com//th?id=OHR.TheBard_ZH-CN7318156185_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp")
			.into(object : SimpleTarget<Bitmap?>() {
				override fun onResourceReady(
					resource: Bitmap,
					transition: Transition<in Bitmap?>?
				) {
					val drawable: Drawable = BitmapDrawable(resource)
					binding.mainCardLayout!!.background = drawable
				}
			})*/


		if (!isXposedActivated()) {
			for (item in xposedButtonList) {
				item.isEnabled = false
			}
			if (CoolUtils.isMIUI()) {
				Snackbar.make(window.decorView, "xposed模块未激活, 相关功能将不可用。", Snackbar.LENGTH_LONG)
					.show()
			}
		} else {
			Snackbar.make(window.decorView, "xposed已激活", Snackbar.LENGTH_LONG).show()
			KotlinUtils.request_permissions(this)
		}

		//逻辑代码

		//启动时
		//启动时检测root权限
		//TODO 启动提权
		Thread(GetRootBgSrv(true, isInMagicWindow)).start() //启动时通过子线程提权, 优化性能

		//如果不是miui那么就隐藏一些专属设置
		if (!CoolUtils.isMIUI()) {
			binding.miuiSettingsCard.visibility = LinearLayout.GONE
			Snackbar.make(window.decorView, "不是MIUI系统, 已为你隐藏部分选项。", Snackbar.LENGTH_LONG).show()
			binding.logsT.append("不是MIUI系统, 已为你隐藏部分选项。")
		}

		//判断是否经历了版本更新, 如果更新了就弹出更新日志
		when (getSharedPreferences("config", Context.MODE_PRIVATE).getString(
			"last_version",
			"1.0"
		)) {
			packageManager.getPackageInfo(packageName, 0).versionName -> {
			}
			else -> {
				getSharedPreferences("config", Context.MODE_PRIVATE).edit().apply {
					putString(
						"last_version",
						packageManager.getPackageInfo(packageName, 0).versionName
					)
					commit()
				}
				ReleaseNotesUtil.showLastReleaseNotes(this)
			}
		}


		//一些Listener
		binding.gotoMiuiPlusSettings.apply {
			setOnClickListener {
				startActivity(Intent().apply {
					action = "com.xiaomi.mirror"
					flags = Intent.FLAG_ACTIVITY_NEW_TASK
					component = ComponentName(
						"com.xiaomi.mirror",
						"com.xiaomi.mirror.settings.SettingsHomeActivity"
					)
				})
			}
			setOnLongClickListener {
				logAndExec("adb shell am force-stop com.xiaomi.mirror")
				true
			}
		}
		binding.gotoMiuiScanQRCode.apply {
			setOnClickListener {
				startActivity(Intent().apply {
					action = "com.xiaomi.scanner"
					flags = Intent.FLAG_ACTIVITY_NEW_TASK
					component =
						ComponentName("com.xiaomi.scanner", "com.xiaomi.scanner.app.ScanActivity")
				})
			}
		}

		binding.switchHarmanKardon.apply {
			isChecked = try {
				CoolConfigHelper.getJsonConfig(this@MainActivity, "harmanKardon_enabled") as Boolean
			} catch (e: Exception) {
				false
			}
			isEnabled = isXposedActivated()
			setOnCheckedChangeListener { buttonView, isChecked ->
				logAndExec("adb shell am force-stop com.miui.misound")
				when (isChecked) {
					true -> {
						Snackbar.make(window.decorView, "哈曼卡顿已启用!", Snackbar.LENGTH_LONG)
							.setAction("前往打开") {
								startActivity(Intent().apply {
									action = "com.miui.misound"
									flags = Intent.FLAG_ACTIVITY_NEW_TASK
									component = ComponentName(
										"com.miui.misound",
										"com.miui.misound.HeadsetSettingsActivity"
									)
								})
							}
							.show()
					}
					false -> {
					}
				}
				CoolConfigHelper.modifyJsonConfig(
					this@MainActivity,
					"harmanKardon_enabled",
					isChecked
				)
			}

			//音质音效没有存储权限，暂时禁用
			visibility = View.GONE
		}
		binding.switchDolby.apply {
			isChecked = try {
				CoolConfigHelper.getJsonConfig(this@MainActivity, "dolby_enabled") as Boolean
			} catch (e: Exception) {
				CoolUtils.isPad()
			}
			isEnabled = isXposedActivated()/* && !CoolUtils.isPad()*/
			setOnCheckedChangeListener { buttonView, isChecked ->
				logAndExec("adb shell am force-stop com.miui.misound")
				when (isChecked) {
					true -> {
						Snackbar.make(window.decorView, "音质音效如打不开请关闭", Snackbar.LENGTH_LONG)
							.setAction("关闭") {
								binding.switchDolby.isChecked = false
							}.show()
					}
					false -> {
					}
				}
				CoolConfigHelper.modifyJsonConfig(this@MainActivity, "dolby_enabled", isChecked)
			}

			//音质音效没有存储权限，暂时禁用
			visibility = View.GONE
		}

		binding.swLock100.apply {
			isChecked = try {
				CoolConfigHelper.getJsonConfig(this@MainActivity, "miui_secure_center_lock_100") as Boolean
			} catch (e: Exception) {
				false
			}
			isEnabled = isXposedActivated()
			setOnCheckedChangeListener { buttonView, isChecked ->
				CoolConfigHelper.modifyJsonConfig(
					this@MainActivity,
					"miui_secure_center_lock_100",
					isChecked
				)
				logAndExec("adb shell am force-stop com.miui.securitycenter")
				if(isChecked){
					Snackbar.make(window.decorView, "手机管家更新频繁, 本开关有概率失效", Snackbar.LENGTH_LONG).show()
				}
			}
		}

		binding.switchMiuiPlusConnectComputer.apply {
			isChecked = try {
				CoolConfigHelper.getJsonConfig(this@MainActivity, "miui_plus_connect_pc") as Boolean
			} catch (e: Exception) {
				false
			}
			when (isChecked) {
				true -> {
					text = "MIUI+ 当前可连接电脑、接力到平板"
					binding.gotoMiuiScanQRCode.visibility = TextView.VISIBLE
				}
				false -> {
					text = "MIUI+ 当前仅可接受来自手机的接力"
					binding.gotoMiuiScanQRCode.visibility = TextView.GONE
				}
			}
			binding.swMiuiPlusPcInfo.isEnabled = isChecked
			binding.etMiuiPlusPcName.isEnabled = isChecked
			binding.etMiuiPlusPcTail.isEnabled = isChecked
			isEnabled = isXposedActivated()
			setOnCheckedChangeListener { buttonView, isChecked ->
				when (isChecked) {
					true -> {
						buttonView.text = "MIUI+ 当前可连接电脑、接力到平板"
						binding.gotoMiuiScanQRCode.visibility = TextView.VISIBLE
					}
					false -> {
						buttonView.text = "MIUI+ 当前仅可接受来自手机的接力"
						binding.gotoMiuiScanQRCode.visibility = TextView.GONE
					}
				}
				binding.swMiuiPlusPcInfo.isEnabled = isChecked
				binding.etMiuiPlusPcName.isEnabled = isChecked
				binding.etMiuiPlusPcTail.isEnabled = isChecked
				CoolConfigHelper.modifyJsonConfig(
					this@MainActivity,
					"miui_plus_connect_pc",
					isChecked
				)
				logAndExec("adb shell am force-stop com.android.settings")
				logAndExec("adb shell am force-stop com.xiaomi.mirror")
			}
		}

		//MIUI+设备名和小尾巴
		binding.swMiuiPlusPcInfo.apply {
			isChecked = try {
				CoolConfigHelper.getJsonConfig(
					this@MainActivity,
					"miui_plus_cust_pc_name_switch"
				) as Boolean
			} catch (e: Exception) {
				false
			}
			binding.etMiuiPlusPcName.isEnabled = isChecked
			binding.etMiuiPlusPcTail.isEnabled = isChecked
			isEnabled = isXposedActivated()
			setOnCheckedChangeListener { buttonView, isChecked ->
				binding.etMiuiPlusPcName.isEnabled = isChecked
				binding.etMiuiPlusPcTail.isEnabled = isChecked
				CoolConfigHelper.modifyJsonConfig(
					this@MainActivity,
					"miui_plus_cust_pc_name_switch",
					isChecked
				)
			}
		}

		//MIUI+平板和手机互相接力
		/*binding.switchMiuiPlusConnectTablet.apply {
			isChecked = try {
				CoolConfigHelper.getJsonConfig("switch_miui_plus_tablet_connect_phone") as Boolean
			} catch (e: Exception) {
				false
			}
			when (isChecked) {
				true -> {
					setText("当前设备可接受来自手机的接力")
				}
				false -> {
					setText("当前设备可接力到平板")
				}
			}
			isEnabled = isXposedActivated()
			setOnCheckedChangeListener { buttonView, isChecked ->
				when (isChecked) {
					true -> {
						buttonView.text = "当前设备可接受来自手机的接力"
					}
					false -> {
						buttonView.text = "当前设备可接力到平板"
					}
				}
				CoolConfigHelper.modifyJsonConfig("switch_miui_plus_tablet_connect_phone", isChecked)
			}
		}*/

		binding.etMiuiPlusPcName.apply {
			//启动时设置默认文本
			val content = try {
				CoolConfigHelper.getJsonConfig(this@MainActivity, "miui_plus_pc_name") as String
			} catch (e: java.lang.Exception) {
				""
			}
			setText(content)
			//实时变更文本
			addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					s: CharSequence,
					start: Int,
					count: Int,
					after: Int
				) {
					//这个方法被调用，说明在s字符串中，从start位置开始的count个字符即将被长度为after的新文本所取代。在这个方法里面改变s，会报错。
				}

				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
					//这个方法被调用，说明在s字符串中，从start位置开始的count个字符刚刚取代了长度为before的旧文本。在这个方法里面改变s，会报错。
				}

				override fun afterTextChanged(s: Editable) {
					//这个方法被调用，那么说明s字符串的某个地方已经被改变。
					CoolConfigHelper.modifyJsonConfig(
						this@MainActivity,
						"miui_plus_pc_name",
						s.toString()
					)
				}
			})
		}
		binding.etMiuiPlusPcTail.apply {
			//启动时设置默认文本
			val content = try {
				CoolConfigHelper.getJsonConfig(this@MainActivity, "miui_plus_pc_tail") as String
			} catch (e: java.lang.Exception) {
				""
			}
			setText(content)
			//实时变更文本
			addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					s: CharSequence,
					start: Int,
					count: Int,
					after: Int
				) {
					//这个方法被调用，说明在s字符串中，从start位置开始的count个字符即将被长度为after的新文本所取代。在这个方法里面改变s，会报错。
				}

				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
					//这个方法被调用，说明在s字符串中，从start位置开始的count个字符刚刚取代了长度为before的旧文本。在这个方法里面改变s，会报错。
				}

				override fun afterTextChanged(s: Editable) {
					//这个方法被调用，那么说明s字符串的某个地方已经被改变。
					CoolConfigHelper.modifyJsonConfig(
						this@MainActivity,
						"miui_plus_pc_tail",
						s.toString()
					)
				}
			})
		}


		//折叠动画相关
		val miuiSettingGroupHeight = ViewUtils.getRealHeight(binding.miuiSettingsView).toInt()
		val rootMagiskSettingGroupHeight =
			ViewUtils.getRealHeight(binding.magiskRootSettings).toInt()
		binding.miuiSettingsFold.setOnClickListener {
			if (binding.miuiSettingsView.height == 0) {
				ShowHideAnimUtil.showAnimator_height(
					binding.miuiSettingsView,
					0,
					miuiSettingGroupHeight
				)
				var drawable = resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24)
				drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
				binding.miuiSettingsFold.setCompoundDrawables(drawable, null, null, null)
				binding.miuiSettingsFold.setText("收起   ")
			} else {
				ShowHideAnimUtil.hideAnimator_height(binding.miuiSettingsView)
				var drawable = resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24)
				drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
				binding.miuiSettingsFold.setCompoundDrawables(drawable, null, null, null)
				binding.miuiSettingsFold.setText("展开   ")
			}
		}
		binding.magiskRootSettingsFold.setOnClickListener {
			if (binding.magiskRootSettings.height == 0) {
				ShowHideAnimUtil.showAnimator_height(
					binding.magiskRootSettings,
					0,
					rootMagiskSettingGroupHeight
				)
				var drawable = resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24)
				drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
				binding.magiskRootSettingsFold.setCompoundDrawables(drawable, null, null, null)
				binding.magiskRootSettingsFold.setText("收起   ")
			} else {
				ShowHideAnimUtil.hideAnimator_height(binding.magiskRootSettings)
				var drawable = resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24)
				drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
				binding.magiskRootSettingsFold.setCompoundDrawables(drawable, null, null, null)
				binding.magiskRootSettingsFold.setText("展开   ")
			}
		}


	}

	override fun onCreateOptionsMenu(menu: Menu)
			: Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem)
			: Boolean {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		val id = item.itemId
		if (id == R.id.clear_log) {
			binding.logsT.text = ""
			Snackbar.make(window.decorView, "完成", Snackbar.LENGTH_LONG).show()
			//			log_t.append(CoolUtils.adbExec_with_root("whoami") + "\n");
			//TODO 检测当前用户是否为root
			return true
		}
		if (id == R.id.enable_root_button) {
			for (b in rootButtonList) {
				b.isEnabled = true
			}
			for (b in xposedButtonList) {
				b.isEnabled = true
			}
			Snackbar.make(window.decorView, "完成", Snackbar.LENGTH_LONG).show()
		}
		if (id == R.id.author_button) {
			/*Snackbar.make(window.decorView, "作者: Coolest Enoch", Snackbar.LENGTH_LONG)
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
			})*/
			startActivity(Intent(this, AboutActivity::class.java))
		}
		if (id == R.id.release_notes) {
			ReleaseNotesUtil.showReleaseNotes(this)
		}
		return super.onOptionsItemSelected(item)
	}

	/**
	 * 提权子线程, 后台获取root进程
	 */
	internal inner class GetRootBgSrv(var startUp: Boolean, var magicWindowNotify: Boolean) :
		Runnable {
		override fun run() {
			val noRootNoti =
				Snackbar.make(window.decorView, "设备无root权限，部分功能可能不可用。", Snackbar.LENGTH_LONG)
			while (!rooted) { //每五秒钟提权一次，成功就退
				if (!startUp) {
					try {
						Thread.sleep(5000)
					} catch (e: InterruptedException) {
						e.printStackTrace()
					}
				}
				rooted = CoolUtils.get_root_access(packageCodePath)
				val user = CoolUtils.adbExec_with_root("whoami")
				if (user.contains("root")) {//拿到root权限了
					rooted = true
					runOnUiThread { // 更新UI的操作
						binding.logsT.append("Login as root successfully.\n")
						binding.tvRootStatus.text = "Root已获取"
					}
				} else {
					rooted = false
					if (startUp) {
						runOnUiThread { // 更新UI的操作
							binding.logsT.append("Permission Denied.\n")
						}
						//无root禁用所有需要root的按钮
						// 更新UI的操作
						for (b in rootButtonList) {
							runOnUiThread {
								b.isEnabled = false
							}
						}
						startUp = false
						//提示无root和平行视界
						noRootNoti.show()
						while (noRootNoti.isShown);
						if (magicWindowNotify) {
							Snackbar.make(window.decorView, "当前运行在平行视界里", Snackbar.LENGTH_LONG)
								.show()
						}
					}
				}
			}
			//拿到权限后操作: 解除按钮禁用
			runOnUiThread { // 更新UI的操作
				for (b in rootButtonList) {
					b.isEnabled = true
				}
			}
		}
	}
	/**
	 * 正式代码: 按钮响应
	 */
	/**
	 * 通知聚合
	 */
	fun notification_integrate(
		v: View?
	) {
		Snackbar.make(window.decorView, "在非MIUI设备上运行可能会出现问题", Snackbar.LENGTH_LONG)
			.setAction("继续执行") {
				logAndExec("setprop persist.sys.notification_ver 2")
				logAndExec("setprop persist.sys.notification_rank 6")
				logAndExec("am force-stop com.miui.notification")
				binding.logsT.append("通知聚合->执行完成\n")
			}.show()
	}

	/**
	 * 通知过滤
	 */
	fun notification_filter(v: View?) {
		Snackbar.make(window.decorView, "在非MIUI设备上运行可能会出现问题", Snackbar.LENGTH_LONG)
			.setAction("继续执行") {
				logAndExec("setprop persist.sys.notification_ver 2")
				logAndExec("setprop persist.sys.notification_rank 3")
				logAndExec("am force-stop com.miui.notification")
				binding.logsT.append("通知过滤->执行完成\n")
			}.show()
	}

	/**
	 * SeLinux设置Enforce
	 */
	fun setEnforce(
		v: View?
	) {
		Snackbar.make(v!!, "可能导致Fake Location异常", Snackbar.LENGTH_LONG)
			.setAction("继续执行") { logAndExec("setenforce 1") }
			.show()
	}

	/**
	 * SeLinux设置Permissive
	 */
	fun setPermissive(v: View?) {
		Snackbar.make(v!!, "请确保Fake Location已关闭", Snackbar.LENGTH_LONG)
			.setAction("继续执行") { logAndExec("setenforce 0") }
			.show()
	}

	/**
	 * 隐藏momo
	 */
	fun hideMomo(
		v: View?
	) {
		Snackbar.make(v!!, "可能导致LSPosed出现异常", Snackbar.LENGTH_LONG).setAction("继续执行") {
			logAndExec("resetprop --delete dalvik.vm.dex2oat-flags")
			logAndExec("settings delete global hidden_api_policy")
			logAndExec("settings delete global hidden_api_policy_p_apps")
			logAndExec("settings delete global hidden_api_policy_pre_p_apps")
		}.show()
	}

	/**
	 * 获取SeLinux状态
	 */
	fun getEnforce(v: View?) {
		binding.logsT.append(">> getenforce\n")
		var tmp = CoolUtils.adbExec_with_root("getenforce")
		if (tmp != "") {
			binding.logsT.append("$tmp".trimIndent())
		}
		if (tmp!!.contains("Enforcing")) {
			Snackbar.make(v!!, "当前状态: Enforcing", Snackbar.LENGTH_LONG).show()
		} else if (tmp.contains("Permissive")) {
			Snackbar.make(v!!, "当前状态: Permissive", Snackbar.LENGTH_LONG).show()
		} else {
			Snackbar.make(v!!, "获取状态时出错", Snackbar.LENGTH_LONG).show()
		}
		tmp = null
	}

	/**
	 * 激活冰箱
	 */
	fun activeIceBox(
		v: View?
	) {
		if (!rooted) {
			Snackbar.make(window.decorView, "没电脑没root激活不了喔", Snackbar.LENGTH_LONG).show()
		} else {
			val ret =
				logAndExec("dpm set-device-owner com.catchingnow.icebox/.receiver.DPMReceiver").toLowerCase()
			var suggestion = ""
			suggestion = if (ret.contains("account")) {
				"退掉账号然后删除所有账户再激活"
			} else if (ret.contains("already set")) {
				"你是不是已经激活冰箱或者同类软件了"
			} else if (ret.contains("unknown admin")) {
				"你是不是没装冰箱"
			} else if (ret.contains("success")) {
				"激活成功!"
			} else {
				"未知错误, 请查看日志"
			}
			Snackbar.make(v!!, suggestion, Snackbar.LENGTH_LONG).show()
		}
	}

	/**
	 * 手动添加哈满卡顿开关
	 */
	fun btnHarmanKardon(view: View) {
		logAndExec("setprop ro.vendor.audio.sfx.harmankardon 1")
		Snackbar.make(view, "已添加哈曼卡顿开关!", Snackbar.LENGTH_LONG)
			.setAction("前往打开") {
				startActivity(Intent().apply {
					action = "com.miui.misound"
					flags = Intent.FLAG_ACTIVITY_NEW_TASK
					component = ComponentName(
						"com.miui.misound",
						"com.miui.misound.HeadsetSettingsActivity"
					)
				})
			}
			.show()
	}

	/**
	 * 小工具类
	 * 往日志区写日志并且以root权限执行语句
	 */
	fun logAndExec(cmd: String): String {
		binding.logsT.append(">> $cmd\n\n")
		val ret = CoolUtils.adbExec_with_root(cmd)
		if (cmd != "") {
			binding.logsT.append("$ret".trimIndent())
		}
		Snackbar.make(window.decorView, "完成", Snackbar.LENGTH_LONG).show()
		return ret
	}
}