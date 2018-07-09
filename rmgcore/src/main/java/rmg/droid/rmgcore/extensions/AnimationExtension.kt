package rmg.droid.rmgcore.extensions

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.view.View
import rmg.droid.rmgcore.R

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
fun Animator.playWith(view: View, onAnimationEnd: () -> Unit = {}) {
    setTarget(view)
    start()
    addListener(object : Animator.AnimatorListener {

        override fun onAnimationEnd(animation: Animator?) {
            removeListener(this)
            onAnimationEnd.invoke()
        }

        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    })
}

fun Context.flipLeftOutAnimation() = AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_out)
fun Context.flipLeftInAnimation() = AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_in)