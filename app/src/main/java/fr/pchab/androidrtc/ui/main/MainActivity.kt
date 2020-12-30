package fr.pchab.androidrtc.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.zxing.integration.android.IntentIntegrator
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.base.BaseActivity
import fr.pchab.androidrtc.ui.scan_qr_code.ScanQRCodeActivity
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
	}
}