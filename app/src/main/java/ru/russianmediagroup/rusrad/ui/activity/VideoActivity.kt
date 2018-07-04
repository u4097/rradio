package ru.russianmediagroup.rusrad.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.ac_fullscreen_video.*
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.network.models.ActiveTranslation
import ru.russianmediagroup.rusrad.ui.fragment.PlayerManager

/**
 * @author by Arthur Korchagin on 24.12.17.
 */
class VideoActivity : AppCompatActivity(), PlayerManager.OnPlayerListener {

    private val compositeDisposable = CompositeDisposable()

    private val playerManager by lazy { PlayerManager() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.ac_fullscreen_video)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        acVideoBtnFullscreen.setOnClickListener { openWatch() }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        acVideoProgress?.visibility = View.VISIBLE
        acVideoExoView?.visibility = View.GONE
        acVideoLPlayback?.visibility = View.GONE

        RusRadioApp.inst.networkService.loadVideoUrl()
                .subscribe(this::onResult, this::onError)
                .unsubscribeOnDestroy()
    }

    private fun startTranslation(url: String) {
        playerManager.init(this, acVideoExoView, this, url)
    }

    override fun onPause() {
        super.onPause()
        playerManager.reset()
    }

    override fun onDestroy() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        playerManager.release()
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onLoading() {
        acVideoProgress?.visibility = View.VISIBLE
        acVideoTvError?.visibility = View.GONE
        acVideoNoTranslation?.visibility = View.GONE
    }

    override fun onError(error: String) {
        acVideoProgress?.visibility = View.GONE
        acVideoTvError?.visibility = View.VISIBLE
        acVideoNoTranslation?.visibility = View.GONE
    }

    override fun onPlay() {
        acVideoExoView?.visibility = View.VISIBLE
        acVideoLPlayback?.visibility = View.VISIBLE

        acVideoProgress?.visibility = View.GONE
        acVideoTvError?.visibility = View.GONE
        acVideoNoTranslation?.visibility = View.GONE
    }

    private fun Disposable.unsubscribeOnDestroy() =
            compositeDisposable.add(this)

    override fun onIdle() {
        playerManager.restart()
    }

    override fun onEnded() {
        playerManager.restart()
    }

    private fun onResult(result: ActiveTranslation) {
        val url = result.primary_stream_url
        if (url != null && result.show_time?.isBlank() == false) {
            startTranslation(url)
        } else {
            acVideoProgress?.visibility = View.GONE
            acVideoTvError?.visibility = View.GONE
            acVideoNoTranslation?.visibility = View.VISIBLE
        }
    }

    private fun onError(error: Throwable) {
        acVideoProgress?.visibility = View.GONE
        acVideoTvError?.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        openWatch()
    }

    private fun openWatch() {
        val intent = Intent(this, MusicActivity::class.java)
        intent.putExtra(MusicActivity.EXTRA_SCREEN, MusicActivity.SCREEN_WATCH)
        startActivity(intent)
        finish()

    }
}