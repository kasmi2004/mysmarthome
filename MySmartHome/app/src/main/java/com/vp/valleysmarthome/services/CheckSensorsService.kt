package com.vp.valleysmarthome.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import com.vp.valleysmarthome.events.CheckPlantEvent
import com.vp.valleysmarthome.events.GenericErrorEvent
import com.vp.valleysmarthome.events.GetCandiesEvent
import com.vp.valleysmarthome.events.TempHumResultEvent
import com.vp.valleysmarthome.networking.RequestsHandler
import org.greenrobot.eventbus.EventBus

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 ** helper methods.
 */
class CheckSensorsService : IntentService("CheckSensorsService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_CHECK_PLANT == action) {
                handleActionCheckPlant()
            } else if (ACTION_GET_TEMP_HUM == action) {
                handleActionGetTempHum()
            } else if (ACTION_GET_CANDIES == action) {
                handleActionGetCandies()
            }
        }
    }

    /**
     * Handle action check plant in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionCheckPlant() {
        val isPlantAlive = RequestsHandler.isPlantSoilOk(this)

        EventBus.getDefault().post(CheckPlantEvent(isPlantAlive))
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionGetTempHum() {
        val result = RequestsHandler.getTempHum(this)
        if (result != null) {
            EventBus.getDefault().post(TempHumResultEvent(result))
        } else {
            EventBus.getDefault().post(GenericErrorEvent(""))
        }
    }

    /**
     * Handle action get candies in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionGetCandies() {
        val dispenserOpened = RequestsHandler.getCandies(this)

        EventBus.getDefault().post(GetCandiesEvent(dispenserOpened))
    }

    companion object {
        private val ACTION_CHECK_PLANT = "check_plant"
        private val ACTION_GET_TEMP_HUM = "get_temp_hum"
        private var ACTION_GET_CANDIES = "get_candies"

        /**
         * Starts this service to perform action checkPlant with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionCheckPlant(context: Context) {
            val intent = Intent(context, CheckSensorsService::class.java)
            intent.action = ACTION_CHECK_PLANT
            context.startService(intent)
        }

        /**
         * Starts this service to perform action get temp hum with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionGetTempHum(context: Context) {
            val intent = Intent(context, CheckSensorsService::class.java)
            intent.action = ACTION_GET_TEMP_HUM
            context.startService(intent)
        }

        /**
         * Starts this service to perform action get candies (to action the candy dispenser). If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionGetCandies(context: Context) {
            val intent = Intent(context, CheckSensorsService::class.java)
            intent.action = ACTION_GET_CANDIES
            context.startService(intent)
        }
    }
}
