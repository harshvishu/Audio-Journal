package com.brotherpowers.audiojournal.Camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Realm.RFile;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.hvcamera.CameraFragmentInteractionInterface;
import com.brotherpowers.hvcamera.CameraOld.CameraOld;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class CameraActivity extends AppCompatActivity implements CameraFragment.CameraInterface, CameraFragmentInteractionInterface {
    private static final String ARG_ID = "ARG_ID";

    public static void start(Activity parentActivity, long id) {
        Intent intent = new Intent(parentActivity, CameraActivity.class);
        intent.putExtra(ARG_ID, id);
        parentActivity.startActivity(intent);
    }

    private CameraOld cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        long id = getIntent().getLongExtra(ARG_ID, -1L);
        if (id == -1L) {
            throw new RuntimeException("Invalid ID");
        }

        cameraFragment = (CameraOld) getSupportFragmentManager().findFragmentById(R.id.fragment_camera);
//        cameraFragment.setCameraInterface(this);

    }


    @Override
    public void onCapturePicture(Bitmap bitmap) {

        long id = getIntent().getLongExtra(ARG_ID, -1L);
        if (id == -1L) {
            throw new RuntimeException("Invalid ID");
        }

        Realm realm = Realm.getDefaultInstance();
        DataEntry dataEntry = realm.where(DataEntry.class).equalTo("id", id).findFirst();

        File file = FileUtils.sharedInstance.getFile(FileUtils.Type.IMAGE, null, this);

        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        RFile rFile = new RFile();
        rFile.generateId(realm);
        rFile.setFileType(FileUtils.Type.IMAGE);
        rFile.setFileName(file.getName());

        realm.executeTransaction(r -> {
            RFile managedFile = r.copyToRealmOrUpdate(rFile);
            dataEntry.getAttachments().add(managedFile);
        });

        Toast.makeText(this, "Added Image", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onImageCaptured(byte[] data, int angle) {

    }

    @Override
    public void onImageCaptured(Bitmap bitmap) {
        onCapturePicture(bitmap);
    }
}
