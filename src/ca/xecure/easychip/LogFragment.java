/*
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

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;

import ca.mint.mintchip.contract.ILogEntry;
import ca.mint.mintchip.contract.LogType;
import ca.mint.mintchip.contract.MintChipException;

import static ca.xecure.easychip.MintChipUtilities.*;
import static ca.xecure.easychip.CommonUtilities.*;


public class LogFragment extends Fragment {
    private LogType mLogType = LogType.DEBIT;
    private ILogEntry[] mLogEntries;
    private ListView logView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logfrag, container, false);
        logView = (ListView) v.findViewById(R.id.logView);

        update_logs();

        return v;
    }

    public void update_logs() {
        try {
            mLogEntries = readTransactionLog(this.mLogType);
        } catch (MintChipException e) {
            Log.e(LOG_TAG, e.toString());
            return;
        }

        LogEntryAdapter adapter = new LogEntryAdapter(getActivity(), mLogEntries, mLogType);
        logView.setAdapter(adapter);
    }

    private static class LogEntryAdapter extends ArrayAdapter<ILogEntry> {

        private LogType mLogType;

        public LogEntryAdapter(Context context, ILogEntry[] entries, LogType logType) {

            super(context, R.layout.logentry, entries);

            this.mLogType = logType;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
            View rowView = inflater.inflate(R.layout.logentry, parent, false);

            ILogEntry entry = this.getItem(position);

            if (entry != null) {
                TextView entryTextView = (TextView) rowView.findViewById(R.id.entryTextView);

                String accountMessage = getContext().getString(R.string.paid_to) + formatId(entry.getPayeeId());

                String text = String.format(getContext().getString(R.string.log_entry_format),
                        formatCurrency(entry.getAmount()),
                        accountMessage,
                        formatDateTime(entry.getTransactionTime()));

                entryTextView.setText(text);
            }

            return rowView;
        }
    }
}
