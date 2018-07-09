package rmg.droid.rmgcore.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.LayoutRes
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import org.jetbrains.anko.find
import org.jetbrains.anko.findOptional
import rmg.droid.montecarlo.entity.Cover
import rmg.droid.rmgcore.R
import rmg.droid.rmgcore.extensions.flipLeftInAnimation
import rmg.droid.rmgcore.extensions.flipLeftOutAnimation
import rmg.droid.rmgcore.extensions.onlyLoadImage
import rmg.droid.rmgcore.extensions.playWith
import rmg.droid.rmgcore.ui.custom.pager.EnchantedViewPager


/**
 *  @author Arthur Korchagin on 14.06.17.
 */
class ChannelsPagerAdapter(val context: Context,
                           @LayoutRes val layoutRes: Int,
                           val covers: MutableList<Cover>,
                           var titles: List<String>) : PagerAdapter() {

    var mCurrentImageView: ImageView? = null
    private var mCurrentPosition: Int = 0

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(layoutRes, collection, false) as ViewGroup
        val imageView = layout.findOptional<ImageView>(R.id.imgChannelCover)
                ?: throw Error("Layout has to contain ImageView with identifier imgChannelCover")

        layout.tag = EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position
        collection.addView(layout)
        setImage(covers[position], imageView)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return covers.count()
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }


    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        mCurrentPosition = position
        mCurrentImageView = (obj as? View)?.find<ImageView>(R.id.imgChannelCover)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    fun replace(index: Int, cover: Cover) {
        if (index == mCurrentPosition && covers[index] != cover) {

            Log.d(javaClass.name, "replace-> covers[index]=${covers[index]} cover=$cover")

            covers[index] = cover.copy()
            mCurrentImageView?.apply { setImage(covers[index], this) }
        }
    }

    private fun setImage(cover: Cover, imageView: ImageView) {
        imageView.onlyLoadImage(cover.placeholder, cover.url) {
            flipCover(imageView, it)
        }
    }

    private fun flipCover(imageView: ImageView, resource: Drawable) {
        // todo block other operations

        context.flipLeftOutAnimation().playWith(imageView) {
            imageView.setImageDrawable(resource)
            context.flipLeftInAnimation().playWith(imageView) {

                // todo unblock and perform Next Operation
            }
        }
    }

}