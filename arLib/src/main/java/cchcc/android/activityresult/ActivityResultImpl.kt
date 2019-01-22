package cchcc.android.activityresult

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
            : Pair<Int, Intent?> = suspendCoroutine { continuation ->
        val requestCode = generateRequestCode(intent.hashCode())
        arContinuations.put(requestCode, continuation)
        ActivityCompat.startActivityForResult(this, intent, requestCode, options)
    }

    override suspend fun Fragment.startActivityForResultAwait(intent: Intent, options: Bundle?)
            : Pair<Int, Intent?> = suspendCoroutine { continuation ->
        val requestCode = generateRequestCode(intent.hashCode())
        arContinuations.put(requestCode, continuation)
        startActivityForResult(intent, requestCode, options)
    }

    // requestCode must be <= 0xffff
    private fun generateRequestCode(seed: Int): Int {
        var code: Int
        var tryCount = 0

        while (true) {
            code = Math.abs(seed + tryCount++) shr 16
            if (arContinuations[code] != null)
                continue

            if (seed in excludeRequestCode)
                continue

            break
        }

        return code
    }

    companion object {
        private val excludeRequestCode by lazy(LazyThreadSafetyMode.NONE) { mutableSetOf<Int>() }

        /**
         *  Add request code that need to be ignored from `generateRequestCode()`.
         *  So, `generateRequestCode()` will not return added code.
         */
        fun addExcludeRequestCode(requestCode: Int) = excludeRequestCode.add(requestCode)
    }
}