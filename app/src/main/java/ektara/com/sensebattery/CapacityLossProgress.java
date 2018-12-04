package ektara.com.sensebattery;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 * Created by mohoque on 14/01/2017.
 */

public class CapacityLossProgress extends Animation {
    private ProgressBar progressBar;
    private float from;
    private float  to;

    public CapacityLossProgress(ProgressBar progressBar, float from, float to) {
        super();
        this.progressBar = progressBar;
        this.from = from;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from+(to - from) * interpolatedTime;
        progressBar.setProgress((int) value);
    }

}
