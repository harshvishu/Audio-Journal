package com.brotherpowers.hvcamera.Camera21;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.brotherpowers.hvcamera.CameraOld.CameraOld;
import com.brotherpowers.hvcamera.HVBaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class Camera21 extends HVBaseFragment {


    public Camera21() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment Camera21.
     */
    public static Camera21 newInstance(boolean frontCameraSupported, boolean isFrontCamera, CameraOld.FlashMode flashMode) {
        Camera21 fragment = new Camera21();
        Bundle args = new Bundle();


        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onClick(View view) {

    }
}
