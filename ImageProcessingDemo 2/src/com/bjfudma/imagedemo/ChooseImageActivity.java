package com.bjfudma.imagedemo;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import com.bjfudma.imagedemo.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class ChooseImageActivity extends Activity {

	private static final int FLAG_CHOOSE = 1; //标记选择，目的：检查图片是否传送到gallery中
	
	private static final int FLAG_TAKE = 2; //标记选择，目的
	
	
	float scale = (float) 0.25;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chooseimage);
	}

	 //”选择图片”按钮的单击事件
	 //跳转到系统内部的选择界面 图片选择界面
	public void onClick(View v) {    
		switch (v.getId()) {
		case R.id.choose_img://点击按钮时的id 
			Intent intent = new Intent();//创建Intent对象
			intent.setAction(Intent.ACTION_PICK);//设置Intent的Action - 调用系统的选择界面Gallery的Action  "Intent.ACTION_PICK"
			//Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT); 
			intent.setType("image/*");// 选择图片
			startActivityForResult(intent, FLAG_CHOOSE); //intent和flag_choose共同开始 startActivityForResult（）这这个方法的的目的是 进行其他按钮事件后 能够返回到当前的界面
			break;
			
		case R.id.take_img://点击按钮时的id 
			Intent intent1 = new Intent();//创建Intent对象
			intent1.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			//Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT); 
			//photoUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; 
			startActivityForResult(intent1, FLAG_TAKE);
			
			//break;
		}
	}

	//获取图片的路径信息，将path传到MainActivity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && null != data) {
			Matrix matrix;
			switch (requestCode) {
			case FLAG_CHOOSE: 
				Uri uri = data.getData();
				
				Log.d("ChooseImageActivity - uri", "uri=" + uri
						+ ", authority=" + uri.getAuthority());
				
				if (!TextUtils.isEmpty(uri.getAuthority())) {

					Cursor cursor = getContentResolver().query(
							uri,
							new String[] { MediaStore.Images.Media.DATA },
							null, 
							null, 
							null);
					if (null == cursor) {
						Toast.makeText(this, R.string.no_found,
								Toast.LENGTH_SHORT).show();
						return;
					}
					cursor.moveToFirst();
					String path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					
					Log.d("ChooseImageActivity - Path :", "path=" + path);
					
					Intent intent = new Intent(this, MainActivity.class);
					intent.putExtra("path", path);
					startActivity(intent);
				} else {
					Log.d("ChooseImageActivity - Path :",
							"path=" + uri.getPath());
					Intent intent = new Intent(this, MainActivity.class);
					intent.putExtra("path", uri.getPath());
					startActivity(intent);
				}
				break;
			default:
				break;
				
			case FLAG_TAKE:
			
			String name = new DateFormat().format("yyyyMMdd_hhmmss",Calendar.getInstance(Locale.CHINA)) + ".jpg";	
			Toast.makeText(this, name, Toast.LENGTH_LONG).show();
			Bundle bundle = data.getExtras();
			Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
		
			FileOutputStream b = null;
		   //???????????????????????????????为什么不能直接保存在系统相册位置呢？？？？？？？？？？？？
			File file = new File(getString(R.string._sdcard_myimage_));
			file.mkdirs();// 创建文件夹
			String fileName = getString(R.string._sdcard_myimage_)+name;

			try {
				b = new FileOutputStream(fileName);
				
				Matrix matrix1 = new Matrix();
				matrix1.postScale(scale, scale);

				int bitmapWidth = bitmap.getWidth();
				int bitmapHeight = bitmap.getHeight();
					// 产生缩放后的Bitmap对象
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
						bitmapHeight, matrix1, false);
				
				bitmap.compress(Bitmap.CompressFormat.JPEG, 50, b);// 把数据写入文件
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					b.flush();
					b.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}		
			}
		}

	public Bitmap transImage(Bitmap bitmap) {
		//Bitmap bitmap = BitmapFactory.decodeFile(fromFile);
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		int width = (int)(bitmapWidth*0.25);
		int height = (int)(bitmapHeight*0.25);

		// 缩放图片的尺寸
		float scaleWidth = (float) width / bitmapWidth;
		float scaleHeight = (float) height / bitmapHeight;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// 产生缩放后的Bitmap对象
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
				bitmapHeight, matrix, false);

		if (!bitmap.isRecycled()) {
			bitmap.recycle();// 记得释放资源，否则会内存溢出
		}
		
		/*if (!resizeBitmap.isRecycled()) {
			resizeBitmap.recycle();
		}*/
		return bitmap;
		
	}

	
}
