package cchcc.android.arrp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class ActivityResultImpl : ActivityResult {
    override val arContinuations by lazy(LazyThreadSafetyMode.NONE) {
        SparseArray<Continuation<Pair<Int, Intent?>>>()
    }

    override suspend fun Activity.startActivityForResultAwait(intent: Intent, options: Bundle?)
            : Pair<Int, Intent?> = activityResultAwait {
        ActivityCompat.startActivityForResult(this, intent, it, options)
    }

    override suspend fun Activity.activityResultAwait(startActivity: (Int) -> Unit)
            : Pair<Int, Intent?> = suspendCoroutine { continuation ->
        val requestCode = generateRequestCode(continuation.hashCode())
        arContinuations.put(requestCode, continuation)
        startActivity(requestCode)
    }

    override suspend fun Fragment.startActivityForResultAwait(intent: Intent, options: Bundle?)
            : Pair<Int, Intent?> = activityResultAwait {
        startActivityForResult(intent, it, options)
    }

    override suspend fun Fragment.activityResultAwait(startActivity: (Int) -> Unit)
            : Pair<Int, Intent?> = suspendCoroutine { continuation ->
        val requestCode = generateRequestCode(continuation.hashCode())
        arContinuations.put(requestCode, continuation)
        startActivity(requestCode)
    }

    // requestCode must be <= 0xffff
    private fun generateRequestCode(seed: Int): Int {
        var code: Int
        var tryCount = 0

        while (true) {
            code = Math.abs(seed + tryCount++) shr 16
            if (arContinuations[code] != null)
                continue

            if (seed in ActivityResult.excludeRequestCode)
                continue

            break
        }

        return code
    }
}