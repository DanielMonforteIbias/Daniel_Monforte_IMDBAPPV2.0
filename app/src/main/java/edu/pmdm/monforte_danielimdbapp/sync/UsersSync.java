package edu.pmdm.monforte_danielimdbapp.sync;

import android.content.Context;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.database.UsersDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.models.Movie;
import edu.pmdm.monforte_danielimdbapp.models.User;

public class UsersSync {
    private UsersDatabaseHelper dbHelper; //Necesitamos un objeto de esta clase para usar metodos de la base de datos local
    private FirebaseFirestore db;

    public UsersSync(Context c){
        db= FirebaseFirestore.getInstance(); //Inicializamos la variable para la base de datos de Firebase
        dbHelper=new UsersDatabaseHelper(c); //Inicializamos tambien la variable para la base de datos local
    }

    /**
     * Método que sincroniza toda la base de datos de usuarios con la de Firebase
     */
    public void syncUsersToFirebase(){
        List<User> users=dbHelper.getUsers();
        for(User u:users){
            DocumentReference userDocument = db.collection("users").document(u.getUserId()); //La colección de favoritos tendrá un documento para cada usuario, con su id como nombre
            Map<String, Object> userMap = new HashMap<>(); //Creamos un mapa para poner los datos del usuario
            userMap.put("userId",u.getUserId());
            userMap.put("name",u.getName());
            userMap.put("email",u.getEmail());
            userDocument.set(userMap);
        }
    }
}
