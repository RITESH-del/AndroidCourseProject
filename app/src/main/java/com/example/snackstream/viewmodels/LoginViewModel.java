package com.example.snackstream.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginViewModel extends ViewModel {
    public MutableLiveData<String> username = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    public MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();

    public MutableLiveData<Boolean> googleSignInEvent = new MutableLiveData<>();




    public void onSignInClicked() {
        String emailVal = username.getValue();
        String passVal = password.getValue();

        if (emailVal == null || passVal == null ||
                emailVal.isEmpty() || passVal.isEmpty()) {
            isSuccess.setValue(false);
            return;
        }

        auth.createUserWithEmailAndPassword(emailVal, passVal)
                .addOnCompleteListener(task -> {
                    isSuccess.setValue(task.isSuccessful());
                });
    }
    

    public void onGoogleSignInClicked() {
        googleSignInEvent.setValue(true);
    }

}
