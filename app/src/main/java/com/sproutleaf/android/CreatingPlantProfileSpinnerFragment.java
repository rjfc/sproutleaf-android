package com.sproutleaf.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CreatingPlantProfileSpinnerFragment extends DialogFragment {
    public void CreatingPlantProfileSpinnerFragment() {
    }

    // Function for giving a name to the dialog fragment
    public static CreatingPlantProfileSpinnerFragment newInstance(String title) {
        CreatingPlantProfileSpinnerFragment frag = new CreatingPlantProfileSpinnerFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);
        this.getDialog().setCanceledOnTouchOutside(false); // Do not cancel dialog when outside is touched
        View rootView = inflater.inflate(R.layout.fragment_creating_plant_profile_spinner, container);
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
