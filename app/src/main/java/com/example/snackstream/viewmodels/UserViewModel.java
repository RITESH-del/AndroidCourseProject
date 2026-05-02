package com.example.snackstream.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.snackstream.models.User;
import com.example.snackstream.repository.UserRepository;

public class UserViewModel extends ViewModel {

    private final UserRepository repo = UserRepository.getInstance();

    public LiveData<User> getUser() {
        return repo.getUser();
    }

    public void fetchUser() {
        repo.fetchUser();
    }

    public void uploadProfileImage(Uri uri) {
        repo.uploadProfileImage(uri);
    }
}