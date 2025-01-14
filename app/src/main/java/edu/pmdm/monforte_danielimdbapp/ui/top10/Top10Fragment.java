package edu.pmdm.monforte_danielimdbapp.ui.top10;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.monforte_danielimdbapp.adapters.MovieAdapter;
import edu.pmdm.monforte_danielimdbapp.api.IMDBApiService;
import edu.pmdm.monforte_danielimdbapp.databinding.FragmentTop10Binding;
import edu.pmdm.monforte_danielimdbapp.models.Movie;
import edu.pmdm.monforte_danielimdbapp.models.MovieOverviewResponse;
import edu.pmdm.monforte_danielimdbapp.models.MovieResponse;

public class Top10Fragment extends Fragment {

    private FragmentTop10Binding binding; //Variable para el binding de este fragmento
    private List<Movie>topMovies=new ArrayList<Movie>(); //Lista de peliculas del top
    private MovieAdapter adaptador; //Adaptador para el RecyclerView de peliculas del top

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTop10Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerViewTop10;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2)); //Le damos al recyclerView un GridLayoutManager para que haya 2 columnas
        adaptador=new MovieAdapter(topMovies,this); //Creamos un adaptador con la lista de películas del top
        recyclerView.setAdapter(adaptador); //Ponemos el adaptador al RecyclerView
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MovieResponse.buscarTop10(new IMDBApiService() { //Llamamos al metodo que busca el top 10 con la API de IMDB com
            @Override
            public void onMoviesReceived(List<Movie> movies) { //Al recibir las peliculas
                topMovies.clear(); //Limpiamos la lista del top para que no se repitan
                topMovies.addAll(movies); //Añadimos todas las peliculas recibidas a la lista
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adaptador.notifyDataSetChanged(); //Notificamos al adaptador de que han cambiado los datos
                    }
                });
            }
            //No se usa, se implementa porque IMDBApiService debe implementar todos los metodos de la interfaz
            @Override
            public void onDescriptionReceived(String descripcion) {

            }
        },getContext()); //Le pasamos tambien el contexto al método
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}