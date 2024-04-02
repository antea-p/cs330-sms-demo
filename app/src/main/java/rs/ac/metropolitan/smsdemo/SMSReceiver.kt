package rs.ac.metropolitan.smsdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //---preuzimanje prosleđene SMS poruke---
        val bundle = intent.extras
        var msgs: Array<SmsMessage?>? = null
        var str: String? = "SMS iz "
        if (bundle != null) {
            //---učitavanje primljene SMS poruke---
            val pdus = bundle["pdus"] as Array<Any>?
            msgs = arrayOfNulls(pdus!!.size)
            for (i in msgs.indices) {
                msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                if (i == 0) {
                    //---preuzimanje podataka o pošiljaocu---
                    str += msgs[i]?.getOriginatingAddress()
                    str += ": "
                }
                //---preuzimanje tela poruke---
                str += msgs[i]?.getMessageBody().toString()
            }

            //---prikazivanje nove SMS poruke---
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
            Log.d("SMSReceiver", str!!)

            /* //---zaustavljanje slanja/primanja---
            this.abortBroadcast();*/

            //---pokretanje SMSActivity---
            val mainActivityIntent = Intent(context, MainActivity::class.java)
            mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(mainActivityIntent)

            //---Slanje namere BroadcastIntent za ažuriranje SMS iz aktivnosti---
            val broadcastIntent = Intent()
            broadcastIntent.action = "SMS_RECEIVED_ACTION"
            broadcastIntent.putExtra("sms", str)
            context.sendBroadcast(broadcastIntent)
        }
    }
}
