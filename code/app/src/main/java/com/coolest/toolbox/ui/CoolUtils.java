package com.coolest.toolbox.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import de.robv.android.xposed.XposedBridge;

public class CoolUtils {

	public final String config_path = "/sdcard";

	/**
	 * 判断是否为MIUI系统
	 *
	 * @return
	 */
	public static boolean isMIUI() {
		String manufacturer = Build.MANUFACTURER.toLowerCase();

		return !TextUtils.isEmpty(manufacturer) && (manufacturer.equals("xiaomi") || manufacturer.equals("blackshark"));
	}

	/**
	 * 判断是否为平板
	 *
	 * @return
	 */
	public static boolean isPad() {
		return adbExec_without_root_stable("getprop ro.build.characteristics").contains("tablet");
	}

	/**
	 * 获取当前apk版本号
	 *
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
//			versioncode = pi.versionCode;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
			XposedBridge.log(e);
		}
		return versionName;
	}

	/**
	 * 判断手机是否已root
	 *
	 * @return
	 */
	public static boolean get_root_state() {
		boolean res = false;
		try {
			if ((!new File("/system/bin/su").exists()) &&
					(!new File("/system/xbin/su").exists())) {
				res = false;
			} else {
				res = true;
			}
			;
		} catch (Exception e) {

		}
		return res;
	}

	/**
	 * root执行adb 稳定版
	 *
	 * @param cmd
	 * @return
	 */
	public static String adbExec_with_root_stable(String cmd) {
		String content = "";
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su"); //切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			XposedBridge.log(e);
//			return "Failed, permission denied.";
			return content;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
				XposedBridge.log(e);
			}
		}
//		return "Success.";
		return content;
	}

	/**
	 * 无root执行adb 稳定版
	 *
	 * @param cmd
	 * @return
	 */
	public static String adbExec_without_root_stable(String cmd) {
		String content = "";
		Process process = null;
		BufferedReader reader = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec(cmd); //切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			os.writeBytes(cmd + "\n");
//			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			StringBuffer output = new StringBuffer();
			int read;
			char[] buffer = new char[4096];
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();
			content = output.toString();
		} catch (Exception e) {
			e.printStackTrace();
			XposedBridge.log(e);
//			return "Failed, permission denied.";
			return content;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
				XposedBridge.log(e);
			}
		}
//		return "Success.";
		return content;
	}

	//TODO 提权函数
	public static boolean get_root_access(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); //切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
			XposedBridge.log(e);
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
				XposedBridge.log(e);
			}
		}
		return true;
	}

	public static String adbExec_with_root(String cmd) {
		String content = "";
		Process process = null;
		BufferedReader reader = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su"); //切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			StringBuffer output = new StringBuffer();
			int read;
			char[] buffer = new char[4096];
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();
			content = output.toString();
		} catch (Exception e) {
			e.printStackTrace();
			XposedBridge.log(e);
//			return "Failed, permission denied.";
			return content;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
				XposedBridge.log(e);
			}
		}
//		return "Success.";
		return content;
	}

	/**
	 * 执行adb命令_旧
	 *
	 * @param cmd
	 * @return
	 */
	public static String adbExec_no_root(String cmd) {
		BufferedReader reader = null;
		String content = "";
		try {
			//("ps -P|grep bg")执行失败，PC端adb shell ps -P|grep bg执行成功
			//Process process = Runtime.getRuntime().exec("ps -P|grep tv");
			//-P 显示程序调度状态，通常是bg或fg，获取失败返回un和er
			// Process process = Runtime.getRuntime().exec("ps -P");
			//打印进程信息，不过滤任何条件
			Process process = Runtime.getRuntime().exec(cmd);
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuffer output = new StringBuffer();
			int read;
			char[] buffer = new char[4096];
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();
			content = output.toString();
			process.destroy();
		} catch (Exception e) {
			e.printStackTrace();
			XposedBridge.log(e);
		}
		return content;
	}
}
