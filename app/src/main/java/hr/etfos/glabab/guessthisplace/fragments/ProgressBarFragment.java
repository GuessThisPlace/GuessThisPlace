package hr.etfos.glabab.guessthisplace.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hr.etfos.glabab.guessthisplace.R;

public class ProgressBarFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View Layout = inflater.inflate(R.layout.fragment_progress_bar, null);
        return Layout;
    }
}
