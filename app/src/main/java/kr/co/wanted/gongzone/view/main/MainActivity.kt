package kr.co.wanted.gongzone.view.main

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import kr.co.wanted.gongzone.R
import kr.co.wanted.gongzone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var getSearchLauncher: ActivityResultLauncher<Intent>
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarTransparent()

        val nearMeFragment = NearMeFragment.newInstance()
        val useStatusFragment = UseStatusFragment.newInstance()
        val myFragment = MyFragment.newInstance()

        getSearchLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when (supportFragmentManager.findFragmentById(R.id.container)) {
                is NearMeFragment -> binding.bottomNav.menu.findItem(R.id.nearMe).isChecked = true
                is UseStatusFragment -> binding.bottomNav.menu.findItem(R.id.useStatus).isChecked = true
                is MyFragment -> binding.bottomNav.menu.findItem(R.id.my).isChecked = true
            }
        }

        changeFragment(nearMeFragment)

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nearMe -> changeFragment(nearMeFragment)

                R.id.search -> {
                    getSearchLauncher.launch(Intent(applicationContext, SearchActivity::class.java))
                    it.isChecked = false
                }
                R.id.useStatus -> changeFragment(useStatusFragment)
                R.id.my -> changeFragment(myFragment)
            }
            true
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
        if (currentFragment is IOnFocusListenable) {
            currentFragment.onWindowFocusChanged(hasFocus)
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            commit()
        }
    }

    /**
     * 상태바는 투명하게 내비게이션 바는 투명이 적용되지 않게 하는 메서드
     */
    private fun setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT in 26..29) {
            window.statusBarColor = Color.TRANSPARENT
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        } else if (Build.VERSION.SDK_INT >= 30) {
            window.statusBarColor = Color.TRANSPARENT
            // Making status bar overlaps with the activity
            WindowCompat.setDecorFitsSystemWindows(window, false)

            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->

                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                // Apply the insets as a margin to the view. Here the system is setting
                // only the bottom, left, and right dimensions, but apply whichever insets are
                // appropriate to your layout. You can also update the view padding
                // if that's more appropriate.

                view.layoutParams =  (view.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = insets.left
                    bottomMargin = insets.bottom
                    rightMargin = insets.right
                }

                // Return CONSUMED if you don't want want the window insets to keep being
                // passed down to descendant views.
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}