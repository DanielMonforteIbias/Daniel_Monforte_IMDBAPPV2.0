package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityEditUserBinding;
import edu.pmdm.monforte_danielimdbapp.models.User;

public class EditUserActivity extends AppCompatActivity {
    private FavoritesDatabaseHelper dbHelper;
    private ActivityEditUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper=new FavoritesDatabaseHelper(this);
        Intent intent=getIntent();
        String userId=intent.getStringExtra("userId");
        User user=dbHelper.getUser(userId);
        binding.editTextName.setText(user.getName());
        binding.editTextMail.setText(user.getEmail());
    }
}