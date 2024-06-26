/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hoppen.rasrcs.service;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class AudioTrackService {
    public enum PlayState {
        init,
        playing,
        playEnd
    }

    // 播放状态
    private PlayState playState;

    // 采样率
    private int sampleRate = 16000;

    // 播放回调
    private AudioTrackServiceCallback audioPlayerCallback;
    // 获取要播放的临时音频。
    private byte[] tempData;

    // 用于存放rtts客户端发过来的合成音频。
    private LinkedBlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue();
    // 初始化播放器
    private int iMinBufSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private AudioTrack audioTrack = audioTrack = new AudioTrack(new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build(),
            new AudioFormat.Builder().setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            iMinBufSize * 10, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);

    public AudioTrackService(AudioTrackServiceCallback audioPlayerCallback) {
        playState = PlayState.init;
        this.audioPlayerCallback = audioPlayerCallback;
    }


    public void setAudioData(byte[] data) {
        audioQueue.add(data);
    }

    public void play() {
        playState = PlayState.playing;
        audioTrack.play();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 回调client端传过来的audioPlayerCallback类的playStart方法
                audioPlayerCallback.playStart();
                while (!((playState == PlayState.playEnd) && (audioQueue.size() == 0))) {
                    try {
                        tempData = audioQueue.take();
                    } catch (InterruptedException e) {
                        Log.e("error", e.toString());
                    }
                    audioTrack.write(tempData, 0, tempData.length);
                    audioQueue.remove(tempData);
                }
                // 回调client端传过来的audioPlayerCallback类的playOver方法
                audioPlayerCallback.playOver();
                Log.i("info", "线程结束");
            }
        }).start();
    }

    public void stop() {
        playState = PlayState.playEnd;
        audioQueue.clear();
        audioTrack.flush();
        audioTrack.pause();
        audioTrack.stop();
    }

    public void releaseTrack() {
        audioTrack.release();
    }

    public void setPlayState(PlayState playState) {
        this.playState = playState;
    }
}
