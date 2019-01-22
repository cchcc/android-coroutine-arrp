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
     *  Activity 와 Fragment 의 멤버 함수 onRequestPermissionsResult 에서 호출 할것
     */
    fun permissionResult(requestCode: Int, grantResults: IntArray) = rpContinuation[requestCode]?.let { continuation ->
        rpContinuation.remove(requestCode)
        continuation.resume(grantResults.all { it == PackageManager.PERMISSION_GRANTED })
    }

    fun Activity.hasPermission(vararg permissions: String): Boolean
    suspend fun Activity.requestPermissionAwait(vararg permissions: String): Boolean

    fun Fragment.hasPermission(vararg permissions: String): Boolean
    suspend fun Fragment.requestPermissionAwait(vararg permissions: String): Boolean

    companion object {
        fun create() = RequestPermissionImpl()
    }
}

