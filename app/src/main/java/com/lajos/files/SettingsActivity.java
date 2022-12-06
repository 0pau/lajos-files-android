package com.lajos.files;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupMenu;

public class SettingsActivity extends AppCompatActivity {

    CheckBox show_hidden_files;
    CheckBox folders_before_files;
    Button def_sort_button;
    SharedPreferences.Editor spe;
    SharedPreferences sp;

    FileBrowserActivity.SortMode current_sort_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences("fileman", MODE_PRIVATE);
        spe = sp.edit();

        show_hidden_files = (CheckBox) findViewById(R.id.hidden_files_checkbox);
        folders_before_files = (CheckBox) findViewById(R.id.folders_before_files_checkbox);
        def_sort_button = (Button) findViewById(R.id.settings_def_sort_button);

        show_hidden_files.setChecked(sp.getBoolean("show_hidden", false));
        folders_before_files.setChecked(sp.getBoolean("folders_before_files", true));

        show_hidden_files.setOnCheckedChangeListener((compoundButton, b) -> spe.putBoolean("show_hidden", b));
        folders_before_files.setOnCheckedChangeListener((compoundButton, b) -> spe.putBoolean("folders_before_files", b));

        def_sort_button.setOnClickListener(view -> showSortSettingMenu());

        String def_sort = sp.getString("default_sort", "alphabetical");

        switch(def_sort) {
            case "alphabetical":
                current_sort_mode = FileBrowserActivity.SortMode.ALPHABETICAL;
                break;
            case "size":
                current_sort_mode = FileBrowserActivity.SortMode.SIZE;
                break;
            case "date":
                current_sort_mode = FileBrowserActivity.SortMode.DATE;
                break;
        }

        def_sort_button.setText(current_sort_mode.toLocalizedString());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spe.apply();
    }

    public void showSortSettingMenu() {
        PopupMenu pop = new PopupMenu(this, def_sort_button);
        pop.getMenuInflater().inflate(R.menu.sort_options, pop.getMenu());

        pop.setOnMenuItemClickListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.sort_by_name:
                    spe.putString("default_sort", "alphabetical");
                    current_sort_mode = FileBrowserActivity.SortMode.ALPHABETICAL;
                    break;
                case R.id.sort_by_date:
                    spe.putString("default_sort", "date");
                    current_sort_mode = FileBrowserActivity.SortMode.DATE;
                    break;
                case R.id.sort_by_size:
                    spe.putString("default_sort", "size");
                    current_sort_mode = FileBrowserActivity.SortMode.SIZE;
                    break;
            }

            def_sort_button.setText(current_sort_mode.toLocalizedString());

            return false;
        });

        pop.show();
    }
}