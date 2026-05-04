package com.example.snackstream.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.example.snackstream.models.ReelModel;
import com.example.snackstream.repository.ReelRepository;

public class ReelViewModel extends ViewModel {

    public LiveData<PagingData<ReelModel>> reelsLiveData;

    public ReelViewModel() {
        ReelRepository repository = new ReelRepository();

        reelsLiveData = PagingLiveData.cachedIn(
                repository.getReelsLiveData(),
                ViewModelKt.getViewModelScope(this)
        );
    }
}