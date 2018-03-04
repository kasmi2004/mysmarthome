package com.vp.valleysmarthome.networking

import android.content.Context
import android.provider.Settings
import ccom.vp.valleysmarthome.networking.VolleyRequest
import com.android.volley.Request
import com.android.volley.toolbox.RequestFuture
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.concurrent.TimeUnit
import com.android.volley.ServerError
import org.jetbrains.anko.error
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException


/**
 * Created by valerica.plesu on 03/12/2017.
 */
object RequestsHandler: AnkoLogger {

    val BASE_URL: String
    val SEND_REG_KEY: String
    val DEFAULT_TIMEOUT: Long
    val CHECK_PLANT: String
    val GET_TEMP_HUM: String
    val GET_CANDIES: String

    init {
        BASE_URL = ""
        SEND_REG_KEY = "sendRegKey"
        DEFAULT_TIMEOUT = 25L
        CHECK_PLANT = "checkPlant"
        GET_TEMP_HUM = "getTempHum"
        GET_CANDIES = "getCandies"
    }

    /**
     *  Send fcm reg key and deviceId to server
     */
    fun sendRegKey(context: Context, fcmRegKey: String): Boolean {
        info { "sendRegKey=$fcmRegKey" }

        val requestUrl = BASE_URL + SEND_REG_KEY
        val future: RequestFuture<GenericResponse> = RequestFuture.newFuture()

        val params = SendFcmTokenPayload(getDeviceId(context), fcmRegKey)

        val request = VolleyRequest(Request.Method.POST, requestUrl, params, GenericResponse::class.java, null, future, future)

        VolleyRequestQueue.instance.addToRequestQueue(context, request)

        info { "[request url] " + requestUrl }

        try {
            val result = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            info { "result=$result" }

            if (result != null) {
                return (result.status == 200)
            }
            return false

        } catch (e: InterruptedException) {
            error ("error during request $e")
        } catch (e: ExecutionException) {
            error ("ExecutionException $e")
            // check error code
            if (e.cause is com.android.volley.ServerError) {
                val code = (e.cause as (ServerError)).networkResponse.statusCode
                info { "code=$code" }
            }
        } catch (e: TimeoutException) {
            error ("TimeoutException $e")
        }
        return false
    }

    fun isPlantSoilOk(context: Context): Boolean {
        info { "isPlantSoilOk" } //1 = no water 0 = happy

        val requestUrl = BASE_URL + CHECK_PLANT
        val future: RequestFuture<CheckPlantResponse> = RequestFuture.newFuture()

        val request = VolleyRequest(Request.Method.GET, requestUrl, null, CheckPlantResponse::class.java, null, future, future)

        VolleyRequestQueue.instance.addToRequestQueue(context, request)

        info { "[request url] " + requestUrl }

        try {
            val result = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            info { "result=$result" }

            if (result != null) {
                return (result.enoughWater == 0)
            }
            return false

        } catch (e: InterruptedException) {
            error ("error during request $e")
        } catch (e: ExecutionException) {
            error ("ExecutionException $e")
            // check error code
            if (e.cause is com.android.volley.ServerError) {
                val code = (e.cause as (ServerError)).networkResponse.statusCode
                info { "code=$code" }
            }
        } catch (e: TimeoutException) {
            error ("TimeoutException $e")
        }
        return false
    }

    fun getTempHum (context: Context): Pair<Int, Int>? {
        info { "getTempHum" }

        val requestUrl = BASE_URL + GET_TEMP_HUM
        val future: RequestFuture<GetTempHumResponse> = RequestFuture.newFuture()

        val request = VolleyRequest(Request.Method.GET, requestUrl, null, GetTempHumResponse::class.java, null, future, future)

        VolleyRequestQueue.instance.addToRequestQueue(context, request)

        info { "[request url] " + requestUrl }

        try {
            val result = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            info { "result=$result" }

            if (result != null) {
                return Pair(result.temp, result.humidity)
            }
            return null

        } catch (e: InterruptedException) {
            error ("error during request $e")
        } catch (e: ExecutionException) {
            error ("ExecutionException $e")
            // check error code
            if (e.cause is com.android.volley.ServerError) {
                val code = (e.cause as (ServerError)).networkResponse.statusCode
                info { "code=$code" }
            }
        } catch (e: TimeoutException) {
            error ("TimeoutException $e")
        }
        return null
    }

    fun getCandies(context: Context): Boolean {
        info { "getCandies" }

        val requestUrl = BASE_URL + GET_CANDIES

        val future: RequestFuture<GenericResponse> = RequestFuture.newFuture()

        val request = VolleyRequest(Request.Method.GET, requestUrl, null, GenericResponse::class.java, null, future, future)

        VolleyRequestQueue.instance.addToRequestQueue(context, request)

        info { "[request url] " + requestUrl }

        try {
            val result = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            info { "result=$result" }

            if (result != null) {
                return (result.status == 200 && result.message == "ok")
            }
            return false

        } catch (e: InterruptedException) {
            error ("error during request $e")
        } catch (e: ExecutionException) {
            error ("ExecutionException $e")
            // check error code
            if (e.cause is com.android.volley.ServerError) {
                val code = (e.cause as (ServerError)).networkResponse.statusCode
                info { "code=$code" }
            }
        } catch (e: TimeoutException) {
            error ("TimeoutException $e")
        }
        return false
    }

    private fun getDeviceId (context: Context): String {
        return Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID)
    }

}