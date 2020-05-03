package com.example.licenseplaterecognize.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.licenseplaterecognize.MainActivity;
import com.example.licenseplaterecognize.models.RecognizeResInfo;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.ContentValues.TAG;

public class RecognizeCallBack implements Callback {
    private Handler handler;

    public RecognizeCallBack(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Message msg = new Message();
        msg.what = MainActivity.MSG_WHAT_RECOGNIZE_FAIL;
        handler.sendMessage(msg);
        e.printStackTrace();
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) {
        ResponseBody body = response.body();
        Message msg = new Message();
        if (body == null) {
            msg.what = MainActivity.MSG_WHAT_RECOGNIZE_FAIL;
        } else {
            try {
                Gson gson = new Gson();
                String resJson = body.string();
                Log.e(TAG, "onResponse: json response: " + resJson);
                RecognizeResInfo info = gson.fromJson(resJson, RecognizeResInfo.class);
                if (info != null && info.results != null && info.results.size() > 0) {
                    msg.what = MainActivity.MSG_WHAT_RECOGNIZE_SUCCESS;
                    msg.obj = info;
                } else if (info != null) {
                    // 列表为空
                    msg.what = MainActivity.MSG_WHAT_RECOGNIZE_NO_RES;
                } else {
                    msg.what = MainActivity.MSG_WHAT_RECOGNIZE_FAIL;
                }
            } catch (Exception e) {
                msg.what = MainActivity.MSG_WHAT_RECOGNIZE_FAIL;
                e.printStackTrace();
            }
        }
        handler.sendMessage(msg);
    }
}
