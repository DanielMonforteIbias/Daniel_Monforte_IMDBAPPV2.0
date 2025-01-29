package edu.pmdm.monforte_danielimdbapp.sync;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.models.Movie;

public class FavoritesSync {
    private FavoritesDatabaseHelper dbHelper; //Necesitamos un objeto de esta clase para usar metodos de la base de datos local
    private FirebaseFirestore db;

    public FavoritesSync(Context c){
        db= FirebaseFirestore.getInstance(); //Inicializamos la variable para la base de datos de Firebase
        dbHelper=new FavoritesDatabaseHelper(c); //Inicializamos tambien la variable para la base de datos local
    }

    /**
     * Método que sincroniza toda la base de datos de favoritos con la de Firebase
     */
    public void syncFavoritesToFirebase(){
        List<String> users= dbHelper.getAllUsers(); //Obtenemos una lista de los IDs de los usuarios
        for (String user : users) { //Recorremos la lista de usuarios
            DocumentReference userDocument = db.collection("favorites").document(user); //La colección de favoritos tendrá un documento para cada usuario, con su id como nombre
            List<Movie> userFavorites = dbHelper.getUserFavorites(user); //Obtenemos los favoritos de ese usuario
            CollectionReference moviesCollection = userDocument.collection("movies"); //El documento del usuario tendra una coleccion de peliculas donde guardaremos sus favoritas
            userDocument.set(Collections.singletonMap("exists", true), SetOptions.merge()); //Nos aseguramos de que el documento tenga un parametro para que Firebase lo considere completo y el documento exista
            for (Movie movie : userFavorites) { //Recorremos las peliculas favoritas del usuario
                String insertionTime = dbHelper.getInsertionTimeFavoriteMovie(user, movie.getId()); //Obtenemos la hora de insercion, que no es un atributo de la pelicula en sí
                Map<String, Object> movieMap = new HashMap<>(); //Creamos un mapa para poner los datos
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

    /**
     * Método que sincroniza toda la base de datos de favoritos de Firebase con la local
     */
    public void syncFavoritesFromFirebase(){
        db.collection("favorites").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot userDocumentSnapshots) {
                System.out.println(userDocumentSnapshots.getDocuments().size());
                for (DocumentSnapshot userDocument : userDocumentSnapshots.getDocuments()) {
                    String userId = userDocument.getId();
                    db.collection("favorites").document(userId).collection("movies").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot movieDocumentSnapshots) {
                            for (DocumentSnapshot movieDocument : movieDocumentSnapshots.getDocuments()) {
                                String movieId = movieDocument.getString("movieId");
                                String movieTitle = movieDocument.getString("movieTitle");
                                String movieImage = movieDocument.getString("movieImage");
                                String movieDate = movieDocument.getString("movieDate");
                                double movieRating = movieDocument.getDouble("movieRating");
                                String insertionTime = movieDocument.getString("insertionTime");
                                if(!dbHelper.movieExists(movieId)) dbHelper.addMovie(new Movie(movieId,movieTitle,movieImage,movieDate,movieRating));
                                if(!dbHelper.movieIsFavorite(userId,movieId))dbHelper.addFavorite(userId,movieId,insertionTime);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Este método sincroniza una película añadida a favoritos por un usuario para que se añada tambien en Firebase
     * @param userId el id del usuario que añadio la pelicula a favoritos
     * @param movieId el id de la película añadida a favoritos
     */
    public void addFavoriteToFirebase(String userId, String movieId){
        DocumentReference userDocument = db.collection("favorites").document(userId); //Obtenemos el documento del usuario
        List<Movie> userFavorites = dbHelper.getUserFavorites(userId); //Obtenemos los favoritos del usuario
        CollectionReference moviesCollection = userDocument.collection("movies"); //Obtenemos la coleccion de peliculas favoritas del usuario
        userDocument.set(Collections.singletonMap("exists", true), SetOptions.merge()); //Nos aseguramos de que el documento tenga un parametro para que Firebase lo considere completo y el documento exista
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

    /**
     * Método que elimina una pelicula de favoritos de Firebase cuando se elimina localmente
     * @param userId el id del usuario que elimino la pelicula de sus favoritos
     * @param movieId el id de la pelicula eliminada de favoritos
     */
    public void deleteFavoriteFromFirebase(String userId, String movieId){
        DocumentReference userRef = db.collection("favorites").document(userId); //Obtenemos el documento del usuario
        CollectionReference moviesRef = userRef.collection("movies"); //Obtenemos la coleccion de favoritas del usuario
        moviesRef.document(movieId).delete(); //Eliminamos la pelicula con el id recibido de dicha coleccion
    }
}
