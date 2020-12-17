package fr.pchab.androidrtc

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.pchab.androidrtc.PeerConnectionClient.PeerConnectionParameters
import fr.pchab.androidrtc.WebRtcClient.RtcListener
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.VideoCapturer

class RtcActivity : AppCompatActivity(), RtcListener {
	private var mWebRtcClient: WebRtcClient? = null

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		window.addFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN
				or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
		setContentView(R.layout.activity_rtc)
		val metrics = DisplayMetrics()
		windowManager.defaultDisplay.getRealMetrics(metrics)
		sDeviceWidth = metrics.widthPixels
		sDeviceHeight = metrics.heightPixels

//        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
//        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);

//        EglBase rootEglBase = EglBase.create();
//        pipRenderer.init(rootEglBase.getEglBaseContext(), null);
//        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//        fullscreenRenderer.init(rootEglBase.getEglBaseContext(), null);
//        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

//        pipRenderer.setZOrderMediaOverlay(true);
//        pipRenderer.setEnableHardwareScaler(true /* enabled */);
//        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);
		// Check for mandatory permissions.
		for (permission in MANDATORY_PERMISSIONS) {
			if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				setResult(RESULT_CANCELED)
				finish()
				return
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startScreenCapture()
		} else {
			init()
		}
	}

	@TargetApi(21)
	private fun startScreenCapture() {
		val mediaProjectionManager = application.getSystemService(
			MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
		startActivityForResult(
			mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE)
	}

	@TargetApi(21)
	private fun createScreenCapturer(): VideoCapturer? {
		if (mMediaProjectionPermissionResultCode != RESULT_OK) {
			report("User didn't give permission to capture the screen.")
			return null
		}
		return ScreenCapturerAndroid(
			mMediaProjectionPermissionResultData, object : MediaProjection.Callback() {
			override fun onStop() {
				report("User revoked permission to capture the screen.")
			}
		})
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) return
		mMediaProjectionPermissionResultCode = resultCode
		mMediaProjectionPermissionResultData = data
		init()
	}

	private fun init() {
		val peerConnectionParameters = PeerConnectionParameters(true, false,
			true, sDeviceWidth / SCREEN_RESOLUTION_SCALE, sDeviceHeight / SCREEN_RESOLUTION_SCALE, 20,
			0, "VP8",
			false,
			true,
			0,
			"OPUS", false, false, false, false, false, false, false, false, null)
		//        mWebRtcClient = new WebRtcClient(getApplicationContext(), this, pipRenderer, fullscreenRenderer, createScreenCapturer(), peerConnectionParameters);


		mWebRtcClient = WebRtcClient(applicationContext, this, createScreenCapturer()!!, peerConnectionParameters)
	}

	fun report(info: String?) {
		Log.e(ContentValues.TAG, info)
	}

	public override fun onPause() {
		super.onPause()
	}

	public override fun onResume() {
		super.onResume()
	}

	public override fun onDestroy() {
		if (mWebRtcClient != null) {
//            mWebRtcClient.onDestroy();
		}
		super.onDestroy()
	}

	override fun onReady(remoteId: String?) {
		mWebRtcClient!!.start(STREAM_NAME_PREFIX)
	}

	override fun onCall(applicant: String?) {
		runOnUiThread { }
	}

	override fun onHandup() {}
	override fun onStatusChanged(newStatus: String?) {
		runOnUiThread { Toast.makeText(applicationContext, newStatus, Toast.LENGTH_SHORT).show() }
	}

	companion object {
		private const val CAPTURE_PERMISSION_REQUEST_CODE = 1

		//    private EglBase rootEglBase;
		private var mMediaProjectionPermissionResultData: Intent? = null
		private var mMediaProjectionPermissionResultCode = 0
		var STREAM_NAME_PREFIX = "android_device_stream"

		// List of mandatory application permissions.／
		private val MANDATORY_PERMISSIONS = arrayOf("android.permission.MODIFY_AUDIO_SETTINGS",
			"android.permission.RECORD_AUDIO", "android.permission.INTERNET")

		//    private SurfaceViewRenderer pipRenderer;
		//    private SurfaceViewRenderer fullscreenRenderer;
		var sDeviceWidth = 0
		var sDeviceHeight = 0
		const val SCREEN_RESOLUTION_SCALE = 2
	}
}