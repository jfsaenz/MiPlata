package com.miplata.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etUsername, etPin;
    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        etUsername = findViewById(R.id.etUsername);
        etPin = findViewById(R.id.etPin);
        Button btnCreate = findViewById(R.id.btnCreate);
        TextView tvLogin = findViewById(R.id.tvLogin);
        TextView tvTerms = findViewById(R.id.tvTerms);

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        btnCreate.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String pin = etPin.getText().toString().trim();

            if (username.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                User existingUser = db.userDao().findByName(username);

                if (existingUser != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Ese nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPin(pin);
                    db.userDao().insert(newUser);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        });

        tvLogin.setOnClickListener(v -> {
            finish();
        });

        tvTerms.setOnClickListener(v -> {
            showTermsDialog();
        });
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Términos y Condiciones")
                .setMessage("1. Aceptación de los Términos\n\nAl descargar, instalar o utilizar la aplicación MiPlata, el usuario acepta estos Términos y Condiciones, así como la Política de Privacidad asociada. Si no está de acuerdo, debe abstenerse de usar la aplicación.\n\n2. Descripción del servicio\n\nMiPlata es una aplicación móvil diseñada para ayudar al usuario a controlar sus finanzas personales, analizando automáticamente notificaciones financieras (como las de bancos o billeteras digitales) y clasificando gastos con inteligencia artificial (IA).\nEl servicio no requiere que el usuario proporcione contraseñas bancarias ni credenciales de acceso a sus cuentas.\n\n3. Permisos y Acceso a Notificaciones\n\nPara ofrecer sus funcionalidades principales, MiPlata requiere el permiso de “Acceso a notificaciones” del dispositivo.\nEste permiso se usa exclusivamente para:\n\nLeer notificaciones de aplicaciones financieras (bancos, billeteras, pagos, etc.)\n\nExtraer información limitada y anónima sobre montos, fechas y categorías de transacción.\n\nGenerar reportes automáticos y clasificaciones de gastos dentro de la app.\n\nMiPlata no almacena ni comparte el texto completo de las notificaciones, ni accede a mensajes personales, contraseñas, números de cuenta ni códigos de seguridad.\n\n4. Privacidad y tratamiento de datos\n\nLos datos recolectados se procesan localmente o mediante servidores en la nube con cifrado seguro.\nToda la información se usa únicamente con fines de análisis financiero personal y nunca se venderá a terceros.\nLos usuarios pueden en cualquier momento revocar permisos o eliminar sus datos desde el menú de configuración.\n\n5. Responsabilidad del usuario\n\nEl usuario se compromete a:\n\nProporcionar información veraz y mantener la aplicación actualizada.\n\nNo usar la app para fines ilícitos o fraudulentos.\n\nComprender que MiPlata no sustituye la asesoría financiera profesional y no tiene acceso directo a fondos o movimientos bancarios.\n\n6. Limitaciones de responsabilidad\n\nMiPlata no será responsable por:\n\nErrores derivados de notificaciones incompletas o mal formateadas.\n\nPérdidas económicas ocasionadas por decisiones tomadas a partir de la información mostrada.\n\nFallos en los servicios de terceros (bancos, billeteras, APIs, o sistemas de notificaciones).\n\n7. Suscripción y pagos\n\nLa app opera bajo un modelo freemium, con una versión gratuita y un plan premium con funcionalidades avanzadas (IA predictiva, metas de ahorro, reportes detallados y recompensas).\nLos pagos se gestionan a través de Google Play o App Store, según las condiciones de esas plataformas.\n\n8. Propiedad intelectual\n\nTodos los derechos sobre el software, el diseño, los textos, las marcas y los algoritmos pertenecen a MiPlata o a sus respectivos titulares.\nEl usuario no puede copiar, modificar o distribuir el contenido sin autorización expresa.\n\n9. Modificaciones\n\nMiPlata podrá actualizar estos términos cuando sea necesario. Los cambios se comunicarán por la aplicación o el sitio web, y su uso posterior implicará la aceptación de las modificaciones.\n\n10. Jurisdicción aplicable\n\nEstos términos se rigen por las leyes de la República de Colombia. Cualquier controversia se resolverá ante los tribunales competentes en Bogotá, D.C.")
                .setPositiveButton("Cerrar", null)
                .show();
    }
}
