package com.miplata.listener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import com.miplata.nlp.TransactionClassifier;

import java.text.NumberFormat;
import java.util.Locale;

public class FinanceNotificationService extends NotificationListenerService {

    private static final String CHANNEL_ID = "MiPlataChannel";
    private static final String TAG = "FinanceService";
    private static final double MIN_CONFIDENCE = 0.80;

    private TransactionClassifier classifier;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        // --- INICIO DE LA CORRECCIÓN FINAL ---
        // Usamos el nuevo método de fábrica estático que devuelve null si falla.
        classifier = TransactionClassifier.create(getApplicationContext());
        if (classifier == null) {
            Log.e(TAG, "Error CRÍTICO al cargar el modelo de IA. La funcionalidad de lectura estará desactivada.");
        } else {
            Log.d(TAG, "Clasificador de IA cargado correctamente.");
        }
        // --- FIN DE LA CORRECCIÓN FINAL ---
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (classifier == null) return;
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        String title = notification.extras.getString(Notification.EXTRA_TITLE, "");
        String text = notification.extras.getString(Notification.EXTRA_TEXT, "");
        String fullText = title + " " + text;

        Double amount = NotificationParser.parseAmount(fullText);
        if (amount == null) return;

        String[] classification = classifier.classify(fullText);
        String type = classification[0];
        double confidence = Double.parseDouble(classification[1]);

        Log.d(TAG, String.format("Clasificación para '%s': Tipo=%s, Confianza=%.2f", fullText, type, confidence));

        if (confidence < MIN_CONFIDENCE || type.equals("UNKNOWN")) return;

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDateMillis(System.currentTimeMillis());
        transaction.setDescription(fullText);
        transaction.setType(type);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            db.transactionDao().insert(transaction);
            Log.d(TAG, "Transacción guardada: " + transaction.getType() + " por " + transaction.getAmount());
            sendConfirmationNotification(transaction);
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void sendConfirmationNotification(Transaction transaction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        String formattedAmount = currencyFormatter.format(transaction.getAmount());
        String notificationTitle = transaction.getType().equals("CREDIT") ? "Ingreso Registrado" : "Gasto Registrado";
        String notificationText = "Se ha registrado un movimiento de " + formattedAmount + ".";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Confirmaciones", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (classifier != null) {
            classifier.close();
        }
    }
}
