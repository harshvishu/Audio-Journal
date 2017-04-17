package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.View.ContextRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReminderListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ReminderListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public ReminderListFragment() {
        // Required empty public constructor
    }

    public static ReminderListFragment newInstance() {

        Bundle args = new Bundle();

        ReminderListFragment fragment = new ReminderListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.current_time)
    TextView _labelCurrentTime;

    @BindView(R.id.recycler_view)
    ContextRecyclerView _recyclerViewReminders;

    private ReminderAdapter reminderAdapter;
    private RealmResults<Reminder> results;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        /// Instantiate realm
        final Realm realm = Realm.getDefaultInstance();

        /// fetch results
        results = realm.where(Reminder.class).isNotNull("remind_at").findAllAsync();

        /// Initialize adapter
        reminderAdapter = new ReminderAdapter(getContext(), results);

        /// Close realm
        realm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_reminders, container, false);
        ButterKnife.bind(this, view);

        /// Show placeholder if list is empty
        results.addChangeListener(changeSet -> showPlaceholder(changeSet.isEmpty()));

        //Set Adapter
        _recyclerViewReminders.setAdapter(reminderAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setCurrentTime();
        // Set Receiver
        getContext().registerReceiver(timeChangeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onResume() {
        /// Initially show/hide placeholder
        showPlaceholder(results.isEmpty());
        super.onResume();
    }

    /**
     * Show hide placeholder
     */
    public void showPlaceholder(boolean visible) {
        final View view = getView();
        if (view != null) {
            ButterKnife.findById(view, R.id.placeholder_container).setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private final TimeChangeReceiver timeChangeReceiver = new TimeChangeReceiver(_labelCurrentTime) {
        @Override
        public void onReceive(Context context, Intent intent) {
            setCurrentTime();
        }
    };

    /**
     * Set the current time into {@link #_labelCurrentTime}
     */
    private void setCurrentTime() {
        _labelCurrentTime.setText(DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
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

    @Override
    public void onDestroyView() {
        // Unregister Time Change Receiver
        getContext().unregisterReceiver(timeChangeReceiver);
        super.onDestroyView();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
