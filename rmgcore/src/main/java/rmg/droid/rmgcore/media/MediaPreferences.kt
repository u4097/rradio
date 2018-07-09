package rmg.droid.rmgcore.media

import android.support.annotation.ArrayRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import rmg.droid.rmgcore.ui.custom.CoverTransform

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
data class MediaPreferences(
        @DrawableRes val iconPlay: Int? = null,
        @DrawableRes val iconPause: Int? = null,
        @DrawableRes val iconOff: Int? = null,
        @DrawableRes val iconNotification: Int,

        @DimenRes val viewPagerMargin: Int,
        @DimenRes val viewPagerPageMargin: Int,

        @ArrayRes val channelLabels: Int,
        @ArrayRes val channelMediaStreams: Int,
        @ArrayRes val channelDataStreams: Int,
        @ArrayRes val channelJingleLabels: Int,
        @StringRes val labelYouAreListen: Int,
        @ArrayRes val channelCoverPlaceholders: Int,
        @ArrayRes val channelGoNetwork: Int,
        val coverTransform: CoverTransform = CoverTransform.roundCorners

)