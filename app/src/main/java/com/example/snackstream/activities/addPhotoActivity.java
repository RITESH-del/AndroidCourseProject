package com.example.snackstream.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.snackstream.repository.UserRepository;
import com.example.snackstream.viewmodels.UserViewModel;
import com.example.snackstream.databinding.ActivityAddPhotoBinding;


public class addPhotoActivity extends AppCompatActivity {
    private ActivityAddPhotoBinding binding;
    ActivityResultLauncher<Intent> photoLauncher;
    Uri selectedImageUri;
    UserViewModel viewModel;
    boolean userUploadedImage = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        viewModel.getUser().observe(this, user -> {
            if (user == null) return;

            if (user != null && user.profileImage != null && !user.profileImage.isEmpty() && userUploadedImage) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });


        photoLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        binding.image.setImageURI(selectedImageUri);
                        viewModel.uploadProfileImage(selectedImageUri);
                        userUploadedImage = true;
                    }
                }
        );

        binding.addPhotoBtn.setOnClickListener(v -> {

            if (selectedImageUri == null) {
                //Pick image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                photoLauncher.launch(intent);
            }
        });



        binding.skip.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }



}