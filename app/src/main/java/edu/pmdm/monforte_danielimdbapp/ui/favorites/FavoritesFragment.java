package edu.pmdm.monforte_danielimdbapp.ui.favorites;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.R;
import edu.pmdm.monforte_danielimdbapp.adapters.MovieAdapter;
import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.FragmentFavoritasBinding;
import edu.pmdm.monforte_danielimdbapp.models.Movie;

public class FavoritesFragment extends Fragment {
    private static final int BLUETOOTH=3; //Variable para los permisos de Bluetooth
    private FragmentFavoritasBinding binding; //Variable para el binding de este fragmento
    private static List<Movie> favoriteMovies=new ArrayList<Movie>(); //Lista de peliculas favoritas
    private static MovieAdapter adaptador; //Adaptador para el RecyclerView de peliculas
    private static FavoritesDatabaseHelper dbHelper; //Variable para usar los métodos de la base de datos

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper=new FavoritesDatabaseHelper(getContext()); //Inicializamos la variable de la base de datos con el contexto
        RecyclerView recyclerView = binding.recyclerViewFavoritas; //Obtenemos el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));//Le damos un LayoutManager lineal, tendrá solo 1 columna
        favoriteMovies=dbHelper.getUserFavorites(FirebaseAuth.getInstance().getCurrentUser().getUid());//Obtenemos la lista de peliculas con el metodo de FavoritesDatabaseHelper, pasando el id del usuario que hay logueado
        adaptador=new MovieAdapter(favoriteMovies,this); //Creamos el adaptador con la lista de peliculas y pasamos el fragmento tambien
        recyclerView.setAdapter(adaptador); //Ponemos el adaptador al RecyclerView
        //OnClick del botón de compartir
        binding.btnCompartirLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!comprobarPermisosBluetooth()){ //Si no tenemos permisos de bluetooth
                    pedirPermisosBluetooth(); //Los pedimos
                    Toast.makeText(getContext(),"Permisos de Bluetooth denegados!",Toast.LENGTH_SHORT).show(); //Informamos al usuario
                }
                else compartirListaFavoritos(); //Si tenemos permisos de bluetooth, compartimos la lista de favoritos
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Método que muestra un diálogo en pantalla simulando que se comparte por bluetooth la lista de favoritos
     * El diálogo tendrá un String en formato JSON con la información de las películas de la lista
     */
    private void compartirListaFavoritos(){
        if(favoriteMovies.size()==0) Toast.makeText(getContext(), R.string.lista_favoritos_vacia,Toast.LENGTH_SHORT).show(); ////Si la lista está vacía, avisamos al usuario
        else{ //Si la lista tiene contenido, mostraremos un dialogo con la lista en formato JSON
            AlertDialog.Builder dialogo = new AlertDialog.Builder(getContext()); //Inicializamos el dialogo
            dialogo.setCancelable(false); //Establecemos que no es cancelable para que no se pueda cerrar al pulsar en otro lado
            dialogo.setTitle("Películas favoritas en JSON"); //Ponemos el título
            dialogo.setMessage(favoriteMovies.toString()); //Establecemos el mensaje del diálogo
            dialogo.setPositiveButton("OK", new DialogInterface.OnClickListener() { //Ponemos un botón para cerrarlo
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            dialogo.show(); //Mostramos el diálogo
        }
    }

    /**
     * Método que pide los permisos CONNECT de Bluetooth, o intenta pedir los permisos Bluetooth si se está por debajo de la API 31
     * Es importante recalcar que estos permisos solo se piden a partir de la API 31, de la 30 para abajo se usan otros permisos
     * que no requieren ser pedidos en tiempo de ejecución
     */
    private void pedirPermisosBluetooth(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN}, BLUETOOTH);
        }
    }

    /**
     * Método que comprueba si tenemos permisos de Bluetooth Connect si estamos en la API 31 o superior, o permisos Bluetooth si estamos por debajo
     * @return true si tenemos, false si no
     */
    private boolean comprobarPermisosBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
        }
    }
}