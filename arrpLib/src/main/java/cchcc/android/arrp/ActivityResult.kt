package cchcc.android.arrp

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
     *  It must be called in `onActivityResult(...)` of [Activity] or [Fragment]
     *
     *  ```
     *  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     *      super.onActivityResult(requestCode, resultCode, data)
     *      activityResult(requestCode, resultCode, data)
     *  }
     *  ```
     */
    fun activityResult(requestCode: Int, resultCode: Int, data: Intent?): Unit =
        arContinuations[requestCode]?.let {
            arContinuations.remove(requestCode)
            it.resume(resultCode to data)
        } ?: Unit

    /**
     *  Convenient version of [Activity.startActivityForResult] on coroutine.
     *  Await till finishing process of `startActivityForResult`, and then returns (resultCode, data).
     *
     *  If return is not working, check if [ActivityResult.activityResult] is placed appropriate.
     *
     *  @param intent [Intent]
     *  @param options [Bundle]
     *  @return [Pair] of (resultCode, data)
     */
    suspend fun Activity.startActivityForResultAwait(intent: Intent, options: Bundle? = null): Pair<Int, Intent?>

    /**
     *  Await till finishing process of `startActivityForResult`, and then return (resultCode, data).
     *
     *  If return is not working, check if [ActivityResult.activityResult] is placed appropriate.
     *
     *  @param startActivity block that should call [Activity.startActivityForResult] with `requestCode` from parameter.
     *  @return [Pair] of (resultCode, data)
     */
    suspend fun Activity.activityResultAwait(startActivity: (requestCode: Int) -> Unit): Pair<Int, Intent?>

    /**
     *  Convenient version of [Fragment.startActivityForResult] on coroutine.
     *
     *  If return is not working, check if [ActivityResult.activityResult] is placed appropriate.
     *
     *  @param intent [Intent]
     *  @param options [Bundle]
     *  @return [Pair] of (resultCode, data)
     */
    suspend fun Fragment.startActivityForResultAwait(intent: Intent, options: Bundle? = null): Pair<Int, Intent?>

    /**
     *  Await till finishing process of `startActivityForResult`, and then return (resultCode, data).
     *
     *  If return is not working, check if [ActivityResult.activityResult] is placed appropriate.
     *
     *  @param startActivity block that should call [Fragment.startActivityForResult] with `requestCode` from parameter.
     *  @return [Pair] of (resultCode, data)
     */
    suspend fun Fragment.activityResultAwait(startActivity: (requestCode: Int) -> Unit): Pair<Int, Intent?>

    companion object {
        /**
         *  Create instance for delegate object.
         *
         *  ```
         *  class MyActivity : AppCompatActivity(), ActivityResult by ActivityResult.create()
         *  ```
         */
        fun create(): ActivityResult = ActivityResultImpl()

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