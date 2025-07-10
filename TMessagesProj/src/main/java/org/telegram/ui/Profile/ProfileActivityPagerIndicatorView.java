package org.telegram.ui.Profile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Components.CubicBezierInterpolator;

class ProfileActivityPagerIndicatorView extends View {

    private final ProfileActivity profileActivity;
    private final RectF indicatorRect = new RectF();

    private final TextPaint textPaint;
    private final Paint backgroundPaint;

    private final ValueAnimator animator;
    private final float[] animatorValues = new float[]{0f, 1f};

    private PagerAdapter adapter() {
        return profileActivity.avatarsViewPager.getAdapter();
    }

    private boolean isIndicatorVisible;

    public ProfileActivityPagerIndicatorView(ProfileActivity profileActivity, Context context) {
        super(context);
        this.profileActivity = profileActivity;
        setVisibility(GONE);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(AndroidUtilities.dpf2(15f));
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0x26000000);
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
        animator.addUpdateListener(a -> {
            final float value = AndroidUtilities.lerp(animatorValues, a.getAnimatedFraction());
            if (profileActivity.searchItem != null && !profileActivity.isPulledDown) {
                profileActivity.searchItem.setScaleX(1f - value);
                profileActivity.searchItem.setScaleY(1f - value);
                profileActivity.searchItem.setAlpha(1f - value);
            }
            if (profileActivity.editItemVisible) {
                profileActivity.editItem.setScaleX(1f - value);
                profileActivity.editItem.setScaleY(1f - value);
                profileActivity.editItem.setAlpha(1f - value);
            }
            if (profileActivity.callItemVisible) {
                profileActivity.callItem.setScaleX(1f - value);
                profileActivity.callItem.setScaleY(1f - value);
                profileActivity.callItem.setAlpha(1f - value);
            }
            if (profileActivity.videoCallItemVisible) {
                profileActivity.videoCallItem.setScaleX(1f - value);
                profileActivity.videoCallItem.setScaleY(1f - value);
                profileActivity.videoCallItem.setAlpha(1f - value);
            }
            setScaleX(value);
            setScaleY(value);
            setAlpha(value);
        });
        boolean expanded = profileActivity.expandPhoto;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isIndicatorVisible) {
                    if (profileActivity.searchItem != null) {
                        profileActivity.searchItem.setClickable(false);
                    }
                    if (profileActivity.editItemVisible) {
                        profileActivity.editItem.setVisibility(GONE);
                    }
                    if (profileActivity.callItemVisible) {
                        profileActivity.callItem.setVisibility(GONE);
                    }
                    if (profileActivity.videoCallItemVisible) {
                        profileActivity.videoCallItem.setVisibility(GONE);
                    }
                } else {
                    setVisibility(GONE);
                }
                profileActivity.updateStoriesViewBounds(false);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (profileActivity.searchItem != null && !expanded) {
                    profileActivity.searchItem.setClickable(true);
                }
                if (profileActivity.editItemVisible) {
                    profileActivity.editItem.setVisibility(VISIBLE);
                }
                if (profileActivity.callItemVisible) {
                    profileActivity.callItem.setVisibility(VISIBLE);
                }
                if (profileActivity.videoCallItemVisible) {
                    profileActivity.videoCallItem.setVisibility(VISIBLE);
                }
                setVisibility(VISIBLE);
                profileActivity.updateStoriesViewBounds(false);
            }
        });
        profileActivity.avatarsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int prevPage;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int realPosition = profileActivity.avatarsViewPager.getRealPosition(position);
                invalidateIndicatorRect(prevPage != realPosition);
                prevPage = realPosition;
                updateAvatarItems();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        adapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                int count = profileActivity.avatarsViewPager.getRealCount();
                if (profileActivity.overlayCountVisible == 0 && count > 1 && count <= 20 && profileActivity.overlaysView.isOverlaysVisible()) {
                    profileActivity.overlayCountVisible = 1;
                }
                invalidateIndicatorRect(false);
                refreshVisibility(1f);
                updateAvatarItems();
            }
        });
    }

    private void updateAvatarItemsInternal() {
        if (profileActivity.otherItem == null || profileActivity.avatarsViewPager == null) {
            return;
        }
        if (profileActivity.isPulledDown) {
            int position = profileActivity.avatarsViewPager.getRealPosition();
            if (position == 0) {
                profileActivity.otherItem.hideSubItem(ProfileActivity.set_as_main);
                profileActivity.otherItem.showSubItem(ProfileActivity.add_photo);
            } else {
                profileActivity.otherItem.showSubItem(ProfileActivity.set_as_main);
                profileActivity.otherItem.hideSubItem(ProfileActivity.add_photo);
            }
        }
    }

    private void updateAvatarItems() {
        if (profileActivity.imageUpdater == null) {
            return;
        }
        if (profileActivity.otherItem.isSubMenuShowing()) {
            AndroidUtilities.runOnUIThread(this::updateAvatarItemsInternal, 500);
        } else {
            updateAvatarItemsInternal();
        }
    }

    public boolean isIndicatorVisible() {
        return isIndicatorVisible;
    }

    public boolean isIndicatorFullyVisible() {
        return isIndicatorVisible && !animator.isRunning();
    }

    public void setIndicatorVisible(boolean indicatorVisible, float durationFactor) {
        if (indicatorVisible != isIndicatorVisible) {
            isIndicatorVisible = indicatorVisible;
            animator.cancel();
            final float value = AndroidUtilities.lerp(animatorValues, animator.getAnimatedFraction());
            if (durationFactor <= 0f) {
                animator.setDuration(0);
            } else if (indicatorVisible) {
                animator.setDuration((long) ((1f - value) * 250f / durationFactor));
            } else {
                animator.setDuration((long) (value * 250f / durationFactor));
            }
            animatorValues[0] = value;
            animatorValues[1] = indicatorVisible ? 1f : 0f;
            animator.start();
        }
    }

    public void refreshVisibility(float durationFactor) {
        setIndicatorVisible(profileActivity.isPulledDown && profileActivity.avatarsViewPager.getRealCount() > 20, durationFactor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        invalidateIndicatorRect(false);
    }

    private void invalidateIndicatorRect(boolean pageChanged) {
        if (pageChanged) {
            profileActivity.overlaysView.saveCurrentPageProgress();
        }
        profileActivity.overlaysView.invalidate();
        final float textWidth = textPaint.measureText(getCurrentTitle());
        indicatorRect.right = getMeasuredWidth() - AndroidUtilities.dp(54f) - (profileActivity.qrItem != null ? AndroidUtilities.dp(48) : 0);
        indicatorRect.left = indicatorRect.right - (textWidth + AndroidUtilities.dpf2(16f));
        indicatorRect.top = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(15f);
        indicatorRect.bottom = indicatorRect.top + AndroidUtilities.dp(26);
        setPivotX(indicatorRect.centerX());
        setPivotY(indicatorRect.centerY());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float radius = AndroidUtilities.dpf2(12);
        canvas.drawRoundRect(indicatorRect, radius, radius, backgroundPaint);
        canvas.drawText(getCurrentTitle(), indicatorRect.centerX(), indicatorRect.top + AndroidUtilities.dpf2(18.5f), textPaint);
    }

    private String getCurrentTitle() {
        return adapter().getPageTitle(profileActivity.avatarsViewPager.getCurrentItem()).toString();
    }

    ActionBarMenuItem getSecondaryMenuItem() {
        if (profileActivity.callItemVisible) {
            return profileActivity.callItem;
        } else if (profileActivity.editItemVisible) {
            return profileActivity.editItem;
        } else if (profileActivity.searchItem != null) {
            return profileActivity.searchItem;
        } else {
            return null;
        }
    }
}
