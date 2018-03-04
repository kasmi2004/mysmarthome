package com.vp.valleysmarthome.networking

import com.google.gson.annotations.SerializedName

/**
 * Created by valerica.plesu on 03/12/2017.
 */


/** ############# responses ################ */
open class GenericResponse(@SerializedName("status") val status: Int,
                           @SerializedName("message") val message: String)


data class CheckPlantResponse(@SerializedName("enough_water") val enoughWater: Int,
                              @Transient val s: Int,
                              @Transient val m: String): GenericResponse(s, m)

data class GetTempHumResponse(@SerializedName("temperature") val temp: Int,
                              @SerializedName("humidity") val humidity: Int,
                              @Transient val s: Int,
                              @Transient val m: String): GenericResponse(s, m)


/** ########### payloads ################## */
data class SendFcmTokenPayload(@SerializedName("deviceRegKey") val deviceRegKey: String,
                                @SerializedName("fcmRegKey") val fcmRegKey: String)
