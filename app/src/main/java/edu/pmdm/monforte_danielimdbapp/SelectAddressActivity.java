package edu.pmdm.monforte_danielimdbapp;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.pmdm.monforte_danielimdbapp.databinding.ActivityEditUserBinding;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivitySelectAddressBinding;

public class SelectAddressActivity extends AppCompatActivity {
    private ActivitySelectAddressBinding binding;

    private String address;
    private SupportMapFragment mapFragment;

    private ActivityResultLauncher<Intent> buscarDireccionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySelectAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        buscarDireccionLauncher=registerForActivityResult( //Cuando termine la actividad de seleccionar direccion
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Place place = Autocomplete.getPlaceFromIntent(data);
                            mostrarUbicacion(new LatLng(place.getLatLng().latitude,place.getLatLng().longitude));
                            address=place.getAddress();
                            binding.textViewDireccion.setText(address);
                        }
                    }
                });
        Intent intentPlace = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)).build(SelectAddressActivity.this);
        buscarDireccionLauncher.launch(intentPlace);
        if(!Places.isInitialized()) Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        binding.btnBuscarDireccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentPlace = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)).build(SelectAddressActivity.this);
                buscarDireccionLauncher.launch(intentPlace);
            }
        });

        binding.btnConfirmarDireccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(address!=null && !address.isEmpty()){
                    Intent intentDireccion = new Intent(getApplicationContext(), EditUserActivity.class);
                    intentDireccion.putExtra("Address",address);
                    setResult(RESULT_OK,intentDireccion);
                    finish();
                }
                else Toast.makeText(SelectAddressActivity.this,"Seleccione una direccion",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarUbicacion(LatLng latLng) {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Address"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });
    }
}