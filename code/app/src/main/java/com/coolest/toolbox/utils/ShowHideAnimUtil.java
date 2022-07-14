package com.coolest.toolbox.utils;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

public class ShowHideAnimUtil {
	/**
	 * 将view从可见变为不可见的动画，原理:动态改变其LayoutParams.height的值
	 * @param view 要展示动画的view
	 */
	public static void hideAnimator_height(final View view){
		if(view!=null){
			int viewHeight=view.getHeight();
			if(viewHeight==0){
				int width=View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
				int height=View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
				view.measure(width,height);
				viewHeight=view.getMeasuredHeight();
			}
			ValueAnimator animator=ValueAnimator.ofInt(viewHeight,0);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					ViewGroup.LayoutParams params=view.getLayoutParams();
					params.height= (int) animation.getAnimatedValue();
					view.setLayoutParams(params);
				}
			});
			animator.start();
		}
	}

	/**
	 * 动态改变view的高度动画效果，动画时长300毫秒[android属性动画默认时长]
	 * 原理:动画改变view LayoutParams.height的值
	 * @param view 要进行高度改变动画的view
	 * @param startHeight 动画前的view的高度
	 * @param endHeight 动画后的view的高度
	 */
	public static void showAnimator_height(final View view, final int startHeight, final int endHeight){
		if(view!=null&&startHeight>=0&&endHeight>=0){
			ValueAnimator animator=ValueAnimator.ofInt(startHeight,endHeight);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					ViewGroup.LayoutParams params=view.getLayoutParams();
					params.height= (int) animation.getAnimatedValue();
					view.setLayoutParams(params);
				}
			});
			animator.start();
		}
	}
}
