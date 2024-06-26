/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huaweicloud.sis.android.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.huaweicloud.sis.android.demo.asr.RasrCsActivity;

/**
 * 功能描述
 * 主活动
 *
 * @since 2022-07-18
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = null;
        if (findViewById(R.id.activity_list) instanceof ListView) {
            listView = (ListView) findViewById(R.id.activity_list);
        }
        String[] aName = new String[]{"一句话识别(http版)", "一句话识别(WebScoket版)", "语音合成(http版)", "语音合成(webSocket版)", "实时语音识别连续模式"};
        Class[] classes = new Class[]{RasrCsActivity.class, RasrCsActivity.class, RasrCsActivity.class, RasrCsActivity.class, RasrCsActivity.class};
        ArrayList<HashMap<String, Object>> listItems = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < aName.length; i++) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("activity_name", aName[i]);
            item.put("activity_class", classes[i]);
            listItems.add(item);
        }
        // 1.上下文对象 2.数据源是含有Map的一个集合 3.每一个item的布局文件
        // 4.Map对象的哪些key对应value来生成列表项
        // 5.填充的组件 Map对象key对应的资源一依次填充组件 顺序有对应关系
        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                new String[]{"activity_name"}, new int[]{R.id.text_item});
        listView.setAdapter(adapter);
        listView.setDividerHeight(2);
        listView.setOnItemClickListener(this);
        checkAudioRecordingPermission(this, getBaseContext());

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<?, ?> map = (HashMap<?, ?>) parent.getAdapter().getItem(position);
        Class<?> clazz = (Class<?>) map.get("activity_class");
        Intent it = new Intent(this, clazz);
        this.startActivity(it);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 功能描述
     * 检查相关权限
     *
     * @param context  制定上下文
     * @param activity 指定活动
     */
    public void checkAudioRecordingPermission(Activity activity, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int state = ContextCompat.checkSelfPermission(context, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (state != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                activity.requestPermissions(permissions, 321);
            }
            while (true) {
                state = ContextCompat.checkSelfPermission(context, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (state == PackageManager.PERMISSION_GRANTED) {
                    break;
                }
            }
        }
    }
}
