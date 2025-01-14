package edu.pmdm.monforte_danielimdbapp.ui.searchMovie;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.MovieListActivity;
import edu.pmdm.monforte_danielimdbapp.R;
import edu.pmdm.monforte_danielimdbapp.adapters.MovieAdapter;
import edu.pmdm.monforte_danielimdbapp.api.TMDBApiService;
import edu.pmdm.monforte_danielimdbapp.databinding.FragmentSearchMovieBinding;
import edu.pmdm.monforte_danielimdbapp.models.Genre;
import edu.pmdm.monforte_danielimdbapp.models.Movie;
import edu.pmdm.monforte_danielimdbapp.models.MovieSearchResponse;

public class SearchMovieFragment extends Fragment {

    private FragmentSearchMovieBinding binding; //Variable para el binding de este fragmento
    private static List<Genre> genres =new ArrayList<Genre>(); //Se hace estática porque no va a cambiar, siempre será la misma. Así solo llamamos a la API la primera vez, y si cambiamos de fragmento al volver se mantendrán los géneros y no habrá que obtenerlos otra vez

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchMovieBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<Genre> adaptador = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, genres); //Adaptador con objetos de tipo Genre
        if(genres.isEmpty()){ //Si la lista de géneros está vacía es que es la primera vez que cargamos el fragmento, asi que buscamos los géneros con la API
            binding.btnBuscar.setEnabled(false); //Desactivamos el botón para no poder buscar sin género
            Toast.makeText(getContext(),R.string.buscando_generos,Toast.LENGTH_SHORT).show(); //Informamos al usuario por si tardan en mostrarse los géneros, para que sepa que la app está trabajando
            MovieSearchResponse.buscarGeneros(new TMDBApiService() { //Llamamos al método que pide la lista de géneros de la API TMDB
                @Override
                public void onGenresReceived(List<Genre> genres) { //Al recibir la lista
                    SearchMovieFragment.this.genres.addAll(genres); //Añadimos todos los generos recibidos a la lista de generos
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.btnBuscar.setEnabled(true); //Volvemos a activar el botón ahora que hay géneros
                            adaptador.notifyDataSetChanged(); //Notificamos al adaptador de que han cambiado los datos, ya que esto no se ejecuta antes que lo de abajo, sino cuando termina la respuesta de la API. Lo de abajo se ejecuta y la lista de generos queda vacia
                        }
                    });
                }
                //Se implementa obligatoriamente al usar la interfaz, pero no se usa
                @Override
                public void onMoviesReceived(List<Movie> movies) {

                }
            },getContext());
        }
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGenero.setAdapter(adaptador); //Ponemos el adaptador creado al spinner
        binding.spinnerGenero.setSelection(0);//Hacemos que esté seleccionado el primer género por defecto
        //OnClick del botón de buscar
        binding.btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid=true; //Variable que establecerá si la búsqueda es válida o no, inicializada en un primer momento a true
                String year=binding.editTextYear.getText().toString(); //Obtenemos el año del editText en un String
                if(year.equals("")){ //Si está vacío
                    valid=false; //El intento de búsqueda no es vçalido
                    Toast.makeText(getContext(), R.string.año_vacio,Toast.LENGTH_SHORT).show(); //Informamos al usuario con un Toast
                }
                //No hace falta controlar casos como que el año sea muy grande o no sea un número más allá de estar vacío, ya que el EditText es de tipo Number y de longitud máxima 4
                if(valid){ //Si el intento de búsqueda es válido
                    Genre selectedGenre=(Genre)binding.spinnerGenero.getSelectedItem(); //Obtenemos el género seleccionado del Spinner
                    Intent intent=new Intent(getContext(),MovieListActivity.class); //Creamos un Intent para abrir la actividad MovieListActivity
                    intent.putExtra("Year",year); //Ponemos el año en el intent
                    intent.putExtra("GenreId",selectedGenre.getId()); //Ponemos el id del género en el intent
                    startActivity(intent); //Lanzamos el intent
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}