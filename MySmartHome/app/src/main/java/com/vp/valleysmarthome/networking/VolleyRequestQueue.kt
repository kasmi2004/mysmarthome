package  com.vp.valleysmarthome.networking

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * Created by valerica.plesu on 26/10/2017.
 */
class VolleyRequestQueue private constructor(): AnkoLogger{

    private var requestQueue: RequestQueue? = null

    init {
        info { "volley request queue singleton" }
    }

    private object Holder { val INSTANCE = VolleyRequestQueue() }

    companion object {
        val instance: VolleyRequestQueue by lazy { Holder.INSTANCE }
    }

    private fun getQueue (context: Context): RequestQueue? {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context)
            }
        return requestQueue
    }

    fun <T> addToRequestQueue(context: Context, req: Request<T>) {
        getQueue(context)?.add(req)
    }
}