package com.brotherpowers.audiojournal.Camera;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Records.PhotosAdapter;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.Utils.DBHelper;
import com.brotherpowers.audiojournal.Utils.FileUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PhotosFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public PhotosFragment() {
        // Required empty public constructor
    }

    public static PhotosFragment newInstance(@Nullable Long entry_id) {

        Bundle args = new Bundle();
        if (entry_id != null) {
            args.putLong(Constants.KEYS.entry_id, entry_id);
        }
        PhotosFragment fragment = new PhotosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;


    private PhotosAdapter photosAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long entry_id = getArguments().getLong(Constants.KEYS.entry_id, -1);
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<Attachment> attachments;

        if (entry_id > 0) {
            final DataEntry entry = DBHelper.findEntryForId(entry_id, realm).findFirst();        // Sync
            attachments = DBHelper.images(entry).findAllAsync();       // Async
        } else {

            attachments = DBHelper.filterFilesForType(FileUtils.Type.IMAGE,
                    RealmQuery.createQuery(realm, Attachment.class))
                    .findAllAsync();
        }

        photosAdapter = new PhotosAdapter(getContext(), attachments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        ButterKnife.bind(this, view);

        // Set the  adapter to photos
        recyclerView.setAdapter(photosAdapter);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * // TODO: 2/22/17 Change thi stub code
     */
    public interface OnFragmentInteractionListener {
        void openDetailedImageGallery(long entry_id, long attachment_id);
    }
}
