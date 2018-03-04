package com.vp.valleysmarthome.networking;

/**
 * Created by valerica.plesu on 03/12/2017.
 */

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Volley request that uses Gson for parsing. Reference:
 * https://developer.android.com/training/volley/request-custom.html
 *
 * @param <S> The relevant Gson model class that represents the request body
 * @param <T> Relevant listener object to receive normal backend response
 * @author yushu
 */
public class GsonRequest<S, T> extends Request<T> {
    protected static final String PROTOCOL_CHARSET = "utf-8";
    /**
     * Charset for request.
     */
    private static final String TAG = GsonRequest.class.getName();
    protected final Gson mGson = new Gson();
    protected final Class<T> mResponseClass;
    private final Map<String, String> mHeaders;
    private final Listener<T> mResponseListener;
    private String mRequestUrl;
    private S mRequestBody;

    /**
     * Make a POST request and return a parsed object from JSON.
     *
     * @param url              URL of the request to make
     * @param requestBody      Relevant GSON model request body
     * @param responseCls      Relevant class object, for Gson's reflection
     * @param headers          Map of request headers
     * @param responseListener Relevant listener object to receive normal backend response
     * @param errorListener    Listener to receive error response
     */
    public GsonRequest(String url, S requestBody, Class<T> responseCls, Map<String, String> headers,
                       Listener<T> responseListener, ErrorListener errorListener) {
        this(Method.POST, url, requestBody, responseCls, headers, responseListener, errorListener);
    }

    /**
     * Make a request with the specified method and return a parsed object from JSON
     *
     * @param method           method type
     * @param url              URL of the request to make
     * @param requestBody      Relevant GSON model request body
     * @param responseCls      Relevant class object, for Gson's reflection
     * @param headers          Map of request headers
     * @param responseListener Relevant listener object to receive normal backend response
     * @param errorListener    Listener to receive error response
     */
    public GsonRequest(int method, String url, S requestBody, Class<T> responseCls, Map<String, String> headers,
                       Listener<T> responseListener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mRequestUrl = url;
        mRequestBody = requestBody;
        mResponseClass = responseCls;
        mHeaders = headers;
        mResponseListener = responseListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        mResponseListener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.i(TAG, "[response] " + json);

            return Response.success(mGson.fromJson(json, mResponseClass), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        // Print error message received (need to convert from byte array to String) for debugging purposes
        Log.e(TAG, String.format("[errorResponse] statusCode:%d cause:%s duration:%d\nmessage:%s",
                (volleyError.networkResponse != null ? volleyError.networkResponse.statusCode : 0),
                volleyError.getClass().getName(),
                volleyError.getNetworkTimeMs(),
                Utils.getMessage(volleyError)));
        return volleyError;
    }

    @Override
    public byte[] getBody() {
        try {
            if (mRequestBody != null) {
                String requestBody = mGson.toJson(mRequestBody);
                Log.i(TAG, "[requestUrl] " + mRequestUrl + " [requestBody] " + requestBody);
                return requestBody.getBytes(PROTOCOL_CHARSET);
            }
            return null;
        } catch (UnsupportedEncodingException uee) {
            Log.wtf(TAG, "Unsupported Encoding while trying to get the bytes of " + mRequestBody.toString() + " using"
                    + PROTOCOL_CHARSET);
            return null;
        }
    }
}