package com.example.android.gymlogapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Can export an sqlite databse into a csv file.
 *
 * The file has on the top dbVersion and on top of each table data the name of the table
 *
 * Inspired by
 * https://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
 * and some other SO threads as well.
 *
 */
public class SqliteExporter {
    private Context mContext;
    public SqliteExporter(Context context){
        mContext=context;
    }

    private static final String TAG = "belloso";
    public static final String DB_BACKUP_DB_VERSION_KEY = "dbVersion";
    public static final String DB_BACKUP_TABLE_NAME = "table";

    public String export(SQLiteDatabase db) throws IOException{
        if( !FileUtils.isExternalStorageWritable() ){
            Log.d(TAG, "cant write");
            throw new IOException("Cannot write to external storage");
        }
        String dirName = createExportDirName();
        File backupDir = FileUtils.createDirIfNotExist(FileUtils.getAppDir(mContext) + dirName);

        File exportClientFile = new File(backupDir, "client.csv");
        File exportPaymentFile = new File(backupDir, "payment.csv");
        File exportVisitFile = new File(backupDir, "visit.csv");
        boolean successClient = exportClientFile.createNewFile();
        boolean successPayment = exportPaymentFile.createNewFile();
        boolean successVisit = exportVisitFile.createNewFile();

        if(!successClient || !successPayment || !successVisit){

            throw new IOException("Failed to create the backup file");

        }

        Log.d(TAG, "Started to fill the backup file in " + backupDir.getAbsolutePath());
        long starTime = System.currentTimeMillis();
        writeClientCsv(exportClientFile, db);
        writePaymentCsv(exportPaymentFile,db);
        writeVisitCsv(exportVisitFile,db);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Creating backup took " + (endTime - starTime) + "ms.");

        return backupDir.getAbsolutePath();
    }

    private static String createExportDirName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
        return "/export_" + sdf.format(new Date()) + "";
    }

    /**
     * Get all the table names we have in db
     *
     * @param db
     * @return
     */
//    public static List<String> getTablesOnDataBase(SQLiteDatabase db){
//        Cursor c = null;
//        List<String> tables = new ArrayList<>();
//        try{
//            c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
//            if (c.moveToFirst()) {
//                while ( !c.isAfterLast() ) {
//                    tables.add(c.getString(0));
//                    c.moveToNext();
//                }
//            }
//        }
//        catch(Exception throwable){
//            Log.d(TAG, "Could not get the table names from db", throwable);
//        }
//        finally{
//            if(c!=null)
//                c.close();
//        }
//        return tables;
//    }

    private static void writeClientCsv(File backupFile, SQLiteDatabase db){
        CSVWriter csvWrite = null;
        Cursor curCSV = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(backupFile));
            curCSV = db.rawQuery("SELECT id [ID de cliente], firstName [Nombre], lastName [Apellido], dob [Fecha de nacimiento]," +
                    "gender [Sexo], occupation [Ocupacion], phone [Telefono], lastUpdated[Creado o modificado] " +
                    "FROM client",null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext()) {
                int columns = curCSV.getColumnCount();
                String[] columnArr = new String[columns];
                for( int i = 0; i < columns; i++){
                    columnArr[i] = curCSV.getString(i);
                }
                csvWrite.writeNext(columnArr);
            }
        }
        catch(Exception sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        }finally {
            if(csvWrite != null){
                try {
                    csvWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if( curCSV != null ){
                curCSV.close();
            }
        }
    }

    private static void writePaymentCsv(File backupFile, SQLiteDatabase db){
        CSVWriter csvWrite = null;
        Cursor curCSV = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(backupFile));
            curCSV = db.rawQuery("SELECT p.id [ID de pago], clientId [ID de cliente], firstName [Nombre], lastName [Apellido], product [Producto]," +
                    "amountUsd [Precio en USD], amountUsd*exchangeRate [Precio en C$], exchangeRate [Taza de Cambio C$/USD], currency [Moneda], " +
                    "substr(paidFrom,1,10) [Desde], substr(paidUntil,1,10) [Hasta], " +
                    "substr(paidFrom,12,5) [Desde hora], substr(paidUntil,12,5) [Hasta hora], comment [Comentario], " +
                    "timestamp [Fecha de pago], isValid [Validez del pago], extra [Informacion adicional] " +
                    "FROM payment p left join client c on p.clientId=c.id",null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext()) {
                int columns = curCSV.getColumnCount();
                String[] columnArr = new String[columns];
                for( int i = 0; i < columns; i++){
                    columnArr[i] = curCSV.getString(i);
                }
                csvWrite.writeNext(columnArr);
            }
        }
        catch(Exception sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        }finally {
            if(csvWrite != null){
                try {
                    csvWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if( curCSV != null ){
                curCSV.close();
            }
        }
    }

    private static void writeVisitCsv(File backupFile, SQLiteDatabase db){
        CSVWriter csvWrite = null;
        Cursor curCSV = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(backupFile));
            curCSV = db.rawQuery("SELECT v.id [ID de visita], clientId [ID de cliente], firstName [Nombre], " +
                    "lastName [Apellido], timestamp [Fecha de la visita], access [Codigo de acceso], " +
                    "case when access='G' then 'autorizado' else 'denegado' end as [Acceso] " +
                    "FROM visit v left join client c on v.clientId=c.id",null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext()) {
                int columns = curCSV.getColumnCount();
                String[] columnArr = new String[columns];
                for( int i = 0; i < columns; i++){
                    columnArr[i] = curCSV.getString(i);
                }
                csvWrite.writeNext(columnArr);
            }
        }
        catch(Exception sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        }finally {
            if(csvWrite != null){
                try {
                    csvWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if( curCSV != null ){
                curCSV.close();
            }
        }
    }

}
