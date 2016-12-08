package com.brotherpowers.hvcamera;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class HVBaseFragment extends Fragment implements View.OnClickListener {

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    public static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    public static final int MAX_PREVIEW_HEIGHT = 1080;

    public CameraFragmentInteractionInterface interactionInterface;
    public AutoFitTextureView textureView;
    public AppCompatImageButton buttonFlash;
    public AppCompatImageButton buttonSwitchCamera;
    public AppCompatImageButton buttonPicture;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            interactionInterface = (CameraFragmentInteractionInterface) getActivity();

        } catch (ClassCastException e) {
            throw new RuntimeException("Implement CameraFragmentInteractionInterface in activity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hvcamera, container, false);

        // Init Views
        textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        buttonFlash = (AppCompatImageButton) view.findViewById(R.id.flash);
        buttonSwitchCamera = (AppCompatImageButton) view.findViewById(R.id.switch_camera);
        buttonPicture = (AppCompatImageButton) view.findViewById(R.id.picture);

        // OnClick
        buttonFlash.setOnClickListener(this);
        buttonPicture.setOnClickListener(this);
        buttonSwitchCamera.setOnClickListener(this);

        return view;
    }
}
