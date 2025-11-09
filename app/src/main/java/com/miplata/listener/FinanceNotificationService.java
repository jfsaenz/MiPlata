package com.miplata.listener;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinanceNotificationService extends NotificationListenerService {

    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.getDatabase(getApplication());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        if (n == null || n.extras == null) return;

        CharSequence title = n.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text  = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        String payload = (title == null ? "" : title + " - ") + (text == null ? "" : text);

        Transaction t = NotificationParser.parse(payload);
        if (t == null) return;

        executorService.execute(() -> {
            db.transactionDao().insert(t);
        });
    }
}
