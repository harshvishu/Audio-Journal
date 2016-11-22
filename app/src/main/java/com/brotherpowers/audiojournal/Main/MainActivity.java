package com.brotherpowers.audiojournal.Main;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Recorder.RecordingActivity;
import com.brotherpowers.audiojournal.View.RecyclerviewDecor;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.Sort;

import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private DataEntryListAdapter dataEntryListAdapter;

    @BindView(R.id.image)
    AppCompatImageView backImageView;

    private final Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordingActivity.start(MainActivity.this, null, null);
            }
        });


        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.addItemDecoration(new RecyclerviewDecor());
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dataEntryListAdapter = new DataEntryListAdapter(this, realm.where(DataEntry.class)
                .findAllAsync()
                .sort("created_at", Sort.DESCENDING)
        );
        recyclerView.setAdapter(dataEntryListAdapter);

        Picasso.with(this)
                .load(R.drawable.b_2)
                .fit()
                .into(backImageView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_REC_PERMISSION: {
                boolean global_result = true;

                if (grantResults.length > 0) {

                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            System.out.println("permission granted");
                            global_result = false;
                        } else {
                            System.out.println("permission not granted");
                        }
                    }

                    /**
                     * If we have all the permissions
                     */
                    if (global_result) {
                        Toast.makeText(this, "You can record audio", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "App doesn't have all the required permissions", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

}
