package edu.pmdm.monforte_danielimdbapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
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

import edu.pmdm.monforte_danielimdbapp.databinding.ActivityMovieDetailsBinding;
import edu.pmdm.monforte_danielimdbapp.models.Movie;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final int ACCEDER_A_CONTACTOS = 1; //Variable para los permisos de acceso a contactos
    private static final int ENVIAR_SMS = 2; //Variable para los permisos de enviar SMS

    private ActivityMovieDetailsBinding binding; //Variable para el binding de esta actividad

    private Movie pelicula=new Movie(); //Pelicula de este intent, inicializada a una por defecto


    private ActivityResultLauncher<Intent> contactosLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMovieDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDetails), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent=getIntent(); //Obtenemos el intent que abrió esta actividad
        pelicula=intent.getParcelableExtra("Movie"); //Obtenemos la pelicula del intent que invoco esta actividad
        //Ponemos los datos de la pelicula en la interfaz
        Glide.with(this).load(pelicula.getPortada()).into(binding.imgPortadaDetalles); //Con Glide pasamos el String de la portada a imagen
        binding.txtTituloDetalles.setText(pelicula.getTitulo());
        binding.txtFechaDetalles.setText("Release date: "+pelicula.getFecha());
        if (pelicula.getRating()==0) binding.txtRatingDetalles.setText("(No rating available)"); //Si el rating es 0 (porque la pelicula no es del top 10), pondremos por defecto que no está disponible
        else binding.txtRatingDetalles.setText("Rating: "+pelicula.getRating());
        binding.txtDescripcionDetalles.setText(pelicula.getDescripcion());

        //OnClick del boton de enviar por SMS
        binding.btnEnviarSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comprobarPermisosContactos()){ //Si tenemos permisos de contactos
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI); //Creamos un Intent de elegir contacto
                    //Poner en la Uri ContactsContract.CommonDataKinds.Phone.CONTENT_URI permite elegir entre varios telefonos, si usasemos ContactsContract.Contacts.CONTENT_URI solo podriamos elegir el contacto en sí, sin elegir a que telefono enviarselo
                    contactosLauncher.launch(intent);
                }
                else{ //Si no, informamos al usuario y los pedimos
                    showToast("Permiso de contactos denegado");
                    pedirPermisosContactos();
                }
            }
        });
        //Launcher del intent para elegir el contacto al que enviar el SMS
        contactosLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Uri data = result.getData().getData(); //Se usa getData() primero para obtener el intent de result, y luego getData en ese intent para obtener la Uri
                            if(data!=null){ //Si hay datos
                                Cursor cursor = getContentResolver().query(data, null, null, null, null); //Creamos un cursos con los datos
                                if (cursor != null && cursor.moveToFirst()) { //Si el cursor no es nulo y hay datos
                                    int index=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER); //Obtenemos el indice donde está el numero
                                    String telefono = cursor.getString(index); //Obtenemos el dato que hay en ese indice, que sera el numero de telefono
                                    if(comprobarPermisosSMS()) enviarSms(telefono); //Si tenemos permiso de SMS, llamamos al metodo que lo envia
                                    else{ //Si no, avisamos al usuario y los pedimos
                                        showToast("Permisos de SMS denegado");
                                        pedirPermisosSMS();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Método que abre el intent para enviar el SMS al teléfono de un contacto
     * @param telefono el teléfono del contacto al que se le va a enviar
     */
    private void enviarSms(String telefono) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+telefono)); //Creamos un intent que abre la app de SMS para enviar un mensaje al telefono indicado en la Uri
        intent.putExtra("sms_body", "Esta película te gustará: "+pelicula.getTitulo()+"\nRating: "+pelicula.getRating()); //Ponemos el mensaje personalizado en el intent
        startActivity(intent); //Iniciamos el intent
    }

    /**
     * Método para pedir los permisos para acceder a los contactos
     */
    private void pedirPermisosContactos(){
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, ACCEDER_A_CONTACTOS);
    }

    /**
     * Método que comprueba si tenemos permiso para acceder a contactos o no
     * @return true si tenemos los permisos, false si no
     */
    private boolean comprobarPermisosContactos() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Método para pedir los permisos para enviar SMS
     */
    private void pedirPermisosSMS(){
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, ENVIAR_SMS);
    }
    /**
     * Método que comprueba si tenemos permiso para enviar SMS o no
     * @return true si tenemos los permisos, false si no
     */
    private boolean comprobarPermisosSMS() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Método para mostrar un Toast con un mensaje recibido
     * @param s el mensaje a mostrar en el Toast
     */
    public void showToast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
}