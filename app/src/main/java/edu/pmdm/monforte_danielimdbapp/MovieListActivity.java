package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.adapters.MovieAdapter;
import edu.pmdm.monforte_danielimdbapp.api.TMDBApiService;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityMovieDetailsBinding;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityMovieListBinding;
import edu.pmdm.monforte_danielimdbapp.models.Genre;
import edu.pmdm.monforte_danielimdbapp.models.Movie;
import edu.pmdm.monforte_danielimdbapp.models.MovieSearchResponse;

public class MovieListActivity extends AppCompatActivity {
    private ActivityMovieListBinding binding; //Variable para el binding de esta actividad
    private MovieAdapter moviesAdapter; //Adaptador para el RecyclerView
    private List<Movie> moviesSearch =new ArrayList<Movie>(); //Lista de películas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMovieListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent=getIntent(); //Obtenemos el intent que invocó la actividad
        String year=intent.getStringExtra("Year"); //Sacamos el año del intent por su clave
        String genreId=intent.getStringExtra("GenreId"); //Sacamos el id del género por su clave
        MovieSearchResponse.buscarPeliculasPorAñoYGenero(year, genreId, new TMDBApiService() { //Llamamos al método que busca las películas con filtros de año y género
            @Override
            public void onGenresReceived(List<Genre> genres) {

            }

            @Override
            public void onMoviesReceived(List<Movie> movies) { //Al recibir las peliculas
                moviesSearch.clear(); //Limpiamos la lista actual para asegurar que no se dupliquen datos y solo estén los recibidos
                moviesSearch.addAll(movies); //Añadimos a nuestra lista de películas todas las recibidas
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        moviesAdapter.notifyDataSetChanged(); //Notificamos al adaptador de que han cambiado los datos
                    }
                });
            }
        },this);

        moviesAdapter=new MovieAdapter(moviesSearch,null); //Creamos un adaptador con la lista de películas, y null en el Fragment ya que estamos en una Activity
        binding.recyclerViewMovies.setLayoutManager(new GridLayoutManager(this,2)); //Le damos al RecyclerView un GridLayoutManager con 2 columnas
        binding.recyclerViewMovies.setAdapter(moviesAdapter); //Ponemos el adaptador al RecyclerView
        //OnClick del método volver
        binding.btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Termina la actividad
                //Este botón hace lo mismo que la flecha de ir hacia atrás del propio dispositivo, pero así facilitamos al usuario volver a la actividad anterior
            }
        });
    }
}