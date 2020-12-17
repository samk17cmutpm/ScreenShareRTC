/*
 * Copyright 2014 Pierre Chabardes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.pchab.androidrtc

import android.content.Context
import android.util.Log
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import fr.pchab.androidrtc.PeerConnectionClient.PeerConnectionParameters
import fr.pchab.androidrtc.WebRtcClient.Peer
import fr.pchab.androidrtc.WebRtcClient.SimpleSdpObserver
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.PeerConnection.*
import org.webrtc.PeerConnectionFactory.InitializationOptions
import java.net.URISyntaxException
import java.util.*

class WebRtcClient(var mContext: Context, private val mListener: RtcListener, var videoCapturer: VideoCapturer, private val mPeerConnParams: PeerConnectionParameters) {
	private val endPoints = BooleanArray(MAX_PEER)
	private val factory: PeerConnectionFactory?
	private val peers = HashMap<String, Peer>()
	private val iceServers = LinkedList<IceServer>()
	private val mPeerConnConstraints = MediaConstraints()
	private var mLocalMediaStream: MediaStream? = null
	private var mVideoSource: VideoSource? = null
	private var mSocket: Socket? = null
	var messageHandler: MessageHandler = MessageHandler()
	var rootEglBase = EglBase.create()
	val CHANNEL = "JXYZT"
	//    final String CHANNEL = "UWFTD";
	/**
	 * Implement this interface to be notified of events.
	 */
	interface RtcListener {
		fun onReady(remoteId: String?)
		fun onCall(applicant: String?)
		fun onHandup()
		fun onStatusChanged(newStatus: String?)
	}

	interface Command {
		@Throws(JSONException::class)
		fun execute(peerId: String?, payload: JSONObject?)
	}

	/**
	 * Send a message through the signaling server
	 *
	 * @param to      id of recipient
	 * @param type    type of message
	 * @param payload payload of message
	 * @throws JSONException
	 */
	@Throws(JSONException::class)
	fun sendMessage(to: String, type: String, payload: JSONObject) {
		val message = JSONObject()
		message.put("to", to)
		message.put("type", type)
		message.put("payload", payload)
		mSocket!!.emit("message", message)
		Log.d(TAG, "socket send $type to $to payload:$payload")
	}

	inner class MessageHandler {
		private val commandMap: HashMap<String, Command> = HashMap()

		var onMessage = Emitter.Listener { args ->
			try {
				val data = args[0] as JSONObject
				//                    String info = (String) args[0];
//                    JSONObject data = new JSONObject(info);
				val from = data.optString("from")
				val type = data.optString("type")
				Log.d(TAG, "socket received $type from $from")
				var payload: JSONObject? = null
				if (type != "init") {
					payload = data.optJSONObject("payload")
				}
				// if peer is unknown, try to add him
				if (!peers.containsKey(from)) {
					// if MAX_PEER is reach, ignore the call
					val endPoint = findEndPoint()
					if (endPoint != MAX_PEER) {
						val peer = addPeer(from, endPoint)
						peer.pc!!.addStream(mLocalMediaStream)
						commandMap[type]!!.execute(from, payload)
					}
				} else {
					val command = commandMap[type]
					command?.execute(from, payload)
				}
			} catch (e: JSONException) {
				e.printStackTrace()
			}
		}
		var onId = Emitter.Listener { args ->
			val id = args[0] as String
			mListener.onReady(id)
			mListener.onStatusChanged("READY")
			Log.d(TAG, "socket onId $id")
		}

		init {
			//            commandMap.put("init", new CreateOfferCommand());
//            commandMap.put("offer", new CreateAnswerCommand());
//            commandMap.put("answer", new SetRemoteSDPCommand());
//            commandMap.put("candidate", new AddIceCandidateCommand());
		}
	}

	inner class Peer(id: String, endPoint: Int) : SdpObserver, PeerConnection.Observer {
		var pc: PeerConnection?
		var id: String
		var endPoint: Int
		override fun onCreateSuccess(sdp: SessionDescription) {
			// TODO: modify sdp to use mPeerConnParams prefered codecs
			try {
				val payload = JSONObject()
				payload.put("type", sdp.type.canonicalForm())
				payload.put("sdp", sdp.description)
				Log.d(TAG, "onCreateSuccess")
				sendMessage(id, sdp.type.canonicalForm(), payload)
				pc!!.setLocalDescription(this@Peer, sdp)
			} catch (e: JSONException) {
				e.printStackTrace()
			}
		}

		override fun onSetSuccess() {}
		override fun onCreateFailure(s: String) {}
		override fun onSetFailure(s: String) {}
		override fun onSignalingChange(signalingState: SignalingState) {}
		override fun onIceConnectionReceivingChange(var1: Boolean) {}
		override fun onIceCandidatesRemoved(var1: Array<IceCandidate>) {}
		override fun onAddTrack(var1: RtpReceiver, var2: Array<MediaStream>) {}
		override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {
			if (iceConnectionState == IceConnectionState.DISCONNECTED) {
				removePeer(id)
				mListener.onStatusChanged("DISCONNECTED")
			}
		}

		override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {}
		override fun onIceCandidate(candidate: IceCandidate) {
			try {
				val payload = JSONObject()
				payload.put("label", candidate.sdpMLineIndex)
				payload.put("id", candidate.sdpMid)
				payload.put("candidate", candidate.sdp)
				sendMessage(id, "candidate", payload)
			} catch (e: JSONException) {
				e.printStackTrace()
			}
		}

		override fun onAddStream(mediaStream: MediaStream) {
//            Log.e("onAddStream", "onAddStream " + mediaStream.label());
			// remote streams are displayed from 1 to MAX_PEER (0 is localStream)
//            mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mRemoteRender));
//            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
		}

		override fun onRemoveStream(mediaStream: MediaStream) {
			removePeer(id)
		}

		override fun onDataChannel(dataChannel: DataChannel) {}
		override fun onRenegotiationNeeded() {}

		init {
			Log.d(TAG, "new Peer: $id $endPoint")
			pc = factory!!.createPeerConnection(iceServers, mPeerConnConstraints, this)
			this.id = id
			this.endPoint = endPoint
			pc!!.addStream(mLocalMediaStream) //, new MediaConstraints()
		}
	}

	private fun addPeer(id: String, endPoint: Int): Peer {
		val peer = Peer(id, endPoint)
		peers[id] = peer
		endPoints[endPoint] = true
		return peer
	}

	private fun removePeer(id: String) {
		val peer = peers[id]
		peer!!.pc!!.close()
		peers.remove(peer.id)
		endPoints[peer.endPoint] = false
	}

	var peerConnection: PeerConnection? = null

	inner class SimpleSdpObserver : SdpObserver {
		override fun onCreateSuccess(sessionDescription: SessionDescription) {
			Log.e("SimpleSdpObserver", "onCreateSuccess")
			val jsonObject = JSONObject()
			try {
				jsonObject.put("channel", CHANNEL)
				jsonObject.put("type", "call")
				val dataObject = JSONObject()
				dataObject.put("sdp", sessionDescription.description)
				dataObject.put("type", "offer")
				jsonObject.put("data", dataObject)
				Log.e("sdp", sessionDescription.description)
			} catch (e: JSONException) {
				e.printStackTrace()
			}
			Log.e("whisper", jsonObject.toString())
			mSocket!!.emit("whisper", jsonObject)
			peerConnection!!.setLocalDescription(this@SimpleSdpObserver, sessionDescription)
		}

		override fun onSetSuccess() {}
		override fun onCreateFailure(s: String) {}
		override fun onSetFailure(s: String) {}
	}

	internal inner class SimplePeerConnectionObserver : PeerConnection.Observer {
		override fun onSignalingChange(signalingState: SignalingState) {}
		override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {
//            Log.e("====>", "onIceConnectionChange");
		}

		override fun onIceConnectionReceivingChange(b: Boolean) {}
		override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {}
		override fun onIceCandidate(iceCandidate: IceCandidate) {
			val jsonObject = JSONObject()
			try {
				jsonObject.put("channel", CHANNEL)
				jsonObject.put("type", "call")
				val dataObject = JSONObject()
				dataObject.put("type", "candidate")
				val candidateObject = JSONObject()
				candidateObject.put("candidate", iceCandidate.sdp)
				candidateObject.put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
				candidateObject.put("sdpMid", iceCandidate.sdpMid)
				dataObject.put("candidate", candidateObject)
				jsonObject.put("data", dataObject)
				Log.e("emit candidate", jsonObject.toString())
			} catch (e: JSONException) {
				e.printStackTrace()
			}
			Log.e("whisper", jsonObject.toString())
			mSocket!!.emit("whisper", jsonObject)
			peerConnection!!.addIceCandidate(iceCandidate)
			Log.e("====>", "onIceCandidate")
		}

		override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {}
		override fun onAddStream(mediaStream: MediaStream) {}
		override fun onRemoveStream(mediaStream: MediaStream) {}
		override fun onDataChannel(dataChannel: DataChannel) {}
		override fun onRenegotiationNeeded() {}
		override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
	}

	/**
	 * Call this method in Activity.onDestroy()
	 */
	fun destroy() {
		for (peer in peers.values) {
			peer.pc!!.dispose()
		}
		factory?.dispose()
		if (mVideoSource != null) {
			mVideoSource!!.dispose()
		}
		//        mSocket.disconnect();
//        mSocket.close();
	}

	private fun findEndPoint(): Int {
		for (i in 0 until MAX_PEER) if (!endPoints[i]) return i
		return MAX_PEER
	}

	/**
	 * Start the mSocket.
	 *
	 *
	 * Set up the local stream and notify the signaling server.
	 * Call this method after onCallReady.
	 *
	 * @param name mSocket name
	 */
	fun start(name: String?) {
		initScreenCapturStream()
		try {
			val message = JSONObject()
			message.put("name", name)
			mSocket!!.emit("readyToStream", message)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun initScreenCapturStream() {
		mLocalMediaStream = factory!!.createLocalMediaStream("ARDAMS")
		val videoConstraints = MediaConstraints()
		videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxHeight", Integer.toString(mPeerConnParams.videoHeight)))
		videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxWidth", Integer.toString(mPeerConnParams.videoWidth)))
		videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(20)))
		videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(20)))

