package com.vp.valleysmarthome.events

/**
 * Created by valerica.plesu on 03/12/2017.
 */
data class CheckPlantEvent(val isPlantAlive: Boolean)
data class TempHumResultEvent (val result: Pair<Int, Int>)
data class GenericErrorEvent(val message:String)
data class GetCandiesEvent(val isDispenserOpened: Boolean)