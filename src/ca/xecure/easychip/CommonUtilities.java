/*
 * Copyright 2012 Google Inc.
 * Copyright 2012 Bo Zhu <zhu@xecurity.ca>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.xecure.easychip;

import android.content.Intent;
import android.content.Context;
import android.util.Log;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


public final class CommonUtilities {
    static final String BASE_URL = "https://xecure-easychip.appspot.com/api";
    static final String REGISTER_URL = BASE_URL + "/register";
    static final String UNREGISTER_URL = BASE_URL + "/unregister";
    static final String SENDER_ID = "354002709222";

    static final String LOG_TAG = "EasyChip";

    static final String PREF_FILE_NAME = "preference_file";
    static final String EMAIL_ADDRESS_PREF = "email_address_pref";

    static final String CONFIRM_PAYMENT_ACTION = "ca.xecure.easychip.CONFIRM_PAYMENT";
    static final String UPDATE_INFO_ACTION = "ca.xecure.easychip.UPDATE_INFO";


    static final int CHOOSE_EMAIL_ADDRESS = 4132;


    public static void ask_for_payment(
            Context context,
            String payee_id,
            String annotation,
            String channel_id,
            String callback_url,
            int amount) {
        Intent intent = new Intent(CONFIRM_PAYMENT_ACTION);
        intent.putExtra("payee_id", payee_id);
        intent.putExtra("annotation", annotation);
        intent.putExtra("channel_id", channel_id);
        intent.putExtra("callback_url", callback_url);
        intent.putExtra("amount", amount);
        context.sendBroadcast(intent);
        Log.v(LOG_TAG, "send intent to main activity to confirm payment");
    }


    public static void http_post(String endpoint, Map<String, String> params) throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        String body = JSONValue.toJSONString(params);
        Log.v(LOG_TAG, "Posting '" + body + "' to " + url);

        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
