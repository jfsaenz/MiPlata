package com.miplata.ui;

import androidx.core.content.ContextCompat;
import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.miplata.R;
import com.miplata.data.Transaction;
import com.miplata.domain.FinanceManager;
import com.miplata.listener.FinanceNotificationService;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final FinanceManager manager = new FinanceManager();
    private TextView tvTotals, tvLast;
    private final Locale CO = Locale.forLanguageTag("es-CO");

    private final BroadcastReceiver rx = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            if (!FinanceNotificationService.ACTION_NEW_TX.equals(i.getAction())) return;
            Transaction t = new Transaction();
            t.setAmount(i.getDoubleExtra(FinanceNotificationService.EXTRA_AMOUNT, 0));
            t.setType(i.getStringExtra(FinanceNotificationService.EXTRA_TYPE));
            t.setDescription(i.getStringExtra(FinanceNotificationService.EXTRA_DESC));
            t.setDateMillis(i.getLongExtra(FinanceNotificationService.EXTRA_DATE, System.currentTimeMillis()));

            manager.processTransaction(t);

            tvLast.setText(String.format(CO, "%s | $%,.2f | %s",
                    t.getType(), t.getAmount(), t.getDescription()));

            tvTotals.setText(String.format(CO,
                    "Ingresos: $%,.2f\nGastos: $%,.2f",
                    manager.totalIngresos(), manager.totalGastos()));
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        tvTotals = findViewById(R.id.tvTotals);
        tvLast   = findViewById(R.id.tvLast);
        Button btnPerms = findViewById(R.id.btnPerms);
        btnPerms.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
    }

    @Override protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter(FinanceNotificationService.ACTION_NEW_TX);
        ContextCompat.registerReceiver(
                this,
                rx,
                f,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override protected void onPause() {
        super.onPause();
        unregisterReceiver(rx);
    }
}
