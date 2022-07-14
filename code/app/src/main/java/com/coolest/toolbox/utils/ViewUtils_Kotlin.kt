package com.coolest.toolbox.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.coolest.toolbox.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.robv.android.xposed.XposedBridge
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.DataInputStream
import java.net.URL
import kotlin.concurrent.thread

object ViewUtils_Kotlin {
	//以下内容需要在build.gradle里添加依赖:
	//	api 'com.google.android.material:material:1.1.0-alpha06'
	//	implementation 'com.afollestad.material-dialogs:core:3.2.1'
	//	implementation 'com.afollestad.material-dialogs:bottomsheets:3.3.0'
	fun dp2px(context: Context, dpValue: Int): Int {
		//获取屏幕分辨率
		val scale = context.resources.displayMetrics.density
		return (dpValue * scale + 0.5f).toInt()
	}

	fun px2dp(context: Context, pxValue: Float): Int {
		//获取屏幕分辨率
		val scale = context.resources.displayMetrics.density
		return (pxValue / scale + 0.5f).toInt()
	}

	//获取屏幕的宽度
	fun getScreenWidth(ctx: Context): Int {
		//从系统服务中获取窗口管理器
		val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val dm = DisplayMetrics()
		//从默认显示器中获取显示参数保存到dm中
		wm.defaultDisplay.getMetrics(dm)
		return dm.widthPixels
	}

	//获取屏幕的高度
	fun getScreenHeight(ctx: Context): Int {
		val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val dm = DisplayMetrics()
		wm.defaultDisplay.getMetrics(dm)
		return dm.heightPixels
	}

	//获取屏幕像素密度
	fun getScreenDensity(ctx: Context): Float {
		val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val dm = DisplayMetrics()
		wm.defaultDisplay.getMetrics(dm)
		return dm.density
	}

