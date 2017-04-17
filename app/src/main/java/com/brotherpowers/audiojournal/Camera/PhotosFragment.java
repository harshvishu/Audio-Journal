package com.brotherpowers.audiojournal.Camera;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.brotherpowers.audiojournal.View.ContextRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PhotosFragment extends Fragment {

    @BindView(R.id.recycler_view)
    ContextRecyclerView recyclerView;

    private OnFragmentInteractionListener mListener;
    private PhotosAdapter photosAdapter;

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

    private RealmResults<Attachment> results;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /// Initialize realm
        Realm realm = Realm.getDefaultInstance();

        final long entry_id = getArguments().getLong(Constants.KEYS.entry_id, -1);


        if (entry_id > 0) {
            final DataEntry entry = DBHelper.findEntryForId(entry_id, realm).findFirst();        // Sync
            results = DBHelper.images(entry).findAllSortedAsync("created_at", Sort.DESCENDING);       // Async
        } else {
            results = DBHelper.filterFilesForType(FileUtils.Type.IMAGE,
                    RealmQuery.createQuery(realm, Attachment.class))
                    .findAllSortedAsync("created_at", Sort.DESCENDING);
        }
        photosAdapter = new PhotosAdapter(getContext(), results);

        /// Close realm
        realm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        ButterKnife.bind(this, view);


        /// Show placeholder if list is empty
        results.addChangeListener(changeSet -> showPlaceholder(changeSet.isEmpty()));

        // Set the  adapter to photos
        recyclerView.setAdapter(photosAdapter);
        return view;
    }

    /**
     * Show hide placeholder
     */
    public void showPlaceholder(boolean visible) {
        View view = getView();
        if (view != null) {
            ButterKnife.findById(view, R.id.placeholder_container).setVisibility(visible ? View.VISIBLE : View.GONE);
        }
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
