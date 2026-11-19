package com.cccuan.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cccuan.app.data.CcDatabaseHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = CcDatabaseHelper(context.applicationContext)
            val cards = db.getAllCards()
            AlarmScheduler.rescheduleAll(context.applicationContext, cards)
        }
    }
}
