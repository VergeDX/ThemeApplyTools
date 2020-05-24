package org.hydev.themeapplytools.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.hydev.themeapplytools.databinding.ActivityMainBinding;
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils;
import org.hydev.themeapplytools.utils.ThemeUtils;

public class MainActivity extends AppCompatActivity {
    private static final String GITHUB_URL = "https://github.com/VergeDX/ThemeApplyTools";
    private static final String ME_COOLAPK_URL = "https://coolapk.com/u/506843";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        // Open dark mode will reCreate activity.
        ThemeUtils.darkMode(this);

        activityMainBinding.mcvApply.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivity.this, ApplyThemeActivity.class);
            startActivity(intent);
        });

        activityMainBinding.mcvGetDirectLink.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivity.this, GetDirectLinkActivity.class);
            startActivity(intent);
        });

        activityMainBinding.mcvLearnHow.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivity.this, LearnHowActivity.class);
            startActivity(intent);
        });

        activityMainBinding.mcvExploreGithub.setOnClickListener(l -> ThemeShareDialogUtils.openBrowser(this, GITHUB_URL));
        activityMainBinding.mcvMeInCoolapk.setOnClickListener(l -> ThemeShareDialogUtils.openBrowser(this, ME_COOLAPK_URL));
    }
}
