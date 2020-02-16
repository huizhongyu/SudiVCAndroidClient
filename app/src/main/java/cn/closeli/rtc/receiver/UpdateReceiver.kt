package cn.closeli.rtc.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.closeli.rtc.GroupActivity
import cn.closeli.rtc.LoginActivity

class UpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Restart app here
        val i = Intent(context, GroupActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }
}
