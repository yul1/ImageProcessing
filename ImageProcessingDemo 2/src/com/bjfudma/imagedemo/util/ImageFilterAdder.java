package com.bjfudma.imagedemo.util;

//import android.app.Activity;
//import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.Canvas;
//import android.graphics.Matrix;
//import android.graphics.PointF;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.media.FaceDetector;
import android.os.Handler;
//import android.widget.Toast;


//import com.bjfudma.imagedemo.util.EditImage.BackgroundJob;
import com.bjfudma.imagedemo.view.CropImageView;
//import com.bjfudma.imagedemo.view.HighlightView;
//import com.bjfudma.imagedemo.R;


//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.Config;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.PixelFormat;
//import android.graphics.Rect;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.LayerDrawable;

//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.concurrent.CountDownLatch;
import android.util.Log;

public class ImageFilterAdder 
{
	public boolean mWaitingToPick;
    public boolean mSaving;
    //public HighlightView mCrop;
    
	private Context mContext;
	private Handler mHandler = new Handler();
	private ImageFilterAdder mImageFilter;
	private CropImageView mImageView;
	private Bitmap mBitmap;
	
	/**
	 * Constructor method 构造方法
	 * @param context
	 * @param imageView
	 * @param bm
	 */
	public ImageFilterAdder(Context context, CropImageView imageView, Bitmap bm)
	{
		mContext = context;
		mImageView = imageView;
		//mImageFilter = imageFilter;
		mBitmap = bm;
	}
	
