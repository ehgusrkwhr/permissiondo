package com.call.permissiondo.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionModule(private val activity: AppCompatActivity) {
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { (permission, granted) ->
            when {
                granted -> {
                    // 권한이 허용되었을 때 콜백
                    onPermissionGranted?.invoke(permission)
                }
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                    // 권한이 거부되었을 때 콜백
                    onPermissionDenied?.invoke(permission)
                }
                else -> {
                    // 권한이 영구적으로 거부되었을 때 콜백
                    onPermissionDeniedPermanently?.invoke(permission)
                }
            }
        }
    }

    var onPermissionGranted: ((String) -> Unit)? = null
    var onPermissionDenied: ((String) -> Unit)? = null
    var onPermissionDeniedPermanently: ((String) -> Unit)? = null



    fun requestPermissions(vararg permissions: String) {
        requestPermissionLauncher.launch(permissions.toList().toTypedArray())
    }

    // 특수 권한 확인 (다른 앱 위에 그리기, 시스템 설정 변경 등)
    fun checkSpecialPermission(permission: String): Boolean {
        return when (permission) {
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(activity)
                } else {
                    true
                }
            }
            else -> false
        }
    }

    // 특수 권한 요청
    fun requestSpecialPermission(permission: String) {
        val intent = when (permission) {
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION -> {
                Intent(permission, Uri.parse("package:${activity.packageName}"))
            }
            else -> return
        }
        activity.startActivity(intent)
    }

}