package edu.pmdm.monforte_danielimdbapp.api;

import java.util.List;

import edu.pmdm.monforte_danielimdbapp.models.Genre;
import edu.pmdm.monforte_danielimdbapp.models.Movie;

public interface TMDBApiService {
    public void onGenresReceived(List<Genre> genres);
    public void onMoviesReceived(List<Movie>movies);
}
