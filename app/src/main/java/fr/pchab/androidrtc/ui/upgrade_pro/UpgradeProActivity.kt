package fr.pchab.androidrtc.ui.upgrade_pro

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.base.BaseActivity
import kotlinx.android.synthetic.main.common_top_bar.*

class UpgradeProActivity : BaseActivity() {
	companion object {
		fun start(
			context: Context?
		) {
			val intent = Intent(context, UpgradeProActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			context?.startActivity(intent)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_upgrade_pro)

		initToolBar(toolbar, icon = R.drawable.ic_back)
		toolbar?.setNavigationOnClickListener { onBackPressed() }
		tvTitle?.setText(R.string.upgrade_pro)
	}
}