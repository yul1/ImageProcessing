package com.bjfudma.imagedemo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.bjfudma.imagedemo.R;
import com.bjfudma.imagedemo.view.CropImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ImageUtil {
	
	// 将Drawable转化为Bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	public static String SaveBitmap(Bitmap bmp, String name) {
		File file = new File("mnt/sdcard/picture/");
		String path = null;
		if (!file.exists())
			file.mkdirs();
		try {
			path = file.getPath() + "/" + name;
			FileOutputStream fileOutputStream = new FileOutputStream(path);

			bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			System.out.println("saveBmp is here");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return path;
	}

	public static Bitmap toGrayscale(Bitmap bitmap) {
		// TODO Auto-generated method stub
		return null;
	}
	

}