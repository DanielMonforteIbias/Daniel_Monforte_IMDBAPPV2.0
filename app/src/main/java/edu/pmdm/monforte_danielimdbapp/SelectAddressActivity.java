package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.pmdm.monforte_danielimdbapp.databinding.ActivityEditUserBinding;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivitySelectAddressBinding;

public class SelectAddressActivity extends AppCompatActivity {
    private ActivitySelectAddressBinding binding;

    private String address;
    private Geocoder geocoder;
    private SupportMapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySelectAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        address=intent.getStringExtra("address");
        if(address!=null && !address.isEmpty()) new GetLatLngTask().execute(address);
        if(!Places.isInitialized()) Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    new GetAddressTask().execute(latLng);
                    mostrarUbicacion(latLng);
                }
            }
            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(),"Error al obtener direccion",Toast.LENGTH_SHORT).show();
            }
        });
        mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        geocoder = new Geocoder(this, Locale.getDefault());

        binding.btnConfirmarDireccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDireccion = new Intent(getApplicationContext(), EditUserActivity.class);
                intentDireccion.putExtra("Address",address);
                setResult(RESULT_OK,intentDireccion);
                finish();
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

    private class GetAddressTask extends AsyncTask<LatLng, Void, String> {
        @Override
        protected String doInBackground(LatLng... params) {
            LatLng latLng = params[0];
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressLine = address.getAddressLine(0);
                    return addressLine;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                address=result;
                System.out.println(address);
            }
        }
    }
    private class GetLatLngTask extends AsyncTask<String, Void, LatLng> {
        @Override
        protected LatLng doInBackground(String... params) {
            String addressString = params[0];
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressString, 1); // Obtener la primera coincidencia
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    double latitude = address.getLatitude();
                    double longitude = address.getLongitude();
                    return new LatLng(latitude, longitude); // Devolvemos LatLng
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            if (latLng != null) {
                mostrarUbicacion(latLng);
            }
            else System.out.println("noooooo");
        }
    }
}