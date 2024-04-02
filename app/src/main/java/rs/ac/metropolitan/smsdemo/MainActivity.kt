package rs.ac.metropolitan.smsdemo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import rs.ac.metropolitan.smsdemo.ui.theme.SmsDemoTheme

class MainActivity : ComponentActivity() {
    val textValue = mutableStateOf("")

    var SENT = "SMS_SENT"
    var DELIVERED = "SMS_DELIVERED"
    var sentPI: PendingIntent? = null
    var deliveredPI:PendingIntent? = null
    var smsSentReceiver: BroadcastReceiver? = null
    var smsDeliveredReceiver:BroadcastReceiver? = null
    var intentFilter: IntentFilter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sentPI = PendingIntent.getBroadcast(
            this, 0,
            Intent(SENT), PendingIntent.FLAG_IMMUTABLE
        )

        deliveredPI = PendingIntent.getBroadcast(
            this, 0,
            Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE
        )
        //-filter prijema SMS poruka-
        intentFilter = IntentFilter()
        intentFilter!!.addAction("SMS_RECEIVED_ACTION")

        //---registrovanje primaoca---
        registerReceiver(intentReceiver, intentFilter)

        setContent {
            SmsDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Layout("")
                }
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_SEND_SMS = 1
    }


    private val intentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //-prikazuje primljeni SMS u TextView pogledu -
            val sms = intent.extras!!.getString("sms")
            textValue.value = sms?: ""
        }
    }


    override fun onResume() {
        super.onResume()

        //---registrovanje primaoca---
        registerReceiver(intentReceiver, intentFilter)

        //---kreira BroadcastReceiver kada je SMS poslat---
        smsSentReceiver = object: BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                when (getResultCode()) {
                    RESULT_OK -> Toast.makeText(
                        baseContext, "SMS prosleđen",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Toast.makeText(
                        baseContext, "Generička greška",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_NO_SERVICE -> Toast.makeText(
                        baseContext, "Nema usluge",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_NULL_PDU -> Toast.makeText(
                        baseContext, "Null PDU",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_RADIO_OFF -> Toast.makeText(
                        baseContext, "Radio isključen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        //---kreira BroadcastReceiver kada SMS dostavljen---
        smsDeliveredReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (resultCode) {
                    RESULT_OK -> Toast.makeText(
                        baseContext, "SMS dostavljen",
                        Toast.LENGTH_SHORT
                    ).show()
                    RESULT_CANCELED -> Toast.makeText(
                        baseContext, "SMS nije dostavljen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        //---registruje dva BroadcastReceiver - a---
        registerReceiver(smsDeliveredReceiver, IntentFilter(DELIVERED))
        registerReceiver(smsSentReceiver, IntentFilter(SENT))
    }


    override fun onPause() {
        super.onPause()
        //---odjavljuje primaoca---
        unregisterReceiver(intentReceiver);

        //---odjavljuje dva BroadcastReceiver-a---
        unregisterReceiver(smsSentReceiver)
        unregisterReceiver(smsDeliveredReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        //---odjavljivanje primaoca---
        unregisterReceiver(intentReceiver)
    }


    //Šalje poruku drugom uređaju”-
    private fun sendSMS(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), MY_PERMISSIONS_REQUEST_SEND_SMS)
        } else {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI)
            Toast.makeText(applicationContext, "SMS sent.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(applicationContext, "Permission granted to send SMS.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "SMS sending permission denied.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    fun onSMSIntentClick(v: View?) {
        val i = Intent(Intent.ACTION_VIEW)
        i.putExtra("address", "5556; 5558; 5560")
        i.putExtra("sms_body", "Pozdravni SMS - primer!")
        i.type = "vnd.android-dir/mms-sms"
        startActivity(i)
    }

    @Composable
    fun Layout(name: String) {
        Column() {
            Button(onClick = {
                sendSMS("5556", "Pozdravni SMS - primer!")
            } ) {
                Text(text = "Pošalji SMS")
            }
            Text(text = textValue.value)
        }
    }

    @Preview(showBackground = true, apiLevel = 29)
    @Composable
    fun DefaultPreview() {
        SmsDemoTheme {
            Layout("Android")
        }
    }
}



