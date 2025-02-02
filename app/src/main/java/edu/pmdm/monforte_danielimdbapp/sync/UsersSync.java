package edu.pmdm.monforte_danielimdbapp.sync;

import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
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

    /**
     * Método que actualiza los datos de un usuario en la base de datos de Firebase
     */
    public void updateUserInFirebase(User user) {
        DocumentReference userDocument = db.collection("users").document(user.getUserId());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getName());
        userMap.put("address", user.getAddress());
        userMap.put("phone", user.getPhone());
        userMap.put("image", user.getImage());
        userDocument.update(userMap);
    }

    public void userExistsInFirebase(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        DocumentReference userDocument = db.collection("users").document(userId);
        userDocument.get().addOnCompleteListener(listener);
    }

    public void addActivityLoginToUser(String userId, long loginTime) {
        DocumentReference userDocument = db.collection("users").document(userId);
        Map<String, String> newLog = new HashMap<>();
        newLog.put("login_time",getTimestamp(loginTime));
        newLog.put("logout_time", null);
        userDocument.update("activity_log", FieldValue.arrayUnion(newLog)); //Hacemos update para que se añada a los anteriores y no los reemplace
    }
    public void addActivityLogoutToUser(String userId, long logoutTime) {
        DocumentReference userDocument = db.collection("users").document(userId);
        userDocument.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> logs = (List<Map<String, Object>>) documentSnapshot.get("activity_log");
                if (logs != null) {
                    for (int i = logs.size() - 1; i >= 0; i--) {
                        Map<String, Object> log = logs.get(i);
                        if (log.get("logout_time") == null) {
                            log.put("logout_time", getTimestamp(logoutTime));
                            break;
                        }
                    }
                    // Actualizar el historial de actividad en Firebase
                    userDocument.update("activity_log", logs);
                }
            }
        });
    }

    public String getTimestamp(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(time);
        return format.format(date);
    }
}