//        VideoCapturer capturer = createScreenCapturer();
		mVideoSource = factory.createVideoSource(true)
		val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
		videoCapturer.initialize(surfaceTextureHelper, mContext, mVideoSource?.getCapturerObserver())
		mVideoSource?.adaptOutputFormat(mPeerConnParams.videoWidth, mPeerConnParams.videoHeight, 20)
		videoCapturer.startCapture(mPeerConnParams.videoWidth, mPeerConnParams.videoHeight, 20)
		val localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource)
		localVideoTrack.setEnabled(true)
		mLocalMediaStream?.addTrack(factory.createVideoTrack("ARDAMSv0", mVideoSource))
		//AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
		//mLocalMediaStream.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));
//        mLocalMediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mLocalRender));
//        mListener.onLocalStream(mLocalMediaStream);
		mListener.onStatusChanged("STREAMING")
	}

	companion object {
		const val VIDEO_TRACK_ID = "ARDAMSv0"
		private const val TAG = "WebRtcClient"
		private const val MAX_PEER = 2
	}

	init {
		val options = InitializationOptions.builder(mContext)
			.setEnableInternalTracer(true) //                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
			.createInitializationOptions()
		val encoderFactory: VideoEncoderFactory
		val decoderFactory: VideoDecoderFactory
		encoderFactory = DefaultVideoEncoderFactory(
			rootEglBase.eglBaseContext, false /* enableIntelVp8Encoder */, true)
		decoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
		PeerConnectionFactory.initialize(options)
		factory = PeerConnectionFactory
			.builder()
			.setVideoDecoderFactory(decoderFactory)
			.setVideoEncoderFactory(encoderFactory)
			.createPeerConnectionFactory()
		val host = "https://screen2.dungno.info"
		try {
			mSocket = IO.socket(host)
		} catch (e: URISyntaxException) {
			e.printStackTrace()
		}
		mSocket!!.on("id", messageHandler.onId)
		mSocket!!.on("message", messageHandler.onMessage)
		mSocket!!.on(Socket.EVENT_DISCONNECT) { Log.d(TAG, "socket state disconnect") }
		mSocket!!.on(Socket.EVENT_ERROR) { Log.d(TAG, "socket state error") }
		mSocket!!.on(CHANNEL) { args -> //                Log.e("on listener " + CHANNEL, Arrays.toString(args));
			val jsonObject = args[0] as JSONObject
			try {
				val dataObject = jsonObject.getJSONObject("data")
				Log.e("SOCKET", dataObject.toString())
				if (dataObject.has("type")) {
					when (dataObject.getString("type")) {
						"answer" -> {
							Log.e("answer", dataObject.toString())
							peerConnection!!.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.ANSWER, dataObject.getString("sdp")))
						}
						"candidate" -> {
							val candidate = dataObject.getJSONObject("candidate")
							Log.e("candidate", candidate.toString())
							peerConnection!!.addIceCandidate(IceCandidate(candidate.getString("sdpMid"), candidate.getInt("sdpMLineIndex"), candidate.getString("candidate")))
						}
					}
				}
			} catch (e: JSONException) {
				e.printStackTrace()
			}
		}
		iceServers.add(IceServer("stun:23.21.150.121"))
		iceServers.add(IceServer("stun:stun.l.google.com:19302"))
		mPeerConnConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
		mPeerConnConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", "20"))
		mPeerConnConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
		mPeerConnConstraints.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
		mSocket!!.on(Socket.EVENT_CONNECT) {
			Log.e(TAG, "socket state connect")
			val jsonObject = JSONObject()
			try {
				jsonObject.put("channel", CHANNEL)
				jsonObject.put("role", 1)
			} catch (e: JSONException) {
				e.printStackTrace()
			}
			Log.e("join", jsonObject.toString())
			mSocket!!.emit("join", jsonObject)
			peerConnection = factory.createPeerConnection(iceServers, mPeerConnConstraints, SimplePeerConnectionObserver())
			peerConnection!!.addStream(mLocalMediaStream)
			val simpleSdpObserver = SimpleSdpObserver()
			peerConnection!!.createOffer(simpleSdpObserver, mPeerConnConstraints)
			//peerConnection.setLocalDescription(simpleSdpObserver, peerConnection.getLocalDescription());
		}
		mSocket!!.connect()
		initScreenCapturStream()
	}
}