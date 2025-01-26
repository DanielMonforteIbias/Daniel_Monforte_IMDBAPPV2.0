package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import edu.pmdm.monforte_danielimdbapp.database.UsersDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityLoginBinding;
import edu.pmdm.monforte_danielimdbapp.sync.FavoritesSync;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;

    private BeginSignInRequest signInRequest;
    private GoogleSignInOptions gOptions;
    private GoogleSignInClient gClient;
    private ActivityLoginBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private UsersDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        dbHelper=new UsersDatabaseHelper(this);
        if (checkLoginStatus()) { //Si ya hay sesion iniciada
            user=auth.getCurrentUser();
            dbHelper.updateUserLoginTime(user.getUid(),System.currentTimeMillis()); //Actualizamos la hora de login del usuario a ahora
            Intent intent = new Intent(this, MainActivity.class); //Abrimos un intent de MainActivity
            startActivity(intent);
            finish(); //Cerramos esta actividad
        }
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDetails), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Sincronizar con Firebase
        FavoritesSync sync=new FavoritesSync(this);
        sync.syncFavoritesToFirebase();


        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true).build()).build();
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        gClient = GoogleSignIn.getClient(this, gOptions);
        //OnClick del botón de Sign In With Google
        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = gClient.getSignInIntent(); //Creamos el intent para elegir cuenta de Google
                activityResultLauncher.launch(signInIntent); //Lanzamos el intent
            }
        });
        //Launcher del intent de elegir cuenta de Google
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) { //Se ejecutará al obtener resultado del intent
                if (result.getResultCode() == LoginActivity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String idToken = account.getIdToken();
                        if (idToken != null) {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                            auth.signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) { //Al completar la tarea
                                            if (task.isSuccessful()) { //Si ha sido exitosa
                                                user = auth.getCurrentUser();
                                                if(!dbHelper.userExists(user.getUid())) dbHelper.addUser(user); //Si el usuario no existe en la base de datos, lo añadimos
                                                dbHelper.updateUserLoginTime(user.getUid(),System.currentTimeMillis()); //Actualizamos la hora de login del usuario a ahora
                                                finish(); //Terminamos esta actividad
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class); //Creamos un Intent para ir a MainActivity
                                                startActivity(intent); //Iniciamos la actividad con el intent
                                            }
                                        }
                                    });
                        } else
                            Toast.makeText(getApplicationContext(), R.string.error_inicio_sesion, Toast.LENGTH_SHORT).show(); //Si el id de la cuenta es nulo, mostramos un Toast con error de inicio de sesión
                    } catch (ApiException e) {
                        Toast.makeText(getApplicationContext(), R.string.error_inicio_sesion_api, Toast.LENGTH_SHORT).show(); //Si hay una excepción de la API, mostramos un Toast con información del error
                    }
                }
            }
        });
        FacebookSdk.sdkInitialize(this);
        // Initialize Facebook Login button
        CallbackManager callbackManager = CallbackManager.Factory.create();
        binding.btnFacebook.setReadPermissions("email", "public_profile");
        binding.btnFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                System.out.println("facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                System.out.println("facebook:onError" + error);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        System.out.println("handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            System.out.println("signInWithCredential:success");
                            user = auth.getCurrentUser();
                            if(!dbHelper.userExists(user.getUid())) dbHelper.addUser(user); //Si el usuario no existe en la base de datos, lo añadimos
                            dbHelper.updateUserLoginTime(user.getUid(),System.currentTimeMillis()); //Actualizamos la hora de login del usuario a ahora
                            finish(); //Terminamos esta actividad
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class); //Creamos un Intent para ir a MainActivity
                            startActivity(intent); //Iniciamos la actividad con el intent
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println("signInWithCredential:failure" + task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "Este correo ya se ha usado con otro proveedor", Toast.LENGTH_SHORT).show();
                            }
                            else Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    private boolean checkLoginStatus() {
        FirebaseUser currentUser = auth.getCurrentUser(); //Obtenemos el usuario con sesión iniciada
        return currentUser != null; //Devuelve true si hay cuenta, es decir, si no es null, y false si es null
    }
}