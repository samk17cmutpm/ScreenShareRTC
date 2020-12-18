package fr.pchab.androidrtc.ui.splash

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.base.BaseActivity
import fr.pchab.androidrtc.ui.main.MainActivity

class SplashActivity : BaseActivity() {
	companion object {
		private const val SPLASH_TIME_OUT: Long = 5000L
		private const val ICON_FADING: Long = 3500L
	}

	private val context by lazy { this@SplashActivity }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		window.decorView.systemUiVisibility = (
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

		window.statusBarColor = Color.TRANSPARENT
		setContentView(R.layout.activity_splash)

		Handler().postDelayed({
			MainActivity.start(context)
			finish()
		}, SPLASH_TIME_OUT)
	}
}