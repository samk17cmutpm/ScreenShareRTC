package fr.pchab.androidrtc.ui.support

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.base.BaseActivity
import kotlinx.android.synthetic.main.activity_support.*
import kotlinx.android.synthetic.main.common_top_bar.*

class SupportActivity : BaseActivity() {
	companion object {
		fun start(
			context: Context?
		) {
			val intent = Intent(context, SupportActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			context?.startActivity(intent)
		}
	}

	private val context by lazy { this@SupportActivity }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_support)

		initToolBar(toolbar, icon = R.drawable.ic_back)
		toolbar?.setNavigationOnClickListener { onBackPressed() }
		tvTitle?.setText(R.string.support)

		val supportItems = arrayListOf<SupportItem>()

		supportItems.add(
			SupportItem(
				ic = R.drawable.ic_upgrade,
				text = getString(R.string.upgrade_to_pro_version)
			)
		)

		supportItems.add(
			SupportItem(
				ic = R.drawable.ic_about,
				text = getString(R.string.about_screen_mirror)
			)
		)

		supportItems.add(
			SupportItem(
				ic = R.drawable.ic_feedback,
				text = getString(R.string.send_your_feedback)
			)
		)

		supportItems.add(
			SupportItem(
				ic = R.drawable.ic_share_app,
				text = getString(R.string.recommend_this_app_to_a_friend)
			)
		)

		supportItems.add(
			SupportItem(
				ic = R.drawable.ic_rate_app,
				text = getString(R.string.rate_this_app)
			)
		)

		val supportAdapter = SupportAdapter(supportItems, context)
		rcSupport?.layoutManager = LinearLayoutManager(context)
		rcSupport?.adapter = supportAdapter
	}
}