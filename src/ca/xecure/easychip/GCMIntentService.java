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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import static ca.xecure.easychip.CommonUtilities.*;


public class GCMIntentService extends GCMBaseIntentService {
    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(LOG_TAG, "Device registered: regId = " + registrationId);

        SharedPreferences settings = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        RegistrationUtilities.register_email(context, registrationId,
                settings.getString(EMAIL_ADDRESS_PREF, null));
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(LOG_TAG, "Device unregistered");

        SharedPreferences settings = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        RegistrationUtilities.unregister_email(context, registrationId,
                settings.getString(EMAIL_ADDRESS_PREF, null));
    }

    @Override
    protected void onMessage(Context context, final Intent intent) {
        Log.i(LOG_TAG, "Received message");

        String payee_id = intent.getStringExtra("payee_id");
        String annotation = intent.getStringExtra("annotation");
        String channel_id = intent.getStringExtra("channel_id");
        String callback_url = intent.getStringExtra("callback_url");
        int amount = Integer.parseInt(intent.getStringExtra("amount"));

        ask_for_payment(context, payee_id, annotation, channel_id, callback_url, amount);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(LOG_TAG, "Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(LOG_TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(LOG_TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }
}
