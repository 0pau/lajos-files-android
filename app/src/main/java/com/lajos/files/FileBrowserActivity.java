package com.lajos.files;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FileBrowserActivity extends AppCompatActivity {

    ListView list;
    TextView itemcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);


        list = findViewById(R.id.file_browser_list);
        itemcount = findViewById(R.id.item_count);

        File f = new File(getIntent().getExtras().getString("path"));
        File[] entries = f.listFiles();

        ArrayList<FileEntry> fel = new ArrayList<FileEntry>();

        for (int i = 0; i < entries.length; i++) {
            try {
                fel.add(new FileEntry(entries[i]));
                Log.i("FILE", entries[i].getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        itemcount.setText(getString(R.string.item_count, entries.length));

        FileAdapter adapter = new FileAdapter(this, fel);
        list.setAdapter(adapter);


    }

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

            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.file_entry, null, true);

            TextView fileName = (TextView) rowView.findViewById(R.id.file_title);
            TextView description = (TextView) rowView.findViewById(R.id.file_desc);

            fileName.setText(getItem(position).displayName);
            description.setText(Tools.df.format(getItem(position).size) + " B");

            return rowView;
        }
    }

    class FileEntry {
        public FileType type;
        public String displayName;
        public String path;
        public long modified;
        public long size; //if file

        public FileEntry(File f) throws IOException {
            displayName = f.getName();
            path = f.getPath();
            modified = f.lastModified();

            if (f.isFile()) {
                size = Files.size(Paths.get(path));
            } else {
                size = 0;
            }

            if (f.isDirectory()) {
                type = FileType.DIRECTORY;
            } else {
                type = FileType.MISC;
            }

        }
    }

    enum FileType {
        DIRECTORY,AUDIO,VIDEO,IMAGE,ARCHIVE,APK,DOCUMENT,MISC,PARENT
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
}