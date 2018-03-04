package com.vp.valleysmarthome.services

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.vp.valleysmarthome.utils.Utils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class MyFirebaseInstanceIDService : FirebaseInstanceIdService(), AnkoLogger {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        refreshedToken?.let {
            info { "@@ Refreshed token: $refreshedToken@@" }

            val sharedPref = Utils.getSharedPref(this)
            sharedPref?.let {

                Utils.addStringToSharePref(it, Utils.FCM_TOKEN, refreshedToken)

                val alreadySent = Utils.getBoolean(it, Utils.FCM_SENT)

                alreadySent?.let {
                    info { "alreadySent=$alreadySent" }
                    if (!alreadySent) {
                        //send it now
                        SendFcmRegKeyService.startSendFcmService(this, refreshedToken)

                    }
                }
            }
        }
    }
}
