package com.example.android.gymlogapp.data;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;

@Database(entities = {ClientEntry.class, PaymentEntry.class, VisitEntry.class},version = 8,exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class GymDatabase extends RoomDatabase {




    private static final String LOG_TAG = GymDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "GymData.db";
    private static GymDatabase sInstance;


    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN exchangeRate REAL DEFAULT 32.5 NOT NULL");
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN currency TEXT DEFAULT 'USD'");
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN comment TEXT");
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN extra TEXT");
        }
    };

    public static GymDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        GymDatabase.class, GymDatabase.DB_NAME)
                        //.allowMainThreadQueries()
                        //.fallbackToDestructiveMigration()
                        .addMigrations(MIGRATION_7_8)
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public  abstract ClientDao clientDao();
    public abstract PaymentDao paymentDao();
    public abstract VisitDao visitDao();





}
