package com.example.bel.softwarefactory.ui.fragments;

import android.app.ActionBar;

import com.example.bel.softwarefactory.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_list_of_recordings)
public class RecordingListFragment extends BaseFragment {

    @AfterViews
    protected void afterViews() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Recording list");
        }
    }

}