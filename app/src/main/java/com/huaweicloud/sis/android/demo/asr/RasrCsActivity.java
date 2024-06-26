/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huaweicloud.sis.android.demo.asr;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.hoppen.rasrcs.Config;
import com.hoppen.rasrcs.service.AudioRecordService;
import com.huaweicloud.sdk.core.utils.JsonUtils;
import com.huaweicloud.sis.android.demo.R;

import sis.android.sdk.RasrClient;
import sis.android.sdk.bean.AuthInfo;
import sis.android.sdk.bean.SisHttpConfig;
import sis.android.sdk.bean.request.RasrRequest;
import sis.android.sdk.bean.response.AsrResponse;
import sis.android.sdk.exception.SisException;
import sis.android.sdk.listeners.RasrResponseListener;
import sis.android.sdk.listeners.process.RasrConnProcessListener;


/**
 * 功能描述
 * 实时语音识别连续模式
 *
 * @since 2022-07-11
 */
public class RasrCsActivity extends AppCompatActivity {
    private TextView title;
    private TextView result;
    private Button startButton;
    private Button endButton;
    private RasrClient rasrClient;
    // 实时显示识别的结果
    private StringBuffer realTimeResult;

    private AudioRecordService audioRecordService;
    private AuthInfo authInfo;

    private RasrConnProcessListener rasrConnProcessListener = new RasrConnProcessListener() {
        /**
         * 连接关闭后回调
         */
        @Override
        public void onTranscriptionClose() {
            Log.i("info", "长连接关闭");
        }

        /**
         * 连接建立后回调
         */
        @Override
        public void onTranscriptionConnect() {
            Log.i("info", "长连接开始");
        }

        /**
         * 长连接连接失败是回调
         *
         * @param asrResponse 返回体
         */
        @Override
        public void onTranscriptionFail(AsrResponse asrResponse) {
            Log.i("info", "长连接异常");
            // 调用失败给用户提示
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "实时语音单句模式长连接失败" + JsonUtils.toJSON(asrResponse), Toast.LENGTH_SHORT).show();
                }
            });
            rasrClient.close();
        }
    };

    private RasrResponseListener rasrResponseListener = new RasrResponseListener() {
        /**
         * 检测到句子开始事件
         */
        @Override
        public void onVoiceStart() {
        }

        /**
         * 检测到句子结束事件
         */
        @Override
        public void onVoiceEnd() {
        }

        /**
         * 返回识别的信息
         * @param message
         */
        @Override
        public void onResponseMessage(AsrResponse message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < message.getSegments().size(); i++) {
                        AsrResponse.Segment segment = message.getSegments().get(i);
                        // 实时语音识别连续模式 回调结果更新到界面UI中
                        result.setText(realTimeResult.toString() + segment.getResult().getText());
                        if (segment.getIsFinal()) {
                            realTimeResult.append(segment.getResult().getText());
                        }
                    }
                }
            });
        }

        /**
         *
         * 静音超长，也即没有检测到声音。响应事件
         */
        @Override
        public void onExcceededSilence() {
        }

        /**
         * 返回识别的信息
         * @param response
         */
        @Override
        public void onResponseBegin(AsrResponse response) {
        }

        @Override
        public void onResponseEnd(AsrResponse response) {
        }

        @Override
        public void onResponseError(AsrResponse response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "实时语音识别连续模式，错误响应:" + JsonUtils.toJSON(response), Toast.LENGTH_SHORT).show();
                }
            });
            rasrClient.close();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rasr);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initResources();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rasrClient != null) {
            rasrClient.close();
        }
        if (audioRecordService != null && audioRecordService.getIsRecording().get()) {
            audioRecordService.stopAudioRecord();
            audioRecordService.releaseAudioRecord();
        }
    }

    /**
     * 初始化界面
     */
    private void initView() {
        result = findViewById(R.id.result);
        title = findViewById(R.id.title);
        title.setText("实时语音识别连续模式");
        startButton = findViewById(R.id.start);
        endButton = findViewById(R.id.end);
        // 定义开始按钮点击事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateButtonState(startButton, false);
                updateButtonState(endButton, true);
                realTimeResult = realTimeResult.delete(0, realTimeResult.length());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            rasrClient = new RasrClient(authInfo, rasrResponseListener, rasrConnProcessListener, new SisHttpConfig());
                            rasrClient.rasrContinueStreamConnect();
                            // 建立连接
                            rasrClient.connect();
                            rasrClient.sendStart(getStartRequest());
                            audioRecordService.startSendRecordingData(rasrClient);
                        } catch (SisException e) {
                            Log.e("error", e.getErrorCode() + e.getErrorMsg());
                        }
                    }
                }).start();
                Toast.makeText(getApplicationContext(), "正在进行录音中...", Toast.LENGTH_SHORT).show();
            }
        });
        // 结束按钮识别事件
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateButtonState(startButton, true);
                updateButtonState(endButton, false);
                if (audioRecordService.getIsRecording().get()) {
                    audioRecordService.stopAudioRecord();
                    Toast.makeText(getApplicationContext(), "识别结束...", Toast.LENGTH_SHORT).show();
                }
                try {
                    rasrClient.sendEnd();
                } catch (SisException e) {
                    Log.e("error", e.getErrorCode() + e.getErrorMsg());
                }
                rasrClient.close();
            }
        });
        updateButtonState(startButton, true);
        updateButtonState(endButton, false);
    }

    /**
     * 初始化设置资源
     */
    private void initResources() {
        authInfo = new AuthInfo(this.getString(R.string.HUAWEICLOUD_SDK_AK), this.getString(R.string.HUAWEICLOUD_SDK_SK),
                Config.REGION, Config.PROJECT_ID);
        audioRecordService = new AudioRecordService(16000);
        realTimeResult = new StringBuffer();
    }

    /**
     * 开始请求
     *
     * @return 返回请求体内容
     */
    private RasrRequest getStartRequest() {
        RasrRequest rasrRequest = new RasrRequest();
        rasrRequest.setCommand("START");
        RasrRequest.Config config = new RasrRequest.Config();
        config.setAudioFormat("pcm16k16bit");
        config.setProperty("chinese_16k_general");
        config.setAddPunc("yes");
        config.setInterimResults("yes");
        rasrRequest.setConfig(config);
        return rasrRequest;
    }

    // 用于设置按钮的状态
    private void updateButtonState(final Button btn, final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn.setEnabled(state);
            }
        });
    }
}
