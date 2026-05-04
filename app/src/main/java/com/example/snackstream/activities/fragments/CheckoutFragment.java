package com.example.snackstream.activities.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.snackstream.R;
import com.example.snackstream.databinding.FragmentCheckoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private float totalAmount;

    View navView;

    public CheckoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalAmount = getArguments().getFloat("totalAmount");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) navView.setVisibility(View.GONE); // hide navbar
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        binding.setTotalAmount(String.format("$%.2f", totalAmount));

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        binding.optionUPI.setOnClickListener(v -> selectPaymentMethod(R.id.rbUPI));
        binding.optionCard.setOnClickListener(v -> selectPaymentMethod(R.id.rbCard));
        binding.optionCOD.setOnClickListener(v -> selectPaymentMethod(R.id.rbCOD));

        binding.rbUPI.setOnClickListener(v -> selectPaymentMethod(R.id.rbUPI));
        binding.rbCard.setOnClickListener(v -> selectPaymentMethod(R.id.rbCard));
        binding.rbCOD.setOnClickListener(v -> selectPaymentMethod(R.id.rbCOD));

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void selectPaymentMethod(int checkedId) {
        binding.rbUPI.setChecked(checkedId == R.id.rbUPI);
        binding.rbCard.setChecked(checkedId == R.id.rbCard);
        binding.rbCOD.setChecked(checkedId == R.id.rbCOD);
        
        // Highlight the selected option (optional UI enhancement)
        binding.optionUPI.setAlpha(checkedId == R.id.rbUPI ? 1.0f : 0.8f);
        binding.optionCard.setAlpha(checkedId == R.id.rbCard ? 1.0f : 0.8f);
        binding.optionCOD.setAlpha(checkedId == R.id.rbCOD ? 1.0f : 0.8f);
    }

    private void placeOrder() {
        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPlaceOrder.setEnabled(false);
        binding.btnPlaceOrder.setText("PLACING ORDER...");

        // Fetch cart items to save them in the order
        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        items.add(doc.getData());
                    }

                    if (items.isEmpty()) {
                        Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                        binding.btnPlaceOrder.setEnabled(true);
                        binding.btnPlaceOrder.setText("PLACE ORDER");
                        return;
                    }

                    saveOrderToFirestore(items);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnPlaceOrder.setEnabled(true);
                    binding.btnPlaceOrder.setText("PLACE ORDER");
                });
    }

    private void saveOrderToFirestore(List<Map<String, Object>> items) {
        String orderId = db.collection("orders").document().getId();
        
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("items", items);
        order.put("totalAmount", totalAmount);
        order.put("status", "Placed");
        order.put("timestamp", System.currentTimeMillis());
        order.put("paymentMethod", getSelectedPaymentMethod());
        order.put("address", "452 West 19th St, Apt 4B, Chelsea, New York, NY 10011");

        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    clearCartAndFinish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnPlaceOrder.setEnabled(true);
                    binding.btnPlaceOrder.setText("PLACE ORDER");
                });
    }

    private String getSelectedPaymentMethod() {
        if (binding.rbUPI.isChecked()) return "UPI";
        if (binding.rbCard.isChecked()) return "Card";
        return "COD";
    }

    private void clearCartAndFinish() {
        CollectionReference cartRef = db.collection("users").document(userId).collection("cart");
        cartRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }
            batch.commit().addOnSuccessListener(aVoid -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Order Placed Successfully!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
                }
            }).addOnFailureListener(e -> {
                // Even if clearing cart fails, order is placed
                if (getContext() != null) {
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (navView != null) navView.setVisibility(View.VISIBLE);
    }
}
