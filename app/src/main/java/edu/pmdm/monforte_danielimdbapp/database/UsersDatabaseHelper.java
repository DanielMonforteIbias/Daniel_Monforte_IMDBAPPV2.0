package edu.pmdm.monforte_danielimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class UsersDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="users.db"; //Nombre de la base de datos
    private SQLiteDatabase database; //Variable para operar en la base de datos

    private final String USERS_TABLE_NAME="USERS";
    private static int databaseVersion=1; //Version actual de la base de datos

    private ContentValues values; //Variable usada para el contenido de las inserciones
    public UsersDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, databaseVersion);
        database=getWritableDatabase(); //Creamos o abrimos la base de datos para leer y escribir
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Query para crear la tabla de users
        String createTableUsers="CREATE TABLE "+USERS_TABLE_NAME+" (userId TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL, loginTime TIMESTAMP, logoutTime TIMESTAMP)";
        db.execSQL(createTableUsers);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
