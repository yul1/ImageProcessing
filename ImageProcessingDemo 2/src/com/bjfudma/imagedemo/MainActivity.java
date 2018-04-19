package com.bjfudma.imagedemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bjfudma.imagedemo.util.EditImage;
import com.bjfudma.imagedemo.util.ImageFrameAdder;
import com.bjfudma.imagedemo.util.ImageFilterAdder;
import com.bjfudma.imagedemo.util.ReverseAnimation;
import com.bjfudma.imagedemo.view.CropImageView;
import com.bjfudma.imagedemo.view.ToneView;
import com.bjfudma.imagedemo.view.menu.MenuView;
import com.bjfudma.imagedemo.view.menu.OnMenuClickListener;
import com.bjfudma.imagedemo.view.menu.SecondaryListMenuView;
import com.bjfudma.imagedemo.view.menu.ToneMenuView;
import com.bjfudma.imagedemo.R;


public class MainActivity extends Activity implements OnSeekBarChangeListener {
	public boolean mWaitingToPick; 
	public boolean mSaving; 
	private Handler mHandler = null;
	private ProgressDialog mProgress;
	private Bitmap mBitmap;
	private Bitmap mTmpBmp;
	
	private CropImageView mImageView; 
	private EditImage mEditImage;    
	private ImageFrameAdder mImageFrame;
	private ImageFilterAdder mImageFilter;
	
	private MenuView mMenuView; 
	//【1.编辑菜单】设置一级菜单下item的图片显示
	private final int[] EDIT_IMAGES = new int[] { 
			R.drawable.ic_menu_crop,//裁剪按钮
			R.drawable.ic_menu_rotate_left, //旋转按钮
			R.drawable.ic_menu_mapmode,//缩放按钮
			R.drawable.btn_rotate_horizontalrotate, //反转按钮
			R.drawable.btn_mainmenu_frame_normal,//边框按钮
			};
	//设置一级菜单下item的文字显示
	private final int[] EDIT_TEXTS = new int[] { 
			R.string.crop,
			R.string.rotate,
			R.string.resize, 
			R.string.reverse_transform,
			R.string.frame,
			};

	//二级菜单
	private SecondaryListMenuView mSecondaryListMenu;
	//旋转图片设置
	private final int[] ROTATE_IMGRES = new int[] {
			R.drawable.ic_menu_rotate_left, //左旋转按钮
			R.drawable.ic_menu_rotate_right //右旋转按钮
			};
	//旋转文字设置
	private final int[] ROTATE_TEXTS = new int[] {
			R.string.rotate_left,
			R.string.rotate_right 
			};
	//缩放文字设置
	/*private final int[] RESIZE_TEXTS = new int[] {
			R.string.resize_two_to_one ,
			R.string.resize_four_to_one ,
			R.string.resize_one_to_two,
			R.string.resize_one_to_four
			};*/
	//缩放图片设置
	private final int[] RESIZE_IMGRES = new int[] {
			R.drawable.resize_two_to_one, 
			R.drawable.resize_four_to_one,
			R.drawable.resize_one_to_two,
			R.drawable.resize_one_to_four
			};
    //反转文字设置
	/*private final int[] FANZHUAN_TEXTS = new int[]{
			R.string.fanzhuan_left_right,
			R.string.fanzhuan_top_bottom 
	};*/
	//反转图片设置
	private final int[] FANZHUAN_IMAGES = new int[]{
			R.drawable.btn_rotate_horizontalrotate,
			R.drawable.btn_rotate_verticalrotate 
			};
    //边框图片设置
	private final int[] FRAME_ADD_IMAGES = new int[] {
			R.drawable.frame_around1, 
			R.drawable.frame_around2,
			//R.drawable.frame_small1 
			};
	
	//【2.调色菜单】
	private ToneMenuView mToneMenu;
	private ToneView mToneView;
	
