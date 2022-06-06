package com.example.maptest.permission

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

object PermissionManager {

    private const val REQUEST_CODE = 9999

    private var activity: WeakReference<Activity>? = null
    private var runnable: Runnable? = null

    private val permissionNotGrantedList = mutableListOf<String>()

    fun requestPermission(
        activity: Activity,
        vararg permission: String,
        runnableAfterPermissionGranted: Runnable? = null
    ) {

        permissionNotGrantedList.clear()

        for (i in permission.indices) {
            if (ActivityCompat.checkSelfPermission(activity, permission[i]) != PackageManager.PERMISSION_GRANTED) {
                // if permission is not granted
                permissionNotGrantedList.add(permission[i])
            }
        }

        if (permissionNotGrantedList.isNotEmpty()) {

            // save the context and runnable
            runnable = runnableAfterPermissionGranted
            this.activity = WeakReference(activity)

            // request permision
            ActivityCompat.requestPermissions(
                activity,
                permissionNotGrantedList.toTypedArray(),
                REQUEST_CODE
            )
        } else {
            // if all of permissions is granted
            runnableAfterPermissionGranted?.run()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CODE) {
            activity?.get()?.let {
                val deniedPermissionList = mutableListOf<String>()

                for (i in grantResults.indices) {

                    val granResult = grantResults[i]
                    val permission = permissions[i]

                    if (granResult != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(it, permission)) {
                            // if users denied permission, should request again
                            deniedPermissionList.add(permission)
                        } else {
                            // if users denied permission twice, than go to app setting page
                            AlertDialog.Builder(it).apply {

                                this.setTitle("your tittle here")
                                this.setMessage("your message here. For example, why need to request permission")
                                this.setPositiveButton("Confirm") { _: DialogInterface?, _: Int ->
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", it.packageName, null)
                                    intent.data = uri
                                    it.startActivityForResult(intent, REQUEST_CODE)
                                }
                                this.create()
                                this.show()
                            }
                            return@let
                        }
                    }
                }

                if (deniedPermissionList.isEmpty()) {
                    runnable?.run()
                }

                activity = null
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == REQUEST_CODE) {
            activity?.get()?.let {

                val deniedPermissionList = mutableListOf<String>()

                if (resultCode == Activity.RESULT_CANCELED) {

                    for (i in permissionNotGrantedList.indices) {

                        val grantResult = ActivityCompat.checkSelfPermission(it, permissionNotGrantedList[i])
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            deniedPermissionList.add(permissionNotGrantedList[i])
                        }
                    }
                }

                if (deniedPermissionList.isEmpty()) {
                    runnable?.run()
                    permissionNotGrantedList.clear()
                }

                activity = null
            }
        }
    }
}