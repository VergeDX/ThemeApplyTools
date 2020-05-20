package org.hydev.themeapplytools.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.hydev.themeapplytools.R;
import org.hydev.themeapplytools.utils.ThemeUtils;

public class LearnHowActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_how);

        ThemeUtils.darkMode(this);
    }
}
