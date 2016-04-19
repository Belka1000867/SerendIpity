package com.example.bel.softwarefactory.ui.fragments;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.example.bel.softwarefactory.R;

import com.example.bel.softwarefactory.preferences.SharedPreferencesManager;
import com.example.bel.softwarefactory.ui.adapters.AudioRecordsRecyclerViewAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.fragment_list_of_recordings)
public class RecordingListFragment extends BaseFragment {

    @ViewById
    protected RecyclerView audioRecords_recyclerView;

    @Bean
    protected SharedPreferencesManager sharedPreferencesManager;

    @AfterViews
    protected void afterViews() {
        final LinearLayoutManager audioRecordsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        AudioRecordsRecyclerViewAdapter audioRecordsRecyclerViewAdapter = new AudioRecordsRecyclerViewAdapter(getActivity(), sharedPreferencesManager.getAudioRecordsList());
        audioRecords_recyclerView.setLayoutManager(audioRecordsLayoutManager);
        audioRecords_recyclerView.setAdapter(audioRecordsRecyclerViewAdapter);
    }

}