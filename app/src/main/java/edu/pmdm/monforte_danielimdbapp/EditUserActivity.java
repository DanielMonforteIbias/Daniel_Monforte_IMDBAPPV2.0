package edu.pmdm.monforte_danielimdbapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyPair;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityEditUserBinding;
import edu.pmdm.monforte_danielimdbapp.models.User;
import edu.pmdm.monforte_danielimdbapp.sync.UsersSync;
import edu.pmdm.monforte_danielimdbapp.utils.AsyncTaskExecutorService;
import edu.pmdm.monforte_danielimdbapp.utils.KeystoreManager;

public class EditUserActivity extends AppCompatActivity {
    private FavoritesDatabaseHelper dbHelper;
    private UsersSync userSync;
    private ActivityEditUserBinding binding;

    public final int PERMISO_UBICACION=1; //Constante para identificar el permiso de ubicacion
    public final int PERMISO_CAMARA=2; //Constante para identificar el permiso de camara
    public final int PERMISO_ALMACENAMIENTO=3; //Constante para identificar el permiso de almacenamiento

    private String image="";

    private ActivityResultLauncher<Intent> selectAddressActivityLauncher;
    private ActivityResultLauncher<Intent> selectImageActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper=new FavoritesDatabaseHelper(this);
        userSync=new UsersSync(this);
        Intent intent=getIntent();
        String userId=intent.getStringExtra("userId");
        User user=dbHelper.getUser(userId);

