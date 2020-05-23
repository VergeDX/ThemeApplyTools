package org.hydev.themeapplytools.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.hydev.themeapplytools.databinding.ActivityMainBinding
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils.openBrowser
import org.hydev.themeapplytools.utils.ThemeUtils
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // Open dark mode will reCreate activity.
        ThemeUtils.darkMode(this)
        activityMainBinding.mcvApply.setOnClickListener { activity(ApplyThemeActivity::class) }
        activityMainBinding.mcvGetDirectLink.setOnClickListener { activity(GetDirectLinkActivity::class) }
        activityMainBinding.mcvLearnHow.setOnClickListener { activity(LearnHowActivity::class) }
        activityMainBinding.mcvExploreGithub.setOnClickListener { openBrowser(this, "https://github.com/VergeDX/ThemeApplyTools") }
        activityMainBinding.mcvMeInCoolapk.setOnClickListener { openBrowser(this, "https://coolapk.com/u/506843") }
    }

    fun activity(cls: KClass<*>) {
        startActivity(Intent(this, cls.java))
    }
}
