package com.example.snackstream.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;


import com.example.snackstream.databinding.ActivityAddPhotoBinding;

import java.util.HashMap;

public class addPhotoActivity extends AppCompatActivity {
    private ActivityAddPhotoBinding binding;
    ActivityResultLauncher<Intent> photoLauncher;
    Uri selectedImageUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        photoLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        binding.image.setImageURI(selectedImageUri);
                    }
                }
        );

        binding.addPhotoBtn.setOnClickListener(v -> {

            if (selectedImageUri == null) {
                //Pick image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                photoLauncher.launch(intent);

            } else {
                // Upload image
                uploadImageAndContinue();
            }
        });



        binding.skip.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void uploadImageAndContinue() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + userId + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        String imageUrl = uri.toString();

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .set(new HashMap<String, Object>() {{
                                    put("profileImage", imageUrl);
                                }});
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });

                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
}