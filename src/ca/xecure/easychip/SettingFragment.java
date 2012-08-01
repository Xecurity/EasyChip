package ca.xecure.easychip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class SettingFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settingfrag, container, false);

        Account[] acc = AccountManager.get(getActivity()).getAccountsByType("com.google");
        if (acc.length <= 1) {
            TextView setting_intro = (TextView)view.findViewById(R.id.settings);
            setting_intro.setText("");
            Button setting_button = (Button)view.findViewById(R.id.change_email_button);
            setting_button.setText("Nothing to set up...");
            setting_button.setEnabled(false);
        }

        return(view);
    }
}
