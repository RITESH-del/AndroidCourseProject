package com.example.snackstream.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends ViewModel {
    public MutableLiveData<String> username = new MutableLiveData<>("");
    public MutableLiveData<String> password = new MutableLiveData<>("");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    public MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    public MutableLiveData<Boolean> googleSignInEvent = new MutableLiveData<>();

    public void onSignInClicked() {
        String emailVal = username.getValue();
        String passVal = password.getValue();

        if (emailVal == null || passVal == null || emailVal.isEmpty() || passVal.isEmpty()) {
            isSuccess.setValue(false);
            return;
        }

        // Try to Sign In
        auth.signInWithEmailAndPassword(emailVal, passVal)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isSuccess.setValue(true);
                    } else {
                        // If user doesn't exist, try to Create Account (Optional logic)
                        auth.createUserWithEmailAndPassword(emailVal, passVal)
                                .addOnCompleteListener(createTask -> {
                                    isSuccess.setValue(createTask.isSuccessful());
                                });
                    }
                });
    }

    public void onGoogleSignInClicked() {
        googleSignInEvent.setValue(true);
    }
}
