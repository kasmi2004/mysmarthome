package com.vp.valleysmarthome.networking;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

/**
 * Created by valerica.plesu on 03/12/2017.
 */

public class Utils {

    public static final String STATUS_CODE = "statusCode";

    /**
     * Retrieves the error message from the specified volley error
     *
     * @param error The volley error received from backend
     * @return The error message from the volley error. Returns null if no error message provided.
     */
    public static String getMessage(VolleyError error) {
        String errorMessage;

        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                errorMessage = new String(error.networkResponse.data,
                        HttpHeaderParser.parseCharset(error.networkResponse.headers));
            } catch (UnsupportedEncodingException e) {
                errorMessage = new String(error.networkResponse.data);
            }
        } else {
            errorMessage = error.getMessage();
        }
        return errorMessage;
    }

    public static int getHTTPErrorCode(VolleyError error) {
        return error.networkResponse != null
                ? error.networkResponse.statusCode
                : 999; //unknown error
    }
}