	//【3.滤镜菜单】设置一级菜单下item的图片显示！！
	private final int[] FILTER_IMAGES = new int[] { 
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			R.drawable.ic_filter,//
			};
	//设置一级菜单下item的文字显示
	private final int[] FILTER_TEXTS = new int[] { 
			R.string.soften,
			R.string.blur,
			R.string.sculpture, 
			R.string.sharpen,
			R.string.rememberpast,
			R.string.negative,
			R.string.sketch,
			R.string.blackandwhite,
			R.string.feather,
			R.string.comic,
			R.string.lineGrey,
			};
	
	/** 调色 */
	private final int FLAG_TONE = 0x1;
	/** 添加边框 */
	private final int FLAG_FRAME_ADD = FLAG_TONE + 6;
	/** 编辑 */
	private final int FLAG_EDIT = FLAG_TONE + 2;
	/** 旋转 */
	private final int FLAG_EDIT_ROTATE = FLAG_TONE + 4;
	/** 缩放 */
	private final int FLAG_EDIT_RESIZE = FLAG_TONE + 5;
	/** 反转 */
	private final int FLAG_EDIT_REVERSE = FLAG_TONE + 8;
	/** 添加滤镜 */
	private final int FLAG_FILTER_ADD = FLAG_TONE + 9;
	
	
	private View mSaveAll;//保存全部视图
	private View mSaveStep;//记录各个步骤的视图

