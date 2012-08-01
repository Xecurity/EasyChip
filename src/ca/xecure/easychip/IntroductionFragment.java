package ca.xecure.easychip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.mint.mintchip.contract.MintChipException;

import static ca.xecure.easychip.CommonUtilities.*;
import static ca.xecure.easychip.MintChipUtilities.getFormattedBalance;
import static ca.xecure.easychip.MintChipUtilities.getMintChip;

import static android.support.v4.app.FragmentActivity.MODE_PRIVATE;

public class IntroductionFragment extends Fragment {
    TextView view_steps;
    TextView view_email;
    TextView view_balance;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.introfrag, container, false);

        view_steps = (TextView) view.findViewById(R.id.steps);
        view_email = (TextView) view.findViewById(R.id.email);
        view_balance = (TextView) view.findViewById(R.id.balance);

        update_info();

        getActivity().registerReceiver(update_info_handler, new IntentFilter(UPDATE_INFO_ACTION));

        return view;
    }

    public void update_info() {
        try {
            getMintChip();
        } catch (MintChipException ex) {
            view_steps.setText("No MintChip is found!");
        }

        try {
            getMintChip();
            view_steps.setText(R.string.steps);
            view_balance.setText("Balance: " + getFormattedBalance());
        } catch (MintChipException ex) {
            view_steps.setText("No MintChip is found!");
            view_balance.setText("Balance: $0");
        }

        String email_address = getActivity().getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE).getString(EMAIL_ADDRESS_PREF, null);
        if (email_address != null) {
            view_email.setText(email_address);
        } else {
            view_email.setText("");
        }
    }


    private final BroadcastReceiver update_info_handler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "handler to update info");

        update_info();
        }
    };
}
