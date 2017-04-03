package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

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
    RecyclerView _recyclerViewReminders;

    private ReminderAdapter reminderAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        final Realm realm = Realm.getDefaultInstance();

        System.out.println(" Reminder count :> " + realm.where(Reminder.class).count());
        System.out.println(" Reminder count NO NULL :> " + realm.where(Reminder.class).isNotNull("remind_at").count());

        reminderAdapter = new ReminderAdapter(getContext(), realm.where(Reminder.class).isNotNull("remind_at").findAllAsync());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reminder_list, container, false);
        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        System.out.println(">>>>>>> VIEW CREATED ");
        setCurrentTime();


        // Set Receiver
        getContext().registerReceiver(timeChangeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        //Set Adapter
        _recyclerViewReminders.setAdapter(reminderAdapter);
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
