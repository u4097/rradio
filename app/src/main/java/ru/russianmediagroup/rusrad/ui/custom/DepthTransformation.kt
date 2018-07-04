package ru.russianmediagroup.rusrad.ui.custom

import android.support.v4.view.ViewPager
import android.view.View

object DepthTransformation : ViewPager.PageTransformer {
    override fun transformPage(page: View, position: Float) =
            when {
                position < -1 -> // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    page.alpha = 0f

                position <= 0 -> {    // [-1,0]
                    page.translationX = -position * page.width
                    page.alpha = 1 - Math.abs(position)
                    page.scaleX = 1 - Math.abs(position) * 0.2f
                    page.scaleY = 1 - Math.abs(position) * 0.2f
                }

                position <= 1 -> {    // (0,1]
                    page.alpha = 1f
                    page.translationX = 0f
                    page.scaleX = 1f
                    page.scaleY = 1f
                }

                else -> // (1,+Infinity]
                    // This page is way off-screen to the right.
                    page.alpha = 0f
            }


}