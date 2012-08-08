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


import android.content.*;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import static android.accounts.AccountManager.newChooseAccountIntent;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.Toast;
import android.view.Window;
import android.view.View;
//import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
import java.io.IOException;
import java.util.HashMap;
import java.lang.NoSuchMethodError;
import java.lang.RuntimeException;

import android.widget.EditText;

import com.google.android.gcm.GCMRegistrar;

import static ca.xecure.easychip.CommonUtilities.*;
import static ca.xecure.easychip.MintChipUtilities.createValueMessage;
import static ca.xecure.easychip.RegistrationUtilities.register_email;


public class MainActivity extends FragmentActivity {
    SharedPreferences settings;

    String payee_id;
    String annotation;
    String channel_id;
    String callback_url;
    int amount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
        } catch (RuntimeException ex) {
            Toast.makeText(this, "Cannot use Google Cloud Message!", Toast.LENGTH_LONG).show();

            moveTaskToBack(true);
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        //Set the pager with an adapter
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new FlipAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(1);

        //Bind the title indicator to the adapter
        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.titles);
        indicator.setViewPager(pager);

        final float density = getResources().getDisplayMetrics().density;
        indicator.setBackgroundColor(0xFF7AA7D6);
//        indicator.setBackgroundColor(0xFFDD7200);
        indicator.setFooterColor(0xFFECF1F2);
        indicator.setFooterLineHeight(0);
        indicator.setFooterIndicatorHeight(7 * density);
        indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
        indicator.setTextColor(0xAAFFFFFF);
        indicator.setSelectedColor(0xFFFFFFFF);
        indicator.setSelectedBold(true);

        registerReceiver(confirm_payment_handler, new IntentFilter(CONFIRM_PAYMENT_ACTION));

        settings = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        String email_address = settings.getString(EMAIL_ADDRESS_PREF, null);
        if (email_address != null) {
            register_cloud_message();
        } else {
            choose_email_address();
        }
    }

    public void choose_email_address() {
        Intent intent;
        try {
            intent = newChooseAccountIntent(null, null, new String[]{"com.google"},
                    false, null, null, null, null);
            startActivityForResult(intent, CHOOSE_EMAIL_ADDRESS);
        } catch (NoSuchMethodError ex) {
            manually_enter_email();
        }
    }

    public void change_email_address(View view) {
        Intent intent;
        try {
            intent = newChooseAccountIntent(null, null, new String[]{"com.google"},
                    false, null, null, null, null);
            startActivityForResult(intent, CHOOSE_EMAIL_ADDRESS);
        } catch (NoSuchMethodError ex) {
            manually_enter_email();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_EMAIL_ADDRESS) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(EMAIL_ADDRESS_PREF, data.getStringExtra(KEY_ACCOUNT_NAME));
                editor.commit();

                register_cloud_message();
            }

            Intent intent = new Intent(UPDATE_INFO_ACTION);
            sendBroadcast(intent);
        }
    }

    protected void register_cloud_message() {
        final String reg_id = GCMRegistrar.getRegistrationId(this);
        if (reg_id.equals("")) {
            Log.v(LOG_TAG, "To register");
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            Log.v(LOG_TAG, "Already registered. ID: " + reg_id);
            register_email(this, reg_id, settings.getString(EMAIL_ADDRESS_PREF, null));
        }
    }

    @Override
    protected void onDestroy() {
        GCMRegistrar.onDestroy(this);
        Log.v(LOG_TAG, "main activity onDestroy");
        unregisterReceiver(confirm_payment_handler);
        super.onDestroy();
    }


    private final BroadcastReceiver confirm_payment_handler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "need to confirm payment");

            payee_id = intent.getStringExtra("payee_id");
            annotation = intent.getStringExtra("annotation");
            channel_id = intent.getStringExtra("channel_id");
            callback_url = intent.getStringExtra("callback_url");
            amount = intent.getIntExtra("amount", 1);

            show_payment_confirmation();
        }
    };

    private void show_payment_confirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        double money = ((double)amount) / 100;
        int slashslash = callback_url.indexOf("//") + 2;
        String domain = callback_url.substring(slashslash, callback_url.indexOf('/', slashslash));

        builder.setTitle("Payment Confirmation")
                .setMessage("Are you sure to pay " + String.format("($%.2f) ", money) + "to " + domain + "?")
                .setCancelable(false)
                .setPositiveButton("Yeah, I want to", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String value_message;
                        try {
                            value_message = createValueMessage(amount, payee_id, annotation);
                        } catch (Exception ex) {
                            Log.e(LOG_TAG, ex.getMessage());
                            return;
                        }

                        HashMap<String, String> post_data = new HashMap<String, String>();
                        post_data.put("channel_id", channel_id);
                        post_data.put("value_message", value_message);

                        try {
                            http_post(callback_url, post_data);
                        } catch (IOException ex) {
                            Log.e(LOG_TAG, ex.getMessage());
                        }
                    }
                })
                .setNegativeButton("No, I don't", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void manually_enter_email() {
        final View form = getLayoutInflater().inflate(R.layout.emaildialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Enter an ID or email address")
                .setView(form)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText email_form = (EditText) form.findViewById(R.id.email_addr);
                        String email_addr = email_form.getText().toString();

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(EMAIL_ADDRESS_PREF, email_addr);
                        editor.commit();

                        register_cloud_message();

                        Intent intent = new Intent(UPDATE_INFO_ACTION);
                        sendBroadcast(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
