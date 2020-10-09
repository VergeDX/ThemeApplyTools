package org.hydev.themeapplytools.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.hydev.themeapplytools.databinding.ActivityMainBinding
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils.openBrowser
import org.hydev.themeapplytools.utils.ThemeUtils

class MainActivity : AppCompatActivity() {
    companion object {
        private const val GITHUB_REPO_URL = "https://github.com/VergeDX/ThemeApplyTools"
        private const val COOLAPK_ME_HOMEPAGE = "https://coolapk.com/u/506843"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // Open dark mode will reCreate activity.
        ThemeUtils.darkMode(this)

        activityMainBinding.mcvApply.setOnClickListener { startActivity(Intent(this, ApplyThemeActivity::class.java)) }
        activityMainBinding.mcvGetDirectLink.setOnClickListener { startActivity(Intent(this, GetDirectLinkActivity::class.java)) }
        activityMainBinding.mcvLearnHow.setOnClickListener { startActivity(Intent(this, LearnHowActivity::class.java)) }
        activityMainBinding.mcvExploreGithub.setOnClickListener { openBrowser(this, GITHUB_REPO_URL) }
        activityMainBinding.mcvMeInCoolapk.setOnClickListener { openBrowser(this, COOLAPK_ME_HOMEPAGE) }
    }
}
