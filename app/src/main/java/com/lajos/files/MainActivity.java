package com.lajos.files;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;

import java.util.List;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 230;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermission() != true) {
            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_text)
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermission();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            show_permission_error();
                        }
                    })
                    .setCancelable(false)
                    .setIcon(R.drawable.perm_icon)
                    .show();
        } else {
            getData();
        }

    }

    public void getData() {
        LinearLayout storages = (LinearLayout) findViewById(R.id.storages);
        StorageManager sm = (StorageManager) this.getSystemService(this.STORAGE_SERVICE);
        List<StorageVolume> devices = sm.getStorageVolumes();

        LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < devices.size(); i++) {

            View cv = inf.inflate(R.layout.device_card_template, null);

            TextView tv = (TextView) cv.findViewById(R.id.deviceName);
            tv.setText(devices.get(i).getDescription(this));

            TextView tv2 = (TextView) cv.findViewById(R.id.free_space);
            tv2.setText(getString(R.string.free_space, Tools.convertWithUnit(devices.get(i).getDirectory().getFreeSpace())));

            ImageView iv = (ImageView) cv.findViewById(R.id.device_icon);

            if (devices.get(i).isPrimary()) {
                iv.setImageResource(R.drawable.ic_round_phone_24);
            } else if (devices.get(i).isRemovable()) {
                iv.setImageResource(R.drawable.ic_round_sd_storage_24);
            }

            CardView card = (CardView) cv.findViewById(R.id.device_card);

            int finalI = i;
            card.setOnClickListener(view -> {
                Intent in = new Intent(getApplicationContext(), FileBrowserActivity.class);
                in.putExtra("path", devices.get(finalI).getDirectory().getPath());
                startActivity(in);
            });

            storages.addView(cv);
        }

        //CardView cv = new CardView(this);
    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    show_permission_error();
                }
            }
        }
    }

    public void show_permission_error() {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setCancelable(false)
                .setTitle(R.string.permission_error_title)
                .setMessage(R.string.permission_error_text)
                .setNegativeButton(R.string.close_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .setIcon(R.drawable.ic_round_error_24)
                .show();
    }



}