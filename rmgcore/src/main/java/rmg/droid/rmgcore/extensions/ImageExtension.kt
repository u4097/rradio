package rmg.droid.rmgcore.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.ui.custom.CircleTransform
import rmg.droid.rmgcore.ui.custom.CoverTransform
import rmg.droid.rmgcore.ui.custom.RoundedCornersTransformation
import java.lang.Exception

/**
 *  @author Arthur Korchagin on 15.06.17.
 */

fun Context.coverTransform()
        = (applicationContext as? CoreRMGApp)?.mediaPreferences?.coverTransform
        ?: CoverTransform.roundCorners


fun <T> DrawableRequestBuilder<T>.applyTransform(context: Context): DrawableRequestBuilder<T> =
        when (context.coverTransform()) {
            CoverTransform.circle -> transform(CircleTransform(context))
            CoverTransform.roundCorners -> bitmapTransform(RoundedCornersTransformation(context, 10, 0))
        }

fun ImageView.onlyLoadImage(@DrawableRes placeHolder: Int, url: String?, onComplete: (Drawable) -> Unit) {

    if (url == null) {
        onComplete(ContextCompat.getDrawable(context, placeHolder)!!)
        return
    }

    Glide.with(context)
            .load(url)
            .centerCrop()
            .placeholder(placeHolder)
            .applyTransform(context)
            .listener(object : RequestListener<String, GlideDrawable> {
                override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                    if (resource != null) {
                        resource.apply { onComplete(this) }
                    } else {
                        onComplete(ContextCompat.getDrawable(context, placeHolder)!!)
                    }
                    return true
                }

                override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                    onComplete(ContextCompat.getDrawable(context, placeHolder)!!)
                    return false
                }
            })
            .preload()
}


fun Context.loadImage(url: String?, placeholder: Int, listener: (Bitmap) -> Unit) {
    if (url == null) {
        listener.invoke(BitmapFactory.decodeResource(resources, placeholder))
    } else {

        Glide.with(this)
                .load(url)
                .asBitmap()
                .placeholder(placeholder)
                .error(placeholder)
                .listener(
                        object : RequestListener<String, Bitmap> {
                            override fun onResourceReady(resource: Bitmap?, model: String?, target: Target<Bitmap>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                if (resource != null) {
                                    resource.apply { listener.invoke(this) }
                                } else {
                                    listener.invoke(BitmapFactory.decodeResource(resources, placeholder))
                                }
                                return false
                            }

                            override fun onException(e: Exception?, model: String?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                                listener.invoke(BitmapFactory.decodeResource(resources, placeholder))
                                return false
                            }
                        })
                // TODO Change This Sizes
                .into(512, 512)
    }
}
