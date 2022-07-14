package com.coolest.toolbox.xposed

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.Context
import android.content.res.XResources
import android.view.View
import com.coolest.toolbox.BuildConfig
import com.coolest.toolbox.ui.CoolUtils
import com.coolest.toolbox.utils.CoolConfigHelper
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookEntry : IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

	override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
		EzXHelperInit.initZygote(startupParam)
		XposedBridge.log("FM: init zygote")
	}

	override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
		when (resparam.packageName) {
			"com.xiaomi.mirror" -> {
				try {
					val enabled = try {
						CoolConfigHelper.getJsonConfig(
							getContext(),
							"miui_plus_cust_pc_name_switch"
						) as Boolean
					} catch (e: Exception) {
						false
					}
					if (enabled) {
						val computerTail =
							CoolConfigHelper.getJsonConfig(getContext(), "miui_plus_pc_tail")
						when (computerTail) {
							"" -> Unit
							else -> {
								XposedBridge.log("电脑小尾巴替换成功: $computerTail")
								resparam.res.setReplacement(
									"com.xiaomi.mirror",
									"string",
									"default_boss_terminal_name",
									computerTail
								)
							}
						}
					}
				} catch (e: Exception) {
					XposedBridge.log(e)
				}
			}
		}
	}

	@SuppressLint("PrivateApi", "ObsoleteSdkInt", "NewApi")
	override fun handleLoadPackage(lparam: XC_LoadPackage.LoadPackageParam?) {
		if (lparam != null) {

			EzXHelperInit.initHandleLoadPackage(lparam)
			EzXHelperInit.setLogTag("Fuck Miui Xposed")
			EzXHelperInit.setToastTag("FM")

			when (lparam.packageName) {
				BuildConfig.APPLICATION_ID -> {
					try {
						XposedBridge.log("FM: Hook self succeed!")

						findMethod("com.coolest.toolbox.ui.MainActivity") {
							name == "isXposedActivated" && isNotStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: detecting activated")
							}
							after { param ->
								XposedBridge.log("FM: set module active state to activated")
								param.result = true
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}
				}

				"android" -> {
					//miui-services.jar
					try {
						if (CoolUtils.isMIUI()) {
							findMethod("com.android.server.wm.MiuiFreeFormStackDisplayStrategy") {
								name == "getMaxMiuiFreeFormStackCount" && isNotStatic && returnType == Int::class.java
							}.hookMethod {
								before { lparam ->
									lparam.result = 256
									XposedBridge.log("[FM] 已将无限小窗个数改为${lparam.result}")
								}
								after { param ->

								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log("[FM] 无限小窗出现错误!")
						XposedBridge.log(e)
					}

					//services.jar
					try {
						if (CoolUtils.isMIUI()) {
							findMethod("com.android.server.wm.Task") {
								name == "isResizeable" && isNotStatic && returnType == Boolean::class.java
							}.hookMethod {
								before { lparam ->
									lparam.result = true
									XposedBridge.log("[FM] 强制设置所有程序允许小窗${lparam.result}")
								}
								after { param ->

								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log("[FM] 强制小窗出现错误!")
						XposedBridge.log(e)
					}

					//miui-framework.jar
					try {
						if (CoolUtils.isMIUI()) {
							findMethod("android.util.MiuiMultiWindowAdapter") {
								name == "getFreeformBlackList" && isStatic
							}.hookMethod {
								before { lparam ->
									lparam.result = listOf("")
									XposedBridge.log("[FM] 小窗黑名单=空列表")
								}
								after { param ->

								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log("[FM] 小窗黑名单出现错误!")
						XposedBridge.log(e)
					}

					//miui-framework.jar
					try {
						if (CoolUtils.isMIUI()) {
							findMethod("android.util.MiuiMultiWindowAdapter") {
								name == "getFreeformBlackListFromCloud" && isStatic
							}.hookMethod {
								before { lparam ->
									lparam.result = listOf("")
									XposedBridge.log("[FM] 小窗云控黑名单=空列表")
								}
								after { param ->

								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log("[FM] 小窗云控黑名单出现错误!")
						XposedBridge.log(e)
					}

					//miui-framework.jar
					try {
						if (CoolUtils.isMIUI()) {
							findMethod("android.util.MiuiMultiWindowUtils") {
								name == "supportFreeform" && isStatic && returnType == Boolean::class.java
							}.hookMethod {
								before { lparam ->
									XposedBridge.log("[FM] 云控设备不支持小窗 = false")
									lparam.result = true
								}
								after { param ->

								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log("[FM] 云控不支持小窗设备列表出现错误!")
						XposedBridge.log(e)
					}
				}

//				"com.miui.misound" -> hookMiSound()
				"com.miui.misound" -> try {
					if (CoolUtils.isMIUI()) {

						findMethod("com.miui.misound.util.Utils") {
							name == "isSupportHarmanKardon" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before { lparam ->
								/*val jsonResult =
									CoolConfigHelper.getJsonConfig(getContext(),"harmanKardon_enabled")
								XposedBridge.log("FM: 哈曼卡顿启用状态: $jsonResult")
								lparam.result = try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}*/
								lparam.result = true
							}
							after { param ->

							}
						}
						/*findMethod("com.miui.misound.util.Utils") {
							name == "isSupportDolbyDax" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before { lparam ->
								*//*val jsonResult = when(CoolUtils.isPad()) {
									true -> true
									false -> CoolConfigHelper.getJsonConfig("dolby_enabled")
								}*//*
								*//*val jsonResult = CoolConfigHelper.getJsonConfig(getContext(),"dolby_enabled")
								XposedBridge.log("FM: 杜比全景声启用状态: $jsonResult")
								lparam.result = try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}*//*
								lparam.result = true
							}
							after { param ->

							}
						}*/

					}
				} catch (e: Exception) {
					XposedBridge.log("[FM] 哈曼卡顿出现错误!")
					XposedBridge.log(e)
				}

				"com.miui.home" -> {
					if (CoolUtils.isMIUI()) {

						try {
							findMethod("com.miui.home.launcher.RecentsAndFSGestureUtils") {
								name == "canTaskEnterSmallWindow" && isStatic && returnType == Boolean::class.java
							}.hookMethod {
								before { param ->
									XposedBridge.log("[FM]: 允许进程进入小窗")
									param.result = true
								}
								after { param ->
								}
							}
						} catch (e: Exception) {
							XposedBridge.log(e)
						}

						try {
							findMethod("com.miui.home.launcher.RecentsAndFSGestureUtils") {
								name == "canTaskEnterMiniSmallWindow" && isStatic && returnType == Boolean::class.java
							}.hookMethod {
								before {
									XposedBridge.log("FM: detecting activated")
								}
								after { param ->
									XposedBridge.log("FM: set module active state to activated")
									param.result = true
								}
							}
						} catch (e: Exception) {
							XposedBridge.log(e)
						}
					}
				}

				"com.miui.securitycenter" -> {
					if (CoolUtils.isMIUI()) {
						val jsonResult =
							CoolConfigHelper.getJsonConfig(
								getContext(),
								"miui_secure_center_lock_100"
							)
						when (jsonResult as Boolean) {
							true -> {
								//防止点击重新检测
								findMethod("com.miui.securityscan.ui.main.MainContentFrame") {
									name == "onClick" && parameterTypes[0] == View::class.java
								}.hookMethod {
									before { param -> param.result = null }
								}

								//锁定100分
								findMethod("com.miui.securityscan.scanner.ScoreManager") {
									name == "B"
								}.hookMethod {
									before { param -> param.result = 0 }
								}
							}
							false -> {}
						}
					}
				}

				"com.xiaomi.mirror" -> {
					XposedBridge.log("FM: MIUI+ app has found!")

					//解锁平板MIUI+
					try {
						findMethod("com.xiaomi.mirror.utils.DeviceUtils") {
							name == "isPadDevice" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
//								XposedBridge.log("FM: Found method in [com.xiaomi.mirror.utils.DeviceUtils]")
								XposedBridge.log("FM: Found method @isPad in [com.xiaomi.mirror]")
							}
							after { param ->
//								XposedBridge.log("FM: com.xiaomi.mirror.utils.DeviceUtils.isPadDevice() returns false")
								val jsonResult =
									CoolConfigHelper.getJsonConfig(
										getContext(),
										"miui_plus_connect_pc"
									)
								XposedBridge.log("FM: Hooked com.xiaomi.mirror @isPad, user's setting is $jsonResult")
								param.result = !try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}

					//解锁平板MIUI+
					//3.5.17c
					try {
						findMethod("com.xiaomi.mirror.device.DeviceUtils") {
							name == "isPadDevice" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
//								XposedBridge.log("FM: Found method in [com.xiaomi.mirror.utils.DeviceUtils]")
								XposedBridge.log("FM: Found method @isPad in [com.xiaomi.mirror] v3.5.17c")
							}
							after { param ->
//								XposedBridge.log("FM: com.xiaomi.mirror.utils.DeviceUtils.isPadDevice() returns false")
								val jsonResult =
									CoolConfigHelper.getJsonConfig(
										getContext(),
										"miui_plus_connect_pc"
									)
								XposedBridge.log("FM: Hooked com.xiaomi.mirror @isPad, user's setting is $jsonResult  v3.5.17c")
								param.result = !try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}

					try {
						findMethod("com.xiaomi.onetrack.util.DeviceUtil") {
							name == "p" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
//								XposedBridge.log("FM: Found method in [com.xiaomi.mirror.utils.DeviceUtils]")
								XposedBridge.log("FM: Found method @isPad in [com.xiaomi.mirror]")
							}
							after { param ->
//								XposedBridge.log("FM: com.xiaomi.mirror.utils.DeviceUtils.isPadDevice() returns false")
								val jsonResult =
									CoolConfigHelper.getJsonConfig(
										getContext(),
										"miui_plus_connect_pc"
									)
								XposedBridge.log("FM: Hooked com.xiaomi.mirror @isPad, user's setting is $jsonResult")
								param.result = !try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}

/*					try {
						findMethod("com.xiaomi.mirror.ak") {
							name == "a" && returnType == Boolean::class.java
						}.hookMethod {
							before {
//								XposedBridge.log("FM: Found method in [com.xiaomi.mirror.ak]")
								XposedBridge.log("FM: Found method @a in [com.xiaomi.mirror]")
							}
							after { param ->
//								XposedBridge.log("FM: com.xiaomi.mirror.ak.a() returns true")
								XposedBridge.log("FM: Hooked com.xiaomi.mirror @a")
								param.result = true
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}*/

					//解锁不支持得的设备的MIUI+
					try {
						findMethod("com.xiaomi.mirror.utils.SystemUtils") {
							name == "isModelSupport" && returnType == Boolean::class.java
						}.hookMethod {
							before {
//								XposedBridge.log("FM: Found method in [com.xiaomi.mirror.utils.SystemUtils]")
								XposedBridge.log("FM: Found method @SystemUtils in [com.xiaomi.mirror]")
							}
							after { param ->
//								XposedBridge.log("FM: com.xiaomi.mirror.utils.SystemUtils.isModelSupport() returns true")
								XposedBridge.log("FM: Hooked com.xiaomi.mirror @SystemUtils!")
								param.result = true
							}
						}

						/*
						//======================================
						//修改类中的变量值示例代码
						val field = findAllFields("com.miui.player.effect.dirac.DiracUtils", false){
							true
						}
						lateinit var myObject: Any;
						lateinit var myField: Field;
						field.forEach {
							XposedBridge.log("[debug] " + it.name)

							if (it.name == "VAL_OFF") {
								myField = it
								myObject = it.get(it.javaClass)!!
							}
						}
						myField.set(myField.javaClass, false)
						//======================================

						//======================================
						//调用某个类中的方法
						val method = findMethod("com.miui.player.effect.dirac.DiracUtils"){
							name == "isSupportEarcompensation" && isStatic && returnType == Boolean::class.java
						}
						val bool = method.invoke(Boolean::class.java)//这个方法返回值是boolean所以参数传的是Boolean::class.java
						//======================================
						 */

					} catch (e: Exception) {
						XposedBridge.log(e)
					}

					//自定义电脑名
					try {
						val enabled = try {
							CoolConfigHelper.getJsonConfig(
								getContext(),
								"miui_plus_cust_pc_name_switch"
							) as Boolean
						} catch (e: Exception) {
							false
						}
						if (enabled) {
							findMethod("com.xiaomi.mirror.MirrorAppService") {
								name == "getMasterName" && isNotStatic && returnType == String::class.java
							}.hookMethod {
								before {
//								XposedBridge.log("FM: Found method in [com.xiaomi.mirror.utils.SystemUtils]")
									XposedBridge.log("FM: Found method @masterName in [com.xiaomi.mirror]")
								}
								after { param ->
//								XposedBridge.log("FM: com.xiaomi.mirror.utils.SystemUtils.isModelSupport() returns true")
									val computerName =
										CoolConfigHelper.getJsonConfig(
											getContext(),
											"miui_plus_pc_name"
										)
									XposedBridge.log("自定义电脑名 $enabled -> $computerName")
									param.result = when (computerName) {
										"" -> param.result
										else -> computerName
									}
								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}
				}

				"com.android.settings" -> {
					XposedBridge.log("FM: Settings Found!")

					try {
						findMethod("com.android.settings.connection.MiMirrorController") {
							name == "isMirrorSupported" && isNotStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
//								XposedBridge.log("FM: Found method in [com.android.settings.connection.MiMirrorController]")
								XposedBridge.log("FM: Found method in [com.android.settings]")
							}
							after { param ->
//								XposedBridge.log("FM: com.android.settings.connection.MiMirrorController.isMirrorSupport() returns true")
								XposedBridge.log("FM: Hooked com.android.settings!")
								param.result = true
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}
				}

				//平板流转手机
				/*"com.xiaomi.mi_connect_service" -> {
					XposedBridge.log("FM: mi_connect_service app has found!")

					try {
						findMethod("b.h.q.e2.q") {
							name == "p" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [mi_connect_service  {p}]")
							}
							after { param ->
								val jsonResult =
									CoolConfigHelper.getJsonConfig("miui_plus_connect_pc")
								XposedBridge.log("FM: Hooked com.xiaomi.mi_connect_service  {p} @isPad, user's setting is $jsonResult")
								param.result = !try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}
							}
						}
						findMethod("b.h.q.e2.q") {
							name == "q" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [mi_connect_service  {q}]")
							}
							after { param ->
								val jsonResult =
									CoolConfigHelper.getJsonConfig("miui_plus_connect_pc")
								XposedBridge.log("FM: Hooked com.xiaomi.mi_connect_service  {q} @isPad, user's setting is $jsonResult")
								param.result = !try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}
							}
						}
						findMethod("b.h.e.e") {
							name == "e" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [mi_connect_service  {e}]")
							}
							after { param ->
								val jsonResult =
									CoolConfigHelper.getJsonConfig("miui_plus_connect_pc")
								XposedBridge.log("FM: Hooked com.xiaomi.mi_connect_service  {e} @isPad, user's setting is $jsonResult")
								param.result = !try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}
							}
						}
						findMethod("com.xiaomi.onetrack.h.h") {
							name == "o" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [mi_connect_service  {o}]")
							}
							after { param ->
								val jsonResult =
									CoolConfigHelper.getJsonConfig("miui_plus_connect_pc")
								XposedBridge.log("FM: Hooked com.xiaomi.mi_connect_service  {o}  @isPad, user's setting is $jsonResult")
								param.result = !try {
									jsonResult as Boolean
								} catch (e: Exception) {
									false
								}
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}
				}*/

				//解锁通话
				/*"com.android.contacts" -> {
					XposedBridge.log("FM: Contacts app has found!")

					try {
						findMethod("com.android.contacts.activities.PeopleActivityV2") {
							name == "processIntent" && isNotStatic && returnType == Boolean::class.java && paramCount == 1
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [com.android.contacts.activities.PeopleActivityV2]")
							}
							after { param ->
								XposedBridge.log("FM: com.android.contacts.activities.PeopleActivityV2.processIntent() returns true")
								param.result = true
							}
						}
					} catch (e: Exception) {
//                        XposedBridge.log(e)
					}

					try {
						findMethod("com.miui.contacts.common.SystemUtil") {
							name == "B" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [com.miui.contacts.common.SystemUtil]")
							}
							after { param ->
								XposedBridge.log("FM: com.miui.contacts.common.SystemUtil.B() returns true")
								param.result = false
							}
						}
					} catch (e: Exception) {
//                        XposedBridge.log(e)
					}

					try {
						findMethod("com.miui.contacts.common.SystemUtil") {
							name == "a" && isStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [com.miui.contacts.common.SystemUtil]")
							}
							after { param ->
								XposedBridge.log("FM: com.miui.contacts.common.SystemUtil.a() returns true")
								param.result = false
							}
						}
					} catch (e: Exception) {
//                        XposedBridge.log(e)
					}
				}

				"com.android.server.telecom" -> {
					XposedBridge.log("FM: ContactServer app has found!")

					try {
						findMethod("com.android.server.telecom.components.UserCallIntentProcessor") {
							name == "isVoiceCapable" && isNotStatic && returnType == Boolean::class.java
						}.hookMethod {
							before {
								XposedBridge.log("FM: Found method in [com.android.server.telecom.components.UserCallIntentProcessor]")
							}
							after { param ->
								XposedBridge.log("FM: com.android.server.telecom.components.UserCallIntentProcessor.isVoiceCapable() returns true")
								param.result = true
							}
						}
					} catch (e: Exception) {
						XposedBridge.log(e)
					}
				}*/

			}
		}
	}
}