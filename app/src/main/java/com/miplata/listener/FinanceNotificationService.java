package com.miplata.listener;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import com.miplata.rules.RuleClassifier;
import com.miplata.ui.LoginActivity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class FinanceNotificationService extends NotificationListenerService {

    private static final String TAG = "FinanceService";

    private static final Set<String> ALLOWED_BANK_PACKAGES = new HashSet<>(Arrays.asList(
            "com.nequi.mobile.app", "com.daviplata", "com.davivienda.app", "com.bancolombia.app",
            "com.bbva.co", "co.com.bancodebogota.bancamovil", "co.com.bancodeoccidente.bancamovil",
            "com.scotiabankcolpatria", "co.com.avvillas.app", "com.movii.app", "com.lulo.bank", "com.nu.mobile"
    ));

    private static final Set<String> BLOCKED_CHAT_PACKAGES = new HashSet<>(Arrays.asList(
            "com.whatsapp", "com.facebook.orca", "com.instagram.android", "org.telegram.messenger"
    ));

    private static final Set<String> SMS_PACKAGES = new HashSet<>(Arrays.asList(
        "com.google.android.apps.messaging", "com.android.messaging", 
        "com.samsung.android.messaging", "com.oneplus.mms"
    ));

    private static final String[] BANK_KEYWORDS = {
        "daviplata","davivienda","nequi","bancolombia","bbva","banco de bogotá", "banco de occidente",
        "colpatria","scotiabank","itau","av villas","popular","movii","lulo","nu"
    };

    private static final Pattern BANK_SMS_SENDER = Pattern.compile(
            "(?i)\\b(daviplata|davivienda|nequi|bancolombia|bbva|banco\\s*de\\s*bogot[aá]|occidente|colpatria|scotiabank|itau|av\\s*villas|popular|movii|lulo|nu)\\b"
    );

    private RuleClassifier rules;

    @Override
    public void onCreate() {
        super.onCreate();
        rules = new RuleClassifier();
        Log.d(TAG, "MiPlata Listener (reglas) iniciado.");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;

        final String pkg = sbn.getPackageName();
        final Notification n = sbn.getNotification();
        final Bundle extras = (n != null) ? n.extras : null;

        if (BLOCKED_CHAT_PACKAGES.contains(pkg) || isMessageCategory(n)) return;

        final String fullText = extractAllText(extras).trim();
        if (fullText.length() < 6) return;

        if (!isFromBank(sbn, fullText, extras)) return;

        RuleClassifier.Result r = rules.classify(fullText);
        if (!r.accept || r.amount == null || r.amount <= 0) {
            Log.d(TAG, "Descartado por reglas: " + r.reason + " | " + fullText);
            return;
        }

        // --- LA SOLUCIÓN AL PROBLEMA #1: Obtener el userId ---
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (currentUserId == -1) {
            Log.e(TAG, "Error: No se pudo obtener el ID del usuario logueado. No se puede guardar la transacción.");
            return;
        }

        final Transaction t = new Transaction();
        t.setAmount(r.amount);
        t.setDateMillis(System.currentTimeMillis());
        t.setDescription(fullText);
        t.setType(r.type);
        t.setUserId(currentUserId); // Asignamos el ID del usuario a la transacción

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                db.transactionDao().insert(t);
                Log.d(TAG, "VICTORIA: Transacción guardada para usuario " + currentUserId + ": " + r.type + " $" + r.amount);
            } catch (Exception e) {
                Log.e(TAG, "Error final al guardar transacción en la base de datos", e);
            }
        }).start();
    }

    private boolean isFromBank(StatusBarNotification sbn, String fullText, Bundle extras) {
        final String pkg = sbn.getPackageName();
        if (ALLOWED_BANK_PACKAGES.contains(pkg)) return true;

        if (SMS_PACKAGES.contains(pkg)) {
            String sender = "";
            if (extras != null) {
                CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
                if (!TextUtils.isEmpty(title)) sender = title.toString();
            }
            return !TextUtils.isEmpty(sender) && BANK_SMS_SENDER.matcher(sender).find();
        }

        return containsAny(fullText.toLowerCase(Locale.ROOT), BANK_KEYWORDS);
    }

    private boolean isMessageCategory(Notification n) {
        return n != null && Notification.CATEGORY_MESSAGE.equals(n.category);
    }

    private boolean containsAny(String s, String[] kws) {
        if (s == null) return false;
        for (String k : kws) if (s.contains(k)) return true;
        return false;
    }

    private String extractAllText(Bundle extras) {
        if (extras == null) return "";
        StringBuilder sb = new StringBuilder();
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (!TextUtils.isEmpty(title)) sb.append(title).append(" ");
        if (!TextUtils.isEmpty(text)) sb.append(text).append(" ");
        return sb.toString().replaceAll("\\s+", " ");
    }
}
