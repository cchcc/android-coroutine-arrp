package cchcc.android.activityresult

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

interface ActivityResult {
    val arContinuations: SparseArray<Continuation<Pair<Int, Intent?>>>

    /**
     *  onActivityResult 에서 호출할것
     */
    fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) = arContinuations[requestCode]?.let {
        arContinuations.remove(requestCode)
        it.resume(resultCode to data)
    }

//    suspend fun startActivityForResultAwait(intent: Intent, options: Bundle? = null): Pair<Int, Intent?>
    suspend fun Activity.startActivityForResultAwait(intent: Intent, options: Bundle? = null): Pair<Int, Intent?>

    suspend fun Fragment.startActivityForResultAwait(intent: Intent, options: Bundle? = null): Pair<Int, Intent?>

    companion object {
        fun create() = ActivityResultImpl()
    }
}