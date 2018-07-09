package rmg.droid.rmgcore.media

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.RemoteException
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import rmg.droid.rmgcore.R
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.extensions.loadImage
import rmg.droid.rmgcore.media.player.ExoPlayerWrapper


/**
 *  @author Arthur Korchagin on 12.06.17.
 */

class MediaNotification(val app: CoreRMGApp, val playerControlsCallback: MediaPlayerService.PlayerControlsCallback) {

    companion object {
        private val NOTIFICATION_ID = 264
        private val PAUSE = "pause"
        private val CLOSE = "close"
    }

    enum class PlaybackStatus {
        PLAYING,
        PAUSED,
        PENDING
    }

    /* Private Properties */
    private var mMediaSession: MediaSessionCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null

    // todo Change it by enevt bus
    var mCurrentStatus = PlaybackStatus.PENDING
        private set
        get

    /* TODO change to DI */
    private val mMediaRepository = app.mediaRepository
    private val preferencies = app.mediaPreferences

    /* Initializers */
    fun initSource() {
        if (mMediaSession == null) initMediaSession()
    }

    @Throws(RemoteException::class)
    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(app.applicationContext, MediaButtonReceiver::class.java)
        mMediaSession = MediaSessionCompat(app.applicationContext, ExoPlayerWrapper.EXO_PLAYER, mediaButtonReceiver, null)
        mMediaSession?.apply {
            mTransportControls = controller.transportControls
            isActive = true
            setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)

            updateNotification()

            setCallback(object : MediaSessionCompat.Callback() {

                override fun onPlay() {
                    super.onPlay()
                    buildNotification(PlaybackStatus.PLAYING)
                    playerControlsCallback.onPlay()
                }

                override fun onPause() {
                    super.onPause()
                    buildNotification(PlaybackStatus.PAUSED)
                    playerControlsCallback.onPause()
                }

                override fun onStop() {
                    super.onStop()
                    removeNotification()
                    playerControlsCallback.onStop()
                }
            })
        }
    }

    /* Builders */
    fun buildNotification(playbackStatus: PlaybackStatus) {
        mCurrentStatus = playbackStatus
        updateNotification()
    }

    private fun showNotification(cover: Bitmap) {
        var notificationAction = preferencies.iconPause ?: R.drawable.ic_pause
        var action: PendingIntent? = null

        if (mCurrentStatus == PlaybackStatus.PLAYING) {
            notificationAction = preferencies.iconPause ?: R.drawable.ic_pause
            action = playbackAction(MediaPlayerService.ACTION_NUMBER_PAUSE)
        } else if (mCurrentStatus == PlaybackStatus.PAUSED) {
            notificationAction = preferencies.iconPause ?: R.drawable.ic_play
            action = playbackAction(MediaPlayerService.ACTION_NUMBER_PLAY)
        }
        val closeAction = playbackAction(MediaPlayerService.ACTION_NUMBER_CLOSE)


        val notificationBuilder = NotificationCompat.Builder(app)
                .setShowWhen(false)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession?.sessionToken)
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0, 1))
                .setLargeIcon(cover)
                .setSmallIcon(preferencies.iconNotification)
                .setContentText(mMediaRepository.artist())
                .setContentTitle(mMediaRepository.title())
                .setContentIntent(mMediaSession?.controller?.sessionActivity)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(notificationAction, PAUSE, action)
                .addAction(preferencies.iconOff ?: R.drawable.ic_off, CLOSE, closeAction)

                .setContentIntent(PendingIntent.getActivity(app, 144, Intent(app, app.activityClass), PendingIntent.FLAG_UPDATE_CURRENT))

        NotificationManagerCompat.from(app).notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(app, MediaPlayerService::class.java)
        playbackAction.action = MediaPlayerService.getActionByNumber(actionNumber)
        return PendingIntent.getService(app, actionNumber, playbackAction, 0)
    }

    fun removeNotification() {
        val notificationManager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun handleIncomingActions(playbackAction: Intent?) = playbackAction?.action?.apply {
        when (this) {
            MediaPlayerService.ACTION_PLAY -> mTransportControls?.play()
            MediaPlayerService.ACTION_PAUSE -> mTransportControls?.pause()
            MediaPlayerService.ACTION_STOP -> mTransportControls?.stop()
        }
    }

    fun updateNotification() {
        val coverModel = mMediaRepository.cover()
        val cachedBmb = coverModel.cachedBmp
        if (cachedBmb != null) {
            updateMetaData(cachedBmb)
            showNotification(cachedBmb)
        } else {

            val url = coverModel.url

            app.loadImage(url, coverModel.placeholder) {
                coverModel.cachedBmp = it
                updateMetaData(it)
                showNotification(it)
            }

        }
    }

    fun updateMetaData(cover: Bitmap) {
        mMediaSession?.setMetadata(MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, cover)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, cover)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, cover)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mMediaRepository.artist())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mMediaRepository.title())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, mMediaRepository.title())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, app.getString(R.string.app_name))
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1)
                .build())
    }

}