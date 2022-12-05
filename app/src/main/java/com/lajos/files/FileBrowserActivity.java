package com.lajos.files;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class FileBrowserActivity extends AppCompatActivity {

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        list = findViewById(R.id.file_browser_list);

        File f = new File("/storage/emulated/0");

        String[] entries = f.list();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.file_entry, R.id.file_title, entries);

        list.setAdapter(adapter);
    }

    class FileEntry {
        public FileType type;
        public String displayName;
        public String path;
        public long modified;
        public long size; //if file
    }

    enum FileType {
        DIRECTORY,AUDIO,VIDEO,IMAGE,ARCHIVE,APK,DOCUMENT,MISC
    }
}