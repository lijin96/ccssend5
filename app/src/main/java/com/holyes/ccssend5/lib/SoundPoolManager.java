package com.holyes.ccssend5.lib;

import android.media.AudioManager;
import android.media.SoundPool;

import com.example.ccssend5.R;

import java.util.HashMap;

/**
 * @ClassName: SoundPoolManager
 * @Description: 音效管理类
 * @Author: lijin
 * @Date: 2021/4/14 10:13
 */
public class SoundPoolManager {
//    companion object {
//        private var instance: SoundPoolManager? = null
//        get() {
//            if (field == null) {
//                field = SoundPoolManager()
//            }
//            return field
//        }
//
//        @Synchronized
//        fun get(): SoundPoolManager {
//            return instance!!
//        }
//
//        const val TASK_CLICK = 1
//        const val TASK_SUCCESS = 2
//        const val TASK_WRONG = 3
//        const val TASK_REWARD = 4
//    }
//
//
//    private var soundPool: SoundPool = SoundPool(4, AudioManager.STREAM_MUSIC, 0)
//    private var sourcesMap = HashMap<Int, Int>()
//
//    init {
//        sourcesMap[TASK_CLICK] = soundPool.load(RiseApp.mContext, R.raw.click, 1)
//        sourcesMap[TASK_SUCCESS] = soundPool.load(RiseApp.mContext, R.raw.success, 1)
//        sourcesMap[TASK_WRONG] = soundPool.load(RiseApp.mContext, R.raw.wrong, 1)
//        sourcesMap[TASK_REWARD] = soundPool.load(RiseApp.mContext, R.raw.reward, 1)
//    }
//
//    fun play(type: Int) {
//        // 防止type不存在
//        if (sourcesMap[type] != null) {
//            soundPool.play(sourcesMap[type]!!, 1.0f, 1.0f, 0, 0, 1.0f)
//        }
//    }
}