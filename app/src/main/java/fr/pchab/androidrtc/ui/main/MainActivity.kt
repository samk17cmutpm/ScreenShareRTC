package fr.pchab.androidrtc.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.google.zxing.integration.android.IntentIntegrator
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.RtcActivity
import fr.pchab.androidrtc.RtcActivity.Companion.CHANNEL
import fr.pchab.androidrtc.base.BaseActivity
import fr.pchab.androidrtc.ui.scan_qr_code.ScanQRCodeActivity
import fr.pchab.androidrtc.ui.support.SupportActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.common_top_bar.*

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

	private val context by lazy { this@MainActivity }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		initToolBar(toolbar)
		tvTitle?.setText(R.string.connect_to_browser)

		input_ed?.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

			override fun afterTextChanged(s: Editable?) {
				val channel = s?.toString()
				btConnect?.isEnabled = channel?.length == 5
			}
		})
		input_ed?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
			override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
				if (actionId == EditorInfo.IME_ACTION_NEXT
					|| actionId == EditorInfo.IME_ACTION_DONE
					|| event?.action == KeyEvent.ACTION_DOWN
					&& event?.keyCode == KeyEvent.KEYCODE_ENTER) {
					connect(input_ed?.editableText?.toString())
					return true
				}
				return false
			}
		})

		btScan?.setOnClickListener {
			IntentIntegrator(context).apply {
				captureActivity = ScanQRCodeActivity::class.java // activity custom để thực hiện scan.
				setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
				setPrompt("")
				setCameraId(0)
				setBeepEnabled(true)
				setOrientationLocked(false)
				initiateScan()
			}
		}

		btConnect?.setOnClickListener {
			connect(input_ed?.editableText?.toString())
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == RESULT_OK) {
			val channel: String? = data?.getStringExtra(CHANNEL)
			connect(channel)
		}
	}

	private fun connect(channel: String?) {
		if (channel?.length == 5) {
			RtcActivity.start(context, input_ed?.editableText?.toString())
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater: MenuInflater = menuInflater
		inflater.inflate(R.menu.connected_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.more -> {
				SupportActivity.start(context)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
}