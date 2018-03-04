package com.vp.valleysmarthome

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.vp.valleysmarthome.utils.Utils
import kotlinx.android.synthetic.main.dialog_check_plant.*
import kotlinx.android.synthetic.main.dialog_check_temp_hum.*

/**
 * Created by valerica.plesu on 03/12/2017.
 */
class SensorsDialog: AppCompatDialogFragment() {

    companion object {
        val EXTRA_SENSOR = "extra_sensor"
        val EXTRA_IS_PLANT_ALIVE = "extra_plant_alive"
        val EXTRA_TEMP = "extra_temp"
        val EXTRA_HUM = "extra_hum"
    }
    lateinit var checkedSensor: String
    var isPlantAlive: Boolean? = false
    var temp: Int? = -1
    var hum: Int? = -1
    lateinit var awesomeIconsFont: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        awesomeIconsFont = Typeface.createFromAsset(activity.assets, "fonts/fontawesome-webfont.ttf")
        checkedSensor = arguments.getString(EXTRA_SENSOR)

        if (checkedSensor == Utils.SOIL_WATERING_SENSOR) {
            isPlantAlive = arguments.getBoolean(EXTRA_IS_PLANT_ALIVE)
        } else if (checkedSensor == Utils.TEMP_HUM_SENSOR) {
            temp = arguments.getInt(EXTRA_TEMP)
            hum = arguments.getInt(EXTRA_HUM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var layoutRes = 0

        if (checkedSensor == Utils.SOIL_WATERING_SENSOR) {
            layoutRes = R.layout.dialog_check_plant
        } else if (checkedSensor == Utils.TEMP_HUM_SENSOR) {
            layoutRes = R.layout.dialog_check_temp_hum
        }

        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkedSensor == Utils.SOIL_WATERING_SENSOR) {

            isPlantAlive?.let {
                safeIcon.typeface = awesomeIconsFont

                checkPlantTitle.text = getText(R.string.check_plant_title)
                if (it) {
                    safeIcon.text = "\uf164"
                    checkPlantDesc.text = getText(R.string.plant_happy)
                } else {
                    safeIcon.text = "\uf119"
                    checkPlantDesc.text = getText(R.string.plant_death)
                }
            }
        } else if (checkedSensor == Utils.TEMP_HUM_SENSOR) {
            getTempHumTitle.text = getText(R.string.check_temp_hum_title)
            tempDesc.text = Html.fromHtml(getString(R.string.temp_desc, temp))
            humDesc.text = Html.fromHtml(getString(R.string.hum_desc, hum))
        }
    }
}