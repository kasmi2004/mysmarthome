package com.vp.valleysmarthome.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.vp.valleysmarthome.R
import com.vp.valleysmarthome.SensorsDialog
import com.vp.valleysmarthome.utils.Utils
import com.vp.valleysmarthome.events.CheckPlantEvent
import com.vp.valleysmarthome.events.GenericErrorEvent
import com.vp.valleysmarthome.events.TempHumResultEvent
import com.vp.valleysmarthome.services.CheckSensorsService
import com.vp.valleysmarthome.services.SendFcmRegKeyService
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), AnkoLogger {

    lateinit var sensorsDialog: SensorsDialog
    private var snackbar: Snackbar? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPlantLayout.setOnClickListener {
            if (Utils.hasNetworkConnectivity(this)) {
                showSnackbar(getString(R.string.checking_sensor))
                CheckSensorsService.startActionCheckPlant(this)
            } else {
                showSnackbar(getString(R.string.enable_network), getString(R.string.goto_settings))
            }
        }

        checkTempHumLayout.setOnClickListener {
            if (Utils.hasNetworkConnectivity(this)) {
                showSnackbar(getString(R.string.checking_sensor))
                CheckSensorsService.startActionGetTempHum(this)
            } else {
                showSnackbar(getString(R.string.enable_network), getString(R.string.goto_settings))
            }
        }

        motionLayout.setOnClickListener {
            Toast.makeText(this, "Not yet implemented!", Toast.LENGTH_SHORT).show()
        }

        candyLayout.setOnClickListener {
            GetCandiesActivity.newInstance(this)
        }
    }

    override fun onResume() {
        super.onResume()

        info { "onResume" }
        EventBus.getDefault().register(this)

        //resentFcmTokenIfNeeded()

    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    fun start(c: Context) {
        c.startActivity(Intent(c, MainActivity::class.java))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CheckPlantEvent) {

        hideShackbar()


        val bundle = Bundle()
        bundle.putString(SensorsDialog.EXTRA_SENSOR, Utils.SOIL_WATERING_SENSOR)
        bundle.putBoolean(SensorsDialog.EXTRA_IS_PLANT_ALIVE, event.isPlantAlive)

        sensorsDialog = SensorsDialog()
        sensorsDialog.arguments = bundle
        sensorsDialog.show(supportFragmentManager, "checkPlant")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TempHumResultEvent) {

        hideShackbar()

        val bundle = Bundle()
        bundle.putString(SensorsDialog.EXTRA_SENSOR, Utils.TEMP_HUM_SENSOR)
        bundle.putInt(SensorsDialog.EXTRA_TEMP, event.result.first)
        bundle.putInt(SensorsDialog.EXTRA_HUM, event.result.second)

        sensorsDialog = SensorsDialog()
        sensorsDialog.arguments = bundle
        sensorsDialog.show(supportFragmentManager, "getTempHum")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GenericErrorEvent) {
        showSnackbar(getString(R.string.checking_sensor_error))
    }

    private fun resentFcmTokenIfNeeded () {
        val sharedPref = Utils.getSharedPref(this)
        sharedPref?.let {

            val alreadySent = Utils.getBoolean(it, Utils.FCM_SENT)
            info { "alreadySent=$alreadySent" }
            val token = Utils.getString(it, Utils.FCM_TOKEN)

            if (alreadySent != null && !alreadySent && token != null) {
                SendFcmRegKeyService.startSendFcmService(this, token)
            }
        }
    }

    private fun showSnackbar(message: String, actionMessage: String? = null) {
        snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)

        snackbar?.let {
            if (actionMessage != null) {
                it.setAction(actionMessage) { Utils.startActivity(this@MainActivity, Intent(Settings.ACTION_SETTINGS)) }
            }
            it.show()
        }
    }

    private fun hideShackbar() {
        snackbar?.dismiss()
    }
}
