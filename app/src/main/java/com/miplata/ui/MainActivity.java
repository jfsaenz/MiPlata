package com.miplata.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miplata.R;
import com.miplata.ui.fragment.AccountFragment;
import com.miplata.ui.fragment.GoalsFragment;
import com.miplata.ui.fragment.HomeFragment;
import com.miplata.ui.fragment.MovementsFragment;
import com.miplata.ui.fragment.RewardsFragment; // <-- Importar el nuevo fragmento

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        if (!isNotificationServiceEnabled()) {
            showPermissionDialog();
        }
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
                .setTitle("Permiso de Notificaciones")
                .setMessage("Para funcionar correctamente, MiPlata necesita acceso a tus notificaciones. Por favor, activa el permiso en los ajustes.")
                .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_movements) {
            selectedFragment = new MovementsFragment();
        } else if (itemId == R.id.nav_goals) {
            selectedFragment = new GoalsFragment();
        } else if (itemId == R.id.nav_rewards) { // <-- LÓGICA AÑADIDA
            selectedFragment = new RewardsFragment();
        } else if (itemId == R.id.nav_account) {
            selectedFragment = new AccountFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        return true;
    };
}
