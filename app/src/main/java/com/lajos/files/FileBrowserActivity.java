package com.lajos.files;

import static android.content.Intent.ACTION_INSTALL_PACKAGE;
import static android.content.Intent.ACTION_VIEW;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class FileBrowserActivity extends AppCompatActivity {

    ListView list;
    TextView itemcount;
    Button sortButton;
    Toolbar toolbar;
    String path;
    boolean show_hidden;
    boolean folders_before_files;
    SortMode current_sort_mode;
    boolean sort_direction; //false - ascending; true - descending //TODO

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        SharedPreferences sp = getSharedPreferences("fileman", MODE_PRIVATE);
        show_hidden = sp.getBoolean("show_hidden", false);
        folders_before_files = sp.getBoolean("folders_before_files", true);

        String def_sort = sp.getString("default_sort", "alphabetical");

        switch(def_sort) {
            case "alphabetical":
                current_sort_mode = SortMode.ALPHABETICAL;
                break;
            case "size":
                current_sort_mode = SortMode.SIZE;
                break;
            case "date":
                current_sort_mode = SortMode.DATE;
                break;
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this));
        toolbar.setNavigationOnClickListener(v -> this.finish());


        int color = SurfaceColors.SURFACE_2.getColor(this);
        this.getWindow().setStatusBarColor(color);
        getWindow().setNavigationBarColor(color);


        list = findViewById(R.id.file_browser_list);

        list.setOnItemClickListener(item_selected);

        itemcount = findViewById(R.id.item_count);
        sortButton = findViewById(R.id.sort_options_button);
        sortButton.setOnClickListener(view -> {
            openSortMenu();
        });
        sortButton.setText(current_sort_mode.toLocalizedString());

        path = getIntent().getExtras().getString("path");

        loadDir();
    }

    public void navigate() {
        loadDir();
    }

    public void loadDir() {
        File f = new File(path);
        getSupportActionBar().setTitle(Tools.getVolumeNameForPath(this, f.getPath()));
        File[] entries = f.listFiles();
        ArrayList<FileEntry> fel = new ArrayList<FileEntry>();
        ArrayList<FileEntry> dirs = new ArrayList<FileEntry>();
        ArrayList<FileEntry> files = new ArrayList<FileEntry>();

        //directories first
        for (int i = 0; i < entries.length; i++) {
            try {

                //skip hidden
                if (entries[i].getName().startsWith(".") && !show_hidden) {
                    continue;
                }

                if (entries[i].isDirectory()) {
                    dirs.add(new FileEntry(entries[i]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dirs.sort(current_sort_mode.toComparator());

        for (int i = 0; i < entries.length; i++) {
            try {

                //skip hidden
                if (entries[i].getName().startsWith(".")) {
                    continue;
                }

                if (!entries[i].isDirectory()) {
                    files.add(new FileEntry(entries[i]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        files.sort(current_sort_mode.toComparator());

        fel.addAll(dirs);
        fel.addAll(files);

        if (!folders_before_files) {
            fel.sort(current_sort_mode.toComparator());
        }

        itemcount.setText(getString(R.string.item_count, fel.size()));

        FileAdapter adapter = new FileAdapter(this, fel);
        list.setAdapter(adapter);
    }

    public AdapterView.OnItemClickListener item_selected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            FileEntry fe = (FileEntry) list.getItemAtPosition(i);

            if (fe.type == FileType.DIRECTORY) {

                File f = new File(fe.path);
                if (!f.canExecute() || !f.canRead()) {
                    showReadErrorDialog();
                    return;
                }
                path = fe.path;
                navigate();
            } else {
                openFile(getBaseContext(), new File(fe.path), fe.type);
            }

        }
    };

    public class FileAdapter extends ArrayAdapter<FileEntry> {

        public ArrayList<FileEntry> fel;
        Activity context;

        public FileAdapter (Activity context, ArrayList<FileEntry> f) {
            super(context, R.layout.file_entry, f);
            this.context = context;
            this.fel = f;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {

            FileEntry current = getItem(position);

            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.file_entry, null, true);

            ImageView iv = (ImageView) rowView.findViewById(R.id.file_icon);
            iv.setImageResource(current.type.toIcon());

            TextView fileName = (TextView) rowView.findViewById(R.id.file_title);
            TextView description = (TextView) rowView.findViewById(R.id.file_desc);

            fileName.setText(current.displayName);

            String d;

            if (current.type == FileType.DIRECTORY) {
                d = current.formatDate();
            } else {
                d = getString(R.string.file_desc_string, Tools.convertWithUnit(current.size), current.formatDate());
            }

            description.setText(d);

            return rowView;
        }
    }

    class FileEntry {
        public FileType type;
        public String displayName;
        public String path;
        public Date modified;
        public long size; //if file

        public FileEntry(File f) throws IOException {
            displayName = f.getName();
            path = f.getPath();
            modified = new Date(f.lastModified());

            if (f.isFile()) {
                size = Files.size(Paths.get(path));
            } else {
                size = 0;
            }

            if (f.isDirectory()) {
                type = FileType.DIRECTORY;
            } else {
                type = Tools.getFileType(getBaseContext(), f);
            }

        }

        public String toString() {
            return displayName;
        }

        public String formatDate() {
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());
            return dateFormat.format(modified);
        }
    }

    enum FileType {
        DIRECTORY,AUDIO,VIDEO,IMAGE,ARCHIVE,APK,DOCUMENT,MISC,PARENT,TEXT,TORRENT;

        public int toIcon() {
            switch (this) {
                case DIRECTORY:
                    return R.drawable.ic_round_folder_24;
                case AUDIO:
                    return R.drawable.ic_round_music_note_24;
                case VIDEO:
                    return R.drawable.ic_round_video_24;
                case IMAGE:
                    return R.drawable.ic_round_image_24;
                case TEXT:
                    return R.drawable.ic_round_text_24;
                case APK:
                    return R.drawable.ic_round_android_24;
                case ARCHIVE:
                    return R.drawable.ic_round_archive_24;
                case TORRENT:
                    return R.drawable.torrent;
            }
            return R.drawable.ic_round_file_misc_24;
        };

        public String toString() {
            switch (this) {
                case AUDIO:
                    return "audio/*";
                case VIDEO:
                    return "video/*";
                case IMAGE:
                    return "image/*";
                case TEXT:
                    return "text/*";
                case APK:
                    return "application/vnd.android.package-archive";
                case ARCHIVE:
                    return "application/zip";
                case TORRENT:
                    return "application/x-bittorrent";
            }
            return "application/octet-stream";
        };
    }

    enum SortMode {
        ALPHABETICAL,SIZE,DATE;

        public Comparator toComparator() {
            switch (this) {
                case ALPHABETICAL:
                    return Tools.Comparators.alphabetical_order;
                case SIZE:
                    return Tools.Comparators.size_order;
                case DATE:
                    return Tools.Comparators.date_order;
            }
            return null;
        }

        public int toLocalizedString() {
            switch (this) {
                case ALPHABETICAL:
                    return R.string.sort_by_name;
                case SIZE:
                    return R.string.sort_by_size;
                case DATE:
                    return R.string.sort_by_date;
            }
            return 0;
        }
    }

    @Override
    public void onBackPressed() {

        if (Tools.isPathRootOfVolume(this, path)) {
            super.onBackPressed();
        } else {
            File f = new File(path);
            path = f.getParent();
            loadDir();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openSortMenu() {
        PopupMenu pop = new PopupMenu(this, sortButton);
        pop.getMenuInflater().inflate(R.menu.sort_options, pop.getMenu());

        pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.sort_by_name:
                        current_sort_mode = SortMode.ALPHABETICAL;
                        break;
                    case R.id.sort_by_date:
                        current_sort_mode = SortMode.DATE;
                        break;
                    case R.id.sort_by_size:
                        current_sort_mode = SortMode.SIZE;
                        break;
                }

                sortButton.setText(current_sort_mode.toLocalizedString());
                loadDir();

                return false;
            }
        });

        pop.show();
    }

    public void showReadErrorDialog() {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle(R.string.directory_read_error)
                .setMessage(R.string.directory_read_error_description)
                .setIcon(R.drawable.ic_round_fatal_24)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show();

    }

    public void openFile(Context baseContext, File f, FileType type) {

        Uri u = Uri.fromFile(new File(f.getPath()));
        Intent i = new Intent(ACTION_VIEW);
        i.setDataAndType(u, type.toString());

        if (type == FileType.APK) {
            return;
        }

        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        }
    }
}