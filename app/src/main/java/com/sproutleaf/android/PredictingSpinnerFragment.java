package com.sproutleaf.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PredictingSpinnerFragment extends DialogFragment {
    public void PredictingSpinnerFragment() {
    }

    // Function for giving a name to the dialog fragment
    public static PredictingSpinnerFragment newInstance(String title) {
        PredictingSpinnerFragment frag = new PredictingSpinnerFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().setCanceledOnTouchOutside(false); // Do not cancel dialog when outside is touched
        View rootView = inflater.inflate(R.layout.fragment_predicting_spinner, container);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void dismissDialog() {
        getDialog().dismiss();
    }
}
