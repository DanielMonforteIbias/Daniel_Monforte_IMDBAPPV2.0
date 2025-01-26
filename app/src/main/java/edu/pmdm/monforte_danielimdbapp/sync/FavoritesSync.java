package edu.pmdm.monforte_danielimdbapp.sync;

import android.content.Context;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.models.Movie;

public class FavoritesSync {
    private FavoritesDatabaseHelper dbHelper;
    FirebaseFirestore db;

    public FavoritesSync(Context c){
        db= FirebaseFirestore.getInstance();
        dbHelper=new FavoritesDatabaseHelper(c);
    }

    public void syncFavoritesToFirebase(){
        List<String> users= dbHelper.getAllUsers();
        for (String user : users) {
            DocumentReference userDocument = db.collection("favorites").document(user);
            List<Movie> userFavorites = dbHelper.getUserFavorites(user);
            CollectionReference moviesCollection = userDocument.collection("movies");
            for (Movie movie : userFavorites) {
                String insertionTime = dbHelper.getInsertionTimeFavoriteMovie(user, movie.getId());
                Map<String, Object> movieMap = new HashMap<>();
                //Ponemos los datos de la pelicula en el map
                movieMap.put("movieId", movie.getId());
                movieMap.put("movieTitle", movie.getTitulo());
                movieMap.put("movieImage", movie.getPortada());
                movieMap.put("movieDate", movie.getFecha());
                movieMap.put("movieRating", movie.getRating());
                movieMap.put("insertionTime",insertionTime);

                moviesCollection.document(movie.getId()).set(movieMap); //Creamos el documento para esa pelicula en la coleccion de peliculas del usuario
            }
        }
    }
    public void addFavoriteToFirebase(String userId, String movieId){
        DocumentReference userDocument = db.collection("favorites").document(userId);
        List<Movie> userFavorites = dbHelper.getUserFavorites(userId);
        CollectionReference moviesCollection = userDocument.collection("movies");
        for (Movie movie : userFavorites) {
            if(movie.getId().equals(movieId)){ //Sincronizamos solo la película añadida nueva
                String insertionTime = dbHelper.getInsertionTimeFavoriteMovie(userId, movie.getId());
                Map<String, Object> movieMap = new HashMap<>();
                //Ponemos los datos de la pelicula en el map
                movieMap.put("movieId", movie.getId());
                movieMap.put("movieTitle", movie.getTitulo());
                movieMap.put("movieImage", movie.getPortada());
                movieMap.put("movieDate", movie.getFecha());
                movieMap.put("movieRating", movie.getRating());
                movieMap.put("insertionTime",insertionTime);

                moviesCollection.document(movie.getId()).set(movieMap); //Creamos el documento para esa pelicula en la coleccion de peliculas del usuario
            }
        }
    }

    public void deleteFavoriteFromFirebase(String userId, String movieId){
        DocumentReference userRef = db.collection("favorites").document(userId);
        CollectionReference moviesRef = userRef.collection("movies");
        moviesRef.document(movieId).delete();
    }
}
