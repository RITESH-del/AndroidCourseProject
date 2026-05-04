package com.example.snackstream.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.example.snackstream.models.ReelModel;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ReelPagingSource extends ListenableFuturePagingSource<DocumentSnapshot, ReelModel> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @NonNull
    @Override
    public ListenableFuture<LoadResult<DocumentSnapshot, ReelModel>> loadFuture(@NonNull LoadParams<DocumentSnapshot> params) {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Query query = db.collection("reels")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(params.getLoadSize());

            if (params.getKey() != null) {
                query = query.startAfter(params.getKey());
            }

            query.get().addOnSuccessListener(snapshot -> {
                try {
                    List<ReelModel> reels = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ReelModel reel = doc.toObject(ReelModel.class);
                        if (reel != null) {
                            reels.add(reel);
                        }
                    }

                    DocumentSnapshot lastDoc = snapshot.getDocuments().isEmpty()
                            ? null
                            : snapshot.getDocuments().get(snapshot.getDocuments().size() - 1);

                    completer.set(new LoadResult.Page<>(
                            reels,
                            null,
                            lastDoc
                    ));
                } catch (Exception e) {
                    completer.setException(e);
                }
            }).addOnFailureListener(completer::setException);

            return "loadReels";
        });
    }

    @Nullable
    @Override
    public DocumentSnapshot getRefreshKey(@NonNull PagingState<DocumentSnapshot, ReelModel> state) {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // For simplicity, we can return null here as it's common for initial implementations
        return null;
    }
}
