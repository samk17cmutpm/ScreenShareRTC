package fr.pchab.androidrtc.ui.scan_qr_code

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.base.BaseActivity
import kotlinx.android.synthetic.main.activity_scan_qr_code.*
import kotlinx.android.synthetic.main.common_top_bar.*

class ScanQRCodeActivity : BaseActivity() {
	companion object {
		fun start(
			context: Context?
		) {
			val intent = Intent(context, ScanQRCodeActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			context?.startActivity(intent)
		}
	}

	private lateinit var capture: CaptureManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_scan_qr_code)

		initToolBar(toolbar)
		tvTitle?.text = getString(R.string.scan_qr_code)

		initScanner(savedInstanceState)
	}

	private fun initScanner(savedInstanceState: Bundle?) {
		capture = CaptureManager(this, bcScanner )
		capture.apply {
			initializeFromIntent(intent, savedInstanceState)
			decode()
		}

		bcScanner?.decodeContinuous(object : BarcodeCallback {
			override fun barcodeResult(result: BarcodeResult?) {
				capture?.onPause()

				handleResult(result?.text)
			}

			override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
		})
	}

	private fun handleResult(rawResult: String?) {

	}

	override fun onResume() {
		super.onResume()
		capture.onResume()
	}

	override fun onPause() {
		super.onPause()
		capture.onPause()
	}

	override fun onDestroy() {
		capture.onDestroy()
		super.onDestroy()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		capture.onSaveInstanceState(outState)
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		return bcScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
	}
}