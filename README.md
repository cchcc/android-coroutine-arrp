# android-coroutine-arrp
Convenient version of [startActivityForResult](https://developer.android.com/reference/android/app/Activity.html#startActivityForResult(android.content.Intent,%20int))
, [requestPermission](https://developer.android.com/reference/android/app/Activity.html#requestPermissions(java.lang.String[],%20int)) on **coroutine**.  
You can get result as **return**.
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
```
// ....
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
Adding code to `BaseActivity` or `BaseFragment` would be efficient.

## use case
_requestPermissionAwait_ is a suspend method.
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
_requestPermissionAwait_ returns [RequestPermission.Result](https://github.com/cchcc/android-coroutine-arrp/blob/b5621d6df73e617e9cf0f0cc160f092f1a5593cd/arrpLib/src/main/java/cchcc/android/arrp/RequestPermission.kt#L13)

```
sealed class Result {
    object Granted : Result()
    data class Denied(val granted: List<String>, val denied: List<String>) : Result()
    val isGranted: Boolean get() = this is Granted
}
```
