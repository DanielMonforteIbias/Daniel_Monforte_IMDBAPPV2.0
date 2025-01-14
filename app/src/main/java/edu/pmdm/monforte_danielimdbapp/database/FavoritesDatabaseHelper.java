package edu.pmdm.monforte_danielimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.models.Movie;

public class FavoritesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="favorites.db"; //Nombre de la base de datos
    private SQLiteDatabase database; //Variable para operar en la base de datos
    private final String MOVIES_TABLE_NAME="MOVIES"; //Nombre de la tabla de peliculas
    private final String FAVORITES_TABLE_NAME="FAVORITES"; //Nombre de la tabla de favoritos
    //Version 1: MOVIES(id,title,image) FAVORITES(userId,movieId)
    //Version 2: Añade la columna insertionTime a FAVORITES
    //Version 3: Añade la columna movieDate a MOVIES
    //Version 4: Añade la columna movieRating a MOVIES
    private static int databaseVersion=4; //Version actual de la base de datos

    private ContentValues values; //Variable usada para el contenido de las inserciones

    public FavoritesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null,databaseVersion); //Pasamos el nombre y la version
        database=getWritableDatabase(); //Creamos o abrimos la base de datos para leer y escribir
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Query para crear la tabla de peliculas
        String createTableMovies="CREATE TABLE "+MOVIES_TABLE_NAME+" (movieId TEXT PRIMARY KEY, movieTitle TEXT NOT NULL, movieImage TEXT, movieDate TEXT, movieRating REAL)";
        db.execSQL(createTableMovies);
        //Query para crear la tabla de favoritos
        String createTableFavorites="CREATE TABLE "+FAVORITES_TABLE_NAME+" (userId TEXT NOT NULL, movieId TEXT NOT NULL, insertionTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(userId, movieId), FOREIGN KEY(movieId) REFERENCES MOVIES(movieId))";
        db.execSQL(createTableFavorites);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE " + FAVORITES_TABLE_NAME + " ADD COLUMN insertionTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); //Si antes estabamos debajo de la version 2, añadimos la columna insertionTime a FAVORITES
        if (oldVersion < 3) db.execSQL("ALTER TABLE " + MOVIES_TABLE_NAME + " ADD COLUMN movieDate TEXT DEFAULT ''"); //Si antes estabamos debajo de la version 3, añadimos la columna movieDate a MOVIES
        if(oldVersion<4)db.execSQL("ALTER TABLE " + MOVIES_TABLE_NAME + " ADD COLUMN movieRating REAL DEFAULT 0"); //Si antes estabamos debajo de la version 4, añadimos la columna movieRating a MOVIES
    }

    /**
     * Método que añade una película recibida a la table MOVIES
     * @param movie la película a añadir
     */
    public void addMovie(Movie movie){
        values = new ContentValues(); //Inicializamos la variable values para guardar en ella a continuación los valores a insertar
        //Ponemos los datos a insertar, usando como key el nombre de la columna
        values.put("movieId", movie.getId());
        values.put("movieTitle", movie.getTitulo());
        values.put("movieImage", movie.getPortada());
        values.put("movieDate", movie.getFecha());
        values.put("movieRating",movie.getRating());
        database.insert(MOVIES_TABLE_NAME,null,values); //Insertamos en la tabla MOVIES los valores que hemos definido
    }

    /**
     * Método que borra una película de la tabla MOVIES
     * @param movieId id de la película a borrar
     */
    public void removeMovie(String movieId){
        String condition = "movieId = ?"; //Condicion para el borrado
        String conditionArgs[] = {movieId}; //Ponemos los parámetros recibidos en los ? de la condicion anterior
        database.delete(MOVIES_TABLE_NAME, condition,conditionArgs);
    }

    /**
     * Método que añade un registro a la tabla FAVORITES que relaciona una película con un usuario para establecer esa película como favorita de ese usuario
     * @param userId el id del usuario
     * @param movieId el id de la película
     */
    public void addFavorite(String userId, String movieId){
        values = new ContentValues(); //Inicializamos la variable values para guardar en ella a continuación los valores a insertar
        //Ponemos los ids, usando como key los nombres de las columnas en la tabla
        values.put("userId", userId);
        values.put("movieId", movieId);
        database.insert(FAVORITES_TABLE_NAME,null,values); //Insertamos el registro en FAVORITES
    }

    /**
     * Método que elimina un registro de FAVORITES, haciendo que una pelicula deje de ser favorita de un usuario
     * @param userId el id del usuario
     * @param movieId el id de la película
     */
    public void removeFavorite(String userId, String movieId){
        String condition = "userId = ? AND movieId = ?"; //Condicion para el borrado
        String conditionArgs[] = { userId, movieId }; //Ponemos los parámetros recibidos en los ? de la condicion anterior
        database.delete(FAVORITES_TABLE_NAME, condition,conditionArgs);
    }

    /*
    Todos los métodos anteriores relacionados con inserciones antes usaban el método execSQL
    Por ejemplo: database.execSQL("INSERT INTO "+MOVIES_TABLE_NAME+" VALUES('"+movie.getId()+"','"+movie.getTitulo()+"','"+movie.getPortada()+"','"+movie.getFecha()+"')");
    Esto, además de ser menos intuitivo, tenía un problema: si el título de una película tenía el símbolo ', el programa pensaría que la cadena termina ahí y se rompería la sintaxis de SQL
    Usando ContentValues y sus métodos para insertar y borrar evitamos esto
     */

    /**
     * Método que comprueba si una película recibida existe en la table MOVIES o no
     * @param movieId el id de la película a comprobar
     * @return true si ya existe, false si no existe
     */
    public boolean movieExists(String movieId){
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + MOVIES_TABLE_NAME + " WHERE movieId = ?", new String[]{movieId}); //Creamos una query con un contador y ponemos el id de la película en el where
        boolean exists = false; //Declaramos la variable existe y la inicializamos a false. Esta variable será la que devolvamos al final
        if(cursor.moveToFirst()) { //Si hay resultado en la consulta
            int count=cursor.getInt(0); //Obtenemos el primer dato que hay (solo habrá uno ya que es un count)
            if(count>0) exists=true; //Si el valor es mayor que 0, es que la película existe, así que ponemos la variable a true
        }
        cursor.close(); //Cerramos el cursor
        return exists; //Devolvemos si existe o no
    }

    /**
     * Método que comprueba si una película recibida es favorita de un usuario recibido
     * @param userId //El id del usuario a comprobar
     * @param movieId //El id de la película a comprobar
     * @return true si la película es favorita del usuario, false si no
     */
    public boolean movieIsFavorite(String userId, String movieId){
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + FAVORITES_TABLE_NAME + " WHERE userId = ? AND movieId=?", new String[]{userId,movieId}); //Creamos una query con un contador y ponemos los id de la pelicula y el usuario en la condicion
        boolean isFavorite = false; //Declaramos la variable isFavorite y la inicializamos a false. Esta variable será la que devolvamos al final
        if(cursor.moveToFirst()) { //Si hay resultado en la consulta
            int count=cursor.getInt(0); //Obtenemos el primer dato que hay (solo habrá uno ya que es un count)
            if(count>0)isFavorite=true; //Si el valor es mayor que 0, es que hay un registro que une al usuario con la pelicula, por lo que es favorita y ponemos la variable a true
        }
        cursor.close(); //Cerramos el cursor
        return isFavorite; //Devolvemos si es favorita o no
    }

    /**
     * Método que comprueba si una película recibida es favorita de algún usuario
     * @param movieId //El id de la película a comprobar
     * @return true si la película es favorita de un usuario, false si no
     */
    public boolean movieExistsInFavorite(String movieId){
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + FAVORITES_TABLE_NAME + " WHERE movieId=?", new String[]{movieId}); //Creamos una query con un contador y ponemos el id de la película en la condicion
        boolean isFavorite = false; //Declaramos la variable isFavorite y la inicializamos a false. Esta variable será la que devolvamos al final
        if(cursor.moveToFirst()) { //Si hay resultado en la consulta
            int count=cursor.getInt(0); //Obtenemos el primer dato que hay (solo habrá uno ya que es un count)
            if(count>0)isFavorite=true; //Si el valor es mayor que 0, es que hay un registro que une al usuario con la pelicula, por lo que es favorita y ponemos la variable a true
        }
        cursor.close(); //Cerramos el cursor
        return isFavorite; //Devolvemos si es favorita o no
    }

    /**
     * Método que obtiene una lista de películas que sean las favoritas de un usuario recibido
     * @param userId el id del usuario del que se quiere obtener la lista de favoritas
     * @return la lista de peliculas favoritas del usuario, una lista con objetos de tipo Movie
     */
    public List<Movie> getUserFavorites(String userId) {
        List<Movie> favorites = new ArrayList<>(); //Creamos e inicializamos la lista que devolveremos
        //Creamos una consulta que obtiene los datos de una película haciendo join por el id de la película con FAVORITES, usando el id de usuario recibido en el where y ordenandola por tiempo de insercion (las que se hayan añadido antes irán primero)
        Cursor cursor = database.rawQuery("SELECT M.movieId, M.movieTitle, M.movieImage, M.movieDate, M.movieRating FROM " +MOVIES_TABLE_NAME+" M JOIN "+FAVORITES_TABLE_NAME+" F ON M.movieId=F.movieId WHERE userID LIKE ? ORDER BY F.insertionTime ASC",new String[]{userId});
        while (cursor.moveToNext()) { //Recorremos el cursor
            //Obtenemos los datos de la película por el índice de columna
            String movieId = cursor.getString(0);
            String movieTitle = cursor.getString(1);
            String movieImage = cursor.getString(2);
            String movieDate = cursor.getString(3);
            double movieRating=cursor.getDouble(4);
            favorites.add(new Movie(movieId, movieTitle,movieImage,movieDate,movieRating)); //Añadimos una nueva película con los datos obtenidos a la lista de favoritos
        }
        cursor.close(); //Cerramos el cursor
        return favorites; //Devolvemos la lista
    }
}
