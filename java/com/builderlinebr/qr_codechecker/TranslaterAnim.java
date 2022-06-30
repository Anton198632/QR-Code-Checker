package com.builderlinebr.qr_codechecker;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class TranslaterAnim implements Animation.AnimationListener {

    View view;
    int duration;
    boolean invert;
    TranslateAnimation translateAnimation;
    float fromY = 0;
    float toY = 0;


    public TranslaterAnim(View view, int duration, float fromY, float toY) {
        this.view = view;
        this.invert = true;
        this.duration = duration;

        this.fromY = fromY;
        this.toY = toY;

        translateAnimation = new TranslateAnimation(0, 0, fromY, toY);
        translateAnimation.setAnimationListener(this);
        translateAnimation.setDuration(duration);
        view.startAnimation(translateAnimation);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if (invert) {
            translateAnimation = new TranslateAnimation(0, 0, toY, fromY);
            invert = false;

        } else {
            translateAnimation = new TranslateAnimation(0, 0, fromY, toY);
            invert = true;
        }
        translateAnimation.setAnimationListener(this);
        translateAnimation.setDuration(duration);
        view.startAnimation(translateAnimation);

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
