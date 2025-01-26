package edu.pmdm.monforte_danielimdbapp.api;

import java.util.ArrayList;

public class RapidApiKeyManager {
    private ArrayList<String>apiKeys=new ArrayList<String>();
    private int currentKeyIndex=0;

    public RapidApiKeyManager(){
        //Añadimos todas las keys que tenemos
        apiKeys.add("f94b3a9b75mshf98573499366620p15aecejsndd002043f0ce");
        apiKeys.add("db11b4b2d1mshf70e3a1da81bda4p1d070djsna023899c32b8");
        apiKeys.add("8bd8c55e73msh0e1794c9dba568cp141c78jsnd5d9db2ae48a");
    }

    public String getCurrentKey(){
        return apiKeys.get(currentKeyIndex); //Devolvemos la key que haya en la posicion actual
    }

    public void switchToNextKey(){
        if(apiKeys.size()>currentKeyIndex+1){ //Si aun hay keys
            currentKeyIndex++; //Aumentamos en 1 el indice de la key actual
        }
    }
}
