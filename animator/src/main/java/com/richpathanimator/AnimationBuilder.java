package com.richpathanimator;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.Interpolator;

import com.richpath.RichPath;

import java.util.ArrayList;
import java.util.List;

import static com.richpathanimator.PathAnimator.RESTART;

/**
 * Created by tarek on 6/29/17.
 */


public class AnimationBuilder {


    private static final long DEFAULT_DURATION = 300;
    private static final long DEFAULT_START_DELAY = 0;

    private final PathAnimator pathAnimator;
    private final RichPath[] paths;
    private final List<ValueAnimator> animators = new ArrayList<>();

    private long duration = DEFAULT_DURATION;
    private long startDelay = DEFAULT_START_DELAY;
    private Interpolator interpolator;
    private int repeatMode = RESTART;
    private int repeatCount = 0;

    public AnimationBuilder(PathAnimator pathAnimator, RichPath... paths) {
        this.pathAnimator = pathAnimator;
        this.paths = paths;
    }

    private void property(String propertyName, float... values) {
        for (final RichPath path : paths) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(path, propertyName, values);
            applyAnimatorProperties(objectAnimator, path);
        }
    }

    public AnimationBuilder andAnimate(RichPath... paths) {
        return pathAnimator.addAnimationBuilder(paths);
    }

    public AnimationBuilder thenAnimate(RichPath... paths) {
        return pathAnimator.thenAnimate(paths);
    }

    /**
     * Custom animation builder.
     *
     * @param listener the AnimationUpdateListener
     * @param values   A set of values that the animation will animate between over time.
     */
    public AnimationBuilder custom(final AnimationUpdateListener listener, float... values) {
        for (final RichPath path : paths) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(values);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (path != null) {
                        if (listener != null) {
                            listener.update(path, (Float) animation.getAnimatedValue());
                        }
                        if (path.getOnRichPathUpdatedListener() != null) {
                            path.getOnRichPathUpdatedListener().onPathUpdated();
                        }
                    }
                }
            });

            applyAnimatorProperties(valueAnimator, path);
        }
        return this;
    }

    public PathAnimator start() {
        pathAnimator.start();
        return pathAnimator;
    }

    List<ValueAnimator> getAnimators() {
        return animators;
    }

    public AnimationBuilder duration(long duration) {
        this.duration = duration;
        for (ValueAnimator animator : animators) {
            animator.setDuration(duration);
        }
        return this;
    }

    public AnimationBuilder durationSet(long duration) {
        pathAnimator.duration(duration);
        return this;
    }

    public AnimationBuilder startDelay(long startDelay) {
        this.startDelay = startDelay;
        for (ValueAnimator animator : animators) {
            animator.setStartDelay(startDelay);
        }
        return this;
    }

    public AnimationBuilder startDelaySet(long startDelay) {
        pathAnimator.startDelay(startDelay);
        return this;
    }

    public AnimationBuilder interpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        for (ValueAnimator animator : animators) {
            animator.setDuration(duration);
        }
        return this;
    }

    public AnimationBuilder interpolatorSet(Interpolator interpolator) {
        pathAnimator.interpolator(interpolator);
        return this;
    }

    public AnimationBuilder repeatMode(@PathAnimator.RepeatMode int repeatMode) {
        this.repeatMode = repeatMode;
        for (ValueAnimator animator : animators) {
            animator.setRepeatMode(repeatMode);
        }
        return this;
    }

    public AnimationBuilder repeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
        for (ValueAnimator animator : animators) {
            animator.setRepeatCount(repeatCount);
        }
        return this;
    }

    public AnimationBuilder fillColor(int... colors) {
        color("fillColor", colors);
        return this;
    }

    public AnimationBuilder strokeColor(int... colors) {
        color("strokeColor", colors);
        return this;
    }

    public AnimationBuilder color(String propertyName, int... colors) {
        for (final RichPath path : paths) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(path, propertyName, colors);
            objectAnimator.setEvaluator(new ArgbEvaluator());
            applyAnimatorProperties(objectAnimator, path);
        }
        return this;
    }

    private void applyAnimatorProperties(ValueAnimator animator, final RichPath path) {

        if (path == null) {
            return;
        }
        animator.setDuration(duration);
        animator.setStartDelay(startDelay);
        animator.setRepeatMode(repeatMode);
        animator.setRepeatCount(repeatCount);
        if (interpolator != null) {
            animator.setInterpolator(interpolator);
        }
        //add animator to the animators list
        this.animators.add(animator);
    }

    public AnimationBuilder strokeAlpha(float... alpha) {
        property("strokeAlpha", alpha);
        return this;
    }

    public AnimationBuilder fillAlpha(float... alpha) {
        property("fillAlpha", alpha);
        return this;
    }

    public AnimationBuilder size(float width, float height) {
        property("width", width);
        property("height", height);
        return this;
    }

    public AnimationBuilder scaleX(float... values) {
        scale("width", values);
        return this;
    }

    public AnimationBuilder scaleY(float... values) {
        scale("height", values);
        return this;
    }

    private void scale(String propertyName, float... values) {

        for (final RichPath path : paths) {

            float[] scaledValues = new float[values.length];

            float pathDimen;
            if (propertyName.equals("height")) {
                pathDimen = path.getOriginalHeight();
            } else {
                pathDimen = path.getOriginalWidth();
            }

            for (int i = 0; i < values.length; i++) {
                scaledValues[i] = pathDimen * values[i];
            }

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(path, propertyName, scaledValues);
            applyAnimatorProperties(objectAnimator, path);
        }
    }

    public AnimationBuilder scale(float... values) {
        scaleX(values);
        scaleY(values);
        return this;
    }

    public AnimationBuilder width(float... values) {
        property("width", values);
        return this;
    }

    public AnimationBuilder height(float... values) {
        property("height", values);
        return this;
    }

    public AnimationBuilder rotation(float... values) {

//        for (final RichPath path : paths) {
//            float fromValue = path.getRotation();
//            Log.d("reer", " fromValue: " + fromValue);
//
//            for (int i = 0; i < values.length; i++) {
//
//                Log.d("reer", "rotation: " + values[i]);
//
//                float deltaValue = values[i] - fromValue;
//                values[i] = deltaValue;
//                Log.d("reer", "drotation: " + deltaValue);
//
//            }
//        }


        for (final RichPath path : paths) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(path, "rotation", values);
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
//                    path.setRotation(-path.getRotation());
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            applyAnimatorProperties(objectAnimator, path);
        }


        return this;

    }

    public AnimationBuilder translationY(float... values) {
        property("translationY", values);
        return this;
    }

    public AnimationBuilder translationX(float... values) {
        property("translationX", values);
        return this;
    }

    public AnimationBuilder trimPathStart(float... values) {
        property("trimPathStart", values);
        return this;

    }

    public AnimationBuilder trimPathEnd(float... values) {
        property("trimPathEnd", values);
        return this;
    }

    public AnimationBuilder trimPathOffset(float... values) {
        property("trimPathOffset", values);
        return this;
    }

    public AnimationBuilder animationListener(AnimationListener listener) {
        pathAnimator.setAnimationListener(listener);
        return this;
    }

}