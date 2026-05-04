package com.example.snackstream.repository;

import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.example.snackstream.models.ReelModel;
import com.google.firebase.firestore.DocumentSnapshot;

public class ReelRepository {

    public LiveData<PagingData<ReelModel>> getReelsLiveData() {

        Pager<DocumentSnapshot, ReelModel> pager = new Pager<>(
                new PagingConfig(5),
                () -> new ReelPagingSource()   // ✅ lambda instead of method reference
        );

        return PagingLiveData.getLiveData(pager);
    }
}