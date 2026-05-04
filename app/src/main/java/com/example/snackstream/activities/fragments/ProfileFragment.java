package com.example.snackstream.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.snackstream.activities.LoginActivity;
import com.example.snackstream.databinding.FragmentProfileBinding;
import com.example.snackstream.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
        observeUserData();
    }

    private void setupListeners() {


        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


        // Navigation to other screens can be added here
        // Example: binding.actionGrid.getChildAt(0).setOnClickListener(...)
    }

    private void observeUserData() {
        UserRepository userRepository = UserRepository.getInstance();
        
        userRepository.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.setUser(user);
                // The bio and stats will be automatically bound by DataBinding
            }
        });

        // Trigger fetch
        userRepository.fetchUser();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}