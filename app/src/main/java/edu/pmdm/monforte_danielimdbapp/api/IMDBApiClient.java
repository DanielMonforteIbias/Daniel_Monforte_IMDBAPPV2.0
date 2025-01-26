package edu.pmdm.monforte_danielimdbapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IMDBApiClient {
    private static RapidApiKeyManager apiKeyManager=new RapidApiKeyManager();

    public static String getApiKey(){
        return apiKeyManager.getCurrentKey();
    }
    public static void switchApiKey(){
        apiKeyManager.switchToNextKey();
    }
}
