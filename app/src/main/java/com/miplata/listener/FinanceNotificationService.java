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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FinanceNotificationService extends NotificationListenerService {

    private static final String CHANNEL_ID = "MiPlataChannel";
    private static final String TAG = "FinanceService";
    private static final double MIN_CONFIDENCE = 0.70;

    // --- EL PORTERO INTELIGENTE: Lista Negra de Paquetes --- 
    private static final List<String> IGNORED_PACKAGES = Arrays.asList(
            "com.whatsapp",
            "com.facebook.orca", // Facebook Messenger
            "com.instagram.android",
            "org.telegram.messenger",
            "com.google.android.apps.messaging" // Mensajes de Google
    );

    private TransactionClassifier classifier;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        classifier = TransactionClassifier.create(getApplicationContext());
        if (classifier == null) {
            Log.e(TAG, "Error CRÍTICO al cargar el modelo de IA. La funcionalidad de lectura estará desactivada.");
        } else {
            Log.d(TAG, "Clasificador de IA cargado correctamente.");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // --- PRIMERA CAPA DE DEFENSA: El Portero ---
        String packageName = sbn.getPackageName();
        if (IGNORED_PACKAGES.contains(packageName)) {
            return; // Si la notificación es de una app ignorada, no hacemos nada.
        }

        if (classifier == null) return;
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        String title = notification.extras.getString(Notification.EXTRA_TITLE, "");
        String text = notification.extras.getString(Notification.EXTRA_TEXT, "");
        String fullText = title + " " + text;

        Double amount = NotificationParser.parseAmount(fullText);
        if (amount == null) return;

        // --- IA + SUPERVISOR ---
        String[] classification = classifier.classify(fullText);
        String predictedType = classification[0];
        double confidence = Double.parseDouble(classification[1]);
        String finalType = refineTypeWithKeywords(fullText, predictedType);

        Log.d(TAG, String.format("IA predijo: %s (%.2f) -> Refinado a: %s | App: %s | Texto: %s", predictedType, confidence, finalType, packageName, fullText));

        if (confidence < MIN_CONFIDENCE || finalType.equals("UNKNOWN")) {
            return;
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDateMillis(System.currentTimeMillis());
        transaction.setDescription(fullText);
        transaction.setType(finalType);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            db.transactionDao().insert(transaction);
            Log.d(TAG, "Transacción guardada: " + transaction.getType() + " por " + transaction.getAmount());
            sendConfirmationNotification(transaction);
        }).start();
    }

    private String refineTypeWithKeywords(String text, String predictedType) {
        String lowerCaseText = text.toLowerCase();

        String[] debitKeywords = {"compra", "pago", "pagado", "envío", "enviado", "retiro", "factura", "costo", "pagas"};
        for (String keyword : debitKeywords) {
            if (lowerCaseText.contains(keyword)) {
                return "DEBIT";
            }
        }

        String[] creditKeywords = {"recibido", "abono", "depósito", "ingreso", "recibes", "te han enviado", "te pagó"};
        for (String keyword : creditKeywords) {
            if (lowerCaseText.contains(keyword)) {
                return "CREDIT";
            }
        }

        return predictedType;
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