	//(高斯模糊)
	public Bitmap blur(Bitmap bmp) {
		long start = System.currentTimeMillis();
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		int pixColor = 0;
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < width; k++) {
				pixColor = pixels[width * i + k];
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				newR = (int) (0.095 * pixR + 0.118 * pixG + 0.095 * pixB);
				newG = (int) (0.118 * pixR + 0.148 * pixG + 0.118 * pixB);
				newB = (int) (0.095 * pixR + 0.118 * pixG + 0.095 * pixB);
				int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255
						: newB);
				pixels[width * i + k] = newColor;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		//long end = System.currentTimeMillis();
		//Log.e("may", "used time=" + (end - start));
		//ImageCache.put("oldRemeber", bitmap);
		return bitmap;
	}


		//浮雕效果
	public Bitmap sculpture(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;
		
		int a = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				pos = i * width + k;
				pixColor = pixels[pos];

				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				pixColor = pixels[pos + 1];
				newR = Color.red(pixColor) - pixR + 127;
				newG = Color.green(pixColor) - pixG + 127;
				newB = Color.blue(pixColor) - pixB + 127;

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				
				a = Color.alpha(pixColor);
		      
				pixels[i * width + k] = Color.argb(a, newR, newG, newB);
			}
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}


		//锐化（拉普拉斯变换）
	public Bitmap sharpen(Bitmap bmp) {
		if (ImageCache.get("sharpen") != null) {
			return ImageCache.get("sharpen");
		}
		long start = System.currentTimeMillis();
		// 拉普拉斯矩阵
		int[] laplacian = new int[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 };

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int idx = 0;
		float alpha = 0.3F;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				idx = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						pixColor = pixels[(i + n) * width + k + m];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);

						newR = newR + (int) (pixR * laplacian[idx] * alpha);
						newG = newG + (int) (pixG * laplacian[idx] * alpha);
						newB = newB + (int) (pixB * laplacian[idx] * alpha);
						idx++;
					}
				}

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[i * width + k] = Color.argb(255, newR, newG, newB);
				newR = 0;
				newG = 0;
				newB = 0;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		long end = System.currentTimeMillis();
		Log.e("sharpenImageAmeliorate", "used time=" + (end - start));
		ImageCache.put("sharpen", bitmap);
		return bitmap;
	}


		
	//怀旧
	//public static Bitmap rememberpast(Bitmap bmp) {
		public Bitmap rememberpast(Bitmap bmp) {
	        //if (((Activity)mContext).isFinishing()) {
	         //   return;
	        //}
			//if (ImageCache.get("rememberpast") != null) {
			//	return ImageCache.get("rememberpast");
			//}
			
			// 速度测试
			long start = System.currentTimeMillis();
			int width = bmp.getWidth();
			int height = bmp.getHeight();
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			int pixColor = 0;
			int pixR = 0;
			int pixG = 0;
			int pixB = 0;
			int newR = 0;
			int newG = 0;
			int newB = 0;
			int[] pixels = new int[width * height];
			bmp.getPixels(pixels, 0, width, 0, 0, width, height);
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					pixColor = pixels[width * i + k];
					pixR = Color.red(pixColor);
					pixG = Color.green(pixColor);
					pixB = Color.blue(pixColor);
					newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
					newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
					newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
					int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255
							: newB);
					pixels[width * i + k] = newColor;
				}
			}

			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			//long end = System.currentTimeMillis();
			//Log.e("may", "used time=" + (end - start));
			//ImageCache.put("oldRemeber", bitmap);
			return bitmap;
		}


	// 负片
		public Bitmap negative(Bitmap bmp) {
			// RGBA的最大值
			//if (((Activity)mContext).isFinishing()) {
	         //   return;
	        //}
			//if (ImageCache.get("rememberpast") != null) {
			//	return ImageCache.get("rememberpast");
			//}
			
			// 速度测试
			long start = System.currentTimeMillis();
			int width = bmp.getWidth();
			int height = bmp.getHeight();
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			int pixColor = 0;
			int pixR = 0;
			int pixG = 0;
			int pixB = 0;
			int newR = 0;
			int newG = 0;
			int newB = 0;
			int[] pixels = new int[width * height];
			bmp.getPixels(pixels, 0, width, 0, 0, width, height);
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					pixColor = pixels[width * i + k];
					pixR = Color.red(pixColor);
					pixG = Color.green(pixColor);
					pixB = Color.blue(pixColor);
					newR = (int) (255 - pixR );
					newG = (int) (255 - pixG );
					newB = (int) (255 - pixB);
					int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255
							: newB);
					pixels[width * i + k] = newColor;
				}
			}

			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			//long end = System.currentTimeMillis();
			//Log.e("may", "used time=" + (end - start));
			//ImageCache.put("oldRemeber", bitmap);
			return bitmap;
		}

		//黑白
				public Bitmap blackandwhite(Bitmap bmp) {
			        //if (((Activity)mContext).isFinishing()) {
			         //   return;
			        //}
					//if (ImageCache.get("rememberpast") != null) {
					//	return ImageCache.get("rememberpast");
					//}
					
					// 速度测试
					long start = System.currentTimeMillis();
					int width = bmp.getWidth();
					int height = bmp.getHeight();
					Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
					int pixColor = 0;
					int pixR = 0;
					int pixG = 0;
					int pixB = 0;

					
					
					int[] pixels = new int[width * height];
					bmp.getPixels(pixels, 0, width, 0, 0, width, height);
					for (int i = 0; i < height; i++) {
						for (int k = 0; k < width; k++) {
							pixColor = pixels[width * i + k];
							int grey = pixels[width * i + k];
							
							pixR = Color.red(pixColor);
							pixG = Color.green(pixColor);
							pixB = Color.blue(pixColor);

							grey = (int) (pixR * 0.33 + pixG* 0.33 + pixB * 0.33);
							
							if(grey>100){grey=255;}
							else {grey=0;}
							
							int newColor = Color.argb(255, grey, grey, grey);
							pixels[width * i + k] = newColor;
							

						}
					}
					bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
					//long end = System.currentTimeMillis();
					//Log.e("may", "used time=" + (end - start));
					//ImageCache.put("oldRemeber", bitmap);
					return bitmap;
				}
				
				
				
				//羽化效果
				  public Bitmap feather(Bitmap bmp) {

					float Size = 0.5f;  
					  
		  			long start = System.currentTimeMillis();
		  			int width = bmp.getWidth();
		  			int height = bmp.getHeight();
		  			int ratio = width > height ? height * 32768 / width : width * 32768 / height;
		  			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		  			int pixColor = 0;
		  			int pixR = 0;
		  			int pixG = 0;
		  			int pixB = 0;
		  			int newR = 0;
		       		int newG = 0;
		       		int newB = 0;  
		  			
		              int cx = width >> 1;
		              int cy = height >> 1;
		              int max = cx * cx + cy * cy;
		              int min = (int) (max * (1 - Size));
		              int diff = max - min;
		              
		             
		      		int[] pixels = new int[width * height];
		      		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		      		for (int i = 0; i < height; i++) {
		      			for (int k = 0; k < width; k++) {
		      				pixColor = pixels[width * i + k];
		      				pixR = Color.red(pixColor);
		      				pixG = Color.green(pixColor);
		      				pixB = Color.blue(pixColor);
		      				
		      				 // Calculate distance to center and adapt aspect ratio
		                    int dx = cx - k;
		                    int dy = cy - i;
		                    if (width > height) {
		                            dx = (dx * ratio) >> 15;
		                    } else {
		                            dy = (dy * ratio) >> 15;
		                    }
		                    int distSq = dx * dx + dy * dy;
		                    float v = ((float) distSq / diff) * 255;
		                    
		      				newR = (int) (pixR + (v));
		      				newG = (int) (pixG + (v));
		      				newB = (int) (pixB + (v));
		      				newR = (newR > 255 ? 255 : (newR < 0 ? 0 : newR));
		      				newG = (newG > 255 ? 255 : (newG < 0 ? 0 : newG));
		      				newB = (newB > 255 ? 255 : (newB < 0 ? 0 : newB));
		      				int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255
		      						: newB);
		      				pixels[width * i + k] = newColor;
		      			}
		    		}

		    		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		    		//long end = System.currentTimeMillis();
		    		//Log.e("may", "used time=" + (end - start));
		    		//ImageCache.put("oldRemeber", bitmap);
		    		return bitmap;
		    	}

				//连环画效果  
				  public Bitmap comic(Bitmap bmp) {
			  			int width = bmp.getWidth();
			  			int height = bmp.getHeight();
			  			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			  			int pixColor = 0;
			  			int pixR = 0;
			  			int pixG = 0;
			  			int pixB = 0;
			  			int newR = 0;
			  			int newG = 0;
			  			int newB = 0;
			  			int pix  = 0;

			  			int[] pixels = new int[width * height];
			  			bmp.getPixels(pixels, 0, width, 0, 0, width, height);
			  			for (int i = 0; i < height; i++) {
			  				for (int k = 0; k < width; k++) {
			  					pixColor = pixels[width * i + k];
			  					pixR = Color.red(pixColor);
			  					pixG = Color.green(pixColor);
			  					pixB = Color.blue(pixColor);
			  					
			  					pix = pixG -pixB + pixG + pixR;
			  					 if (pix < 0){
		                             pix = -pix;
		                             pix = pix * pixR / 256;
			  					 }
		                     if (pix > 255){
		                             pix = 255;
		                     newR = pix;
		                     }
		                     
		                     
		                     pix = pixB - pixG + pixB + pixR;;
			  					 if (pix < 0){
		                            pix = -pix;
		                            pix = pix * pixR / 256;
			  					 }
		                    if (pix > 255){
		                            pix = 255;
		                    newG = pix;
		                    }

		                    
		                    pix = pixB - pixG + pixB + pixR;
		                    if (pix < 0)
		                            pix = -pix;
		                    pix = pix * pixG / 256;
		                    if (pix > 255)
		                            pix = 255;
		                    newB = pix;
		                    
		                    int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255
		    						: newB);

		                    
		                    pixels[width * i + k] = newColor;
			  				}
			  			}

			  			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			  			//bitmap = ImageUtil.toGrayscale(bitmap); // 图片灰度化处理
			  			bitmap = lineGrey(bitmap);
			  			//long end = System.currentTimeMillis();
			  			//Log.e("may", "used time=" + (end - start));
			  			//ImageCache.put("oldRemeber", bitmap);
			  			return bitmap;
			  		}

		        //线性灰度图


				  public Bitmap lineGrey(Bitmap bmpOriginal) {
				        int width, height;
				        height = bmpOriginal.getHeight();
				        width = bmpOriginal.getWidth();    

				        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
				        Canvas c = new Canvas(bmpGrayscale);
				        Paint paint = new Paint();
				        ColorMatrix cm = new ColorMatrix();
				        cm.setSaturation(0);
				        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
				        paint.setColorFilter(f);
				        c.drawBitmap(bmpOriginal, 0, 0, paint);
				        return bmpGrayscale;
				    }
	
}