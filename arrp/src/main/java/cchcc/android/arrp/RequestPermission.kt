package cchcc.android.arrp

import android.app.Activity
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.fragment.app.Fragment
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

interface RequestPermission {
    val rpContinuation: SparseArray<Continuation<RequestPermission.Result>>

    sealed class Result {
        object Granted : Result()
        data class Denied(val granted: List<String>, val denied: List<String>) : Result()

        val isGranted: Boolean get() = this is Granted
    }

    /**
     *  It must be called in `onRequestPermissionsResult(...)`
     *
     *  ```
     *  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
     *      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
     *      permissionResult(requestCode, permissions, grantResults)
     *  }
     *  ```
     */
    fun permissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Unit =
        rpContinuation[requestCode]?.let { continuation ->
            rpContinuation.remove(requestCode)
            var idx = 0
            val (granted, denied) = permissions.partition { grantResults[idx++] == PackageManager.PERMISSION_GRANTED }
            val result = if (denied.isEmpty()) Result.Granted else Result.Denied(granted, denied)
            continuation.resume(result)
        } ?: Unit

    /**
     *  Convenient version of [Activity.requestPermissions] on coroutine.
     *  Await till finish to process of `requestPermission`, and then return whether it is granted.
     *  This method checks if permission is all granted at first. If not, it request permission to user by system popup.
     *
     *  If return is not working, check if [RequestPermission.permissionResult] is right in place.
     *
     *  @param permissions array of [android.Manifest.permission]
     *  @return [RequestPermission.Result] whether permission is granted
     */
    suspend fun Activity.requestPermissionAwait(vararg permissions: String): RequestPermission.Result

    /**
     *  Convenient version of [Fragment.requestPermissions] on coroutine.
     *  Await till finish to process of `requestPermission`, and then return whether it is granted.
     *  This method checks if permission is all granted at first. If not, it request permission to user by system popup.
     *
     *  If return is not working, check if [RequestPermission.permissionResult] is right place.
     *
     *  @param permissions array of [android.Manifest.permission]
     *  @return [RequestPermission.Result] whether permission is granted
     */
    suspend fun Fragment.requestPermissionAwait(vararg permissions: String): RequestPermission.Result

    companion object {
        /**
         *  Create instance for delegate object.
         *
         *  ```
         *  class BaseActivity : AppCompatActivity(), RequestPermission by RequestPermission.create()
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

