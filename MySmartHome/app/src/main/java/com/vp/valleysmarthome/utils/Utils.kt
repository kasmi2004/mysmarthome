package com.vp.valleysmarthome.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager


/**
 * Created by valerica.plesu on 03/12/2017.
 */
object Utils {

    /**
     * #### sensors
     */

    @JvmField
    val SOIL_WATERING_SENSOR = "soil"
    @JvmField
    val TEMP_HUM_SENSOR = "temp_hum"

    @JvmField
    val FCM_SENT = "fcm_sent"
    @JvmField
    val FCM_TOKEN = "fcm_token"

    private val MyPrefName = "NewsReaderPref"
    private var sharedpreferences: SharedPreferences? = null

    fun getSharedPref(context: Context): SharedPreferences? {
        if (sharedpreferences == null) {
            // parse Preference file
            sharedpreferences = context.getSharedPreferences(MyPrefName, Context.MODE_PRIVATE)
        }

        return sharedpreferences
    }

    fun addStringToSharePref(shP: SharedPreferences, key: String, value: String) {
        // get Editor object
        val editor = shP.edit()
        // put values in editor
        editor?.putString(key, value)

        // commit your putted values to the SharedPreferences object synchronously
        // returns true if success
        val result = editor?.commit()
    }

    fun addBoolean(shP: SharedPreferences, key: String, value: Boolean) {
        // get Editor object
        val editor = shP.edit()
        // put values in editor
        editor?.putBoolean(key, value)
        editor?.commit()
    }

    public fun getBoolean(shP: SharedPreferences, key: String): Boolean? {
        return shP.getBoolean(key, false)
    }

    public fun getString(shP: SharedPreferences, key: String): String? {
        return shP.getString(key, "")
    }

    @JvmStatic
    fun hasNetworkConnectivity(context: Context): Boolean {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * Starts the specified intent with exception handling.
     *
     * @param context
     * @param intent The intent to be launched
     * @return True if the intent is launched successfully, otherwise false
     */
    @JvmStatic
    fun startActivity(context: Context, intent: Intent): Boolean {
        try {
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            return false
        } catch (e: SecurityException) {
            return false
        }

    }
}