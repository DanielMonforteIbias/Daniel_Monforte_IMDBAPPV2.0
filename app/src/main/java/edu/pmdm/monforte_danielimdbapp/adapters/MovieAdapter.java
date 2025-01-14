package edu.pmdm.monforte_danielimdbapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.util.List;

import edu.pmdm.monforte_danielimdbapp.MovieDetailsActivity;
import edu.pmdm.monforte_danielimdbapp.R;
import edu.pmdm.monforte_danielimdbapp.api.IMDBApiService;
import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.models.Movie;
import edu.pmdm.monforte_danielimdbapp.models.MovieOverviewResponse;
import edu.pmdm.monforte_danielimdbapp.ui.favorites.FavoritesFragment;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{
    private List<Movie> movies; //Lista de peliculas del adaptador
    private Fragment fragment; //Fragmento donde se usa el adaptador (null si no es un fragmento)
    private FavoritesDatabaseHelper dbHelper;

    public MovieAdapter(List<Movie> movies, Fragment fragment) {
        this.movies = movies;
        this.fragment=fragment;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView portada; //Cada item se representa por una imagen de su portada
        public ViewHolder(View itemView) {
            super(itemView);
            portada = itemView.findViewById(R.id.imgPortada);
        }

        public ImageView getPortada() {
            return portada;
        }
    }
    @NonNull
    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.ViewHolder holder, int position) {
        Movie pelicula=movies.get(position); //Obtenemos la pelicula de la lista en base a la posicion
        Context context=holder.itemView.getContext(); //Obtenemos el contexto del holder
        dbHelper=new FavoritesDatabaseHelper(context); //Inicializamos el helper de la base de datos con el contexto obtenido
        Glide.with(context).load(pelicula.getPortada()).into(holder.portada); //Usamos Glide para convertir el String de la portada a imagen
        //Al pulsar en una pelicula, abriremos una actividad de sus detalles
        //En lugar de cargar todas las descripciones, cargamos la descripcion aqui solo para la pelicula en la que hagamos click, no para todas para no sobrecargar la API con peticiones (el limite es 5 por segundo y se ralentizaria mucho de todas formas)
        holder.portada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieOverviewResponse.obtenerDescripcion(pelicula.getId(), new IMDBApiService() { //Llamamos al metodo que obtiene la descripcion de la API con su id
                    //No se usa, se implementa porque IMDBApiService debe implementar todos los metodos de la interfaz
                    @Override
                    public void onMoviesReceived(List<Movie> movies) {

                    }

                    @Override
                    public void onDescriptionReceived(String descripcion) { //Al recibir la descripcion
                        pelicula.setDescripcion(descripcion); //Le damos a la pelicula pulsada la descripcion que hemos obtenido de la API
                        Intent intent=new Intent(holder.itemView.getContext(), MovieDetailsActivity.class); //Creamos un Intent de la actividad de detalles
                        intent.putExtra("Movie",pelicula); //Pasamos la pelicula en el intent, ya que es Parcelable
                        context.startActivity(intent); //Iniciamos la actividad de detalles
                    }
                });
            }
        });
        //Al hacer click largo en una pelicula la añadiremos o quitaremos de favoritos
        holder.portada.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String userId=GoogleSignIn.getLastSignedInAccount(context).getId(); //Obtenemos el id de la cuenta de Google
                if(!dbHelper.movieExists(pelicula.getId()))dbHelper.addMovie(pelicula); //Si la pelicula no existe en la base de datos, la añadimos
                if(dbHelper.movieIsFavorite(userId, pelicula.getId())){ //Si la pelicula ya es favorita del usuario
                    if(fragment instanceof FavoritesFragment){ //Si estamos en el fragmento de Favoritas
                        dbHelper.removeFavorite(userId, pelicula.getId()); //La eliminamos de sus favoritos
                        if(!dbHelper.movieExistsInFavorite(pelicula.getId())) dbHelper.removeMovie(pelicula.getId()); //Si  ya no es favorita de ningun usuario, tambien la borramos de la tabla MOVIES
                        Toast.makeText(context,pelicula.getTitulo()+" eliminada de favoritos",Toast.LENGTH_SHORT).show(); //Mostramos un Toast informando al usuario
                        movies.remove(holder.getAdapterPosition()); //La eliminamos de la lista del adaptador
                        notifyItemRemoved(holder.getAdapterPosition()); //Notificamos al adaptador
                        //Esto sirve para que se borre no solo de la base de datos, sino tambien de la lista en tiempo real
                    }
                    else Toast.makeText(context,pelicula.getTitulo()+" ya está en Favoritos",Toast.LENGTH_SHORT).show(); //Si no, decimos al usuario que ya está en favoritos
                }
                else{ //Si no es favorita del usuario
                    dbHelper.addFavorite(userId,pelicula.getId()); //La añadimos a sus favoritos
                    Toast.makeText(context,"Agregada a favoritos: "+pelicula.getTitulo(),Toast.LENGTH_SHORT).show(); //Mostramos un Toast con la informacion
                }
                return true; //Devolvemos true para consumir la llamada
            }
        });
    }
    @Override
    public int getItemCount() {
        return movies.size();
    }
}
