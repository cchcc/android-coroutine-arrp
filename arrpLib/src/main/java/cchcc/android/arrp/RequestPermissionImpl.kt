package cchcc.android.arrp

import android.app.Activity
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RequestPermissionImpl : RequestPermission {
    override val rpContinuation by lazy(LazyThreadSafetyMode.NONE) {
        SparseArray<Continuation<Boolean>>()
    }

    override fun Activity.hasPermission(vararg permissions: String): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun Fragment.hasPermission(vararg permissions: String): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun Activity.requestPermissionAwait(vararg permissions: String)
            : Boolean = suspendCoroutine { continuation ->
        val activity = this@requestPermissionAwait

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_DENIED
        }

        if (notGranted.isEmpty()) {
            continuation.resume(true)
        } else {
            val requestCode = generateRequestCode(permissions.hashCode())
            rpContinuation.put(requestCode, continuation)
            ActivityCompat.requestPermissions(activity, notGranted.toTypedArray(), requestCode)
        }
    }

    override suspend fun Fragment.requestPermissionAwait(vararg permissions: String)
            : Boolean = suspendCoroutine { continuation ->
        val context = this@requestPermissionAwait.requireContext()

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
        }

        if (notGranted.isEmpty()) {
            continuation.resume(true)
        } else {
            val requestCode = generateRequestCode(permissions.hashCode())
            rpContinuation.put(requestCode, continuation)
            requestPermissions(notGranted.toTypedArray(), requestCode)
        }
    }

    private fun generateRequestCode(seed: Int): Int {
        var code: Int
        var tryCount = 0

        while (true) {
            code = Math.abs(seed + tryCount++) shr 16
            if (rpContinuation[code] != null)
                continue

            if (seed in RequestPermission.excludeRequestCode)
                continue

            break
        }

        return code
    }
}