package cchcc.android.arrp.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.telephony.TelephonyManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cchcc.android.arrp.ActivityResult
import cchcc.android.arrp.RequestPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity()
        , ActivityResult by ActivityResult.create()
        , RequestPermission by RequestPermission.create() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // sample for requestPermissionAwait
        bt_phone_number.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val permissionResult = requestPermissionAwait(android.Manifest.permission.READ_PHONE_STATE)
                if (!permissionResult.isGranted)
                    return@launch

                val telephonyManager = (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                @SuppressLint("MissingPermission", "HardwareIds")
                val phoneNumber = telephonyManager.line1Number

                AlertDialog.Builder(this@MainActivity)
                        .setMessage(phoneNumber)
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
            }
        }

        // sample for startActivityForResultAwait
        bt_file_name.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                }
                val (resultCode, data) = startActivityForResultAwait(intent)
                if (resultCode != Activity.RESULT_OK)
                    return@launch

                val fileName = contentResolver.query(
                        data!!.data!!
                        , null
                        , null
                        , null
                        , null
                ).use { cursor ->
                    cursor!!.moveToFirst()
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.getString(nameIdx)
                }

                AlertDialog.Builder(this@MainActivity)
                        .setMessage(fileName)
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
        permissionResult(requestCode, permissions, grantResults)
    }
}
