package rmg.droid.rmgcore.analytics

import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import rmg.droid.montecarlo.entity.Channel

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
class AnalyticsManager(val tracker: Tracker) {

    fun trackLike(channel: Channel) {
        tracker.send(HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Like")
                .setLabel("${channel.artist} - ${channel.title}")
                .build())
    }

    fun trackDislike(channel: Channel) {
        tracker.send(HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Dislike")
                .setLabel("${channel.artist} - ${channel.title}")
                .build())
    }

}