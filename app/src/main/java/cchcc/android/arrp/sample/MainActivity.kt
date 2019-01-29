package cchcc.android.arrp.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cchcc.android.arrp.ActivityResult
import cchcc.android.arrp.RequestPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity()
    , ActivityResult by ActivityResult.create()
    , RequestPermission by RequestPermission.create() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.Main).launch {
            val isGranted = requestPermissionAwait(android.Manifest.permission.READ_CONTACTS)
            if (!isGranted)
                return@launch

            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            val (resultCode, data) = startActivityForResultAwait(intent)

            if (resultCode == Activity.RESULT_OK) {

                val message = contentResolver.query(
                    data!!.data!!
                    , null
                    , null
                    , null
                    , null
                ).use { cursor ->
                    if (cursor != null) {
                        cursor.moveToFirst()

                        try {
                            val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                            val name = cursor.getString(nameIdx)
                            val phoneIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DATA)
                            val phone = cursor.getString(phoneIdx)
                            "name: $name\nphone: $phone"
                        } catch (e: Exception) {
                            e.toString()
                        }

                    } else {
                        "??"
                    }
                }

                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("OK") { _, _ -> }
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionResult(requestCode, grantResults)
    }
}
