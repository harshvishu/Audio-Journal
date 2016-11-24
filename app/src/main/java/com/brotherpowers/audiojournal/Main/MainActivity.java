package com.brotherpowers.audiojournal.Main;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Recorder.AudioPlayer;
import com.brotherpowers.audiojournal.Recorder.RecordingActivity;
import com.brotherpowers.audiojournal.View.RecyclerviewDecor;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;

public class MainActivity extends AppCompatActivity implements DataEntryListAdapter.Callback {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private DataEntryListAdapter dataEntryListAdapter;

    @BindView(R.id.image)
    AppCompatImageView backImageView;

    @BindView(R.id.header_image)
    AppCompatImageView headerImageView;

    private final Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(getContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordingActivity.start(getContext());
            }
        });


        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(llm);
        recyclerView.addItemDecoration(new RecyclerviewDecor());
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dataEntryListAdapter = new DataEntryListAdapter(getContext(), realm.where(DataEntry.class)
                .findAllAsync()
                .sort("created_at", Sort.DESCENDING)
        );
        recyclerView.setAdapter(dataEntryListAdapter);

        Paint p = new Paint();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                System.out.println("..... swiped ");
                int position = viewHolder.getAdapterPosition();
                DataEntry item = dataEntryListAdapter.getItem(position);
                if (item != null) {
                    actionDelete(item.getId());
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (dataEntryListAdapter.getActualSizeOfData() > 0) {

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                        View itemView = viewHolder.itemView;
                        float height = (float) itemView.getBottom() - (float) itemView.getTop();
                        float width = height / 3;
                        Drawable icon;

                        if (dX > 0) {
                            p.setColor(Color.parseColor("#388E3C"));
                            RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                            c.drawRect(background, p);

                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_mode_edit);
                            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);


                            int size = Math.max(icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                            int right = (int) background.right - size;
                            int left = right - size;
                            int top = (int) background.centerY() - size / 2;
                            int bottom = top + size;

                            icon.setBounds(left, top, right, bottom);
                            icon.draw(c);

                        } else {

                        /*// Stop at 1/4
                        if (Math.abs(dX) > c.getWidth() / 4) {
                            dX = -c.getWidth() / 4;
                        }*/

                            p.setColor(Color.parseColor("#D32F2F"));
                            RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                            c.drawRect(background, p);

                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_delete);
                            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

                            int size = Math.max(icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                            int left = (int) background.left + size;
                            int top = (int) background.centerY() - size / 2;
                            int right = left + size;
                            int bottom = top + size;

                            icon.setBounds(left, top, right, bottom);
                            icon.draw(c);


                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        Picasso.with(getContext())
                .load(R.drawable.b_2)
                .fit()
                .into(backImageView);

        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_niumpa);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            drawable.setColorFilter(Color.parseColor("#404CAF50"), PorterDuff.Mode.OVERLAY);
        }
        headerImageView.setImageDrawable(drawable);

      /*  Picasso.with(getContext())
                .load(R.drawable.ic_niumpa)
                .into(headerImageView);*/

    }

    @NonNull
    private MainActivity getContext() {
        return MainActivity.this;
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
                        Toast.makeText(getContext(), "You can record audio", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "App doesn't have all the required permissions", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void actionDelete(long id) {
        System.out.println(".....");
        realm.executeTransaction(r -> {
            RealmResults<DataEntry> dataEntries = r.where(DataEntry.class).equalTo("id", id).findAll();
            if (!dataEntries.isEmpty()) {
                for (DataEntry dataEntry : dataEntries) {
                    dataEntry.deleteFromRealm();
                    System.out.println("item removed");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (AudioPlayer.sharedInstance.isPlaying()) {
            AudioPlayer.sharedInstance.cancel();
        } else {
            super.onBackPressed();
        }
    }
}
