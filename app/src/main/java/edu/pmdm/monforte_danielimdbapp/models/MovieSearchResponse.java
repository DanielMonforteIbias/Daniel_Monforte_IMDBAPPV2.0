package edu.pmdm.monforte_danielimdbapp.models;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.R;
import edu.pmdm.monforte_danielimdbapp.api.IMDBApiService;
import edu.pmdm.monforte_danielimdbapp.api.TMDBApiService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieSearchResponse {
    /**
     * Método que usa el endpoint Movie List de genres para obtener una lista de generos de peliculas de la API TMDB
     * @param service la interfaz que tiene el metodo a ejecutar tras obtener la lista de generos
     * @param context el contexto desde donde se llamó, por si hay que mostrar un Toast
     */
    public static void buscarGeneros(TMDBApiService service, Context context){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/genre/movie/list?language=en")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI4YmUwNWUzZDU4MWE1NzFjNzYzNzIwMTEyMDc3YjY5MyIsIm5iZiI6MTczNTkzMjgyOS42NTIsInN1YiI6IjY3NzgzYjlkMzIxYTNhMTY2YTc0YTY4NSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.kYScL-FHJi2LtErS_x8xtXX-ccD8gmhnRmF4ialnF9w")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { //Si la request falla (por ejemplo porque no tenemos conexion a internet)
                if(context!=null) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, R.string.error_solicitud_api, Toast.LENGTH_SHORT).show()); //Mostramos un Toast con mensaje de error de solicitud
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException { //En la respuesta de la API
                if (response.isSuccessful()) { //Si la respuesta ha sido exitosa
                    String datos = response.body().string(); //Obtenemos el JSON en un String
                    List<Genre>genres=JSONExtractor.extractGenres(datos); //Usamos el metodo que extrae los nombres de los generos de la respuesta recibida
                    if(service!=null){
                        service.onGenresReceived(genres); //Ejecutamos el metodo que procesa la lista de generos creada a partir de los datos del JSON
                    }
                } else { //Si no ha sido exitosa
                    if(context!=null) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, R.string.error_respuesta_api, Toast.LENGTH_SHORT).show()); //Mostramos un Toast con mensaje de error de respuesta
                }
            }
        });
    }

    /**
     * Método que usa el endpoint Discover -> Movie de la API TMDB para obtener una lista de películas filtradas por año y género
     * @param year el año de la película
     * @param genre el género de la película
     * @param service la interfaz que tiene el método a ejecutar tras obtener la respuesta de la API
     * @param context el contexto donde mostrar un Toast si ocurre algun error
     */
    public static void buscarPeliculasPorAñoYGenero(String year, String genre, TMDBApiService service,Context context){
        OkHttpClient client = new OkHttpClient();
        //El endpoint usado permite obtener un listado de peliculas aplicando gran variedad de filtros. En este caso se ha usado el año (primary_release_year) y el género (with_genres). Buscamos por un género, aunque una película puede tener varios géneros. Se ha concatenado los parámetros recibidos en el método con la URL que da la API
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&primary_release_year="+year+"&sort_by=popularity.desc&with_genres="+genre)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI4YmUwNWUzZDU4MWE1NzFjNzYzNzIwMTEyMDc3YjY5MyIsIm5iZiI6MTczNTkzMjgyOS42NTIsInN1YiI6IjY3NzgzYjlkMzIxYTNhMTY2YTc0YTY4NSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.kYScL-FHJi2LtErS_x8xtXX-ccD8gmhnRmF4ialnF9w")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { //Si la request falla (por ejemplo porque no tenemos conexion a internet)
                if(context!=null) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, R.string.error_solicitud_api, Toast.LENGTH_SHORT).show()); //Mostramos un Toast con mensaje de error de solicitud
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //Si la respuesta es exitosa
                    String datos = response.body().string(); //Obtenemos el JSON en un String
                    List<Movie>movies=JSONExtractor.extractMoviesByYearAndGenre(datos); //Obtenemos una lista de peliculas extrayendolas del String en formato JSON que hemos conseguido, usando el método de la clase JSONExtractor
                    if(service!=null){
                        service.onMoviesReceived(movies); //Ejecutamos el método de la interfaz que procesa las películas una vez se han recibido
                    }
                } else { //Si no ha sido exitosa
                    if(context!=null) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, R.string.error_respuesta_api, Toast.LENGTH_SHORT).show()); //Mostramos un Toast con mensaje de error de respuesta
                }
            }
        });
    }
}
