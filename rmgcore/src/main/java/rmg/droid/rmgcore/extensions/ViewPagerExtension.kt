package rmg.droid.rmgcore.extensions

import android.support.v4.view.ViewPager

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
fun ViewPager.setListener(onSelected: (Int) -> Unit) =

        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) = onSelected(position)
        })