	/**
	 * 计算线性布局的实际高度
	 * @param child
	 * @return
	 */
	fun getRealHeight(child: View): Float {
		val linearLayout = child as LinearLayout
		//获取线性布局的参数
		var params = linearLayout.layoutParams
		if (params == null) {
			params = ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
			)
		}
		//获取布局参数里的参数规则
		val widthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width)
		val heightSpec: Int
		heightSpec = if (params.height > 0) {
			//高度大于0说明这是明确的dp值
			//按照精确值的情况计算高度规格
			View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
		} else {
			//MATCH_PARENT = -1, WRAP_CONTENT = -2进入此分支
			//按照不确定的情况计算高度规则
			View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
		}
		//重新进行线性布局的宽高测量
		linearLayout.measure(widthSpec, heightSpec)
		//获取并返回线性布局测量后的高度值，用getMeasuredWidth方法获得宽度数值
		return linearLayout.measuredHeight.toFloat()
	}

	/**
	 * 拖更弹窗
	 * @param context
	 */
	fun codeNotDone(context: Context) {
		val dialog = MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT))
		dialog.title(null, "啊哦")
		dialog.customView(R.layout.still_not_done, null, true, true, true, true)
		dialog.positiveButton(null, "确定") { materialDialog: MaterialDialog? ->
			val a = context as Activity
			//判断调用此方法的是否是主界面
			val packageManager = a.application.packageManager
			val intent = packageManager.getLaunchIntentForPackage(a.packageName)
			val launchComponentName = intent!!.component
			val componentName = a.componentName
			if (componentName.toString() == launchComponentName.toString()) {
				// Log.i("min77",componentName.getClassName()+"是第一个启动的activity");
			} else {
				// Log.i("min77",componentName.getClassName()+"不是第一个启动的activity");
				a.finish()
			}
			null
		}
		dialog.show()
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
	fun getBigCardFromBottom(context: Context?, title: String?, viewInCard: View?): MaterialDialog {
		val dialog = MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT))
		dialog.title(null, title)
		dialog.customView(null, viewInCard, true, true, true, true)
		dialog.positiveButton(null, "确定") { materialDialog: MaterialDialog? ->
			null
		}
		return dialog
	}

	/**
	 * 创建一张Material Design风格卡片，其中StringList的首项将作为标题，会被加粗
	 *
	 * 准备弃用
	 *
	 * @param context     这个不用解释了吧2333
	 * @param bg_color_id 背景颜色
	 * @param icon_url     左边提示图标的资源url
	 * @param strList     首项作为标题，会被加粗
	 * @return
	 */
	@Deprecated(message = "This method is no longer support and maintain.")
	fun createCardView(
		context: Context,
		bg_color_id: Int,
		icon_id_or_URL: Any?,
		strList: List<String?>?,
		card_bg_pic_URL_or_bitmap: Any?,
		onClickEvent: () -> Unit?
	): MaterialCardView? {
		if (strList == null) {
			return null
		}
		/*
		//创建卡片 - mainCard
		val mainCard = MaterialCardView(context)
		val cardParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		cardParams.setMargins(dp2px(context, 18), dp2px(context, 10), dp2px(context, 18), 0)
		mainCard.layoutParams = cardParams
		mainCard.setBackgroundColor(context.resources.getColor(bg_color_id))
		mainCard.isClickable = true
		mainCard.isFocusable = true
		mainCard.shapeAppearanceModel = mainCard.shapeAppearanceModel
			.toBuilder()
			.setBottomLeftCorner(CornerFamily.ROUNDED, dp2px(context, 10).toFloat())
			.setBottomRightCorner(CornerFamily.ROUNDED, dp2px(context, 10).toFloat())
			.setTopLeftCorner(CornerFamily.ROUNDED, dp2px(context, 10).toFloat())
			.setTopRightCorner(CornerFamily.ROUNDED, dp2px(context, 10).toFloat())
			.build()
		//创建卡片内的框架LinearLayout - frameworkLinearLayout
		val frameworkLinearLayout = LinearLayout(context)
		val frameworkLinearLayoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		frameworkLinearLayout.layoutParams = frameworkLinearLayoutParams
		frameworkLinearLayout.orientation = LinearLayout.HORIZONTAL
		//创建卡片内的图标ImageView - icon
		val icon = ImageView(context)
		val iconParams = LinearLayout.LayoutParams(dp2px(context, 28), dp2px(context, 28))
		iconParams.gravity = Gravity.CENTER_VERTICAL
		iconParams.setMargins(dp2px(context, 26), 0, 0, 0) //卡片图标左右边距
		icon.layoutParams = iconParams
		when (icon_url_or_id) {
			is Int -> icon.setImageBitmap(
				BitmapFactory.decodeResource(
					context.resources,
					icon_url_or_id
				)
			)
			is String -> try {
				Glide.with(context).load(icon_url_or_id).into(icon)
			} catch (e: java.lang.Exception) {
				e.printStackTrace()
				XposedBridge.log(e)
			}
		}
		//创建在图标右边的LinearLayout - infoLinearLayout
		val infoLinearLayout = LinearLayout(context)
		val infoLinearLayoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		infoLinearLayout.layoutParams = infoLinearLayoutParams
		infoLinearLayout.orientation = LinearLayout.VERTICAL
		infoLinearLayout.setPadding(dp2px(context, 20), dp2px(context, 25), 0, dp2px(context, 25))
		//往infoLinearLayout里添加文本框
		var firstEle = true //实现第一个文本框加粗
		for (str in strList) {
			val tv = TextView(context)
			val layoutParams = LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
			)
			tv.layoutParams = layoutParams
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
			tv.setTextColor(context.resources.getColor(R.color.white))
			if (firstEle) { //实现第一个文本框加粗
				firstEle = false
				tv.setTypeface(null, Typeface.BOLD)
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
				tv.setPadding(0, 0, 0, dp2px(context, 5))
			} else {
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
			}
			tv.isAllCaps = false
			tv.text = str
			infoLinearLayout.addView(tv)
		}
		//添加视图
		when (icon_url_or_id) {
			null -> Unit
			else -> frameworkLinearLayout.addView(icon)
		}
		frameworkLinearLayout.addView(infoLinearLayout)
		mainCard.addView(frameworkLinearLayout)
		//设置点击事件
		mainCard.setOnClickListener {
			onClickEvent()
		}
		return mainCard*/
		//创建大卡片 - cardMain
		val cardMain = MaterialCardView(context)
		val cardMainLayoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		cardMainLayoutParams.setMargins(
			dp2px(context, 18),
			dp2px(context, 10),
			dp2px(context, 18),
			0
		)
		cardMain.layoutParams = cardMainLayoutParams
		cardMain.setCardBackgroundColor(
			context.resources.getColor(
				bg_color_id,
				context.resources.newTheme()
			)
		)
		//		cardMain.setCardBackgroundColor(0xFF018786);
		cardMain.isFocusable = true
		cardMain.isClickable = true
		//创建框架LinearLayout - frameworkLinearLayout
		val frameworkLinearLayout = LinearLayout(context)
		val frameworkLinearLayoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		frameworkLinearLayout.layoutParams = frameworkLinearLayoutParams
		frameworkLinearLayout.orientation = LinearLayout.HORIZONTAL
		//创建小的图标 - icon
		val icon = ImageView(context)
		val iconParams = LinearLayout.LayoutParams(dp2px(context, 28), dp2px(context, 28))
		iconParams.gravity = Gravity.CENTER_VERTICAL
		iconParams.setMargins(dp2px(context, 20), 0, dp2px(context, 16), 0)
		icon.layoutParams = iconParams
		//		icon.setBackground(context.getResources().getDrawable(icon_id));//弃用了，用下面这句话
		when (icon_id_or_URL) {
			null -> Unit
			else -> Glide.with(context).load(icon_id_or_URL).into(icon)
		}

		val strLinearLayout = LinearLayout(context)
		val strLinearLayoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		strLinearLayout.layoutParams = strLinearLayoutParams
		strLinearLayout.orientation = LinearLayout.VERTICAL
		strLinearLayout.setPadding(0, dp2px(context, 25), 0, dp2px(context, 25))
		var firstEle = true //实现第一个文本框加粗
		strList.forEach { str ->
			val tv = TextView(context)
			val layoutParams = LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
			)
			tv.layoutParams = layoutParams
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
			tv.setTextColor(context.resources.getColor(R.color.white, context.theme))
			if (firstEle) { //实现第一个文本框加粗
				firstEle = false
				tv.setTypeface(null, Typeface.BOLD)
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
				tv.setPadding(0, 0, 0, dp2px(context, 5))
			} else {
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
			}
			tv.isAllCaps = false
			tv.text = str
			strLinearLayout.addView(tv)
		}

		//添加布局
		when (icon_id_or_URL) {
			null -> Unit
			else -> frameworkLinearLayout.addView(icon)
		}

		frameworkLinearLayout.addView(strLinearLayout)

		frameworkLinearLayout.layoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			getRealHeight(frameworkLinearLayout).toInt()
		)


		/*when (card_bg_pic_URL_or_bitmap) {
			null -> Unit
			else -> {
				thread {
					var bitmap:Bitmap? = null
					when (card_bg_pic_URL_or_bitmap) {
						is String -> {
							val dis = DataInputStream(URL(card_bg_pic_URL_or_bitmap).openStream())
							bitmap = BitmapFactory.decodeStream(dis)
						}
						is Bitmap -> bitmap = card_bg_pic_URL_or_bitmap
					}
					runOnMainThread {
						try {
							Glide.with(context)
								.asBitmap()
								.load(bitmap)
								.transform(BlurTransformation(200))
								.into(object : SimpleTarget<Bitmap?>() {
									override fun onResourceReady(
										resource: Bitmap,
										transition: Transition<in Bitmap?>?
									) {
										val drawable: Drawable = BitmapDrawable(resource)
										frameworkLinearLayout!!.background = drawable
									}
								})
						} catch (e: Exception) {
							e.printStackTrace()
				XposedBridge.log(e)
						}
					}
				}
			}
		}*/

		when (card_bg_pic_URL_or_bitmap) {
			null -> Unit
			else -> {
				when (card_bg_pic_URL_or_bitmap) {
					is String -> {
						thread {
							val dis = DataInputStream(URL(card_bg_pic_URL_or_bitmap).openStream())
							val bitmap = BitmapFactory.decodeStream(dis)
							runOnMainThread {
								try {
									Glide.with(context)
										.asBitmap()
										.load(bitmap)
										.transform(BlurTransformation(200))
										.into(object : SimpleTarget<Bitmap?>() {
											override fun onResourceReady(
												resource: Bitmap,
												transition: Transition<in Bitmap?>?
											) {
												val drawable: Drawable = BitmapDrawable(resource)
												frameworkLinearLayout.background = drawable
											}
										})
								} catch (e: Exception) {
									e.printStackTrace()
									XposedBridge.log(e)
								}
							}
						}
					}
					is Bitmap -> try {
						Glide.with(context)
							.asBitmap()
							.load(card_bg_pic_URL_or_bitmap)
							.transform(BlurTransformation(200))
							.into(object : SimpleTarget<Bitmap?>() {
								override fun onResourceReady(
									resource: Bitmap,
									transition: Transition<in Bitmap?>?
								) {
									val drawable: Drawable = BitmapDrawable(resource)
									frameworkLinearLayout!!.background = drawable
								}
							})
					} catch (e: Exception) {
						e.printStackTrace()
						XposedBridge.log(e)
					}
				}

			}
		}

		cardMain.addView(frameworkLinearLayout)
		cardMain.setOnClickListener {
			onClickEvent()
		}
		return cardMain
	}

	/**
	 * 创建Material Designed风格大按钮
	 *
	 * @param context  这个不用解释了吧2333
	 * @param icon_id  左边提示图标的资源id
	 * @param color_id 背景颜色
	 * @param str      按钮文本
	 * @param card_bg_pic_URL 卡片背景图链接
	 * @param onClickEvent 单击事件
	 * @return
	 */
	fun createBigButton(
		context: Context,
		icon_id_or_URL: Any?,
		bg_color_id: Int,
		strList: List<String?>?,
		card_bg_pic_URL_or_bitmap: Any?,
		text_color_id_or_rgb_value: Int,
		onClickEvent: () -> Unit?
	): MaterialCardView {
		//创建大卡片 - cardMain
		val cardMain = MaterialCardView(context)
		val cardMainLayoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		cardMainLayoutParams.setMargins(
			dp2px(context, 18),
			dp2px(context, 10),
			dp2px(context, 18),
			0
		)
		cardMain.layoutParams = cardMainLayoutParams
		cardMain.setCardBackgroundColor(
			context.resources.getColor(
				bg_color_id,
				context.resources.newTheme()
			)
		)
		//		cardMain.setCardBackgroundColor(0xFF018786);
		cardMain.isFocusable = true
		cardMain.isClickable = true
		//创建框架LinearLayout - frameworkLinearLayout
		val frameworkLinearLayout = LinearLayout(context)
		val frameworkLinearLayoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		frameworkLinearLayout.layoutParams = frameworkLinearLayoutParams
		frameworkLinearLayout.orientation = LinearLayout.HORIZONTAL
		//创建小的图标 - icon
		//		icon.setBackground(context.getResources().getDrawable(icon_id));//弃用了，用下面这句话
		when (icon_id_or_URL) {
			null -> Unit
			else -> {
				val icon = ImageView(context)
				val iconParams = LinearLayout.LayoutParams(dp2px(context, 28), dp2px(context, 28))
				iconParams.gravity = Gravity.CENTER_VERTICAL
				iconParams.setMargins(dp2px(context, 20), 0, 0, 0)
				icon.layoutParams = iconParams
				Glide.with(context).load(icon_id_or_URL).into(icon)
				frameworkLinearLayout.addView(icon)
			}
		}

		when (strList) {
			null -> {
			}
			else -> {
				//创建标题 - title
				frameworkLinearLayout.addView(LinearLayout(context).apply {
					layoutParams = LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT
					)
					orientation = LinearLayout.VERTICAL
					setPadding(
						dp2px(context, 16),
						dp2px(context, 25),
						0,
						dp2px(context, 25)
					)
					var firstEle = true //实现第一个文本框加粗
					strList.forEach { str ->
						addView(TextView(context).apply {
							layoutParams = LinearLayout.LayoutParams(
								ViewGroup.LayoutParams.WRAP_CONTENT,
								ViewGroup.LayoutParams.WRAP_CONTENT
							)
							setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
							var color = try {
								context.resources.getColor(
									text_color_id_or_rgb_value,
									context.theme
								)
							} catch (e: java.lang.Exception) {
								text_color_id_or_rgb_value
							}
							setTextColor(color)
							if (firstEle) { //实现第一个文本框加粗
								firstEle = false
								setTypeface(null, Typeface.BOLD)
								setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
								setPadding(0, 0, 0, dp2px(context, 5))
							} else {
								setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
							}
							isAllCaps = false
							text = str
						})
					}
				})
			}
		}


		frameworkLinearLayout.layoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			getRealHeight(frameworkLinearLayout).toInt()
		)


		when (card_bg_pic_URL_or_bitmap) {
			null -> Unit
			else -> {
				when (card_bg_pic_URL_or_bitmap) {
					is String -> {
						thread {
							val dis = DataInputStream(URL(card_bg_pic_URL_or_bitmap).openStream())
							val bitmap = BitmapFactory.decodeStream(dis)
							runOnMainThread {
								try {
									Glide.with(context)
										.asBitmap()
										.load(bitmap)
										.transform(BlurTransformation(200))
										.into(object : SimpleTarget<Bitmap?>() {
											override fun onResourceReady(
												resource: Bitmap,
												transition: Transition<in Bitmap?>?
											) {
												val drawable: Drawable = BitmapDrawable(resource)
												frameworkLinearLayout.background = drawable
											}
										})
								} catch (e: Exception) {
									e.printStackTrace()
									XposedBridge.log(e)
								}
							}
						}
					}
					is Bitmap -> try {
						Glide.with(context)
							.asBitmap()
							.load(card_bg_pic_URL_or_bitmap)
							.transform(BlurTransformation(200))
							.into(object : SimpleTarget<Bitmap?>() {
								override fun onResourceReady(
									resource: Bitmap,
									transition: Transition<in Bitmap?>?
								) {
									val drawable: Drawable = BitmapDrawable(resource)
									frameworkLinearLayout.background = drawable
								}
							})
					} catch (e: Exception) {
						e.printStackTrace()
						XposedBridge.log(e)
					}
				}

			}
		}

		cardMain.addView(frameworkLinearLayout)
		cardMain.setOnClickListener {
			onClickEvent()
		}
		return cardMain
	}

	/**
	 * 创建一个MD输入框
	 *
	 * @param width_ViewGroup_LayoutParams_WHAT
	 * @param height_ViewGroup_LayoutParams_WHAT
	 * @return
	 */
	fun createTextInputDefault(
		context: Context,
		width_ViewGroup_LayoutParams_WHAT: Int,
		height_ViewGroup_LayoutParams_WHAT: Int
	): TextInputLayout {
		/**
		 * width和height从ViewGroup.LayoutParams里选MATCH_PARENT或WRAP_CONTENT
		 */
		val inputLayout = TextInputLayout(
			context,
			null,
			R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox
		)
		val inputLayoutParams = LinearLayout.LayoutParams(
			width_ViewGroup_LayoutParams_WHAT,
			height_ViewGroup_LayoutParams_WHAT
		)
		inputLayoutParams.setMargins(
			dp2px(context, 18),
			dp2px(context, 10),
			dp2px(context, 18),
			0
		)
		inputLayout.layoutParams = inputLayoutParams
		val inputEditText = TextInputEditText(context)
		val inputEditTextParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
		inputEditText.layoutParams = inputEditTextParams
		inputLayout.addView(inputEditText)
		return inputLayout
	}
}