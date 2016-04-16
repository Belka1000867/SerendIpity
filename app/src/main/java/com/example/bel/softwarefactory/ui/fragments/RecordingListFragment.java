package com.example.bel.softwarefactory.ui.fragments;

import android.app.ActionBar;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.api.Api;
import com.example.bel.softwarefactory.entities.AudioRecordEntity;
import com.example.bel.softwarefactory.ui.adapters.AudioRecordsRecyclerViewAdapter;
import com.example.bel.softwarefactory.utils.AppConstants;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_list_of_recordings)
public class RecordingListFragment extends BaseFragment {

    @ViewById
    protected RecyclerView audioRecords_recyclerView;

    @AfterViews
    protected void afterViews() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Recording list");
        }

        showProgress(getActivity().getString(R.string.downloading));
        Api api = new Api();
        api.getAudioRecordsList()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(this::recordsLoaded, this::handleError);
    }

    private void recordsLoaded(List<AudioRecordEntity> audioRecordEntities) {
        hideProgress();
        final LinearLayoutManager audioRecordsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        AudioRecordsRecyclerViewAdapter audioRecordsRecyclerViewAdapter = new AudioRecordsRecyclerViewAdapter(getActivity(), audioRecordEntities);
        audioRecords_recyclerView.setLayoutManager(audioRecordsLayoutManager);
        audioRecords_recyclerView.setAdapter(audioRecordsRecyclerViewAdapter);
    }

}