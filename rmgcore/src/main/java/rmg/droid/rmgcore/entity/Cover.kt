package rmg.droid.montecarlo.entity

import android.graphics.Bitmap
import android.support.annotation.DrawableRes

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
data class Cover(@DrawableRes val placeholder: Int, val url: String? = null) {
    var cachedBmp: Bitmap? = null
}