	private final int STATE_CROP = 0x1;
	private final int STATE_NONE = STATE_CROP << 2;
	private final int STATE_TONE = STATE_CROP << 3;
	private final int STATE_REVERSE = STATE_CROP << 4;
	private int mState;
	//反转动画
	private ReverseAnimation mReverseAnim;
	private int mImageViewWidth;
	private int mImageViewHeight;
	private ProgressDialog mProgressDialog;
	private TextView mShowHandleName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				closeProgress();
				reset();
			}
		};
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.image_main);
		
		mSaveAll = findViewById(R.id.save_all);
		mSaveStep = findViewById(R.id.save_step);
		mShowHandleName = (TextView) findViewById(R.id.handle_name);

		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		Log.d("MainActivity", "path=" + path);
		if (null == path) {
			Toast.makeText(this, R.string.load_failure, Toast.LENGTH_SHORT)
					.show();
			finish();
		}
		mBitmap = BitmapFactory.decodeFile(path);
		mTmpBmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
		mImageView = (CropImageView) findViewById(R.id.crop_image);
		mImageView.setImageBitmap(mBitmap);
	    mImageView.setImageBitmapResetBase(mBitmap, true);//递归调用将图片的具体视图进行重置
		mEditImage = new EditImage(this, mImageView, mBitmap);//编辑图片
		
		mImageFilter = new ImageFilterAdder(this, mImageView, mBitmap);
		
		mImageFrame = new ImageFrameAdder(this, mImageView, mBitmap);

		mImageView.setEditImage(mEditImage);//当编辑渲染操作完成时，还能继续进行其他的功能渲染通过这个方法
	}

	//-----------------菜单事件----------------
	public void onClick(View v) {
		int flag = -1;
		switch (v.getId()) {
		case R.id.save:
			String path = saveBitmap(mBitmap);//invoke saveBitmap();
			Log.v("savePath", path);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			Intent data = new Intent();
			data.putExtra("path", path);
			setResult(RESULT_OK, data);
			finish();
			return;
		case R.id.cancel://取消
			setResult(RESULT_CANCELED);
			finish();
			return;
		case R.id.save_step://步骤保存
			if (mState == STATE_CROP) {
				mTmpBmp = mEditImage.cropAndSave(mTmpBmp);
			} else if (mState == STATE_TONE) {
				mTmpBmp = mToneView.getBitmap();
			}else if (mState == STATE_REVERSE) {
				mReverseAnim.cancel();
				mReverseAnim = null;
			}
			mBitmap = mTmpBmp;
			showSaveAll();
			reset();
			mEditImage.mSaving = true;
			mImageViewWidth = mImageView.getWidth();
			mImageViewHeight = mImageView.getHeight();
			return;
			
		case R.id.cancel_step://步骤取消
			if (mState == STATE_CROP) {
				mEditImage.cropCancel();
			}  else if (mState == STATE_REVERSE) {
				mReverseAnim.cancel();
			}
			showSaveAll();
			resetToOriginal();
			return;
		case R.id.edit://编辑
			flag = FLAG_EDIT;
			break;
		case R.id.tone://色调
			initTone();
			showSaveStep();
			return;
		case R.id.filter://滤镜！！
			flag = FLAG_FILTER_ADD;
			break;	
		}
		initMenu(flag);
	}
	
	// 调色功能初始化
	private void initTone() {
		if (null == mToneMenu) {
			mToneMenu = new ToneMenuView(this);
		}
		mToneMenu.show();
		mState = STATE_TONE;
		mToneView = mToneMenu.getToneView();
		mShowHandleName.setText(R.string.tone);
		mToneMenu.setHueBarListener(this);
		mToneMenu.setLumBarListener(this);
		mToneMenu.setSaturationBarListener(this);
	}

	//【编辑】【滤镜】按钮的一级菜单初始化
	private void initMenu(int flag) {
		if (null == mMenuView) {
			mMenuView = new MenuView(this);
			mMenuView.setBackgroundResource(R.drawable.popup);
			mMenuView.setTextSize(16);
			switch (flag) {
			case FLAG_EDIT:
				mMenuView.setImageRes(EDIT_IMAGES);
				mMenuView.setText(EDIT_TEXTS);
				mMenuView.setOnMenuClickListener(editListener);
				break;
			case FLAG_FILTER_ADD://！！
				mMenuView.setImageRes(FILTER_IMAGES);
				mMenuView.setText(FILTER_TEXTS);
				mMenuView.setOnMenuClickListener(filterListener);
				break;
			}
		}
		mMenuView.show();
	}
	
	//对一级菜单的按钮添加监听事件
	private OnMenuClickListener editListener = new OnMenuClickListener() {
		public void onMenuItemClick(AdapterView<?> parent, View view,
				int position) {
			int[] location = new int[2];
			view.getLocationInWindow(location);//记录点击的位置数
			int left = location[0];//二级菜单的位置
			int flag = -1;
			switch (position) {//返回所点击的数
			case 0: // 裁剪
				mMenuView.hide();//隐藏一级菜单
				crop();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 1: // 旋转
				flag = FLAG_EDIT_ROTATE;
				break;
			case 2:// 缩放
				flag = FLAG_EDIT_RESIZE;
				break;
			case 3: // 反转
				flag = FLAG_EDIT_REVERSE;
				break;
			case 4://边框
				flag = FLAG_FRAME_ADD;
				break;
			case 5://滤镜
				flag = FLAG_FILTER_ADD;
				break;
			}
			initSecondaryMenu(flag, left);
		}
		
		@Override
		public void hideMenu() {
			dimissMenu();
		}
	};

	//菜单消失处理
	private void dimissMenu() {
		mMenuView.dismiss();
		mMenuView = null;
	}

	//初始化二级菜单
	private void initSecondaryMenu(int flag, int left) {
		mSecondaryListMenu = new SecondaryListMenuView(this);
		mSecondaryListMenu.setBackgroundResource(R.drawable.popup_bottom_tip);
		mSecondaryListMenu.setTextSize(16);
		mSecondaryListMenu.setWidth(240);
		//mSecondaryListMenu.setHeight(240);
		mSecondaryListMenu.setMargin(left);
		switch (flag) {
		case FLAG_EDIT_ROTATE: // 旋转
			mSecondaryListMenu.setImageRes(ROTATE_IMGRES);
			mSecondaryListMenu.setText(ROTATE_TEXTS);
			mSecondaryListMenu.setOnMenuClickListener(rotateListener());
			break;
		case FLAG_EDIT_RESIZE: // 缩放
			mSecondaryListMenu.setImageRes(RESIZE_IMGRES);
			//mSecondaryListMenu.setText(RESIZE_TEXTS);
			mSecondaryListMenu.setOnMenuClickListener(resizeListener());
			break;
		case FLAG_EDIT_REVERSE: // 反转
			mSecondaryListMenu.setImageRes(FANZHUAN_IMAGES);
			//mSecondaryListMenu.setText(FANZHUAN_TEXTS);
			mSecondaryListMenu.setOnMenuClickListener(reverseListener());
			break;
		case FLAG_FRAME_ADD: // 添加边框
			mSecondaryListMenu.setImageRes(FRAME_ADD_IMAGES);
			mSecondaryListMenu.setOnMenuClickListener(addFrameListener());
		    break;
		}
		mSecondaryListMenu.show();
	}

	//旋转事件监听
	private OnMenuClickListener rotateListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				switch (position) {
				case 0: // 左旋转
					rotate(-90);
					break;
				case 1: // 右旋转
					rotate(90);
					break;
				}
				// 一级菜单隐藏
				mMenuView.hide();
				showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	
	//图片缩放事件监听
	private OnMenuClickListener resizeListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				float scale = 1.0F;
				switch (position) {
				case 0: // 1:2
					scale /= 2;
					break;
				case 1: // 1:3
					scale /= 4;
					break;
				case 2: // 1:4
					scale *= 2;
					break;
				case 3: // 1:4
					scale *= 4;
					break;
				}

				resize(scale);
				mMenuView.hide();
				showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	
	//图片反转事件监听
	private OnMenuClickListener reverseListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				int flag = -1;
				switch (position) {
				case 0: // 水平反转
					flag = 0;
					break;
				case 1: // 垂直反转
					flag = 1;
					break;
				}
				reverse(flag);
				// 一级菜单隐藏
				mMenuView.hide();
				showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	//添加边框事件监听
	private OnMenuClickListener addFrameListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				int flag = -1;
				int res = 0;
				switch (position) {
				case 0: // 边框1
					flag = ImageFrameAdder.FRAME_SMALL;
					res = 0;
					break;
				case 1: // 边框2
					flag = ImageFrameAdder.FRAME_SMALL;
					res = 1;
					break;
				//case 2: // 边框3
					//flag = ImageFrameAdder.FRAME_BIG;
					//res = R.drawable.frame_big1;
					//break;
				case 3: // 边框4
					flag = ImageFrameAdder.FRAME_BIG;
					res = 2;
					break;
				}

				addFrame(flag, res);
				// mImageView.center(true, true);

				// 一级菜单隐藏
				mMenuView.hide();
				showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	
	//隐藏二级菜单
	private void dismissSecondaryMenu() {
		mSecondaryListMenu.dismiss();
		mSecondaryListMenu = null;
	}

	//步骤操作方法（保存与取消）
	private void showSaveStep() {
		mSaveStep.setVisibility(View.VISIBLE);
		mSaveAll.setVisibility(View.GONE);
	}

	private void showSaveAll() {
		mSaveStep.setVisibility(View.GONE);
		mSaveAll.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mMenuView != null && mMenuView.isShow() || null != mToneMenu
					&& mToneMenu.isShow()) {
				mMenuView.hide();
				mToneMenu.hide();
				mToneMenu = null;
			} else {
				if (mSaveAll.getVisibility() == View.GONE) {
					showSaveAll();
				} else {
					finish();
				}
			}
			break;
		case KeyEvent.KEYCODE_MENU:
			break;

		}
		return super.onKeyDown(keyCode, event);
	}

	// --------------功能---------------
	/**
	 * 进行操作前的准备
	 * 
	 * @param state
	 *            当前准备进入的操作状态
	 * @param imageViewState
	 *            ImageView要进入的状态
	 * @param hideHighlight
	 *            是否隐藏裁剪框
	 */
	private void prepare(int state, int imageViewState, boolean hideHighlight) {
		resetToOriginal();
		mEditImage.mSaving = false;
		if (null != mReverseAnim) {
			mReverseAnim.cancel();
			mReverseAnim = null;
		}

		if (hideHighlight) {
			mImageView.hideHighlightView();
		}
		mState = state;
		mImageView.setState(imageViewState);
		mImageView.invalidate();
	}

	//裁剪
	private void crop() {
		// 进入裁剪状态
		prepare(STATE_CROP, CropImageView.STATE_HIGHLIGHT, false);
		mShowHandleName.setText(R.string.crop);
		mEditImage.crop(mTmpBmp);
		reset();
	}

	//旋转
	private void rotate(float degree) {
		// 未进入特殊状态
		mImageViewWidth = mImageView.getWidth();
		mImageViewHeight = mImageView.getHeight();
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.rotate);
		Bitmap bm = mEditImage.rotate(mTmpBmp, degree);
		mTmpBmp = bm;
		reset();
	}

	//反转
	private void reverse(int flag) {
		// 未进入特殊状态
		prepare(STATE_REVERSE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.reverse_transform);
		int type = 0;
		switch (flag) {
		case 0:
			type = ReverseAnimation.HORIZONTAL;
			break;
		case 1:
			type = ReverseAnimation.VERTICAL;
			break;
		}

		mReverseAnim = new ReverseAnimation(0F, 180F,
				mImageViewWidth == 0 ? mImageView.getWidth() / 2
						: mImageViewWidth / 2,
				mImageViewHeight == 0 ? mImageView.getHeight() / 2
						: mImageViewHeight / 2, 0, true);
		mReverseAnim.setReverseType(type);
		mReverseAnim.setDuration(1000);
		mReverseAnim.setFillEnabled(true);
		mReverseAnim.setFillAfter(true);
		mImageView.startAnimation(mReverseAnim);
		Bitmap bm = mEditImage.reverse(mTmpBmp, flag);
		mTmpBmp = bm;
		//reset();
	}

    //缩放
	private void resize(float scale) {
		// 未进入特殊状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.resize);
		Bitmap bmp = mEditImage.resize(mTmpBmp, scale);
		mTmpBmp = bmp;
		reset();
	}

	//添加边框
	private void addFrame(int flag, int res) {
		// 未进入特殊状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.frame);
		mTmpBmp = mImageFrame.addFrame(flag, mBitmap, res);
		reset();
	}
	
	//重新设置一下图片
	private void reset() {
		mImageView.setImageBitmap(mTmpBmp);
		mImageView.invalidate();
	}

	private void resetToOriginal() {
		mTmpBmp = mBitmap;
		mImageView.setImageBitmap(mBitmap);
		// 已经保存图片
		mEditImage.mSaving = true;
		// 清空裁剪操作
		mImageView.mHighlightViews.clear();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int flag = -1;
		switch ((Integer) seekBar.getTag()) {
		
		case 1: // 饱和度
			flag = 0;
			mToneView.setSaturation(progress);
			break;
		case 2: // 亮度
			flag = 1;
			mToneView.setLum(progress);
			break;
		case 3: // 色调
			flag = 2;
			mToneView.setHue(progress);
			break;
		
		}

		Bitmap bm = mToneView.handleImage(mTmpBmp, flag);
		mImageView.setImageBitmapResetBase(bm, true);
		mImageView.center(true, true);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * 显示进度条
	 */
	private void showProgress() {
		Context context = this;
		mProgress = ProgressDialog.show(context, null, context.getResources()
				.getString(R.string.handling));
		mProgress.show();
		Log.d("may", "show Progress");
	}

	/**
	 * 关闭进度条
	 */
	private void closeProgress() {
		if (null != mProgress) {
			mProgress.dismiss();
			mProgress = null;
		}
	}
	/**
	 * 保存图片到本地同时 然后进行保存图片的操作 图片进行等待画面
	 */
	private String saveBitmap(Bitmap bm) {
		mProgressDialog = ProgressDialog.show(this, null, getResources()
				.getString(R.string.save_bitmap));
		mProgressDialog.show();
		return mEditImage.saveToLocal(bm);//saveToLocal()这个方法是输入年月日的基本操作
	}

	//【滤镜菜单】对一级菜单的各个按钮添加监听事件
	private OnMenuClickListener filterListener = new OnMenuClickListener() {
		public void onMenuItemClick(AdapterView<?> parent, View view,int position) {
			int[] location = new int[3];
			view.getLocationInWindow(location);//记录点击的位置数
			//int left = location[0];//二级菜单的位置
			int flag = -1;
			//Bitmap tmpBitmap;
			switch (position) {//返回所点击的数
			/*case 0: // 柔化
				mMenuView.hide();//隐藏一级菜单
				soften();//进入剪切状态
				showSaveStep();//步骤保存
				return;*/
			case 1: // 模糊
				mMenuView.hide();//隐藏一级菜单
				blur();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 2:// 浮雕
				mMenuView.hide();//隐藏一级菜单
				sculpture();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 3: // 锐化
				mMenuView.hide();//隐藏一级菜单
				sharpen();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 4://怀旧
				mMenuView.hide();//隐藏一级菜单
				rememberpast();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 5://负片
				mMenuView.hide();//隐藏一级菜单
				negative();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			/*case 6://素描
				mMenuView.hide();//隐藏一级菜单
				sketch();//进入剪切状态
				showSaveStep();//步骤保存
				return;*/
			case 7://负片
				mMenuView.hide();//隐藏一级菜单
				blackandwhite();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 8://负片
				mMenuView.hide();//隐藏一级菜单
				feather();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 9://负片
				mMenuView.hide();//隐藏一级菜单
				comic();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 10://负片
				mMenuView.hide();//隐藏一级菜单
				lineGrey();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			}
			//initSecondaryMenu(flag, left);
		}
		
		@Override
		public void hideMenu() {
			dimissMenu();
		}
	};
	

	//柔化
	/*private void soften() {
		// 进入柔化状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.soften);
		mImageFilter.soften(mTmpBmp);
		reset();
	}*/

	//模糊
	private void blur() {
		// 进入模糊状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.blur);
		//mImageFilter.blur(mTmpBmp);
		Bitmap bmp = mImageFilter.blur(mTmpBmp);
		mTmpBmp = bmp;
		reset();
	}

		//浮雕
	private void sculpture() {
		// 进入浮雕状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.sculpture);
		//mImageFilter.sculpture(mTmpBmp);
		Bitmap bmp = mImageFilter.sculpture(mTmpBmp);
		mTmpBmp = bmp;
		reset();
	}

		//锐化
	private void sharpen() {
		// 进入锐化状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.sharpen);
		//mImageFilter.sharpen(mTmpBmp);
		Bitmap bmp = mImageFilter.sharpen(mTmpBmp);
		mTmpBmp = bmp;
		reset();
	}

		//怀旧
	private void rememberpast() {
		// 进入怀旧状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.rememberpast);
		//mImageFilter.rememberpast(mTmpBmp);
		Bitmap bmp = mImageFilter.rememberpast(mTmpBmp);
		mTmpBmp = bmp;
		reset();
	}

		//负片
	private void negative() {
		// 进入负片状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.negative);
		//mImageFilter.negative(mTmpBmp);
		Bitmap bmp = mImageFilter.negative(mTmpBmp);
		mTmpBmp = bmp;
		reset();
	}

		//素描
	/*private void sketch() {
		// 进入素描状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.sketch);
		mImageFilter.sketch(mTmpBmp);
		reset();
	}*/
	
	//黑白
		private void blackandwhite() {
			// 进入负片状态
			prepare(STATE_NONE, CropImageView.STATE_NONE, true);
			mShowHandleName.setText(R.string.blackandwhite);
			//mImageFilter.negative(mTmpBmp);
			Bitmap bmp = mImageFilter.blackandwhite(mTmpBmp);
			mTmpBmp = bmp;
			reset();
		}
		
	//羽化
	private void feather() {
		// 进入负片状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.feather);
		//mImageFilter.negative(mTmpBmp);
		Bitmap bmp = mImageFilter.feather(mTmpBmp);
		mTmpBmp = bmp;
		reset();
	}
	
	//连环画
		private void comic() {
			// 进入负片状态
			prepare(STATE_NONE, CropImageView.STATE_NONE, true);
			mShowHandleName.setText(R.string.comic);
			//mImageFilter.negative(mTmpBmp);
			Bitmap bmp = mImageFilter.comic(mTmpBmp);
			mTmpBmp = bmp;
			reset();
		}
		
	//线性灰度
	private void lineGrey() {
		// 进入负片状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.lineGrey);
		//mImageFilter.negative(mTmpBmp);
		Bitmap bmp = mImageFilter.lineGrey(mTmpBmp);
		mTmpBmp = bmp;
		reset();
	}

}

