package com.miplata.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Transaction.class, User.class, Goal.class, Category.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract UserDao userDao();
    public abstract GoalDao goalDao();
    public abstract CategoryDao categoryDao();

    private static volatile AppDatabase INSTANCE;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `pin` TEXT)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `users` ADD COLUMN `interests` TEXT");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT, `targetAmount` REAL NOT NULL, `currentAmount` REAL NOT NULL, FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT)");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `transactions` ADD COLUMN `category` TEXT");
        }
    };

    // --- INICIO DE LA CORRECCIÓN PROFESIONAL ---
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. Añadimos la columna userId a la tabla de transacciones. 
            // Ponemos un valor por defecto (0) para las filas existentes, ya que no podemos saber a qué usuario pertenecían.
            database.execSQL("ALTER TABLE `transactions` ADD COLUMN `userId` INTEGER NOT NULL DEFAULT 0");

            // 2. Creamos una nueva tabla temporal con la estructura CORRECTA, incluyendo la llave foránea.
            database.execSQL("CREATE TABLE `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `dateMillis` INTEGER NOT NULL, `type` TEXT, `description` TEXT, `category` TEXT, `userId` INTEGER NOT NULL, FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE)");

            // 3. Copiamos los datos de la tabla vieja a la nueva.
            database.execSQL("INSERT INTO `transactions_new` (id, amount, dateMillis, type, description, category, userId) SELECT id, amount, dateMillis, type, description, category, userId FROM `transactions`");

            // 4. Borramos la tabla vieja.
            database.execSQL("DROP TABLE `transactions`");

            // 5. Renombramos la tabla nueva para que tenga el nombre original.
            database.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`");
        }
    };
    // --- FIN DE LA CORRECCIÓN PROFESIONAL ---

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "miplata_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
