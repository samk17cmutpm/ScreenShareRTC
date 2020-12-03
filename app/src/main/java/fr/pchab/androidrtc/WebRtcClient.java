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

package fr.pchab.androidrtc;

import android.media.projection.MediaProjection;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


import android.content.Context;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class WebRtcClient {

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private final static String TAG = "WebRtcClient";
    private final static int MAX_PEER = 2;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private PeerConnectionFactory factory;
    private HashMap<String, Peer> peers = new HashMap<>();
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private PeerConnectionClient.PeerConnectionParameters mPeerConnParams;
    private MediaConstraints mPeerConnConstraints = new MediaConstraints();
    private MediaStream mLocalMediaStream;
    private VideoSource mVideoSource;
    private RtcListener mListener;
    private Socket mSocket;
    VideoCapturer videoCapturer;
    MessageHandler messageHandler = new MessageHandler();
    Context mContext;
    EglBase rootEglBase = EglBase.create();
    final String CHANNEL = "1E4D9";


    /**
     * Implement this interface to be notified of events.
     */
    public interface RtcListener {
        void onReady(String remoteId);

        void onCall(String applicant);

        void onHandup();

        void onStatusChanged(String newStatus);
    }

    public interface Command {
        void execute(String peerId, JSONObject payload) throws JSONException;
    }

    /**
     * Send a message through the signaling server
     *
     * @param to      id of recipient
     * @param type    type of message
     * @param payload payload of message
     * @throws JSONException
     */
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        mSocket.emit("message", message);
        Log.d(TAG, "socket send " + type + " to " + to + " payload:" + payload);
    }

    public class MessageHandler {
        private HashMap<String, Command> commandMap;

        public MessageHandler() {
            this.commandMap = new HashMap<>();
//            commandMap.put("init", new CreateOfferCommand());
//            commandMap.put("offer", new CreateAnswerCommand());
//            commandMap.put("answer", new SetRemoteSDPCommand());
//            commandMap.put("candidate", new AddIceCandidateCommand());
        }

        public Emitter.Listener onMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
//                    String info = (String) args[0];
//                    JSONObject data = new JSONObject(info);
                    String from = data.optString("from");
                    String type = data.optString("type");
                    Log.d(TAG, "socket received " + type + " from " + from);
                    JSONObject payload = null;
                    if (!type.equals("init")) {
                        payload = data.optJSONObject("payload");
                    }
                    // if peer is unknown, try to add him
                    if (!peers.containsKey(from)) {
                        // if MAX_PEER is reach, ignore the call
                        int endPoint = findEndPoint();
                        if (endPoint != MAX_PEER) {
                            Peer peer = addPeer(from, endPoint);
                            peer.pc.addStream(mLocalMediaStream);
                            commandMap.get(type).execute(from, payload);
                        }
                    } else {
                        Command command = commandMap.get(type);
                        if (command != null) {
                            command.execute(from, payload);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        public Emitter.Listener onId = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String id = (String) args[0];
                mListener.onReady(id);
                mListener.onStatusChanged("READY");
                Log.d(TAG, "socket onId " + id);
            }
        };
    }

    public class Peer implements SdpObserver, PeerConnection.Observer {
        public PeerConnection pc;
        public String id;
        public int endPoint;

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            // TODO: modify sdp to use mPeerConnParams prefered codecs
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                Log.d(TAG, "onCreateSuccess");
                sendMessage(id, sdp.type.canonicalForm(), payload);
                pc.setLocalDescription(Peer.this, sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {
        }

        @Override
        public void onSetFailure(String s) {
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        }


        public void onIceConnectionReceivingChange(boolean var1) {

        }

        public void onIceCandidatesRemoved(IceCandidate[] var1) {

        }

        public void onAddTrack(RtpReceiver var1, MediaStream[] var2) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                removePeer(id);
                mListener.onStatusChanged("DISCONNECTED");
            }
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
                sendMessage(id, "candidate", payload);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
//            Log.e("onAddStream", "onAddStream " + mediaStream.label());
            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
//            mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mRemoteRender));
//            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            removePeer(id);
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
        }

        @Override
        public void onRenegotiationNeeded() {

        }

        public Peer(String id, int endPoint) {
            Log.d(TAG, "new Peer: " + id + " " + endPoint);
            this.pc = factory.createPeerConnection(iceServers, mPeerConnConstraints, this);
            this.id = id;
            this.endPoint = endPoint;
            pc.addStream(mLocalMediaStream); //, new MediaConstraints()
        }
    }

    private Peer addPeer(String id, int endPoint) {
        Peer peer = new Peer(id, endPoint);
        peers.put(id, peer);

        endPoints[endPoint] = true;
        return peer;
    }

    private void removePeer(String id) {
        Peer peer = peers.get(id);
        peer.pc.close();
        peers.remove(peer.id);
        endPoints[peer.endPoint] = false;
    }

    public WebRtcClient(Context context, RtcListener listener, VideoCapturer capturer, PeerConnectionClient.PeerConnectionParameters params) {
        mContext = context;
        mListener = listener;
        mPeerConnParams = params;
        videoCapturer = capturer;
        PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
//                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions();
         VideoEncoderFactory encoderFactory;
         VideoDecoderFactory decoderFactory;
        encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, true);
        decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        PeerConnectionFactory.initialize(options);
        factory = PeerConnectionFactory
                .builder()
                .setVideoDecoderFactory(decoderFactory)
                .setVideoEncoderFactory(encoderFactory)
                .createPeerConnectionFactory();
        String host = "https://screen2.dungno.info";
        try {
            mSocket = IO.socket(host);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.on("id", messageHandler.onId);
        mSocket.on("message", messageHandler.onMessage);

        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "socket state disconnect");
            }
        });
        mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "socket state error");
            }
        });

        mSocket.on(CHANNEL, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
//                Log.e("on listener " + CHANNEL, Arrays.toString(args));

                JSONObject jsonObject = (JSONObject) args[0];

                try {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    Log.e("SOCKET", dataObject.toString());
                    switch (dataObject.getString("type")) {

                        case "answer":

                            Log.e("answer", dataObject.toString());

                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.ANSWER, dataObject.getString("sdp")));

                            break;

                        case "candidate":


                            JSONObject candidate = dataObject.getJSONObject("candidate");

                            Log.e("candidate", candidate.toString());

                            peerConnection.addIceCandidate(new IceCandidate(candidate.getString("sdpMid"), candidate.getInt("sdpMLineIndex"), candidate.getString("candidate")));
                            break;
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        mPeerConnConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mPeerConnConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mPeerConnConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "socket state connect");

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("channel", CHANNEL);
                    jsonObject.put("role", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("join", jsonObject.toString());
                mSocket.emit("join", jsonObject);

                peerConnection = factory.createPeerConnection(iceServers, mPeerConnConstraints, new SimplePeerConnectionObserver());
                peerConnection.addStream(mLocalMediaStream);

                SimpleSdpObserver simpleSdpObserver = new SimpleSdpObserver();

                peerConnection.createOffer(simpleSdpObserver, mPeerConnConstraints);
                peerConnection.setRemoteDescription(simpleSdpObserver, peerConnection.getLocalDescription());
            }
        });
        mSocket.connect();

        initScreenCapturStream();
    }

    public PeerConnection peerConnection;

    public class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.e("SimpleSdpObserver", "onCreateSuccess");

            peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("channel", CHANNEL);
                jsonObject.put("type", "call");

                JSONObject dataObject = new JSONObject();
                dataObject.put("sdp", sessionDescription.description);
                dataObject.put("type", "offer");

                jsonObject.put("data", dataObject);

                Log.e("sdp", sessionDescription.description);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("whisper", jsonObject.toString());
            mSocket.emit("whisper", jsonObject);

        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {

        }

        @Override
        public void onSetFailure(String s) {

        }
    }


    class SimplePeerConnectionObserver implements PeerConnection.Observer {

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//            Log.e("====>", "onIceConnectionChange");
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("channel", CHANNEL);
                jsonObject.put("type", "call");

                JSONObject dataObject = new JSONObject();
                dataObject.put("type", "candidate");

                JSONObject candidateObject = new JSONObject();

                candidateObject.put("candidate", iceCandidate.sdp);
                candidateObject.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                candidateObject.put("sdpMid", iceCandidate.sdpMid);

                dataObject.put("candidate", candidateObject);

                jsonObject.put("data", dataObject);

                Log.e("emit candidate", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("whisper", jsonObject.toString());

            mSocket.emit("whisper", jsonObject);

           peerConnection.addIceCandidate(iceCandidate);

            Log.e("====>", "onIceCandidate");
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

        }
    }


    /**
     * Call this method in Activity.onDestroy()
     */
    public void destroy() {
        for (Peer peer : peers.values()) {
            peer.pc.dispose();
        }
        if (factory != null) {
            factory.dispose();
        }
        if (mVideoSource != null) {
            mVideoSource.dispose();
        }
//        mSocket.disconnect();
//        mSocket.close();
    }

    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER; i++) if (!endPoints[i]) return i;
        return MAX_PEER;
    }

    /**
     * Start the mSocket.
     * <p>
     * Set up the local stream and notify the signaling server.
     * Call this method after onCallReady.
     *
     * @param name mSocket name
     */
    public void start(String name) {
        initScreenCapturStream();
        try {
            JSONObject message = new JSONObject();
            message.put("name", name);
            mSocket.emit("readyToStream", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initScreenCapturStream() {
        mLocalMediaStream = factory.createLocalMediaStream("ARDAMS");
        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(mPeerConnParams.videoHeight)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(mPeerConnParams.videoWidth)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(mPeerConnParams.videoFps)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(mPeerConnParams.videoFps)));

//        VideoCapturer capturer = createScreenCapturer();
        mVideoSource = factory.createVideoSource(true);
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), rootEglBase.getEglBaseContext());
        videoCapturer.initialize(surfaceTextureHelper, mContext, mVideoSource.getCapturerObserver());

        videoCapturer.startCapture(mPeerConnParams.videoWidth, mPeerConnParams.videoHeight, mPeerConnParams.videoFps);
        VideoTrack localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource);
        localVideoTrack.setEnabled(true);
        mLocalMediaStream.addTrack(factory.createVideoTrack("ARDAMSv0", mVideoSource));
        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        mLocalMediaStream.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));
//        mLocalMediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mLocalRender));
//        mListener.onLocalStream(mLocalMediaStream);
        mListener.onStatusChanged("STREAMING");
    }
}
