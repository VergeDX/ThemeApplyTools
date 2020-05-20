package org.hydev.themeapplytools.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import org.hydev.themeapplytools.R;
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils;
import org.hydev.themeapplytools.utils.ThemeUtils;

public class MainActivityNew extends AppCompatActivity {
    private static final String GITHUB_URL = "https://github.com/VergeDX/ThemeApplyTools";
    private static final String ME_COOLAPK_URL = "https://coolapk.com/u/506843";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        ThemeUtils.darkMode(this);

        MaterialCardView mcv_appUsage = findViewById(R.id.mcv_appUsage);
        mcv_appUsage.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivityNew.this, ApplyThemeActivity.class);
            startActivity(intent);
        });

        MaterialCardView mcv_getDirectLink = findViewById(R.id.mcv_getDirectLink);
        mcv_getDirectLink.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivityNew.this, GetDirectLinkActivity.class);
            startActivity(intent);
        });

        MaterialCardView mcv_learnHow = findViewById(R.id.mcv_learnHow);
        mcv_learnHow.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivityNew.this, LearnHowActivity.class);
            startActivity(intent);
        });

        MaterialCardView mcv_exploreGithub = findViewById(R.id.mcv_exploreGithub);
        mcv_exploreGithub.setOnClickListener(l -> ThemeShareDialogUtils.openBrowser(this, GITHUB_URL));

        MaterialCardView mcv_meInCoolapk = findViewById(R.id.mcv_meInCoolapk);
        mcv_meInCoolapk.setOnClickListener(l -> ThemeShareDialogUtils.openBrowser(this, ME_COOLAPK_URL));
    }
}
