package com.miplata.listener;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.miplata.data.Transaction;

public class FinanceNotificationService extends NotificationListenerService {

    public static final String ACTION_NEW_TX = "com.miplata.NEW_TX";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_TYPE   = "type";
    public static final String EXTRA_DESC   = "desc";
    public static final String EXTRA_DATE   = "date";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        if (n == null || n.extras == null) return;

        CharSequence title = n.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text  = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        String payload = (title == null ? "" : title + " - ") + (text == null ? "" : text);

        Transaction t = NotificationParser.parse(payload);
        if (t == null) return;

        Intent i = new Intent(ACTION_NEW_TX);
        i.putExtra(EXTRA_AMOUNT, t.getAmount());
        i.putExtra(EXTRA_TYPE,   t.getType());
        i.putExtra(EXTRA_DESC,   t.getDescription());
        i.putExtra(EXTRA_DATE,   t.getDateMillis());
        sendBroadcast(i);
    }
}
