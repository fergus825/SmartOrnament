package com.edu.chzu.fg.smartornament.utils;

import android.view.animation.LinearInterpolator;

/**
 * Created by FG on 2017/2/7.
 */

public class JellyInterpolator extends LinearInterpolator{
    private float factor;

    public JellyInterpolator() {
        this.factor = 0.15f;
    }

    @Override
    public float getInterpolation(float input) {
        return (float) (Math.pow(2, -10 * input)
                * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
    }
}
