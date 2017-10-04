package com.example.srikate.ibeacondemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by srikate on 10/4/2017 AD.
 */

public class BeaconSimulatorFragment extends Fragment {

    public static BeaconSimulatorFragment newInstance(){
        return new BeaconSimulatorFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.beacon_simu_fragment, container, false);
        return v;
    }
}
