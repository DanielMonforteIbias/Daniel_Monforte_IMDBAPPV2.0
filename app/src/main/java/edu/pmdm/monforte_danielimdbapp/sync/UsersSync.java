package edu.pmdm.monforte_danielimdbapp.sync;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.models.Movie;
import edu.pmdm.monforte_danielimdbapp.models.User;

public class UsersSync {
    private FirebaseFirestore db;
    FavoritesDatabaseHelper dbHelper;

    public UsersSync(Context c) {
        db = FirebaseFirestore.getInstance(); //Inicializamos la variable para la base de datos de Firebase
        dbHelper = new FavoritesDatabaseHelper(c); //Inicializamos tambien la variable para la base de datos local
    }

    /**
     * Método que sincroniza el contenido de la base de daos de Firebase con la tabla local de usuarios
     */
    public void syncUsersFromFirebase() {
        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot userDocumentSnapshots) {
                for (DocumentSnapshot userDocument : userDocumentSnapshots.getDocuments()) {
                    String userId = userDocument.getString("userId");
                    String userName = userDocument.getString("name");
                    String userEmail = userDocument.getString("email");
                    String userAddress=userDocument.getString("address");
                    String userPhone=userDocument.getString("phone");
                    String userImage = userDocument.getString("image");
                    if(!dbHelper.userExists(userId)) dbHelper.addUser(new User(userId,userName,userEmail,userAddress,userPhone,userImage));
                }
            }
        });
    }

    /**
     * Método que añade un usuario recibido a la base de datos de Firebase
     */
    public void addUserToFirebase(User user) {
        DocumentReference userDocument = db.collection("users").document(user.getUserId()); //La colección de favoritos tendrá un documento para cada usuario, con su id como nombre
        Map<String, Object> userMap = new HashMap<>(); //Creamos un mapa para poner los datos del usuario
        userMap.put("userId", user.getUserId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("address",user.getAddress());
        userMap.put("phone",user.getPhone());
        userMap.put("image", user.getImage());
        userDocument.set(userMap);
    }

    public void addActivityLogToUser(String userId) {
        DocumentReference userDocument = db.collection("users").document(userId);
        Map<String, String> newLog = new HashMap<>();
        User user = dbHelper.getUser(userId);
        //Guardamos en Firebase la ultima pareja de login-logout, que es la que hay en local
        newLog.put("login_time", user.getLoginTime());
        newLog.put("logout_time", user.getLogoutTime());
        userDocument.update("activity_log", FieldValue.arrayUnion(newLog)); //Hacemos update para que se añada a los anteriores y no los reemplace
    }
}
