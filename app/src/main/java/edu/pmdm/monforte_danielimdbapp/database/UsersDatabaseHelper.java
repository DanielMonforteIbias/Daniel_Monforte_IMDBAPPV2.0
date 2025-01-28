package edu.pmdm.monforte_danielimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public void addUser(FirebaseUser user){
        values = new ContentValues();
        values.put("userId",user.getUid());
        values.put("name",user.getDisplayName());
        values.put("email",user.getEmail());
        values.put("loginTime", (String) null);
        values.put("logoutTime", (String) null);
        database.insert(USERS_TABLE_NAME,null,values);
    }

    public boolean userExists(String userId){
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + USERS_TABLE_NAME + " WHERE userId = ?", new String[]{userId});
        boolean exists = false; //Declaramos la variable existe y la inicializamos a false. Esta variable será la que devolvamos al final
        if(cursor.moveToFirst()) { //Si hay resultado en la consulta
            int count=cursor.getInt(0); //Obtenemos el primer dato que hay (solo habrá uno ya que es un count)
            if(count>0) exists=true; //Si el valor es mayor que 0, es que la película existe, así que ponemos la variable a true
        }
        cursor.close(); //Cerramos el cursor
        return exists; //Devolvemos si existe o no
    }

    public void updateUserLoginTime(String userId, long loginTime){
        values=new ContentValues();
        values.put("loginTime",getTimestamp(loginTime));
        String where="userId=?";
        database.update(USERS_TABLE_NAME,values,where,new String[]{userId});
    }

    public void updateUserLogoutTime(String userId, long logoutTime){
        values=new ContentValues();
        values.put("logoutTime",getTimestamp(logoutTime));
        String where="userId=?";
        database.update(USERS_TABLE_NAME,values,where,new String[]{userId});
    }

    public String getTimestamp(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        Date date = new Date(time);
        return format.format(date);
    }
}