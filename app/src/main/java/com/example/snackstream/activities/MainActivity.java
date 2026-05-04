package com.example.snackstream.activities;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.snackstream.R;
import com.example.snackstream.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private ActivityResultLauncher<String> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.navView, navController);

            videoPickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            // ✅ Navigate to UploadReelFragment with the video URI
                            Bundle args = new Bundle();
                            args.putString("videoUri", uri.toString());
                            navController.navigate(R.id.uploadReelFragment, args);
                        }
                    }
            );

            binding.navView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.uploadReel) {
                    videoPickerLauncher.launch("video/*"); // 🎬 open gallery
                    return false; // don't navigate automatically
                }
                return NavigationUI.onNavDestinationSelected(item, navController);
            });
        }
    }
}