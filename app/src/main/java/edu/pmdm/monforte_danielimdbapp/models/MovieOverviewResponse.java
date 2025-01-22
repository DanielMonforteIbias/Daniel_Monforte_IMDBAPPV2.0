package edu.pmdm.monforte_danielimdbapp.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.api.IMDBApiClient;
import edu.pmdm.monforte_danielimdbapp.api.IMDBApiService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieOverviewResponse {
    /**
     * Método que usa el endpoint get-overview para obtener la descripcion de una pelicula dado su id
     * @param id el id de la pelicula a buscar
     * @param service la interfaz que tiene el metodo a ejecutar al obtener la descripcion
     */
    public static void obtenerDescripcion(String id, IMDBApiService service){
        String apiKey= IMDBApiClient.getApiKey();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://imdb-com.p.rapidapi.com/title/get-overview?tconst="+id)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", "imdb-com.p.rapidapi.com")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Error en la solicitud de detalles: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string(); //Obtenemos el JSON de la API en un String
                        JSONObject jsonObject = new JSONObject(jsonResponse); //Obtenemos un JSONObject del String recibido
                        String description = jsonObject.getJSONObject("data").getJSONObject("title").getJSONObject("plot").getJSONObject("plotText").getString("plainText"); //Obtenemos el texto del plot navegando por los JSONObject
                        if(service!=null){
                            service.onDescriptionReceived(description); //Ejecutamos el metodo de onDescriptionReceived de la interfaz recibida como parametro cuando hayamos obtenido la descripcion
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else if(response.code()==429){
                    System.out.println("Limite de solicitudes alcanzado, cambiando la key");
                    IMDBApiClient.switchApiKey();
                    obtenerDescripcion(id, service);
                }
                else {
                    if(service!=null){
                        service.onDescriptionReceived("(No description found)");
                    }
                }
            }
        });
    }
}
