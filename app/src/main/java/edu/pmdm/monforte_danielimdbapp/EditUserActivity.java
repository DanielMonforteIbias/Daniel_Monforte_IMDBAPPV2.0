package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.pmdm.monforte_danielimdbapp.database.UsersDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityEditUserBinding;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityMainBinding;
import edu.pmdm.monforte_danielimdbapp.models.User;

public class EditUserActivity extends AppCompatActivity {
    private UsersDatabaseHelper dbHelper;
    private ActivityEditUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper=new UsersDatabaseHelper(this);
        Intent intent=getIntent();
        String userId=intent.getStringExtra("userId");
        User user=dbHelper.getUser(userId);
        binding.editTextName.setText(user.getName());
        binding.editTextMail.setText(user.getEmail());
    }
}