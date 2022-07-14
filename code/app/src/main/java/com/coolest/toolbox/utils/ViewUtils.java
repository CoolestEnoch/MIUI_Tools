package com.coolest.toolbox.utils;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.LayoutMode;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.bottomsheets.BottomSheet;
import com.afollestad.materialdialogs.customview.DialogCustomViewExtKt;
import com.bumptech.glide.Glide;
import com.coolest.toolbox.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ViewUtils {

	//以下内容需要在build.gradle里添加依赖:
	//	api 'com.google.android.material:material:1.1.0-alpha06'
	//	implementation 'com.afollestad.material-dialogs:core:3.2.1'
	//	implementation 'com.afollestad.material-dialogs:bottomsheets:3.3.0'


	public static int dp2px(Context context, float dpValue) {
		//获取屏幕分辨率
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dp(Context context, float pxValue) {
		//获取屏幕分辨率
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	//获取屏幕的宽度
	public static int getScreenWidth(Context ctx) {
		//从系统服务中获取窗口管理器
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		//从默认显示器中获取显示参数保存到dm中
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	//获取屏幕的高度
	public static int getScreenHeight(Context ctx) {
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	//获取屏幕像素密度
	public static float getScreenDensity(Context ctx) {
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.density;
	}

	/**
	 * 计算线性布局的实际高度
	 * @param child
	 * @return
	 */
	public static float getRealHeight(View child){
		LinearLayout linearLayout = (LinearLayout) child;
		//获取线性布局的参数
		ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
		if(params==null){
			params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		//获取布局参数里的参数规则
		int widthSpec = ViewGroup.getChildMeasureSpec(0,0,params.width);
		int heightSpec;
		if(params.height>0){
			//高度大于0说明这是明确的dp值
			//按照精确值的情况计算高度规格
			heightSpec=View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		}else{
			//MATCH_PARENT = -1, WRAP_CONTENT = -2进入此分支
			//按照不确定的情况计算高度规则
			heightSpec=View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		}
		//重新进行线性布局的宽高测量
		linearLayout.measure(widthSpec,heightSpec);
		//获取并返回线性布局测量后的高度值，用getMeasuredWidth方法获得宽度数值
		return linearLayout.getMeasuredHeight();
	}

	/**
	 * 拖更弹窗
	 * @param context
	 */
	public static void codeNotDone(Context context){
		MaterialDialog dialog = new MaterialDialog(context, new BottomSheet(LayoutMode.WRAP_CONTENT));
		dialog.title(null, "啊哦");
		DialogCustomViewExtKt.customView(dialog, R.layout.still_not_done, null, true, true, true, true);
		dialog.positiveButton(null, "确定", new Function1<MaterialDialog, Unit>() {
			@Override
			public Unit invoke(MaterialDialog materialDialog) {
				Activity a = (Activity) context;
				//判断调用此方法的是否是主界面
				PackageManager packageManager = a.getApplication().getPackageManager();
				Intent intent = packageManager.getLaunchIntentForPackage(a.getPackageName());
				ComponentName launchComponentName = intent.getComponent();
				ComponentName componentName = a.getComponentName();
				if(componentName.toString().equals(launchComponentName.toString())){
					// Log.i("min77",componentName.getClassName()+"是第一个启动的activity");
				}else {
					// Log.i("min77",componentName.getClassName()+"不是第一个启动的activity");
					a.finish();
				}

				return null;
			}
		});
		dialog.show();
	}

	/**
	 * 从屏幕底部弹出一张大卡片
	 * 在build.gradle里添加以下依赖然后再使用:
	 * implementation 'com.afollestad.material-dialogs:core:3.2.1'
	 * implementation 'com.afollestad.material-dialogs:bottomsheets:3.3.0'
	 *
	 * @param context
	 * @return
	 */
	public static MaterialDialog getBigCardFromBottom(Context context, String title, View view) {
		MaterialDialog dialog = new MaterialDialog(context, new BottomSheet(LayoutMode.WRAP_CONTENT));
		dialog.title(null, title);
		DialogCustomViewExtKt.customView(dialog, null, view, true, true, true, true);
		dialog.positiveButton(null, "确定", new Function1<MaterialDialog, Unit>() {
			@Override
			public Unit invoke(MaterialDialog materialDialog) {
				Activity a = (Activity) context;
//				a.finish();
				return null;
			}
		});
		return dialog;
	}



	/**
	 * 创建一张Material Design风格卡片，其中StringList的首项将作为标题，会被加粗
	 *
	 * @param context     这个不用解释了吧2333
	 * @param bg_color_id 背景颜色
	 * @param icon_url     左边提示图标的资源url
	 * @param strList     首项作为标题，会被加粗
	 * @return
	 */
	public static MaterialCardView createCardView(Context context, int bg_color_id, String icon_url, List<String> strList) {
		if (strList == null) {
			return null;
		}
		//创建卡片 - mainCard
		MaterialCardView mainCard = new MaterialCardView(context);
		LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		cardParams.setMargins(ViewUtils.dp2px(context, 18), ViewUtils.dp2px(context, 10), ViewUtils.dp2px(context, 18), 0);
		mainCard.setLayoutParams(cardParams);
		mainCard.setBackgroundColor(context.getResources().getColor(bg_color_id));
		mainCard.setClickable(true);
		mainCard.setFocusable(true);
		mainCard.setShapeAppearanceModel(mainCard.getShapeAppearanceModel()
				.toBuilder()
				.setBottomLeftCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.setBottomRightCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.setTopLeftCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.setTopRightCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.build()
		);
		//创建卡片内的框架LinearLayout - frameworkLinearLayout
		LinearLayout frameworkLinearLayout = new LinearLayout(context);
		LinearLayout.LayoutParams frameworkLinearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		frameworkLinearLayout.setLayoutParams(frameworkLinearLayoutParams);
		frameworkLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		//创建卡片内的图标ImageView - icon
		ImageView icon = new ImageView(context);
		LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(ViewUtils.dp2px(context, 28), ViewUtils.dp2px(context, 28));
		iconParams.gravity = Gravity.CENTER_VERTICAL;
		iconParams.setMargins(ViewUtils.dp2px(context, 26), 0, ViewUtils.dp2px(context, 20), 0);//卡片图标左右边距
		icon.setLayoutParams(iconParams);
		Glide.with(context).load(icon_url).into(icon);
		//创建在图标右边的LinearLayout - infoLinearLayout
		LinearLayout infoLinearLayout = new LinearLayout(context);
		LinearLayout.LayoutParams infoLinearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		infoLinearLayout.setLayoutParams(infoLinearLayoutParams);
		infoLinearLayout.setOrientation(LinearLayout.VERTICAL);
		infoLinearLayout.setPadding(0, ViewUtils.dp2px(context, 25), 0, ViewUtils.dp2px(context, 25));
		//往infoLinearLayout里添加文本框
		boolean firstEle = true;//实现第一个文本框加粗
		for (String str : strList) {
			TextView tv = new TextView(context);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(layoutParams);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			tv.setTextColor(context.getResources().getColor(R.color.white));
			if (firstEle) {//实现第一个文本框加粗
				firstEle = false;
				tv.setTypeface(null, Typeface.BOLD);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
				tv.setPadding(0, 0, 0, ViewUtils.dp2px(context, 5));
			} else {
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			}
			tv.setAllCaps(false);
			tv.setText(str);
			infoLinearLayout.addView(tv);
		}
		//添加视图
		frameworkLinearLayout.addView(icon);
		frameworkLinearLayout.addView(infoLinearLayout);
		mainCard.addView(frameworkLinearLayout);
		return mainCard;
	}

	/**
	 * 创建一张Material Design风格卡片，其中StringList的首项将作为标题，会被加粗
	 *
	 * @param context     这个不用解释了吧2333
	 * @param bg_color_id 背景颜色
	 * @param icon_id     左边提示图标的资源id
	 * @param strList     首项作为标题，会被加粗
	 * @return
	 */
	public static MaterialCardView createCardView(Context context, int bg_color_id, int icon_id, List<String> strList) {
		if (strList == null) {
			return null;
		}
		//创建卡片 - mainCard
		MaterialCardView mainCard = new MaterialCardView(context);
		LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		cardParams.setMargins(ViewUtils.dp2px(context, 18), ViewUtils.dp2px(context, 10), ViewUtils.dp2px(context, 18), 0);
		mainCard.setLayoutParams(cardParams);
		mainCard.setBackgroundColor(context.getResources().getColor(bg_color_id));
		mainCard.setClickable(true);
		mainCard.setFocusable(true);
		mainCard.setShapeAppearanceModel(mainCard.getShapeAppearanceModel()
				.toBuilder()
				.setBottomLeftCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.setBottomRightCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.setTopLeftCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.setTopRightCorner(CornerFamily.ROUNDED, (float) ViewUtils.dp2px(context, 10))
				.build()
		);
		//创建卡片内的框架LinearLayout - frameworkLinearLayout
		LinearLayout frameworkLinearLayout = new LinearLayout(context);
		LinearLayout.LayoutParams frameworkLinearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		frameworkLinearLayout.setLayoutParams(frameworkLinearLayoutParams);
		frameworkLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		//创建卡片内的图标ImageView - icon
		ImageView icon = new ImageView(context);
		LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(ViewUtils.dp2px(context, 28), ViewUtils.dp2px(context, 28));
		iconParams.gravity = Gravity.CENTER_VERTICAL;
		iconParams.setMargins(ViewUtils.dp2px(context, 26), 0, ViewUtils.dp2px(context, 20), 0);//卡片图标左右边距
		icon.setLayoutParams(iconParams);
		icon.setBackground(context.getResources().getDrawable(icon_id));
		//创建在图标右边的LinearLayout - infoLinearLayout
		LinearLayout infoLinearLayout = new LinearLayout(context);
		LinearLayout.LayoutParams infoLinearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		infoLinearLayout.setLayoutParams(infoLinearLayoutParams);
		infoLinearLayout.setOrientation(LinearLayout.VERTICAL);
		infoLinearLayout.setPadding(0, ViewUtils.dp2px(context, 25), 0, ViewUtils.dp2px(context, 25));
		//往infoLinearLayout里添加文本框
		boolean firstEle = true;//实现第一个文本框加粗
		for (String str : strList) {
			TextView tv = new TextView(context);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(layoutParams);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			tv.setTextColor(context.getResources().getColor(R.color.white));
			if (firstEle) {//实现第一个文本框加粗
				firstEle = false;
				tv.setTypeface(null, Typeface.BOLD);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
				tv.setPadding(0, 0, 0, ViewUtils.dp2px(context, 5));
			} else {
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			}
			tv.setAllCaps(false);
			tv.setText(str);
			infoLinearLayout.addView(tv);
		}
		//添加视图
		frameworkLinearLayout.addView(icon);
		frameworkLinearLayout.addView(infoLinearLayout);
		mainCard.addView(frameworkLinearLayout);
		return mainCard;
	}

	/**
	 * 创建Material Designed风格大按钮
	 *
	 * @param context  这个不用解释了吧2333
	 * @param icon_id  左边提示图标的资源id
	 * @param color_id 背景颜色
	 * @param str      按钮文本
	 * @return
	 */
	public static MaterialCardView createBigButton(Context context, int icon_id, int color_id, String str) {
		//创建大卡片 - cardMain
		MaterialCardView cardMain = new MaterialCardView(context);
		LinearLayout.LayoutParams cardMainLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		cardMainLayoutParams.setMargins(dp2px(context, 18), dp2px(context, 10), dp2px(context, 18), 0);
		cardMain.setLayoutParams(cardMainLayoutParams);
		cardMain.setCardBackgroundColor(context.getResources().getColor(color_id));
//		cardMain.setCardBackgroundColor(0xFF018786);
		cardMain.setFocusable(true);
		cardMain.setClickable(true);
		//创建框架LinearLayout - frameworkLinearLayout
		LinearLayout frameworkLinearLayout = new LinearLayout(context);
		LinearLayout.LayoutParams frameworkLinearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		frameworkLinearLayout.setLayoutParams(frameworkLinearLayoutParams);
		frameworkLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		//创建小的图标 - icon
		ImageView icon = new ImageView(context);
		LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp2px(context, 28), dp2px(context, 28));
		iconParams.gravity = Gravity.CENTER_VERTICAL;
		iconParams.setMargins(dp2px(context, 20), 0, dp2px(context, 16), 0);
		icon.setLayoutParams(iconParams);
//		icon.setBackground(context.getResources().getDrawable(icon_id));//弃用了，用下面这句话
		icon.setBackgroundResource(icon_id);
		//创建标题 - title
		TextView title = new TextView(context);
		LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		title.setLayoutParams(tvParams);
		title.setPadding(0, dp2px(context, 21), 0, dp2px(context, 21));
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		title.setTextColor(context.getResources().getColor(R.color.black));
//		title.setTextAppearance(R.attr.textAppearanceHeadline6);
		title.setText(str);
		//添加布局
		frameworkLinearLayout.addView(icon);
		frameworkLinearLayout.addView(title);
		cardMain.addView(frameworkLinearLayout);
		return cardMain;
	}

	/**
	 * 创建一个MD输入框
	 *
	 * @param width_ViewGroup_LayoutParams_WHAT
	 * @param height_ViewGroup_LayoutParams_WHAT
	 * @return
	 */
	public static TextInputLayout createTextInputDefault(Context context, int width_ViewGroup_LayoutParams_WHAT, int height_ViewGroup_LayoutParams_WHAT) {
		/**
		 * width和height从ViewGroup.LayoutParams里选MATCH_PARENT或WRAP_CONTENT
		 */
		TextInputLayout inputLayout = new TextInputLayout(context, null, R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
		LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(width_ViewGroup_LayoutParams_WHAT, height_ViewGroup_LayoutParams_WHAT);
		inputLayoutParams.setMargins(dp2px(context, 18), dp2px(context, 10), dp2px(context, 18), 0);
		inputLayout.setLayoutParams(inputLayoutParams);

		TextInputEditText inputEditText = new TextInputEditText(context);
		LinearLayout.LayoutParams inputEditTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		inputEditText.setLayoutParams(inputEditTextParams);

		inputLayout.addView(inputEditText);
		return inputLayout;
	}
}
