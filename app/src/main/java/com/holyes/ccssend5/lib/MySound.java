package com.holyes.ccssend5.lib;

import android.app.Service;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.example.ccssend5.R;



/**
 * @ClassName: MySound
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:27
 */
public class MySound {
    private static SoundPool soundpool = null;
    private static int errorsoundid, scansoundid, errorsoundLack1, errorsoundChange,
            errorsoundLack2, errorsoundBeyond, errorsoundRepeat, soundPass;//数量不足1,数量不足2,超出，重复，通过
    private static MySound mySound = null;

    // 上下文
//    static Context mContext;

    /**
     * SoundPool是否加载完成
     */
    private boolean isPrepare = false;


//    public static MySound getMySound(Context context) {
//        if (mySound == null) {
//            mySound = new MySound(context);
//        }
//        return mySound;
//    }


    public static MySound getMySound(Context context) {

        if (mySound == null) {

            mySound = new MySound();

        }
        // 初始化声音
//        mContext = context;
        scansoundid = soundpool.load(context, R.raw.beep, 1);
        errorsoundid = soundpool.load(context, R.raw.errbeep1, 1);
        errorsoundChange = soundpool.load(context, R.raw.changemodle, 1);//切换型号
        errorsoundBeyond = soundpool.load(context, R.raw.beyond, 1);//数量超出

        return mySound;
    }


    public MySound() {

        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(5);
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .setUsage(AudioAttributes.USAGE_NOTIFICATION);
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            soundpool = builder.build();
        } else {
//        创建SoundPool对象
            soundpool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

//
//        scansoundid = soundpool.load(context, R.raw.beep1, 1);
//        errorsoundid = soundpool.load(context, R.raw.errsend, 1);
//        errorsoundChange = soundpool.load(context, R.raw.changemodle, 1);//切换型号
//        errorsoundBeyond = soundpool.load(context, R.raw.beyond, 1);//数量超出
        //errorsoundLack1 = soundpool.load(context, R.raw.lack_one, 1);//数量不足1
        //errorsoundLack2 = soundpool.load(context, R.raw.lack_two, 1);//数量不足2
        //errorsoundRepeat = soundpool.load(context, R.raw.repeat, 1);//数量超出
        //soundPass = soundpool.load(context, R.raw.pass,  1);//通过

//        Log.d("beep", "beep" + getRawFileVoiceTime(context,R.raw.beep1) +"---"+"errbeep1" + getRawFileVoiceTime(context,R.raw.errbeep1));
//        AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        String nativeSampleRate = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
//        Log.i("main", "hardware support samplerate: " + nativeSampleRate);

    }


    public static void scanSound() {

//        scansoundid = soundpool.load(context, R.raw.beep, 1);
//
//        soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        soundpool.play(scansoundid, 1, 1, 0, 0, 1);
//        Log.d("main", "scansoundid----" + scansoundid);
//        Log.d("main", "scanid----" + scanid);
//            }
//        });

    }


    public static void errorSound() {
        //play方法第二、三个参数用于指定左右声道的音量，取值范围是0.0~1.0
//        errorsoundid = soundpool.load(context, R.raw.errbeep1, 1);
//
//        soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        soundpool.play(errorsoundid, 1, 1, 0, 0, 1);
//        Log.d("main", "errorsoundid----" + errorsoundid);
//        Log.d("main", "errorid----" + errorid);
//            }
//        });

    }

    //	//数量不足1
//	public void errorSoundLack1() {
//		//		startAlarm();
//
//		soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
//			@Override
//			public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
//				soundpool.play(errorsoundLack1, 1, 1, 0, 0, 1);
//			}});
//
//	}
//	//数量不足2
//	public void errorSoundLack2() {
//
//		soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
//			@Override
//			public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
//				soundpool.play(errorsoundLack2, 1, 1, 0, 0, 1);
//			}});
//		//			startAlarm();
//
//	}
//
    //数量超出
    public static void errorSoundBeyond() {

//        errorsoundBeyond = soundpool.load(context, R.raw.beyond, 1);//数量超出
//
//        soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        soundpool.play(errorsoundBeyond, 1, 1, 0, 0, 1);
//            }
//        });


    }

    //	//条码重复
//	public void errorSoundRepeat() {
//		//			startAlarm();
//		soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
//			@Override
//			public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
//				soundpool.play(errorsoundRepeat, 1, 1, 0, 0, 1);
//			}});
//
//	}
//
//	//通过
//	public void soundPass() {
//		//		startAlarm();
//		soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
//			@Override
//			public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
//				soundpool.play(soundPass, 1, 1, 0, 0, 1);
//			}});
//
//	}
//
    public static void errorChange() {
//        errorsoundChange = soundpool.load(context, R.raw.changemodle, 1);//切换型号
//
//        soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        soundpool.play(errorsoundChange, 1, 1, 0, 0, 1);
//            }
//        });


    }

    //
//    public  void Vibrate(long milliseconds) {
//        Vibrator vib = (Vibrator) context
//                .getSystemService(Service.VIBRATOR_SERVICE);
//        vib.vibrate(milliseconds);
//    }

//    public void release() {
//        if (soundpool != null) {
//            soundpool.release();
//            soundpool=null;
//        }
//    }


    /**
     * 获取音频文件的总时长大小
     *
     * @param rawId raw资源文件ID
     * @return 返回时长大小
     */
    public long getRawFileVoiceTime(Context mContext, int rawId) {
        long mediaPlayerDuration = 0L;
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + rawId);
            mediaPlayer.setDataSource(mContext, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayerDuration = mediaPlayer.getDuration();
        } catch (Exception exception) {
            Log.i("MySound", exception.getMessage());
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        return mediaPlayerDuration;
    }

}