        binding.editTextName.setText(user.getName());
        binding.editTextMail.setText(user.getEmail());
        image=user.getImage();
        if(user.getAddress()!=null && !user.getAddress().isEmpty()){ //Si el usuario tiene direccion
            String userAddress=KeystoreManager.decrypt(user.getAddress());  //La desencriptamos
            binding.editTextAddress.setText(userAddress); //La ponemos en el EditText
        }
        if(image!=null && !image.isEmpty()){ //Si el usuario tiene foto
            if (image.startsWith("http://") || image.startsWith("https://") || image.startsWith("content://") || image.startsWith("file://")){ //Si la imagen es de una URL o una URI, usamos Glide
                Glide.with(this).load(image).placeholder(R.drawable.usuario).into(binding.imgUsuario); //Usamos Glide para poner la foto del usuario en el ImageView. Si ocurriese algun problema y fuese null, se pondría la foto del placeholder
            }
            else {//Si no, es un String en Base64 (viene de la camara), asi que haremos un bitmap y despues usaremos Glide
                byte[] decodedString = Base64.decode(user.getImage(), Base64.DEFAULT);
                Bitmap bitmap= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(this).load(bitmap).placeholder(R.drawable.usuario).into(binding.imgUsuario);
            }
        }
        else Glide.with(this).load(R.drawable.usuario).into(binding.imgUsuario); //Si no tiene foto ponemos la default
        if(user.getPhone()!=null && !user.getPhone().isEmpty()){ //Si el telefono del usuario no esta vacio
            String userPhone=KeystoreManager.decrypt(user.getPhone()); //Lo desencriptamos
            if(!userPhone.isEmpty()){ //Si lo hemos obtenido bien
                binding.countryCodePicker.setCountryForPhoneCode(Integer.parseInt(splitPhoneNumber(userPhone).first.replace("+",""))); //Seleccionamos el codigo del pais en el code picker
                binding.editTextPhone.setText(splitPhoneNumber(userPhone).second); //Ponemos el numero sin prefijo en el editText
            }
        }
        binding.btnSeleccionarDireccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comprobarPermisosUbicacion()){
                    Intent intent = new Intent(getApplicationContext(), SelectAddressActivity.class);
                    if(user.getAddress()!=null && !user.getAddress().isEmpty()) intent.putExtra("address",KeystoreManager.decrypt(user.getAddress()));
                    selectAddressActivityLauncher.launch(intent);
                } else{
                    Toast.makeText(getApplicationContext(),"Permisos de ubicacion denegados!",Toast.LENGTH_SHORT).show();
                    pedirPermisosUbicacion();
                }
            }
        });
        selectAddressActivityLauncher = registerForActivityResult( //Cuando termine la actividad de seleccionar direccion
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            binding.editTextAddress.setText(data.getStringExtra("Address")); //Obtenemos la direccion del intent y la ponemos en su EditText
                        }
                    }
                });
        binding.btnSeleccionarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Mostramos un dialogo para elegir las opciones de foto
                AlertDialog.Builder builder = new AlertDialog.Builder(EditUserActivity.this);
                builder.setTitle(R.string.select_image_option)
                        .setItems(new String[]{"Sacar foto", "Elegir desde galería", "Foto desde URL"}, (dialog, opcion) -> {
                            switch (opcion) {
                                case 0:
                                    if(comprobarPermisosCamara()){
                                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //El intent abre la camara
                                        selectImageActivityLauncher.launch(cameraIntent);
                                    }
                                    else{
                                        Toast.makeText(EditUserActivity.this,"Permisos de camara denegado",Toast.LENGTH_SHORT).show();
                                        pedirPermisosCamara();
                                    }
                                    break;
                                case 1:
                                    if(comprobarPermisosAlmacenamiento()){
                                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //El intent abre las fotos ya existentes
                                        galleryIntent.setType("image/*");
                                        selectImageActivityLauncher.launch(galleryIntent);
                                    }
                                    else{
                                        Toast.makeText(EditUserActivity.this,"Permisos de almacenamiento denegado",Toast.LENGTH_SHORT).show();
                                        pedirPermisosAlmacenamiento();
                                    }
                                    break;
                                case 2:
                                    EditText editTextUrl = new EditText(EditUserActivity.this);
                                    AlertDialog urlDialog = new AlertDialog.Builder(EditUserActivity.this)
                                            .setTitle(R.string.introduce_image_url)
                                            .setView(editTextUrl)
                                            .setPositiveButton("Cargar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String url = editTextUrl.getText().toString();
                                                    new DownloadUrlImage().execute(url);
                                                }
                                            })
                                            .setNegativeButton("Cancelar", null)
                                            .create();
                                    urlDialog.show();
                                    break;
                            }
                        })
                        .show();
            }
        });
        selectImageActivityLauncher=registerForActivityResult( //Cuando termine la actividad de seleccionar imagen
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Uri imageUri = data.getData();
                                if (imageUri != null) { //Si no es nulo, viene de la galeria
                                    Glide.with(EditUserActivity.this).load(imageUri).placeholder(R.drawable.usuario).into(binding.imgUsuario); //Ponemos la URI en el ImageView
                                    image=imageUri.toString(); //Guardamos la Uri en un String
                                } else { //Si la Uri es nula, viene de la camara
                                    Bitmap photo = (Bitmap) data.getExtras().get("data"); //Obtenemos el bitmap de data
                                    Glide.with(EditUserActivity.this).load(photo).placeholder(R.drawable.usuario).into(binding.imgUsuario); //Ponemos el bitmap en el ImageView
                                    image=convertBitmapToBase64(photo); //Guardamos un String en base64
                                }
                            }
                        }
                    }
                });
        binding.btnGuardarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtenemos los datos de los campos
                String name=binding.editTextName.getText().toString();
                String address=binding.editTextAddress.getText().toString();
                String phone=binding.editTextPhone.getText().toString();

                user.setName(name); //Ponemos el nombre nuevo al usuario
                if(!phone.isEmpty()){ //Si el campo de telefono no esta vacio
                    if(!isValidPhoneNumber(phone,binding.countryCodePicker.getSelectedCountryNameCode())){ //Comprobamos que sea valido segun el prefijo
                        Toast.makeText(getApplicationContext(),"Numero de telefono no valido",Toast.LENGTH_SHORT).show(); //Si no es valido, avisamos al usuario
                        return; //No seguimos
                    }
                    //Si es valido
                    phone=formatPhoneNumber(phone,binding.countryCodePicker.getSelectedCountryNameCode()); //Primero lo formateamos añadiendo el prefijo
                    phone=KeystoreManager.encrypt(phone); //Lo encriptamos
                    user.setPhone(phone); //Se lo ponemos al usuario
                }
                if(!address.isEmpty()){ //Si el campo de direccion no esta vacio
                    address=KeystoreManager.encrypt(address); //Encriptamos la direccion
                    user.setAddress(address); //Se la ponemos al usuario
                }
                if(!image.isEmpty()) user.setImage(image); //Si la imagen no esta vacia, se la ponemos al usuario


                dbHelper.updateUser(user); //Actualizamos el usuario de forma local
                userSync.updateUserInFirebase(user); //Actualizamos el usuario en Firebase
                Toast.makeText(getApplicationContext(),"Cambios guardados",Toast.LENGTH_SHORT).show(); //Informamos al usuario de que se han guardado sus cambios
                setResult(RESULT_OK,intent);
                finish(); //Cerramos esta actividad
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber, String countryCode) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, countryCode);
            return phoneUtil.isValidNumber(number);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String formatPhoneNumber(String number, String countryCode) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, countryCode);
            return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (Exception e) {
            Toast.makeText(this,"Error al formatear el numero de telefono",Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public Pair<String, String> splitPhoneNumber(String fullPhone) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(fullPhone, null);
            String countryCode = "+" + phoneNumber.getCountryCode();
            String nationalNumber = String.valueOf(phoneNumber.getNationalNumber());
            return new Pair<>(countryCode, nationalNumber);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"Numero de telefono no valido",Toast.LENGTH_SHORT).show();
            return new Pair<>("","");
        }
    }

    /**
     * Método que comprueba si tenemos permisos de ubicacion o no
     * @return true si tenemos permisos, false si no
     */
    private boolean comprobarPermisosUbicacion() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Método que pide los permisos de ubicación al usuario
     */
    private void pedirPermisosUbicacion() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_UBICACION);
    }

    /**
     * Método que comprueba si tenemos permisos de camara o no
     * @return true si tenemos permisos, false si no
     */
    private boolean comprobarPermisosCamara() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Método que pide los permisos de camara al usuario
     */
    private void pedirPermisosCamara() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISO_CAMARA);
    }

    /**
     * Método que comprueba si tenemos permisos de camara o no
     * @return true si tenemos permisos, false si no
     */
    private boolean comprobarPermisosAlmacenamiento() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Método que pide los permisos de camara al usuario
     */
    private void pedirPermisosAlmacenamiento() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISO_ALMACENAMIENTO);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISO_ALMACENAMIENTO);
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Comprimir como JPEG
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT); // Convertir a base64
    }

    private class DownloadUrlImage extends AsyncTaskExecutorService<String, Void, Boolean> {
        private String url;
        @Override
        protected Boolean doInBackground(String s) {
            try {
                url=s;
                URL imageUrl = new URL(s);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();
                String contentType = connection.getContentType();
                return contentType != null && contentType.startsWith("image/");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean validUrl) {
            if (validUrl) {
                Glide.with(EditUserActivity.this).load(url).placeholder(R.drawable.usuario).into(binding.imgUsuario);
                image = url;
            } else {
                Toast.makeText(EditUserActivity.this, "URL no válida o no es una imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}