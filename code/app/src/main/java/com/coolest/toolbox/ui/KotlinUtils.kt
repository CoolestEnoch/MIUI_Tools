package com.coolest.toolbox.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.permissionx.guolindev.PermissionX

class KotlinUtils {

	companion object {

		fun request_permissions(activity: AppCompatActivity) {
			PermissionX.init(activity)
				.permissions(
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.MANAGE_EXTERNAL_STORAGE
				)
				.explainReasonBeforeRequest()
				.onExplainRequestReason { scope, deniedList ->
					scope.showRequestReasonDialog(deniedList, "配置文件存取权限", "好", "取消")
				}
				.onForwardToSettings { scope, deniedList ->
					scope.showForwardToSettingsDialog(
						deniedList,
						"不授予权限则可能出现闪退!",
						"好",
						"取消"
					)
				}
				.request { allGranted, grantedList, deniedList ->
					if (allGranted) {
						Snackbar.make(
							activity.window.decorView,
							"权限状态正常",
							Snackbar.LENGTH_LONG
						).show()
					} else {
						Snackbar.make(
							activity.window.decorView,
							"以下权限被拒绝: $deniedList",
							Snackbar.LENGTH_LONG
						).show()
					}
				}

			/*//判断能否在在安卓11以上的设备免root管理Android/data
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
					//表明已经有这个权限了
					val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
					startActivity(intent)
				}*/
		}
	}
}