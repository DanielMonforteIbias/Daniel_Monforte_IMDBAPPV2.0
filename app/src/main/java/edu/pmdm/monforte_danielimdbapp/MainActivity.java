package edu.pmdm.monforte_danielimdbapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityMainBinding;
import edu.pmdm.monforte_danielimdbapp.models.User;
import edu.pmdm.monforte_danielimdbapp.sync.UsersSync;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding; //Variable para el binding de esta actividad
    //Variables para las vistas del header que queremos modificar
    private ImageView imgFoto;
    private TextView txtNombre;
    private TextView txtEmail;
    private Button btnLogout;

    private FirebaseUser firebaseUser;
    private FavoritesDatabaseHelper dbHelper;
    private UsersSync usersSync;
    private String providerId;

    private GoogleSignInClient gClient;
    private GoogleSignInOptions gOptions;

    private ActivityResultLauncher<Intent> editUserActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_top10, R.id.nav_favorites, R.id.nav_slideshow).setOpenableLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        dbHelper = new FavoritesDatabaseHelper(this);
        usersSync = new UsersSync(this);
        //Obtenemos algunos componentes del headerView para modificarlos
        txtNombre = headerView.findViewById(R.id.txtNombre);
        txtEmail = headerView.findViewById(R.id.txtEmail);
        btnLogout = headerView.findViewById(R.id.btnLogout);
        imgFoto = headerView.findViewById(R.id.imgViewFoto);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        providerId = firebaseUser.getProviderData().get(1).getProviderId();
        if (providerId.equals("google.com")) { //Si el proveedor es Google
            gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            gClient = GoogleSignIn.getClient(this, gOptions);
        }

        updateUserUI();

        //OnClick del botón para cerrar sesión
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.updateUserLogoutTime(firebaseUser.getUid(), System.currentTimeMillis());
                usersSync.addActivityLogToUser(firebaseUser.getUid());
                FirebaseAuth.getInstance().signOut();
                switch (providerId) { //Comprobamos el proveedor con el que habia sesion iniciada usando su id para cerrarla
                    case "google.com": //Si era Google
                        gClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() { //Cerramos sesión y añadimos el listener OnComplete
                            @Override
                            public void onComplete(@NonNull Task<Void> task) { //Cuando se complete la tarea de cerrar sesión
                                System.out.println("Cerraste sesión de Google");
                            }
                        });
                        break;
                    case "facebook.com": //Si era Facebook
                        if (AccessToken.getCurrentAccessToken() != null) {
                            LoginManager.getInstance().logOut();
                            System.out.println("Cerraste sesión de Facebook");
                        }
                        break;
                    case "password":
                        //No se hace nada, es necesario contemplarlo para que no salte el default
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Error en el ID del proveedor al cerrar sesion", Toast.LENGTH_SHORT).show();
                        break;
                }
                finish(); //Terminamos esta actividad
                startActivity(new Intent(MainActivity.this, LoginActivity.class)); //Abrimos la actividad de LoginActivity
            }
        });

        editUserActivityLauncher = registerForActivityResult( //Cuando termine la actividad de editar usuario
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            updateUserUI(); //Actualizamos los datos del usuario
                        }
                    }
                });
    }

    /**
     * Método que crea las opciones del menú, inflando el layout con los items
     *
     * @param menu The options menu in which you place your items
     * @return true para que el menú se muestre
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Método que controla las opciones del menú de la ActionBar
     *
     * @param item The menu item that was selected
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); //Obtenemos el id del item pulsado
        if (id == R.id.action_credits) { //Si el item pulsado es el de creditos
            mostrarCreditos(); //Mostramos el dialogo de creditos
        } else if (id == R.id.action_edit_user) {
            Intent intent = new Intent(getApplicationContext(), EditUserActivity.class);
            intent.putExtra("userId", firebaseUser.getUid());
            editUserActivityLauncher.launch(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Método que muestra un diálogo en pantalla con los créditos de la app
     */
    public void mostrarCreditos() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this); //Inicializamos el dialogo
        dialogo.setCancelable(false); //Establecemos que no es cancelable para que no se pueda cerrar al pulsar en otro lado
        dialogo.setTitle("Créditos"); //Ponemos el título como "Créditos"
        String instruccionesMensaje = "Aplicación hecha por Daniel Monforte Ibias\n\nDAM2 2024/25"; //Creamos el mensaje
        dialogo.setMessage(instruccionesMensaje); //Establecemos el mensaje del diálogo
        dialogo.setPositiveButton("OK", new DialogInterface.OnClickListener() { //Ponemos un botón para cerrarlo
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        dialogo.show(); //Mostramos el diálogo
    }

    public void updateUserUI(){
        //Dependiendo del proveedor obtendremos los datos de una u otra forma
        User user = dbHelper.getUser(firebaseUser.getUid());
        txtNombre.setText(user.getName());
        if (providerId.equals("facebook.com")) {
            txtEmail.setText("Conectado con Facebook");
            if (user.getImage() == null || user.getImage().isEmpty()){ //Ponemos la foto de Facebook si el usuario no tiene foto en local
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken != null) {
                    GraphRequest request = GraphRequest.newMeRequest(accessToken, (object, response) -> {
                        try {
                            String photoUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                            dbHelper.updateUserImage(firebaseUser.getUid(), photoUrl);
                            user.setImage(photoUrl);
                            usersSync.updateUserInFirebase(user);
                            Glide.with(this).load(photoUrl).placeholder(R.drawable.usuario).into(imgFoto); //Actualizamos la foto de la interfaz
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,picture.type(large)");
                    request.setParameters(parameters);
                    request.executeAsync();
                }
            }
        } else txtEmail.setText(user.getEmail());
        String image=user.getImage();
        if (image.startsWith("http://") || image.startsWith("https://") || image.startsWith("content://") || image.startsWith("file://")){ //Si la imagen es de una URL o una URI, usamos Glide
            Glide.with(this).load(image).placeholder(R.drawable.usuario).into(imgFoto); //Usamos Glide para poner la foto del usuario en el ImageView. Si ocurriese algun problema y fuese null, se pondría la foto del placeholder
        }
        else {//Si no, es un String en Base64 (viene de la camara), asi que haremos un bitmap y despues usaremos Glide
            byte[] decodedString = Base64.decode(user.getImage(), Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(this).load(bitmap).placeholder(R.drawable.usuario).into(imgFoto);
        }
    }
}