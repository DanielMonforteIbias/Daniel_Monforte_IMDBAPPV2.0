package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Esta actividad es la que se invoca al iniciar la app, y decide qué verá primero el usuario en base a si hay sesión iniciada o no
 */
public class SplashActivity extends AppCompatActivity {
    boolean sesionIniciada=false; //Variable para saber si hay sesión iniciada o no
    private GoogleSignIn GoogleSignIn;
    private Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sesionIniciada= checkLoginStatus(); //COmprobamos si hay sesion iniciada
        if (sesionIniciada) { //Si hay sesion iniciada, iremos a la pantalla principal
            intent = new Intent(this, MainActivity.class);
        } else { //Si no, iremos a la pantalla de Login
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent); //Iniciamos la actividad con el intent que hemos hecho
        finish(); //Terminamos esta actividad
    }

    /**
     * Método que comprueba si hay sesión iniciada o no
     * @return true si hay una cuenta con sesión iniciada, false si no
     */
    private boolean checkLoginStatus() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this); //Obtenemos la cuenta con sesión iniciada
        return account != null; //Devuelve true si hay cuenta, es decir, si no es null, y false si es null
    }
}