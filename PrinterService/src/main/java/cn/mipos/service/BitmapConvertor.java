package cn.mipos.service;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;

public class BitmapConvertor{

	private int mDataWidth;
	private byte mRawBitmapData[];
	private int[] mDataArray;
	private static final String TAG = "BitmapConvertor";
	private int mWidth, mHeight;
	private String mStatus;
	private String mFileName;


	public BitmapConvertor() {

	}


	public byte[] toBytes(Bitmap inputBitmap)
	{
		Log.d("BitmapConvertor", "print toBytes start...");
		mWidth = inputBitmap.getWidth();
		mHeight = inputBitmap.getHeight();
		mDataWidth=((mWidth+31)/32)*4*8;
		mDataArray = new int[(mDataWidth * mHeight)];
		mRawBitmapData = new byte[(mDataWidth * mHeight) / 8];

		Arrays.fill(mDataArray, 1);
		convertArgbToGrayscale2(inputBitmap);
		createRawMonochromeData();
		return mRawBitmapData;
	}

	public byte[] toBytes(byte[][] arrays, int width, int height)
	{
		mWidth = width;
		mHeight = height;
		mDataWidth=((mWidth+31)/32)*4*8;
		mDataArray = new int[(mDataWidth * mHeight)];
		mRawBitmapData = new byte[(mDataWidth * mHeight) / 8];
		convertArgbToGrayscale2(arrays, mWidth, mHeight);
		createRawMonochromeData();
		return mRawBitmapData;
	}

	/**
	 * Converts the input image to 1bpp-monochrome bitmap
	 * @param inputBitmap : Bitmpa to be converted
	 * @param fileName : Save-As filename
	 * @return :  Returns a String. Success when the file is saved on memory card or error.
	 */
	public String convertBitmap(Bitmap inputBitmap, String fileName){

		mWidth = inputBitmap.getWidth();
		mHeight = inputBitmap.getHeight();
		mFileName = fileName;
		mDataWidth=((mWidth+31)/32)*4*8;
		mDataArray = new int[(mDataWidth * mHeight)];
		mRawBitmapData = new byte[(mDataWidth * mHeight) / 8];
		return mStatus;

	}

	public void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height){
		int pixel;
		int k = 0;
		int B=0,G=0,R=0;
		try{
			for(int x = 0; x < height; x++) {
				for(int y = 0; y < width; y++, k++) {
					// get one pixel color
					pixel = bmpOriginal.getPixel(y, x);

					// retrieve color of all channels
					R = Color.red(pixel);
					G = Color.green(pixel);
					B = Color.blue(pixel);

					if((R!=0xff && R!=0x00) || (G!=0xff && G!=0x00) || (B!=0xff && B!=0x00))
					{
						Log.i("t", String.format("%02x %02x %02x", R, G, B));
					}

					// take conversion up to one single value by calculating pixel intensity.
					R = G = B = (int)(0.299 * R + 0.587 * G + 0.114 * B);
					// set new pixel color to output bitmap
					if (R < 128) {
						mDataArray[k] = 0;
					} else {
						mDataArray[k] = 1;
					}
				}
				if(mDataWidth>width){
					for(int p=width;p<mDataWidth;p++,k++){
						mDataArray[k]=1;
					}
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, e.toString());
		}
	}

	public void convertArgbToGrayscale2(Bitmap bitmap){
		int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

		// 0:：黑色  1：白色
		int pixel=0, A=0, R=0, G=0, B=0;
		for(int i=0; i<pixels.length; i++){
			pixel = pixels[i];
			A = Color.alpha(pixel);
			R = Color.red(pixel);
			G = Color.green(pixel);
			B = Color.blue(pixel);

			if((R <= 255 && R >= 130 && G <= 255 && G >= 228 && B <= 255 && B >=220) || A <= 25){
				mDataArray[i] = 1;
			}else {
				mDataArray[i] = 0;
			}
		}
	}

	public void convertArgbToGrayscale2(byte[][] arrays, int width, int height){
		int k = 0;
		for(int x = 0; x < height; x++) {
			for(int y = 0; y < width; y++, k++) {
				mDataArray[k] = (byte) (arrays[y][x] == 1? 0 : 1);
			}
			if(mDataWidth>width){
				for(int p=width;p<mDataWidth;p++,k++){
					mDataArray[k]=1;
				}
			}
		}
	}

	public void createRawMonochromeData(){
		int length = 0;
		for (int i = 0; i < mDataArray.length; i = i + 8) {
			byte first = (byte) mDataArray[i];
			for (int j = 0; j <= 7; j++) {
				byte second = (byte) ((first << 1) | mDataArray[i + j]);
				first = second;
			}

			mRawBitmapData[length] = (byte)~first;

			if(mRawBitmapData[length] != 0)
				Log.i("...","");

			length++;
		}
	}


}
