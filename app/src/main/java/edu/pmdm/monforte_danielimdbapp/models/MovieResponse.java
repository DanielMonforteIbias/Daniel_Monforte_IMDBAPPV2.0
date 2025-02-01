package edu.pmdm.monforte_danielimdbapp.models;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.R;
import edu.pmdm.monforte_danielimdbapp.api.IMDBApiClient;
import edu.pmdm.monforte_danielimdbapp.api.IMDBApiService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieResponse {
    /**
     * Método que usa el endpoint get-top-meter de IMDBApi para obtener una lista de peliculas y series mas populares
     * @param service la interfaz que tiene el metodo a ejecutar tras obtener la lista
     * @param context el contexto desde donde se llamó, por si hay que mostrar un Toast
     */
    public static void buscarTop10(IMDBApiService service, Context context){

        String apiKey= IMDBApiClient.getApiKey();
        OkHttpClient client = new OkHttpClient();
        //La peticion tiene la condicion limit=10 para obtener el top 10, se podria cambiar este numero para obtener mas o menos. Tambien podemos cambiar ALL para obtener solo series o solo peliculas
        Request request = new Request.Builder()
                .url("https://imdb-com.p.rapidapi.com/title/get-top-meter?topMeterTitlesType=ALL&limit=10")
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", "imdb-com.p.rapidapi.com")
                .build();
        //No usamos execute porque no se puede ejecutar en el hilo principal
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { //Si la request falla (por ejemplo porque no tenemos conexion a internet)
                if(context!=null) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, R.string.error_solicitud_api, Toast.LENGTH_SHORT).show()); //Mostramos un Toast con informacion del error. Se usa Handler para que se haga en el hilo principal
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException { //Al recibir una respuesta de la API
                if (response.isSuccessful()) { //Si la respuesta ha sido exitosa
                    String datos = response.body().string(); //Obtenemos el JSON en un String
                    List<Movie>movies= JSONExtractor.extractMovies(datos); //Extraemos los datos del JSON recibido con el metodo de MovieExtractor
                    if(service!=null){
                        service.onMoviesReceived(movies); //Ejecutamos el metodo que procesa la lista de peliculas creada a partir de los datos del JSON
                    }
                } else if(response.code()==429){ //429 es el codigo de error cuando una API no tiene calls
                    System.out.println("Limite de solicitudes alcanzado, cambiando la key");
                    IMDBApiClient.switchApiKey(); //Cambiamos la key
                    buscarTop10(service,context); //Volvemos a llamar al metodo con los mismos parametros que recibió una vez cambiada la key
                }
                else { //Si la respuesta no ha sido exitosa
                    if(context!=null) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, R.string.error_respuesta_api, Toast.LENGTH_SHORT).show()); //Mostramos un Toast con informacion del error. Se usa Handler para que se haga en el hilo principal
                    System.out.println("Error de la API: "+response.message()+" "+response.code()+" "+response.body().toString().toString());
                }
            }
        });
    }
}
