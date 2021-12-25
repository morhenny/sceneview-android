package com.gorisse.thomas.sceneview.sample.arcursorplacement

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.material.internal.ViewUtils
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen

class Activity : AppCompatActivity(R.layout.activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            fullScreen = true, hideSystemBars = false,
            fitsSystemWindows = false, rootView = findViewById(R.id.rootView)
        )

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            // TODO : Try if the bellow works instead
//            ViewUtils.doOnApplyWindowInsets(findViewById<Toolbar>(R.id.toolbar)) { view, insets, paddings ->
//                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//                (view.layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
//                WindowInsetsCompat.CONSUMED
//            }
            title = ""
        })

        supportFragmentManager.commit {
            add(R.id.containerFragment, MainFragment::class.java, Bundle())
        }
    }
}