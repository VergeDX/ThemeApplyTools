package org.hydev.themeapplytools.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.hydev.themeapplytools.databinding.ActivityLearnHowBinding
import org.hydev.themeapplytools.utils.ThemeUtils

class LearnHowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityLearnHowBinding = ActivityLearnHowBinding.inflate(layoutInflater)
        setContentView(activityLearnHowBinding.root)

        ThemeUtils.darkMode(this)
    }
}
