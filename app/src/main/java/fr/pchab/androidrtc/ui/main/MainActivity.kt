package fr.pchab.androidrtc.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
	companion object {
		fun start(
			context: Context?
		) {
			val intent = Intent(context, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			context?.startActivity(intent)
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		initToolBar(toolbar)
	}
}