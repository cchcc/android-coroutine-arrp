package cchcc.android.requestpermission

import android.app.Activity
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.fragment.app.Fragment
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

interface RequestPermission {
    val rpContinuation: SparseArray<Continuation<Boolean>>

    /**
     *  It must be called in `onRequestPermissionsResult(...)`
     *
     *  ```
     *  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
     *      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
     *      permissionResult(requestCode, grantResults)
     *  }
     *  ```
     */
    fun permissionResult(requestCode: Int, grantResults: IntArray) = rpContinuation[requestCode]?.let { continuation ->
        rpContinuation.remove(requestCode)
        continuation.resume(grantResults.all { it == PackageManager.PERMISSION_GRANTED })
    }

    /**
     *  @param permissions array of [android.Manifest.permission]
     *  @return whether permission is granted
     */
    fun Activity.hasPermission(vararg permissions: String): Boolean

    /**
     *  Await till finish to process of `requestPermission`, and then return whether it is granted.
     *  First, check if permission is all granted. If not, request permission to user by system popup.
     *
     *  If return is not working, check if [RequestPermission.permissionResult] is placed appropriate.
     *
     *  @param permissions array of [android.Manifest.permission]
     *  @return whether permission is granted
     */
    suspend fun Activity.requestPermissionAwait(vararg permissions: String): Boolean

    /**
     *  @param permissions array of [android.Manifest.permission]
     *  @return whether permission is granted
     */
    fun Fragment.hasPermission(vararg permissions: String): Boolean

    /**
     *  Await till finish to process of `requestPermission`, and then return whether it is granted.
     *  First, check if permission is all granted. If not, request permission to user by system popup.
     *
     *  If return is not working, check if [RequestPermission.permissionResult] is placed appropriate.
     *
     *  @param permissions array of [android.Manifest.permission]
     *  @return whether permission is granted
     */
    suspend fun Fragment.requestPermissionAwait(vararg permissions: String): Boolean

    companion object {
        /**
         *  Create instance for delegate object.
         *
         *  ```
         *  class MyActivity : AppCompatActivity(), RequestPermission by RequestPermission.create()
         *  ```
         */
        fun create() = RequestPermissionImpl()
    }
}

