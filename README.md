# android-coroutine-arrp
[ ![Download](https://api.bintray.com/packages/cchcc/maven/android-coroutine-arrp/images/download.svg) ](https://bintray.com/cchcc/maven/android-coroutine-arrp/_latestVersion)

Convenient version of [startActivityForResult](https://developer.android.com/reference/android/app/Activity.html#startActivityForResult(android.content.Intent,%20int))
, [requestPermission](https://developer.android.com/reference/android/app/Activity.html#requestPermissions(java.lang.String[],%20int)) on **coroutine**.  
You can get result as **return**. You don't need to care about _requestCode_.
```kotlin
CoroutineScope(Dispatchers.Main).launch {

  val permissionResult = requestPermissionAwait(android.Manifest.permission.READ_PHONE_STATE)
  if (permissionResult.isGranted) {
      // ...
  }
  
  
  val (resultCode, data) = startActivityForResultAwait(intent)
  if (resultCode == Activity.RESULT_OK) {
      // ...
  }
  
}
```

## setup
#### prerequisite
- kotlin version 1.3 or later
- androidx AppCompat
#### step
1. add to dependencies.
```groovy
implementation 'cchcc.android:arrp:0.5.0'
```
2. add some code below.
```kotlin
class BaseActivity : AppCompatActivity()
        , ActivityResult by ActivityResult.create()  // add
        , RequestPermission by RequestPermission.create()  // add
{
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResult(requestCode, resultCode, data)  // add
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionResult(requestCode, permissions, grantResults)  // add
    }
}
```
`BaseActivity` or `BaseFragment` would be a appropriate class to add.

## usage
This lightweight library provides some methods below.
```kotlin
suspend fun requestPermissionAwait(vararg permissions: String): RequestPermission.Result
suspend fun startActivityForResultAwait_(intent: Intent, options: Bundle? = null): Pair<Int, Intent?>
suspend fun activityResultAwait(startActivity: (requestCode: Int) -> Unit): Pair<Int, Intent?>  
```
Those are all **suspend** method. So should be called only from a coroutine or another suspend function.
```kotlin
val permissionResult = requestPermissionAwait(android.Manifest.permission.CAMERA
                                            , android.Manifest.permission.RECORD_AUDIO)
if (permissionResult is RequestPermission.Result.Denied) {
    if (android.Manifest.permission.CAMERA in permissionResult.denied) {
        // ...
    }
    return@launch
}

// from here, permission is granted
```
_requestPermissionAwait_ returns `RequestPermission.Result`.

```kotlin
sealed class Result {
    object Granted : Result()
    data class Denied(val granted: List<String>, val denied: List<String>) : Result()
    val isGranted: Boolean get() = this is Granted
}
```
_startActivityForResultAwait_ returns `Pair<Int, Intent?>` corresponding to _resultCode_ and _data_.
```kotlin
val (resultCode, data) = startActivityForResultAwait(intent)
if (resultCode != Activity.RESULT_OK)
    return@launch
val cursor = contentResolver.query(data!!.data!!, null, null, null, null)

// 
```
_activityResultAwait_ has a lambda parameter for using _requstCode_. You sholud call own way like _startActivityForResult_ in the block with _requestCode_.
```kotlin
val (resultCode, data) = activityResultAwait { requestCode ->
    startActivityForResult(intent, requestCode)
}
```
```kotlin
val (resultCode, data) = activityResultAwait { requestCode ->
    googleApiAvailability.getErrorDialog(
        activity
        , connectionStatusCode
        , requestCode
    ).show()
}
```
## advance
#### avoid duplicated requestCode
There are maybe some cases that use own _requestCode_. In that case, let `arrp` knows the _requestCode_.
```kotlin
const val REQUEST_CODE = 1000
ActivityResult.addExcludeRequestCode(REQUEST_CODE)
RequestPermission.addExcludeRequestCode(REQUEST_CODE)
```
So `arrp` will not use the added _requestCode_. It is enough to add only once for lifecyle of App.
