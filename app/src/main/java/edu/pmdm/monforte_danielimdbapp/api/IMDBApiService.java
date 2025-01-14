package edu.pmdm.monforte_danielimdbapp.api;

import java.util.List;

import edu.pmdm.monforte_danielimdbapp.models.Movie;

public interface IMDBApiService {
    public void onMoviesReceived(List<Movie> movies);
    public void onDescriptionReceived(String descripcion);
}
