package rmg.droid.rmgcore.ui.custom.pager;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import rmg.droid.rmgcore.app.CoreRMGApp;
import rmg.droid.rmgcore.R;
import rmg.droid.rmgcore.media.MediaPreferences;

/**
 * Created by tiago on 8/15/16
 */
public class EnchantedViewPager extends ViewPager {

    public final static float BIG_SCALE = 1.0f;
    public final static float SMALL_SCALE = 0.7f;
    public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;

    public static final String ENCHANTED_VIEWPAGER_POSITION = "ENCHANTED_VIEWPAGER_POSITION";

    private float mSwipeThreshold; //to avoid single touchs

    private boolean mUseAlpha;
    private boolean mUseScale;
    private boolean mUseSwipe;

    //variables to help swipe movements interpretation
    private float lastYactionDown = 0;

    private float originalDragXposition;
    private float originalDragYposition;

    private boolean dragStarted;

    private EnchantedViewPagerSwipeListener swipeListener;
    @Nullable
    private MediaPreferences mPreferencies = null;

    public EnchantedViewPager(Context context) {
        super(context);
        init();
    }

    public EnchantedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Context app = getContext().getApplicationContext();
        if (app instanceof CoreRMGApp) {
            mPreferencies = ((CoreRMGApp) app).getMediaPreferences();
        }

        useAlpha();
        useScale();

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset >= 0f && positionOffset <= 1f) {
                    View curPage = findViewWithTag(ENCHANTED_VIEWPAGER_POSITION + position);

                    if (curPage != null) {
                        if (mUseScale) {
                            curPage.setScaleY(BIG_SCALE - (DIFF_SCALE * positionOffset));
                            curPage.setScaleX(BIG_SCALE - (DIFF_SCALE * positionOffset));
                        }

                        if (mUseAlpha) {
                            curPage.setAlpha(1.0f - (0.5f * positionOffset));
                        }

                    }

                    View nextPage = findViewWithTag(ENCHANTED_VIEWPAGER_POSITION + (position + 1));
                    if (nextPage != null) {
                        if (mUseScale) {
                            nextPage.setScaleY(SMALL_SCALE + (DIFF_SCALE * positionOffset));
                            nextPage.setScaleX(SMALL_SCALE + (DIFF_SCALE * positionOffset));
                        }

                        if (mUseAlpha) {
                            nextPage.setAlpha(0.5f + (0.5f * positionOffset));
                        }
                    }

                    View previousPage = findViewWithTag(ENCHANTED_VIEWPAGER_POSITION + (position - 1));
                    if (previousPage != null) {
                        if (mUseScale) {
                            previousPage.setScaleY(SMALL_SCALE + (DIFF_SCALE * positionOffset));
                            previousPage.setScaleX(SMALL_SCALE + (DIFF_SCALE * positionOffset));
                        }

                        if (mUseAlpha) {
                            previousPage.setAlpha(0.5f + (0.5f * positionOffset));
                        }
                    }

                    View nextAfterPage = findViewWithTag(ENCHANTED_VIEWPAGER_POSITION + (position + 2));
                    if (nextAfterPage != null) {
                        if (mUseScale) {
                            nextAfterPage.setScaleX(SMALL_SCALE);
                            nextAfterPage.setScaleY(SMALL_SCALE);
                        }

                        if (mUseAlpha) {
                            nextAfterPage.setAlpha(0.5f);
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    //TouchEvent used for swipe actions
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mUseSwipe) return super.onTouchEvent(event);

        int action = MotionEventCompat.getActionMasked(event);

        final int position = getCurrentItem();
        final View currPage = findViewWithTag(ENCHANTED_VIEWPAGER_POSITION + position);
        if (currPage == null) {
            return super.onTouchEvent(event);
        }

        mSwipeThreshold = (currPage.getHeight() / 4);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                originalDragXposition = currPage.getX();
                originalDragYposition = currPage.getY();
                lastYactionDown = event.getY();
                return super.onTouchEvent(event);
            case (MotionEvent.ACTION_MOVE):

                if (!dragStarted && checkSwipe(event.getY())) {
                    dragStarted = true;
                }

                if (dragStarted) {
                    onDrag(event.getY(), currPage);
                    return true;
                } else {
                    return super.onTouchEvent(event);
                }

            case (MotionEvent.ACTION_UP):
                dragStarted = false;
                boolean dismissed = checkDismiss(event.getY(), currPage);

                if (!dismissed) {
                    currPage.setX(originalDragXposition);
                    currPage.setY(originalDragYposition);
                }
                return super.onTouchEvent(event);
            default:
                return super.onTouchEvent(event);
        }
    }

    private boolean checkDismiss(float y, View view) {
        float viewDismissThreshold = view.getHeight() / 2;
        if (originalDragYposition < y) {
            if ((y - lastYactionDown) > viewDismissThreshold) {
                onSwipe(SWIPE_DIRECTION.SWIPE_DOWN, view);
                return true;
            }
        } else {
            if ((lastYactionDown - y) > viewDismissThreshold) {
                onSwipe(SWIPE_DIRECTION.SWIPE_UP, view);
                return true;
            }
        }


        return false;
    }

    private boolean checkSwipe(float eventY) {
        if (lastYactionDown < eventY) { //swipe down
            //check if the user swiped long enough
            if ((eventY - lastYactionDown) > mSwipeThreshold) {
                return true;
            }

        } else { // swipe up
            //check if the user swiped long enough
            if ((lastYactionDown - eventY) > mSwipeThreshold) {
                return true;
            }
        }

        return false;
    }

    private void onDrag(float y, View view) {
        view.setX(originalDragXposition);
        view.setY(y - (view.getHeight() / 2));
    }

    private void onSwipe(SWIPE_DIRECTION direction, View view) {
        float translationValue = 0;

        switch (direction) {
            case SWIPE_UP:
                translationValue = -view.getHeight();
                break;
            case SWIPE_DOWN:
                translationValue = view.getHeight();
                break;
        }

        view.animate()
                .translationY(translationValue)
                .alpha(0.0f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                swipeListener.onSwipeFinished(getCurrentItem());
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public void addSwipeToDismiss(EnchantedViewPagerSwipeListener listener) {
        this.swipeListener = listener;
        mUseSwipe = true;
    }

    public void useScale() {
        boolean hasPrefs = mPreferencies != null;
        int paddingInPx = getResources().getDimensionPixelSize(hasPrefs ? mPreferencies.getViewPagerMargin() : R.dimen.view_pager_margin);
        int pageMarginInPx = getResources().getDimensionPixelSize(hasPrefs ? mPreferencies.getViewPagerPageMargin() : R.dimen.view_pager_page_margin);
        useScale(paddingInPx, pageMarginInPx);
    }

    public void useScale(int paddingInPx, int pageMarginInPx) {
        setClipToPadding(false);
        setPadding(paddingInPx, 0, paddingInPx, 0);
        setPageMargin(pageMarginInPx);

        mUseScale = true;

    }

    public void useAlpha() {
        mUseAlpha = true;
    }

    public void removeSwipe() {
        mUseSwipe = false;
    }

    public void removeAlpha() {
        mUseAlpha = false;
    }

    public void removeScale() {
        mUseScale = false;
        setPadding(0, 0, 0, 0);
    }

    public interface EnchantedViewPagerSwipeListener {
        void onSwipeFinished(int position);
    }

    private enum SWIPE_DIRECTION {
        SWIPE_UP, SWIPE_DOWN
    }
}