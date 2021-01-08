package fr.pchab.androidrtc.ui.connected

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.base.BaseActivity
import kotlinx.android.synthetic.main.common_top_bar.*

class ConnectedActivity : BaseActivity() {
	companion object {
		fun start(
			context: Context?
		) {
			val intent = Intent(context, ConnectedActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			context?.startActivity(intent)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_connected)

		initToolBar(toolbar)
		tvTitle?.setText(R.string.connected)
	}
}