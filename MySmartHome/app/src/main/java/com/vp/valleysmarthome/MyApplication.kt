package com.vp.valleysmarthome

import android.app.Application
import com.vp.valleysmarthome.events.GetCandiesEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.longToast

/**
 * Created by valerica.plesu on 03/12/2017.
 */
class MyApplication: Application(), AnkoLogger {

    companion object {
        lateinit var INSTANCE: MyApplication
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = MyApplication()

        EventBus.getDefault().register(this) // no need to unregister since this is th application object
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetCandiesEvent) {

        info { "event GetCandies received - is dispenser opened: ${event.isDispenserOpened}" }

        val mess = if (event.isDispenserOpened) "Congrats! Enjoy!" else "Try again!"
        longToast(mess)
    }
}