package ccom.vp.valleysmarthome.networking

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.JsonSyntaxException
import com.vp.valleysmarthome.networking.GsonRequest
import com.vp.valleysmarthome.networking.Utils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * Created by valerica.plesu on 26/10/2017.
 */
/**
 * Class that extends {@link GsonRequest} to handle the network error case. The OE backend sends the errors as
 * network errors with status codes larger than 900 and they need to be handled separately
 *
 * @param <S> The relevant Gson model class that represents the request body
 * @param <T> Relevant listener object to receive normal backend response
 * @author yushu
 */
class VolleyRequest<S, T>(method: Int, url: String?, requestBody: S?, responseCls: Class<T>?, headers: MutableMap<String, String>?,
                          responseListener: Response.Listener<T>?, errorListener: Response.ErrorListener?) :

        GsonRequest<S, T>(method, url, requestBody, responseCls, headers, responseListener, errorListener), AnkoLogger {

    companion object {
        val DEFAULT_TIMEOUT_MS = 4000
    }

    /**
     * Content type for request.
     */
    private val PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", GsonRequest.PROTOCOL_CHARSET)

    /**
     * Construct an volley request with the specified parameters
     *
     * @param method           The http method
     * @param url              URL of the request to make
     * @param requestBody      Relevant GSON model request body
     * @param responseCls      Relevant class object, for Gson's reflection
     * @param responseListener Relevant listener object to receive normal backend response
     * @param errorListener    Listener to receive error response
     */
    constructor(method: Int, url: String, requestBody: S?, responseCls: Class<T>, responseListener: Response.Listener<T>,
                      errorListener: Response.ErrorListener) : this (method, url, requestBody, responseCls, responseListener, errorListener, DEFAULT_TIMEOUT_MS)



    /**
     * Construct an volley request with the specified parameters
     *
     * @param url              URL of the request to make
     * @param requestBody      Relevant GSON model request body
     * @param responseCls      Relevant class object, for Gson's reflection
     * @param responseListener Relevant listener object to receive normal backend response
     * @param errorListener    Listener to receive error response
     * @param initialTimeoutMs  The initial timeout for the policy
     */
    constructor(method: Int, url: String, requestBody: S?, responseCls: Class<T>, responseListener: Response.Listener<T>,
                      errorListener: Response.ErrorListener, initialTimeoutMs: Int) : this (method, url, requestBody, responseCls, null, responseListener, errorListener) {
        this.retryPolicy = DefaultRetryPolicy(initialTimeoutMs, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    }

    /**
     * Construct a volley request with prams
     * @param url
     * @param responseCls
     * @param responseListener
     * @param errorListener
     */
    constructor(url: String, responseCls: Class<T>, responseListener: Response.Listener<T>,
                errorListener: Response.ErrorListener) : this (Request.Method.GET, url, null, responseCls, responseListener, errorListener)

    /**
     *     public APIRequest(String url, Class<T> responseClass, Response.Listener<T> listener, Response.ErrorListener errorListener) {
    super(Method.GET, url, null, listener, errorListener);
    this.responseClass = responseClass;
    }
     */

    override fun getBodyContentType(): String {
        return PROTOCOL_CONTENT_TYPE
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {

        try {

            // Status code is not kept in the response body (response.data) but rather response.statusCode
            // so we need to manually include it into the json result here
            val oriJsonString = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
            val jsonObject = JSONObject(oriJsonString)
            if (!jsonObject.has(Utils.STATUS_CODE)) {
                jsonObject.put(Utils.STATUS_CODE, response.statusCode)
            }

            val newJsonString = jsonObject.toString()
            info { "[response]=$newJsonString" }

            return Response.success<T>(mGson.fromJson(newJsonString, mResponseClass), HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            return Response.error<T>(ParseError(e))
        } catch (e: JsonSyntaxException) {
            return Response.error<T>(ParseError(e))
        } catch (e: JSONException) {
            return Response.error<T>(ParseError(e))
        }

    }
}