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

import static ca.xecure.easychip.CommonUtilities.*;

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public final class RegistrationUtilities {
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;


    static boolean register_email(final Context context, final String reg_id, final String email) {
        Log.i(LOG_TAG, "registering device (regId = " + reg_id + ")");

        Map<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("reg_id", reg_id);

        long backoff = BACKOFF_MILLI_SECONDS;

        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(LOG_TAG, "Attempt #" + i + " to register");
            try {
                http_post(REGISTER_URL, params);
                GCMRegistrar.setRegisteredOnServer(context, true);

                return true;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to register on attempt " + i, e);

                if (i == MAX_ATTEMPTS) {
                    break;
                }

                try {
                    Log.d(LOG_TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    Log.d(LOG_TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }

                backoff *= 2;
            }
        }

        return false;
    }

    static void unregister_email(final Context context, final String reg_id, final String email) {
        Log.i(LOG_TAG, "unregistering device (regId = " + reg_id + ")");

        Map<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("reg_id", reg_id);
        try {
            GCMRegistrar.setRegisteredOnServer(context, false);
            http_post(UNREGISTER_URL, params);
        } catch (IOException e) {
            // do nothing
        }
    }
}
