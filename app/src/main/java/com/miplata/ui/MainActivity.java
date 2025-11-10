package com.miplata.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miplata.R;
import com.miplata.ui.fragment.AccountFragment;
import com.miplata.ui.fragment.HomeFragment;
import com.miplata.ui.fragment.MovementsFragment;

public class MainActivity extends AppCompatActivity {

    // --- INICIO: Lógica para el permiso de POST_NOTIFICATIONS ---
    private final ActivityResultLauncher<String> requestPermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            // No necesitamos hacer nada especial si se concede o deniega, 
            // la app funcionará igualmente. La notificación de confirmación simplemente no aparecerá.
        });

    private void requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Muestra el diálogo del sistema para pedir el permiso.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    // --- FIN: Lógica para el permiso de POST_NOTIFICATIONS ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        // Comprobamos y pedimos ambos permisos al iniciar
        if (!isNotificationServiceEnabled()) {
            showPermissionDialog();
        }
        requestPostNotificationsPermission(); // <-- LLAMADA AL NUEVO MÉTODO
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso de Lectura de Notificaciones")
                .setMessage("Para registrar transacciones automáticamente, MiPlata necesita permiso para LEER tus notificaciones. Por favor, activa el permiso en la siguiente pantalla.")
                .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (item.getItemId() == R.id.nav_movements) {
            selectedFragment = new MovementsFragment();
        } else if (item.getItemId() == R.id.nav_account) {
            selectedFragment = new AccountFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        return true;
    };
}
