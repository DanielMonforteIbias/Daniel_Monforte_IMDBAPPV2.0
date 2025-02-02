package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityLoginBinding;
import edu.pmdm.monforte_danielimdbapp.models.User;
import edu.pmdm.monforte_danielimdbapp.sync.FavoritesSync;
import edu.pmdm.monforte_danielimdbapp.sync.UsersSync;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;

    private BeginSignInRequest signInRequest;
    private GoogleSignInOptions gOptions;
    private GoogleSignInClient gClient;
    private ActivityLoginBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private FavoritesDatabaseHelper dbHelper;

    private FavoritesSync favoritesSync;
    private UsersSync usersSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        dbHelper =new FavoritesDatabaseHelper(this);
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
        usersSync=new UsersSync(this);
        usersSync.syncUsersFromFirebase();
        favoritesSync=new FavoritesSync(this);
        favoritesSync.syncFavoritesFromFirebase();


        //Sincronizar con Firebase
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
                                                iniciarSesion("google");
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

        //Registro o Login con email y contraseña
        //Registrarse
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=binding.editTextEmail.getText().toString();
                String password=binding.editTextTextPassword.getText().toString();
                boolean registroValido=true;
                if(email.equals("")){
                    registroValido=false;
                    showToast("El correo no puede estar vacio");
                }
                else if (!correoValido(email)){
                    registroValido=false;
                    showToast("El correo no es valido");
                }
                if(password.equals("")){
                    registroValido=false;
                    showToast("La contraseña no puede estar vacia");
                }
                else if(password.length()<6){
                    registroValido=false;
                    showToast("La contraseña debe tener como minimo 6 caracteres");
                }
                if(registroValido){
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                showToast("Registro exitoso. Ya puede iniciar sesion");
                            }
                            else{
                                System.out.println(task.getException());
                                if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) showToast("Registro fallido, el correo no es valido");
                                else if(task.getException() instanceof FirebaseAuthUserCollisionException) showToast("Registro fallido, ya existe un usuario con ese correo.");
                                else showToast("Registro fallido");
                            }
                        }
                    });
                }
            }
        });

        //Login
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=binding.editTextEmail.getText().toString();
                String password=binding.editTextTextPassword.getText().toString();
                boolean loginValido=true;
                if(email.equals("")){
                    loginValido=false;
                    showToast("El correo no puede estar vacio");
                }
                else if (!correoValido(email)){
                    loginValido=false;
                    showToast("El correo no es valido");
                }
                if(password.equals("")){
                    loginValido=false;
                    showToast("La contraseña no puede estar vacia");
                }
                if(loginValido){
                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                iniciarSesion("password");
                            }
                            else{
                                System.out.println(task.getException());
                                if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) showToast("No se pudo iniciar sesion, credenciales incorrectas");
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Método que comprueba si una cadena es un correo válido o no
     * @param correo la cadena a comprobar
     * @return true si sigue el formato de correo válido, false si no
     */
    public boolean correoValido(String correo){
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches(); //Devolvemos el resultado de si el correo concuerda con el patrón o no
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
                            iniciarSesion("facebook");
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println("signInWithCredential:failure" + task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "Este correo ya se ha usado con otro proveedor", Toast.LENGTH_SHORT).show();
                                if (AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();
                            }
                            else Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    private void iniciarSesion(String provider){
        user = auth.getCurrentUser();
        if(!dbHelper.userExists(user.getUid())) { //Si el usuario no existe en la base de datos
            String image="";
            if(provider.equals("google"))image=user.getPhotoUrl().toString(); //Si el proveedor es Google obtenemos su imagen con getPhotoUrl
            dbHelper.addUser(new User(user.getUid(),user.getDisplayName(),user.getEmail(),image)); //Lo añadimos a la local
        }
        usersSync.userExistsInFirebase(user.getUid(), new OnCompleteListener<DocumentSnapshot>() { //Comprobamos que existe en Firebase
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) { //Si no existe
                        usersSync.addUserToFirebase(dbHelper.getUser(user.getUid())); //Lo añadimos a Firebase
                    }
                }
            }
        });
        dbHelper.updateUserLoginTime(user.getUid(),System.currentTimeMillis()); //Actualizamos la hora de login del usuario a ahora
        finish(); //Terminamos esta actividad
        Intent intent = new Intent(LoginActivity.this, MainActivity.class); //Creamos un Intent para ir a MainActivity
        startActivity(intent); //Iniciamos la actividad con el intent
    }

    private boolean checkLoginStatus() {
        FirebaseUser currentUser = auth.getCurrentUser(); //Obtenemos el usuario con sesión iniciada
        return currentUser != null; //Devuelve true si hay cuenta, es decir, si no es null, y false si es null
    }

    private void showToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
}