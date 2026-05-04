package com.example.snackstream.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

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

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.navView, (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottomInset
            );
            return insets;
        });

        // for testing
        FirebaseAuth.getInstance().signOut();

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.navView, navController);
        }


        // TOP NAVBAR CLICK HANDLING


        binding.tabFollowing.setOnClickListener(v -> {
            binding.tabFollowing.setTextColor(getResources().getColor(android.R.color.white));
            binding.tabForYou.setTextColor(getResources().getColor(android.R.color.darker_gray));

            Toast.makeText(this, "Following clicked", Toast.LENGTH_SHORT).show();
        });

        binding.tabForYou.setOnClickListener(v -> {
            binding.tabForYou.setTextColor(getResources().getColor(android.R.color.white));
            binding.tabFollowing.setTextColor(getResources().getColor(android.R.color.darker_gray));

            Toast.makeText(this, "For You clicked", Toast.LENGTH_SHORT).show();
        });

        binding.btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
