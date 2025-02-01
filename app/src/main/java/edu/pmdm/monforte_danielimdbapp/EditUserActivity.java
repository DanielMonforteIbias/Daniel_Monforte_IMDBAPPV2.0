package edu.pmdm.monforte_danielimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.security.KeyPair;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.databinding.ActivityEditUserBinding;
import edu.pmdm.monforte_danielimdbapp.models.User;
import edu.pmdm.monforte_danielimdbapp.sync.UsersSync;
import edu.pmdm.monforte_danielimdbapp.utils.KeystoreManager;

public class EditUserActivity extends AppCompatActivity {
    private FavoritesDatabaseHelper dbHelper;
    private UsersSync userSync;
    private ActivityEditUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper=new FavoritesDatabaseHelper(this);
        userSync=new UsersSync(this);
        Intent intent=getIntent();
        String userId=intent.getStringExtra("userId");
        User user=dbHelper.getUser(userId);

        binding.editTextName.setText(user.getName());
        binding.editTextMail.setText(user.getEmail());
        Glide.with(this).load(user.getImage()).placeholder(R.drawable.usuario).into(binding.imgUsuario);
        if(user.getPhone()!=null && !user.getPhone().isEmpty()){
            String userPhone=KeystoreManager.decrypt(user.getPhone());
            if(!userPhone.isEmpty()){
                binding.countryCodePicker.setCountryForPhoneCode(Integer.parseInt(splitPhoneNumber(userPhone).first.replace("+",""))); //Seleccionamos el codigo del pais en el code picker
                binding.editTextPhone.setText(splitPhoneNumber(userPhone).second); //Ponemos el numero sin prefijo en el editText
            }
        }

        binding.btnGuardarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=binding.editTextName.getText().toString();
                String address=binding.editTextAddress.getText().toString();
                String phone=binding.editTextPhone.getText().toString();
                String image="";
                System.out.println(user.getName());
                System.out.println(user.getAddress());
                System.out.println(user.getPhone());
                System.out.println(user.getImage());
                if(!isValidPhoneNumber(phone,binding.countryCodePicker.getSelectedCountryNameCode())){
                    Toast.makeText(getApplicationContext(),"Numero de telefono no valido",Toast.LENGTH_SHORT).show();
                    return;
                }
                phone=formatPhoneNumber(phone,binding.countryCodePicker.getSelectedCountryNameCode());
                phone=KeystoreManager.encrypt(phone);
                user.setName(name);
                user.setAddress(address);
                user.setPhone(phone);
                user.setImage(image);
                dbHelper.updateUser(user);
                //userSync.updateUserInFirebase(user);
                Toast.makeText(getApplicationContext(),"Cambios guardados",Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK,intent);
                finish();
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
}