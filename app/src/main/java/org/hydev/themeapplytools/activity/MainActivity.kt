package org.hydev.themeapplytools.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.hydev.themeapplytools.activity.MainActivity
import org.hydev.themeapplytools.databinding.ActivityMainBinding
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils
import org.hydev.themeapplytools.utils.ThemeUtils

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // Open dark mode will reCreate activity.
        ThemeUtils.darkMode(this)
        activityMainBinding.mcvApply.setOnClickListener { l: View? ->
            val intent = Intent(this@MainActivity, ApplyThemeActivity::class.java)
            startActivity(intent)
        }
        activityMainBinding.mcvGetDirectLink.setOnClickListener { l: View? ->
            val intent = Intent(this@MainActivity, GetDirectLinkActivity::class.java)
            startActivity(intent)
        }
        activityMainBinding.mcvLearnHow.setOnClickListener { l: View? ->
            val intent = Intent(this@MainActivity, LearnHowActivity::class.java)
            startActivity(intent)
        }
        activityMainBinding.mcvExploreGithub.setOnClickListener { l: View? -> ThemeShareDialogUtils.openBrowser(this, GITHUB_URL) }
        activityMainBinding.mcvMeInCoolapk.setOnClickListener { l: View? -> ThemeShareDialogUtils.openBrowser(this, ME_COOLAPK_URL) }
    }

    companion object
    {
        private const val GITHUB_URL = "https://github.com/VergeDX/ThemeApplyTools"
        private const val ME_COOLAPK_URL = "https://coolapk.com/u/506843"
    }
}
