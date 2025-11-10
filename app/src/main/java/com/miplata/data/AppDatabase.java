package com.miplata.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Transaction.class, User.class, Goal.class, Category.class, Reward.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract UserDao userDao();
    public abstract GoalDao goalDao();
    public abstract CategoryDao categoryDao();
    public abstract RewardDao rewardDao();

    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Migraciones
    static final Migration MIGRATION_1_2 = new Migration(1, 2) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `pin` TEXT)"); } };
    static final Migration MIGRATION_2_3 = new Migration(2, 3) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("ALTER TABLE `users` ADD COLUMN `interests` TEXT"); } };
    static final Migration MIGRATION_3_4 = new Migration(3, 4) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT, `targetAmount` REAL NOT NULL, `currentAmount` REAL NOT NULL, FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"); } };
    static final Migration MIGRATION_4_5 = new Migration(4, 5) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT)"); } };
    static final Migration MIGRATION_5_6 = new Migration(5, 6) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("ALTER TABLE `transactions` ADD COLUMN `category` TEXT"); } };
    static final Migration MIGRATION_6_7 = new Migration(6, 7) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("ALTER TABLE `transactions` ADD COLUMN `userId` INTEGER NOT NULL DEFAULT 0"); database.execSQL("CREATE TABLE `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `dateMillis` INTEGER NOT NULL, `type` TEXT, `description` TEXT, `category` TEXT, `userId` INTEGER NOT NULL, FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE)"); database.execSQL("INSERT INTO `transactions_new` (id, amount, dateMillis, type, description, category, userId) SELECT id, amount, dateMillis, type, description, category, userId FROM `transactions`"); database.execSQL("DROP TABLE `transactions`"); database.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`"); } };
    static final Migration MIGRATION_7_8 = new Migration(7, 8) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("ALTER TABLE `users` ADD COLUMN `reward_points` INTEGER NOT NULL DEFAULT 0"); database.execSQL("CREATE TABLE IF NOT EXISTS `rewards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `partner_name` TEXT, `title` TEXT, `description` TEXT, `cost_in_points` INTEGER NOT NULL, `category` TEXT, `qr_code_url` TEXT, `logo_url` TEXT)"); } };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "miplata_database")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Limpia recompensas anteriores y añade las nuevas
                db.execSQL("DELETE FROM rewards");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('CineAlpes', 'Boleta 2D Gratis', 'Válido para cualquier película 2D de lunes a jueves.', 600, 'Ocio', 'logo_ocio1', 'qr_ocio1')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('La Granja Hamburguesas', '2x1 en Hamburguesas', 'Compra una hamburguesa especial y lleva la segunda gratis.', 800, 'Comida', 'logo_comida1', 'qr_comida1')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('M&H', '25% Dto. en Chaquetas', 'Válido para la nueva colección de chaquetas de hombre y mujer.', 1200, 'Ropa', 'logo_ropa1', 'qr_ropa1')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('GymAlpes', 'Mes 2x1 en Gimnasio', 'Paga un mes y entrena dos. Válido para nuevos usuarios.', 2000, 'Gimnasio', 'logo_gimnasio1', 'qr_gimnasio1')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Sara Home', '15% Dto. en Adornos', 'Decora tu espacio con 15% de descuento en todos los adornos.', 900, 'Hogar', 'logo_hogar1', 'qr_hogar1')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Albert II', 'Bono de $50.000', 'Utiliza este bono para comprar comida o juguetes para tu mascota.', 700, 'Mascotas', 'logo_mascotas1', 'qr_mascotas1')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Blind', 'Agua Micelar Gratis', 'Por compras superiores a $100.000 en maquillaje, lleva un agua micelar.', 500, 'Belleza', 'logo_belleza1', 'qr_belleza1')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Teatro Bogotano', '30% Dto. en Tickets', 'Disfruta de las mejores obras con un descuento especial.', 1100, 'Ocio', 'logo_ocio2', 'qr_ocio2')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Luis Postres', 'Postre Gratis', 'Por la compra de un plato fuerte, recibe un mini waffle de arequipe.', 400, 'Comida', 'logo_comida2', 'qr_comida2')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Sara', '15% Dto. en Jeans', 'Válido para todas las referencias de jeans de hombre y mujer.', 1000, 'Ropa', 'logo_ropa2', 'qr_ropa2')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Fit', '1 Semana Gratis', 'Entrena 7 días sin costo y conoce nuestras instalaciones.', 300, 'Gimnasio', 'logo_gimnasio2', 'qr_gimnasio2')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Mickiso', '20% Dto. en Peluches', 'Decora tu cuarto con tus personajes favoritos.', 650, 'Hogar', 'logo_hogar2', 'qr_hogar2')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Paisdemascotas.com', 'Envío Gratis', 'Envío gratis en todas tus compras superiores a $80.000.', 250, 'Mascotas', 'logo_mascotas2 ', 'qr_mascotas2')");
                db.execSQL("INSERT INTO rewards (partner_name, title, description, cost_in_points, category, logo_url, qr_code_url) VALUES ('Leal', 'Protector Solar 50% Dto', 'En la segunda unidad de protectores solares UV Defender.', 850, 'Belleza', 'logo_belleza2', 'qr_belleza2')");
            });
        }
    };
}
