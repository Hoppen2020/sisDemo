/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hoppen.rasrcs.service;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 功能描述
 *
 * @since 2022-08-12
 */
public class MediaPlayerService {
    private MediaPlayer mPlayer = null;

    /**
     * 功能描述
     * 开始播放
     *
     * @param createFilePath 音频数据路径
     */
    public void startPlay(String createFilePath) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(createFilePath);
            mPlayer.prepare();
            mPlayer.start();
            while (!mPlayer.isPlaying()) {
                if (mPlayer != null) {
                    mPlayer.stop();
                    mPlayer.release();
                    delCacheFile(createFilePath);
                    mPlayer = null;
                }
            }
        } catch (IOException e) {
            Log.e("tag", "prepare() failed");
        }
    }

    /**
     * 功能描述
     * 开始播放
     *
     * @param context    制定上下文
     * @param aFormat    指定创建的文件类型
     * @param base64Data 音频数据
     * @return 返回创建的文件路径
     */
    public String createAudioFile(Context context, String aFormat, String base64Data) {
        File file = context.getCacheDir();
        File tempFile = null;
        FileOutputStream out = null;
        String filePath = "";
        try {
            // 创建临时音频文件
            tempFile = File.createTempFile("recording.", aFormat, file);
            byte[] data = Base64.decode(base64Data, Base64.DEFAULT);
            out = new FileOutputStream(tempFile.getCanonicalPath());
            out.write(data);
            filePath = tempFile.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                Log.e("error", "文件流关闭异常");
            }
        }
        return filePath;
    }

    // 清除缓存数据
    private void delCacheFile(String filePath) {
        File file = new File(filePath);
        file.delete();
    }


    /**
     * 功能描述
     * 停止播放释放资源
     */
    public void stopMyPlayer(String filePath) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            delCacheFile(filePath);
            mPlayer = null;
        }
    }
}
