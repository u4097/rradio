package rmg.droid.rmgcore.media

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.os.RemoteException
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.google.android.exoplayer2.ExoPlaybackException
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.BuildConfig
import rmg.droid.rmgcore.event.NotificationUpdateEvent
import rmg.droid.rmgcore.media.player.ExoPlayerWrapper
import rmg.droid.rmgcore.media.player.ExoWrapperListener
import rmg.droid.rmgcore.services.MediaRepository
import java.io.IOException

/**
 * Created by Arthur Korchagin on 11.06.17
 */

class MediaPlayerService : Service(),
        AudioManager.OnAudioFocusChangeListener,
        ExoWrapperListener {

    interface PlayerControlsCallback {
        fun onPlay()
        fun onPause()
        fun onStop()
    }

    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    companion object {

        val KEY_EXTRA_MEDIA = "media"
        val KEY_EXTRA_VOLUME = "key_volume"

        val ACTION_NUMBER_PLAY = 1
        val ACTION_NUMBER_PAUSE = 2
        val ACTION_NUMBER_CLOSE = 3

        val ACTION_TOGGLE = "${BuildConfig.APPLICATION_ID}_ACTION_TOGGLE"
        val ACTION_PLAY = "${BuildConfig.APPLICATION_ID}_ACTION_PLAY"
        val ACTION_PAUSE = "${BuildConfig.APPLICATION_ID}_ACTION_PAUSE"
        val ACTION_STOP = "${BuildConfig.APPLICATION_ID}_ACTION_STOP"

        val ACTION_CLOSE = "${BuildConfig.APPLICATION_ID}_ACTION_STOP"
        val ACTION_VOLUME: String? = "${BuildConfig.APPLICATION_ID}_ACTION_VOLUME"

        val ACTION_SWITCH_CHANNEL = "${BuildConfig.APPLICATION_ID}_ACTION_SWITCH_CHANNEL"

        fun getActionByNumber(actionNumber: Int) = when (actionNumber) {
            ACTION_NUMBER_PLAY -> ACTION_PLAY
            ACTION_NUMBER_PAUSE -> ACTION_PAUSE
            ACTION_NUMBER_CLOSE -> ACTION_CLOSE
            else -> ""
        }
    }

    private var mMediaPlayer: ExoPlayerWrapper? = null
    private var mAudioManager: AudioManager? = null

    // Binder given to clients
    private val iBinder = LocalBinder()

    //Handle incoming phone calls
    private var mOngoingCall = false
    private var mPhoneStateListener: PhoneStateListener? = null
    private var mTelephonyManager: TelephonyManager? = null

    private val mPlayerControlsCallback: PlayerControlsCallback = object : PlayerControlsCallback {
        override fun onPlay() {
            resumeMedia()
        }

        override fun onPause() {
            pauseMedia()
        }

        override fun onStop() {
            stopMedia()
            stopSelf()
        }
    }

    private lateinit var mMediaRepository: MediaRepository
    private lateinit var mMediaNotification: MediaNotification
    private lateinit var mApplication: CoreRMGApp

    /** Lifecycle Methods */
    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)

        val app = (application as? CoreRMGApp)
        if (app != null) {
            mApplication = app
            mMediaRepository = app.mediaRepository
            mMediaNotification = MediaNotification(app, mPlayerControlsCallback)
            callStateListener()
            registerBecomingNoisyReceiver()
        } else {
            throw Error("Current application is not extend CoreRMGApp")
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (!requestAudioFocus() || intent == null) {
            stopSelf()
            mMediaNotification.removeNotification()
        }

        intent?.apply {

            if (action == ACTION_VOLUME) {
                mMediaPlayer?.apply { setVolume(getIntExtra(KEY_EXTRA_VOLUME, 100) * 0.01F) }
            } else if (action == ACTION_TOGGLE) {
                action = if (mMediaNotification.mCurrentStatus == MediaNotification.PlaybackStatus.PLAYING)
                    ACTION_PAUSE else ACTION_PLAY
            } else if (action == ACTION_CLOSE) {

                //  todo Checkit Out
                mApplication.serviceConnection.onServiceDisconnected(component) // todo this is quick fix!!!

                mMediaRepository.isPlaying = false
                stopMedia()
                stopSelf()
                mMediaNotification.removeNotification()

            }

            if (hasExtra(KEY_EXTRA_MEDIA)) {
                val mediaFile = getStringExtra(KEY_EXTRA_MEDIA)

                if (mMediaPlayer == null) {
                    initMediaPlayer(mediaFile)
                } else {
                    setSourceAndPlay(mediaFile)
                }

                if (action == ACTION_SWITCH_CHANNEL) {
//                mMediaNotification.initSource()

                } else {
                    try {
                        mMediaNotification.initSource()
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                        stopSelf()
                    }
                    mMediaNotification.buildNotification(MediaNotification.PlaybackStatus.PLAYING)
                }
            }

            //Handle Intent action from MediaSession.TransportControls
            mMediaNotification.handleIncomingActions(intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        stopSelf()
        mMediaNotification.removeNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        mMediaPlayer?.apply {
            pauseMedia()
            release()
        }
        removeAudioFocus()
        mPhoneStateListener?.apply {
            mTelephonyManager?.listen(this, PhoneStateListener.LISTEN_NONE)
        }
        mMediaNotification.removeNotification()
        unregisterReceiver(becomingNoisyReceiver)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationUpdateEvent(event: NotificationUpdateEvent) {
        mMediaNotification.updateNotification()
    }

    private fun initMediaPlayer(sourceUrl: String) {
        mMediaPlayer = ExoPlayerWrapper(this)
        mMediaPlayer?.apply {
            dataListener = mMediaRepository.dataListener
            exoWrapperListener = this@MediaPlayerService
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setSourceAndPlay(sourceUrl)
        }
    }

    /* Control Functions */
    private fun setSourceAndPlay(mediaFile: String) = mMediaPlayer?.apply {
        try {
            dataListener = mMediaRepository.dataListener
            setDataSource(mediaFile)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun pauseMedia() = mMediaPlayer?.apply {
        if (isPlaying) pause()
    }

    private fun resumeMedia() = mMediaPlayer?.apply {
        if (isPlaying) resume()
    }

    private fun stopMedia() = mMediaPlayer?.apply {
        if (isPlaying) stop()
    }

    /* Handle incoming phone calls */
    private fun callStateListener() {
        // Get the telephony manager
        mTelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        mPhoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                //if at least one call exists or the phone is ringing
                //pause the MediaPlayer
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mMediaPlayer != null) {
                        pauseMedia()
                        mOngoingCall = true
                    }

                    TelephonyManager.CALL_STATE_IDLE ->
                        // Phone idle. Start playing.
                        if (mMediaPlayer != null) {
                            if (mOngoingCall) {
                                mOngoingCall = false
                                resumeMedia()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager. Listen for changes to the device call state.
        mTelephonyManager?.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun requestAudioFocus(): Boolean {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = mAudioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true
        }
        //Could not gain focus
        return false
    }

    private fun removeAudioFocus() =
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager?.abandonAudioFocus(this)

    /* Becoming noisy */
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pauseMedia()
            mMediaNotification.buildNotification(MediaNotification.PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    /** LISTENERS */

    /* OnAudioFocusChangeListener */
    override fun onAudioFocusChange(focusState: Int) {
        // Invoked when the audio focus of the system is updated.
        when (focusState) {
        // resume playback
            AudioManager.AUDIOFOCUS_GAIN -> {
                mMediaPlayer?.apply {
                    if (!isPlaying) resumeMedia()
                    setVolume(1.0f)
                }
            }

        // Lost focus for an unbounded amount of time: pause playback and release media player
            AudioManager.AUDIOFOCUS_LOSS -> {
                mMediaPlayer?.apply {
                    if (isPlaying) pauseMedia()
                    release()
                }
                mMediaPlayer = null
            }

        // Lost focus for a short time, but we have to pause playback.
        // We don't release the media player because playback is likely to resume
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> mMediaPlayer?.apply {
                if (isPlaying) pauseMedia()
            }

        // Lost focus for a short time, but it's ok to keep playing at an attenuated level
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mMediaPlayer?.apply {
                if (isPlaying) setVolume(0.1f)
            }
        }
    }

    /* ExoWrapperListener */ /* TODO Check it! Maybe Don't needed */
    override fun onCompletion() {
        pauseMedia()
        stopSelf()
    }

    override fun onError(exoPlaybackError: ExoPlaybackException) {
        exoPlaybackError.printStackTrace()
        stopSelf()
    }

    override fun onStateChanged(isPlaying: Boolean) {
        mMediaRepository.isPlaying = isPlaying
    }

}
