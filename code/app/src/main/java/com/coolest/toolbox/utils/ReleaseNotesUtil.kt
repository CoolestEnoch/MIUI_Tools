package com.coolest.toolbox.utils

import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import com.coolest.toolbox.R
import android.widget.ScrollView
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import java.lang.StringBuilder
import java.util.*

object ReleaseNotesUtil {
	var noteList: MutableList<Version?> = ArrayList()
	var alert: AlertDialog? = null
	var builder: AlertDialog.Builder? = null
	const val tab_and_new_line = "\n"
	fun showAlertDialog(title: String?, str: String?, context: Context?) {
		builder = AlertDialog.Builder(context!!)
		alert = builder!!
			.setTitle(title)
			.setMessage(str)
			.setPositiveButton("确定") { dialog, which -> }.create() //创建AlertDialog对象
		alert!!.show() //显示对话框
		/*alert = builder!!
			.setTitle(title)
			.setMessage(str)
			.setNegativeButton("取消") { dialog, which ->
				Toast.makeText(
					context,
					"你点击了取消按钮~",
					Toast.LENGTH_SHORT
				).show()
			}
			.setPositiveButton("确定", object : DialogInterface.OnClickListener {
				fun onClck(dialog: DialogInterface?, which: Int) {
					Toast.makeText(context, "你点击了确定按钮~", Toast.LENGTH_SHORT).show()
				}
			})
			.setNeutralButton("中立") { dialog, which ->
				Toast.makeText(
					context,
					"你点击了中立按钮~",
					Toast.LENGTH_SHORT
				).show()
			}
			.create() //创建AlertDialog对象
		alert!!.show() //显示对话框*/
	}

	fun setReleaseNotes() {
		noteList.apply {
			add(Version(1.0, "第一个版本"))
			add(
				Version(
					1.1, "[优化]在已root设备上的启动速度"
							+ tab_and_new_line + "[新增]root授权防误触"
							+ tab_and_new_line + "[新增]能看更新日志啦~"
				)
			)
			add(Version(1.2, "[新增]适配MIUI设备小白条沉浸" + tab_and_new_line + "[新增]适配MIUI平行世界"))
			add(
				Version(
					1.3,
					"[新增]添加XPosed模块支持" + tab_and_new_line + "[新增]支持解锁米板5 MIUI+连电脑" + tab_and_new_line + "[新增]让运行MIUI12.5及以上系统的不支持MIUI+的手机支持MIUI+"
				)
			)
			add(Version(1.4, "[新增]连接MIUI+之后可修改本机显示的电脑名"))
			add(
				Version(
					1.5,
					"[优化]大幅重构了UI" + tab_and_new_line + "[新增]支持修改MIUI+设置里电脑的小尾巴" + tab_and_new_line + "[新增]关于作者页及其每日壁纸背景图(需联网)" + tab_and_new_line + "[新增]支持在线检查更新和查询所有历史版本"
				)
			)
			add(Version(1.6, "[修复]操作过快导致的几率性闪退问题" + tab_and_new_line + "[修复]安卓12上检查更新可能引发的闪退"))
			add(
				Version(
					1.7,
					"[修复]非MIUI设备上的显示问题" + tab_and_new_line + "[新增]更新渠道切换" + tab_and_new_line + "[新增]部分按钮高斯模糊" + tab_and_new_line + "[修复]部分场景下的闪退问题" + tab_and_new_line + "[修复]时间显示错误"
				)
			)
			add(Version(1.71, "[优化]部分场景的流畅度"))
			add(Version(1.72, "[优化]重构部分底层代码"))
			add(Version(2.0, "[适配]平板上版本号为3.5.17c的MIUI+"))
			add(Version(2.5, "[新增]解锁系统小窗应用个数从2个改成20个"))
			add(Version(2.51, "[修复]在非MIUI for Pad系统上会莫名其妙出现一堆FC的bug"))
			add(
				Version(
					2.6,
					"[新增]解锁哈曼卡顿(仅在米10U和米板5的第一个MIUI13稳定版上测试过可用)" + tab_and_new_line + "[修复]深色模式显示异常"
				)
			)
			add(Version(2.61, "[改动]哈曼卡顿默认自动开启" + tab_and_new_line + "[优化]日志系统优化"))
			add(
				Version(
					3.0,
					"[新增]MIUI13手机也能开好几个小窗啦" + tab_and_new_line + "[新增]任何程序皆可小窗" + tab_and_new_line + "[新增]锁定手机管家分数100分" + tab_and_new_line + "[新增]适配Material You动态配色(需Android 12或更高版本)"
				)
			)
			add(Version(3.01, "[修复]JoyUI无法使用的问题"))
		}
		Collections.reverse(noteList)
	}

	val releaseNotes: String
		get() {
			if (noteList.isEmpty()) setReleaseNotes()
			val sb = StringBuilder()
			for (v in noteList) {
				sb.append(v)
			}
			return sb.toString()
		}

	fun showReleaseNotes(context: Context) {
//		showAlertDialog("更新日志" + "   " + "\n当前版本" + CoolUtils.getAppVersionName(context), getReleaseNotes(), context);
		releaseNotes
		val linearLayout = LinearLayout(context)
		val params = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		linearLayout.layoutParams = params
		linearLayout.orientation = LinearLayout.VERTICAL
		Log.e("for", "before")
		for (version in noteList) {
			val list = LinkedList<String>()
			list.add(version!!.version.toString())
			val detail = version.notes.split(tab_and_new_line.toRegex()).toTypedArray()
			for (str in detail) {
				list.add(str)
			}
			linearLayout.addView(
				ViewUtils_Kotlin.createBigButton(
					context,
					R.drawable.ic_baseline_grass_24,
					R.color.item_card_bg,
					list, null, R.color.white
				) {}
			)
			Log.e("for", "run in")
		}
		Log.e("for", "after")
		val scrollView = ScrollView(context)
		val scrollParams: ViewGroup.LayoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		scrollView.layoutParams = scrollParams
		scrollView.addView(linearLayout)
		ViewUtils.getBigCardFromBottom(
			context,
			"更新日志\t当前版本${
				context.packageManager.getPackageInfo(
					context.packageName,
					0
				).versionName
			}",
			scrollView
		).show()
	}

	fun showLastReleaseNotes(context: Context?) {
//		showAlertDialog("更新日志" + "   " + "\n当前版本" + CoolUtils.getAppVersionName(context), getReleaseNotes(), context);
		releaseNotes
		val linearLayout = LinearLayout(context)
		val params = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		linearLayout.layoutParams = params
		linearLayout.orientation = LinearLayout.VERTICAL
		val version = noteList[0]
		val list = LinkedList<String>()
		list.add(version!!.version.toString())
		val detail = version.notes.split(tab_and_new_line.toRegex()).toTypedArray()
		for (str in detail) {
			list.add(str)
		}
		linearLayout.addView(
			ViewUtils.createCardView(
				context,
				R.color.item_card_bg,
				R.drawable.ic_baseline_grass_24,
				list
			)
		)
		Log.e("for", "run in")
		val scrollView = ScrollView(context)
		val scrollParams: ViewGroup.LayoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		scrollView.layoutParams = scrollParams
		scrollView.addView(linearLayout)
		ViewUtils.getBigCardFromBottom(context, "这个版本更新了啥", scrollView).show()
	}

	class Version(var version: Double, var notes: String) {
		override fun toString(): String {
			return "$version $notes\n"
		}
	}
}