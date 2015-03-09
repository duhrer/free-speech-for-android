/**
 * Copyright 2012-2015 Upright Software <info@uprightsoftware.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Upright Software ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Upright Software OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;


/* Adapted from the Google I/O scheduling app, licensed under the Apache 2.0 license:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * */
public class FlingableTabHost extends TabHost {
    GestureDetector mGestureDetector;

    Animation mRightInAnimation;
    Animation mRightOutAnimation;
    Animation mLeftInAnimation;
    Animation mLeftOutAnimation;

    public FlingableTabHost(final Context context, AttributeSet attrs) {
        super(context, attrs);

        mRightInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_in);
        mRightOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_out);
        mLeftInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
        mLeftOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);

        final int minScaledFlingVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity() * 10; // 10 = fudge by experimentation

        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                    float velocityY) {
            	

            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            	boolean flingTabs = prefs.getBoolean(Constants.SWIPE_TAB_PREF, false);
            	if (flingTabs) {
            		int tabCount = getTabWidget().getTabCount();
            		int currentTab = getCurrentTab();
            		if (Math.abs(velocityX) > minScaledFlingVelocity &&
            				Math.abs(velocityY) < minScaledFlingVelocity) {
            			
            			final boolean right = velocityX < 0;
            			int newTab = currentTab;
            			// move one tab higher or wrap to the beginning on a "right" fling
            			if (right) { newTab = (currentTab == tabCount -1) ? 0 : currentTab +1; }
            			// move one tab lower or wrap to the end on a "left" fling
            			else { newTab = (currentTab == 0) ? tabCount -1 : currentTab +1; }
            			if (newTab != currentTab) {
            				// Somewhat hacky, depends on current implementation of TabHost:
            				// http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;
            				// f=core/java/android/widget/TabHost.java
            				View currentView = getCurrentView();
            				setCurrentTab(newTab);
            				View newView = getCurrentView();
            				
            				newView.startAnimation(right ? mRightInAnimation : mLeftInAnimation);
            				currentView.startAnimation(
            						right ? mRightOutAnimation : mLeftOutAnimation);
            			}
            		}
            	}
            	
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
