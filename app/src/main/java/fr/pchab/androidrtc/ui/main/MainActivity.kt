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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.integration.android.IntentIntegrator
import fr.pchab.androidrtc.R
import fr.pchab.androidrtc.RtcActivity
import fr.pchab.androidrtc.RtcActivity.Companion.CHANNEL
import fr.pchab.androidrtc.base.BaseActivity
import fr.pchab.androidrtc.ext.checkRequestPermissionsResult
import fr.pchab.androidrtc.helper.PermissionHelper
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

		private const val cameraPermission = android.Manifest.permission.CAMERA
		private const val locationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
		private const val recordAudioPermission = android.Manifest.permission.RECORD_AUDIO
		private const val phonePermission = android.Manifest.permission.READ_PHONE_STATE
		private const val storePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

		private const val PERMISSION_CAMERA_REQUEST_CODE = 1111
		private const val PERMISSION_LOCATION_REQUEST_CODE = 2222
		private const val PERMISSION_AUDIO_REQUEST_CODE = 3333
		private const val PERMISSION_PHONE_REQUEST_CODE = 4444
		private const val PERMISSION_STORE_REQUEST_CODE = 5555
	}

	private val context by lazy { this@MainActivity }

	private var channel: String? = null

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
			this.channel = channel
			checkCameraPermission()
		}
	}

	private fun checkCameraPermission() {
		PermissionHelper.checkPermission(context, cameraPermission, object : PermissionHelper.PermissionAskListener {
			override fun onPermissionAsk() {
				requestSpecificPermissions(arrayOf(cameraPermission), R.string.camera_permission_warning, PERMISSION_CAMERA_REQUEST_CODE)
			}

			override fun onPermissionPreviouslyDenied() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.camera_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionDisabled() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.camera_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionGranted() {
				checkLocationPermission()
			}
		})
	}

	private fun checkLocationPermission() {
		PermissionHelper.checkPermission(context, locationPermission, object : PermissionHelper.PermissionAskListener {
			override fun onPermissionAsk() {
				requestSpecificPermissions(arrayOf(locationPermission), R.string.location_permission_warning, PERMISSION_LOCATION_REQUEST_CODE)
			}

			override fun onPermissionPreviouslyDenied() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.location_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionDisabled() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.location_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionGranted() {
				checkRecordAudioPermission()
			}
		})
	}

	private fun checkRecordAudioPermission() {
		PermissionHelper.checkPermission(context, recordAudioPermission, object : PermissionHelper.PermissionAskListener {
			override fun onPermissionAsk() {
				requestSpecificPermissions(arrayOf(recordAudioPermission), R.string.record_audio_permission_warning, PERMISSION_AUDIO_REQUEST_CODE)
			}

			override fun onPermissionPreviouslyDenied() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.record_audio_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionDisabled() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.record_audio_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionGranted() {
				checkPhonePermission()
			}
		})
	}

	private fun checkPhonePermission() {
		PermissionHelper.checkPermission(context, phonePermission, object : PermissionHelper.PermissionAskListener {
			override fun onPermissionAsk() {
				requestSpecificPermissions(arrayOf(phonePermission), R.string.phone_permission_warning, PERMISSION_PHONE_REQUEST_CODE)
			}

			override fun onPermissionPreviouslyDenied() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.phone_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionDisabled() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.phone_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionGranted() {
				checkStorePermission()
			}
		})
	}

	private fun checkStorePermission() {
		PermissionHelper.checkPermission(context, storePermission, object : PermissionHelper.PermissionAskListener {
			override fun onPermissionAsk() {
				requestSpecificPermissions(arrayOf(storePermission), R.string.phone_permission_warning, PERMISSION_STORE_REQUEST_CODE)
			}

			override fun onPermissionPreviouslyDenied() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.storage_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionDisabled() {
				MaterialAlertDialogBuilder(context)
					.setTitle(resources.getString(R.string.app_name))
					.setMessage(resources.getString(R.string.storage_permission_warning))
					.setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
						openSettings()
					}
					.show()
			}

			override fun onPermissionGranted() {
				RtcActivity.start(context, channel)
			}
		})
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

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		when(requestCode) {
			PERMISSION_CAMERA_REQUEST_CODE -> {
				if (checkRequestPermissionsResult(grantResults)) {
					checkLocationPermission()
				} else {
					checkCameraPermission()
				}
			}

			PERMISSION_LOCATION_REQUEST_CODE -> {
				if (checkRequestPermissionsResult(grantResults)) {
					checkRecordAudioPermission()
				} else {
					checkLocationPermission()
				}
			}

			PERMISSION_AUDIO_REQUEST_CODE -> {
				if (checkRequestPermissionsResult(grantResults)) {
					checkPhonePermission()
				} else {
					checkRecordAudioPermission()
				}
			}

			PERMISSION_PHONE_REQUEST_CODE -> {
				if (checkRequestPermissionsResult(grantResults)) {
					checkStorePermission()
				} else {
					checkPhonePermission()
				}
			}

			PERMISSION_STORE_REQUEST_CODE -> {
				if (checkRequestPermissionsResult(grantResults)) {
					RtcActivity.start(context, channel)
				} else {
					checkCameraPermission()
				}
			}
		}
	}
}