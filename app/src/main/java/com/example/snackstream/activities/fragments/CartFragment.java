package com.example.snackstream.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.snackstream.R;
import com.example.snackstream.adapters.CartAdapter;
import com.example.snackstream.databinding.FragmentCartBinding;
import com.example.snackstream.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemChangeListener {

    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration cartListener;

    private double deliveryFee = 4.50;
    private double taxAndFees = 2.50;
    private double currentTotal = 0;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) navView.setVisibility(View.GONE); // hide navbar
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        setupRecyclerView();
        setupListeners();
        fetchCartItems();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter();
        adapter.setOnCartItemChangeListener(this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCartItems.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        binding.btnClear.setOnClickListener(v -> clearCart());



        binding.btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Bundle args = new Bundle();
                args.putFloat("totalAmount", (float) currentTotal);
                Navigation.findNavController(v).navigate(R.id.checkoutFragment, args);
            }
        });
    }

    private void fetchCartItems() {
        if (userId == null) return;

        cartListener = db.collection("users").document(userId).collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (value == null || binding == null) return;

                    cartItems.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        CartItem item = doc.toObject(CartItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            cartItems.add(item);
                        }
                    }
                    adapter.setCartItems(cartItems);
                    calculateSummary();
                });
    }

    private void calculateSummary() {
        if (binding == null) return;

        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        if (cartItems.isEmpty()) {
            currentTotal = 0;
            deliveryFee = 0;
            taxAndFees = 0;
        } else {
            deliveryFee = 4.50;
            taxAndFees = 2.50;
            currentTotal = subtotal + deliveryFee + taxAndFees;
        }

        binding.tvSubtotal.setText(String.format("$%.2f", subtotal));
        binding.tvDeliveryFee.setText(String.format("$%.2f", deliveryFee));
        binding.tvTax.setText(String.format("$%.2f", taxAndFees));
        binding.tvTotal.setText(String.format("$%.2f", currentTotal));
        binding.btnCheckout.setText(String.format("Proceed to Checkout  •  $%.2f", currentTotal));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        db.collection("users").document(userId).collection("cart")
                .document(item.getId())
                .update("quantity", newQuantity);
    }

    @Override
    public void onDeleteItem(CartItem item) {
        db.collection("users").document(userId).collection("cart")
                .document(item.getId())
                .delete();
    }

    private void clearCart() {
        if (userId == null) return;

        CollectionReference cartRef = db.collection("users").document(userId).collection("cart");
        cartRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }
            batch.commit().addOnSuccessListener(aVoid -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Cart cleared", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        if (cartListener != null) {
            cartListener.remove();
        }
        super.onDestroyView();
        binding = null;

    }
}