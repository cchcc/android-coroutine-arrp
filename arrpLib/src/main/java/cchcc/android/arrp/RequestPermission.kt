package cchcc.android.arrp

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
    fun permissionResult(requestCode: Int, grantResults: IntArray): Unit =
        rpContinuation[requestCode]?.let { continuation ->
            rpContinuation.remove(requestCode)
            continuation.resume(grantResults.all { it == PackageManager.PERMISSION_GRANTED })
        } ?: Unit

    /**
     *  @param permissions array of [android.Manifest.permission]
     *  @return whether permission is granted
     */
    fun Activity.hasPermission(vararg permissions: String): Boolean

    /**
     *  Convenient version of [Activity.requestPermissions] on coroutine.
     *  Await till finish to process of `requestPermission`, and then return whether it is granted.
     *  This method checks if permission is all granted at first. If not, request permission to user by system popup.
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
     *  Convenient version of [Fragment.requestPermissions] on coroutine.
     *  Await till finish to process of `requestPermission`, and then return whether it is granted.
     *  This method checks if permission is all granted at first. If not, request permission to user by system popup.
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
        fun create(): RequestPermission = RequestPermissionImpl()

        /**
         *  Add requestCode that need to be prevent to generate requestCode internally.
         *
         *  @param requestCode
         *  @return whether adding is success
         */
        fun addExcludeRequestCode(requestCode: Int): Boolean = excludeRequestCode.add(requestCode)

        internal val excludeRequestCode by lazy(LazyThreadSafetyMode.NONE) { mutableSetOf<Int>() }
    }
}

