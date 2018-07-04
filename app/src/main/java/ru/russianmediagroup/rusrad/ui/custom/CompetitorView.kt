package ru.russianmediagroup.rusrad.ui.custom

import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.like.LikeButton
import com.like.OnLikeListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.v_competitor.view.*
import rmg.droid.rmgcore.analytics.AnalyticsService
import rmg.droid.rmgcore.extensions.playWith
import ru.russianmediagroup.rusrad.BuildConfig
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.extensions.apiError
import ru.russianmediagroup.rusrad.network.NetworkServiceImpl
import ru.russianmediagroup.rusrad.network.models.Image
import ru.russianmediagroup.rusrad.network.models.Poll
import ru.russianmediagroup.rusrad.network.models.PollOption
import kotlin.properties.Delegates


/**
 * @author by Arthur Korchagin on 03.05.18.
 */

@SuppressLint("ViewConstructor")
class CompetitorView(context: Context, private var pollOption: PollOption, private val poll: Poll) : FrameLayout(context), OnLikeListener {

    private val mAnalytics: AnalyticsService?
        get() = (context.applicationContext as? RusRadioApp)?.analyticsService

    override fun liked(likeButton: LikeButton?) {
        toggleLike()
    }

    override fun unLiked(likeButton: LikeButton?) {
        likeButton?.isLiked = true
    }

    private val compositeDisposable = CompositeDisposable()

    private var position by Delegates.observable(0) { _, old, new ->
        updatePosition(old, new)
    }

    init {
        inflate(context, R.layout.v_competitor, this)
        vCompetitorBtnPrev.setOnClickListener { previous() }
        vCompetitorBtnInfo.setOnClickListener { info() }
        vCompetitorBtnNext.setOnClickListener { next() }
        vCompetitorIndicatorBtnShare.setOnClickListener { share() }
        vCompetitorBtnLike.setOnLikeListener(this)

        vCompetitorIndicator.count = (pollOption.poll_item?.pictures?.size ?: 0) + 1
        position = 0

        vCompetitorTvDescription.text = pollOption.poll_item?.description ?: ""
        vCompetitorTvTitle.text = pollOption.poll_item?.title ?: ""
        vCompetitorTvNumber.text = pollOption.order.toString()
        vCompetitorTvSubtitle.text = pollOption.poll_item?.info?.joinToString(separator = "\t") { if (it.value.isNotBlank()) "${it.key.replace(' ', ' ')}:\u00A0${it.value.replace(' ', ' ')}" else "" } ?: ""

        updateLikes()
    }

    private fun updateLikes() {
        vCompetitorBtnLike.isLiked = pollOption.is_voted
        vCompetitorTvLikes.text = pollOption.rating.toString()
    }

    private fun updatePosition(oldPosition: Int, newPosition: Int) {
        vCompetitorIndicator.selected = newPosition
        pollOption.poll_item?.pictures?.elementAtOrNull(newPosition)?.let(::updateImage)
        if (newPosition == vCompetitorIndicator.count - 1) {
            flip(true)
        } else if (oldPosition == vCompetitorIndicator.count - 1) {
            flip(false)
        }
    }

    private fun flip(info: Boolean) {
        vCompetitorCardView.cardElevation = 0f

        val scale = context.resources.displayMetrics.density
        vCompetitorCardView.cameraDistance = 10000 * scale

        context?.outAnimation()?.playWith(vCompetitorCardView) {
            changeState(info)
            context?.inAnimation()?.playWith(vCompetitorCardView) {
                vCompetitorCardView.cardElevation = resources.getDimension(R.dimen.card_elevation)
            }
        }
    }

    private fun changeState(info: Boolean) {

        vCompetitorTvDescription.visibility = if (info) View.VISIBLE else View.GONE
        val competitorVis = if (info) View.GONE else View.VISIBLE
        vCompetitorIvPicture.visibility = competitorVis
        vCompetitorVOverlay.visibility = competitorVis
        vCompetitorIndicatorBtnShare.visibility = competitorVis

        val bgColor = if (info) R.color.white else R.color.black
        val txtColor = if (info) R.color.black else R.color.white
        vCompetitorCardView.setCardBackgroundColor(ContextCompat.getColor(context, bgColor))

        vCompetitorTvLikes.setTextColor(ContextCompat.getColor(context, txtColor))
        vCompetitorTvSubtitle.setTextColor(ContextCompat.getColor(context, txtColor))
        vCompetitorTvTitle.setTextColor(ContextCompat.getColor(context, txtColor))

        val constraintSet = ConstraintSet()
        constraintSet.clone(vCompetitorConstraintRoot)
        if (info) {
            constraintSet.clear(R.id.vCompetitorIndicatorLInfo, ConstraintSet.BOTTOM)
        } else {
            constraintSet.connect(R.id.vCompetitorIndicatorLInfo, ConstraintSet.BOTTOM, R.id.vCompetitorIndicator, ConstraintSet.TOP)
        }
        constraintSet.applyTo(vCompetitorConstraintRoot)
    }

    private fun updateImage(image: Image) {
        Glide.with(context)
                .load(image.url)
                .asBitmap()
                .placeholder(R.drawable.bg_placeholder)
                .animate(R.anim.fade_in)
                .into(vCompetitorIvPicture)
    }

    private fun previous() {
        if (position > 0) {
            position--
        }
    }

    private fun share() {
        val pollItemTitle = pollOption.poll_item?.title ?: return

        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        val text = if (poll.sharing_text.run { isNotBlank() && contains('$') }) "${poll.sharing_text.replace("$", pollItemTitle)}\n" else ""
        sendIntent.putExtra(Intent.EXTRA_TEXT, "$text https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
        sendIntent.type = "text/plain"
        context.startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
    }

    private fun next() {
        if (position < vCompetitorIndicator.count - 1) {
            position++
        }
    }

    private fun info() {
        position = if (position == vCompetitorIndicator.count - 1) 0 else vCompetitorIndicator.count - 1
    }

    private fun toggleLike() {
        val pollId = pollOption.id
        if (pollId != null) {
            vCompetitorTvLikes.text = (pollOption.rating + 1).toString()

            compositeDisposable.add(RusRadioApp.inst.networkService.toggleLike(pollId)
                    .subscribe(this::onLiked, this::onError))
        }
    }

    private fun onError(error: Throwable) {
        updateLikes()

        error.apiError()?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun onLiked(option: PollOption) {
        mAnalytics?.vote(option.poll_item_id ?: "")
        pollOption = option
        updateLikes()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    private fun Context.outAnimation() =
            AnimatorInflater.loadAnimator(this, R.animator.card_flip_top_out)

    private fun Context.inAnimation() =
            AnimatorInflater.loadAnimator(this, R.animator.card_flip_top_in)
}

