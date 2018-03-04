package com.vp.valleysmarthome.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import com.vp.valleysmarthome.utils.Utils
import com.vp.valleysmarthome.networking.RequestsHandler
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 ** helper methods.
 */
class SendFcmRegKeyService : IntentService("SendFcmRegKeyService"), AnkoLogger {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val token = intent.extras.getString(EXTRA_FCM_TOKEN)
            handleSendToken(token)
        }
    }

    private fun handleSendToken(token: String) {
        val sharedPref = Utils.getSharedPref(this)
        sharedPref?.let {

            val alreadySent = Utils.getBoolean(it, Utils.FCM_SENT)

            alreadySent?.let {
                info { "alreadySent=$alreadySent" }
                if (!alreadySent) {
                    //send it now
                    val result = RequestsHandler.sendRegKey(this, token)

                    info { "result=$result" }
                    if (result) {
                        Utils.addBoolean(sharedPref, Utils.FCM_SENT, true)
                    }
                } else {
                    error { "something went wrong, we should retry sending fcm token" }
                }
            }
        }
    }

    companion object {
        val EXTRA_FCM_TOKEN = "extra_token"

        fun startSendFcmService(context: Context, fcmRegkey: String) {
            val intent = Intent(context, SendFcmRegKeyService::class.java)
            intent.putExtra(EXTRA_FCM_TOKEN, fcmRegkey)
            context.startService(intent)
        }
    }
}
