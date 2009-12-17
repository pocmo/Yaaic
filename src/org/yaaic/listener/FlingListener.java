package org.yaaic.listener;

import org.yaaic.R;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

public class FlingListener extends SimpleOnGestureListener
{
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	
	private ViewFlipper flipper;
	
	public FlingListener(ViewFlipper flipper)
	{
		this.flipper = flipper;
		
		slideLeftIn   = AnimationUtils.loadAnimation(flipper.getContext(), R.anim.slide_left_in);
		slideLeftOut  = AnimationUtils.loadAnimation(flipper.getContext(), R.anim.slide_left_out);
		slideRightIn  = AnimationUtils.loadAnimation(flipper.getContext(), R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(flipper.getContext(), R.anim.slide_right_out);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            return false;
        }
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			flipper.setInAnimation(slideLeftIn);
			flipper.setOutAnimation(slideLeftOut);
			flipper.showNext();
		}
		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			flipper.setInAnimation(slideRightIn);
			flipper.setOutAnimation(slideRightOut);
			flipper.showPrevious();
		}
		
		return false;
	}
}
