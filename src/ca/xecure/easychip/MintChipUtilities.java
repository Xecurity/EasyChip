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

import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;

import ca.mint.mintchip.android.api.MessageFactory;
import ca.mint.mintchip.android.api.MintChipFactory;
import ca.mint.mintchip.contract.*;


public final class MintChipUtilities {
    private static final int LOG_RECORDS_TO_SHOW = 20; // To display up to 20 records

    private static IMintChip sMintChip = null;


    public synchronized static IMintChip getMintChip() throws MintChipException {
        if (sMintChip == null){
            try {
                sMintChip = MintChipFactory.getInstance().createMintChip("/mnt/external_sd/");
            } catch (MintChipException ex1) {
                try {
                    sMintChip = MintChipFactory.getInstance().createMintChip("/sdcard/usbStorage/sda1/");
                } catch (MintChipException ex2) {
                    try {
                        sMintChip = MintChipFactory.getInstance().createMintChip("/sdcard/usbStorage/sdb1/");
                    } catch (MintChipException ex3) {
                        sMintChip = MintChipFactory.getInstance().createMintChip();
                    }
                }
            }
        }

        return sMintChip;
    }


    public static boolean isValidMintChipID(String mintChipId) throws MintChipException {
        return getMintChip().isValidId(mintChipId);
    }


    public static ILogEntry[] readTransactionLog(LogType logType) throws MintChipException {
        IMintChipStatus status = getMintChip().getStatus();

        // Calculate the start index and the number of entries to read
        int totalCount = 0;
        if (logType == LogType.CREDIT){
            totalCount = status.getCreditLogCount();
        }
        else {
            totalCount = status.getDebitLogCount();
        }

        int startIndex = totalCount - LOG_RECORDS_TO_SHOW;
        int numOfEntries = LOG_RECORDS_TO_SHOW;
        if (startIndex < 0) {
            numOfEntries = LOG_RECORDS_TO_SHOW + startIndex;
            startIndex = 0;
        }

        if (numOfEntries < 1) {
            numOfEntries = 1;
        }

        // Read the MintChip transaction log
        ILogEntry[] entries = getMintChip().readTransactionLog(logType, startIndex, numOfEntries);

        // List entries in the reverse order
        if (entries != null && entries.length > 0) {

            List<ILogEntry> list = Arrays.asList(entries);
            Collections.reverse(list);
            list.toArray(entries);
        }

        return entries;
    }


    public static String createValueMessage(
            int amount,
            String payee,
            String annotation)
            throws MintChipException, IOException {
        IValueRequestMessage request = MessageFactory.getInstance().createValueRequestMessage(
                payee, amount, getMintChip().getCurrencyCode());

        request.setAnnotation(annotation);
        request.setChallenge(new Date().hashCode());

        IValueMessage valueMessage = getMintChip().createValueMessage(request);
        return valueMessage.toBase64String();
    }


    public static String getFormattedChipInfo() throws MintChipException {
        IMintChipStatus status = getMintChip().getStatus();

        StringBuffer text = new StringBuffer();

        text.append("MintChip Id: ").append(formatId(getMintChip().getId()))
                .append("\nCurrency: ").append(getMintChip().getCurrencyCode())
                .append("\nApplet Version: ").append(getMintChip().getVersion())
                .append("\nBalance: ").append(formatCurrency(status.getBalance()))
                .append("\nCurrent Credit Count: ").append(status.getCreditLogCount())
                .append("\nCurrent Debit Count: ").append(status.getDebitLogCount())
                .append("\nRemaining Credit Count: ").append(status.getCreditLogCountRemaining())
                .append("\nRemaining Debit Count: ").append(status.getDebitLogCountRemaining())
                .append("\nMax Credit Allowed: ").append(formatCurrency(status.getMaxCreditAllowed()))
                .append("\nMax Debit Allowed: ").append(formatCurrency(status.getMaxDebitAllowed()));

        return text.toString();
    }


    public static String getFormattedMintChipID() throws MintChipException {
        String id = getMintChip().getId();
        return formatId(id);
    }


    public static String getFormattedBalance() throws MintChipException  {
        IMintChipStatus status = getMintChip().getStatus();
        return formatCurrency(status.getBalance());
    }


    public static IValueMessage getLastCreatedValueMessage() throws MintChipException {
        return getMintChip().getLastCreatedValueMessage("This is a duplicate.");
    }


    public static String formatId(String id){
        return String.format("%s-%s-%s-%s",
                id.substring(0, 4),
                id.substring(4, 8),
                id.substring(8, 12),
                id.substring(12, 16));
    }


    public static String formatCurrency(int cents) {
        double amount = ((double)cents) / 100;
        return String.format("$%.2f", amount);
    }


    public static String formatDateTime(Calendar dateTime) {
        final SimpleDateFormat sDateFormat = new SimpleDateFormat("MMM d yyyy, HH:mm aaa");

        return sDateFormat.format(dateTime.getTime());
    }
}
