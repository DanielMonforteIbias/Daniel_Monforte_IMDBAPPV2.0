package edu.pmdm.monforte_danielimdbapp.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase cuenta con métodos para tratar JSON recibidos de APIs, leerlos y pasar su información a objetos
 * Los métodos deben ser específicos para cada llamada y respuesta de la API, ya que trabajan en base a la estructura
 * del JSON y las claves que tenga, y esto varía en base a la API y endpoints usados
 */
public class JSONExtractor {
    /**
     * Método que recibe un String en formato JSON con peliculas/series y lee los datos para pasarlos a objetos
     * @param jsonResponse el String en formato JSON del que sacar los datos
     * @return lista de peliculas extraidas del JSON recibido
     */
    public static List<Movie> extractMovies(String jsonResponse) {
        List<Movie> movieList = new ArrayList<>(); //Declaramos e inicializamos la lista de peliculas
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse); //Obtenemos un JSONObject del String en formato JSON recibido
            //El endpoint usado devuelve un JSON que tiene varias peliculas en un array llamado edges
            JSONArray edges = jsonObject.getJSONObject("data").getJSONObject("topMeterTitles").getJSONArray("edges"); //Navegamos por objetos y arrays hasta llegar al que contiene las peliculas
            for (int i = 0; i < edges.length(); i++) { //Recorremos el JSONArray que contiene las peliculas
                JSONObject node = edges.getJSONObject(i).getJSONObject("node"); //Cada JSONObject que contiene una pelicula se identifica con la clave node
                //Sacamos los datos que nos interesan por sus claves
                String id = node.getString("id");
                String title = node.getJSONObject("titleText").getString("text");
                String fecha = node.getJSONObject("releaseDate").getInt("year")+"-"+node.getJSONObject("releaseDate").getInt("month")+"-"+node.getJSONObject("releaseDate").getInt("day");
                String imageUrl = node.getJSONObject("primaryImage").getString("url");
                double rating = node.getJSONObject("meterRanking").getDouble("currentRank");
                Movie movie = new Movie(id,title, imageUrl, fecha,rating); //Construimos una pelicula con los datos obtenidos
                movieList.add(movie); //Añadimos la pelicula a la lista
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieList; //Devolvemos la lista de películas
    }

    /**
     * Método que recibe un String en formato JSON con géneros y lee los datos para obtener una lista con sus nombres
     * @param jsonResponse el String en formato JSON donde están los datos
     * @return lista de String con los nombres de los generos
     */
    public static List<Genre> extractGenres(String jsonResponse){
        List<Genre> genresList = new ArrayList<>(); //Declaramos e inicializamos la lista de géneros, con objetos de tipo Genre
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse); //Obtenemos un JSONObject del String en formato JSON recibido
            //El endpoint usado devuelve un JSON que tiene varios generos en un array llamado genres
            JSONArray genres = jsonObject.getJSONArray("genres"); //Obtenemos el JSONArray de géneros por su clave
            for (int i = 0; i < genres.length(); i++) { //Recorremos el JSONArray de generos
                JSONObject genre = genres.getJSONObject(i); //Obtenemos el JSONObject que contiene un género por su posición
                String genreId=genre.getString("id"); //Sacamos el id del género por su clave
                String genreName=genre.getString("name");//Sacamos el nombre del genero por su clave
                genresList.add(new Genre(genreId,genreName)); //Añadimos el genero a la lista
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genresList; //Devolvemos la lista de géneros
    }

    /**
     * Método que recibe un String en formato JSON con películas y lee los datos para obtener una lista con sus nombres
     * El JSON viene de la API TMDB
     * @param jsonResponse el String en formato JSON donde están los datos
     * @return lista de Movie con las películas
     */
    public static List<Movie> extractMoviesByYearAndGenre(String jsonResponse){
        List<Movie> moviesList = new ArrayList<>(); //Declaramos e inicializamos la lista de películas
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse); //Hacemos un JSONObject a partir del String recibido
            //El endpoint usado devuelve un JSON que tiene varios generos en un array llamado results
            JSONArray movies = jsonObject.getJSONArray("results");
            for (int i = 0; i < movies.length(); i++) { //Recorremos el JSONArray de peliculas
                JSONObject movieJson = movies.getJSONObject(i); //Obtenemos cada pelicula por su posicion
                //Sacamos los datos de la pelicula por su clave en el JSON
                String movieId=movieJson.getString("id");
                String movieTitle=movieJson.getString("original_title");
                String movieImage="https://image.tmdb.org/t/p/w500"+movieJson.getString("poster_path"); //La API solo da la ruta relativa, asi que lo concatenamos con el resto de la URL para que sea correcta
                String movieReleaseDate=movieJson.getString("release_date");
                double movieRating=movieJson.getDouble("vote_average");
                moviesList.add(new Movie(movieId,movieTitle,movieImage,movieReleaseDate,movieRating)); //Creamos un objeto movie con los datos leidos y lo añadimos a la lista
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moviesList; //Devolvemos la lista de peliculas
    }
}
