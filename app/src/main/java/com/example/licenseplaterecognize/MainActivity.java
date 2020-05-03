package com.example.licenseplaterecognize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licenseplaterecognize.models.Bound;
import com.example.licenseplaterecognize.models.LicenseInfo;
import com.example.licenseplaterecognize.models.RecognizeResInfo;
import com.example.licenseplaterecognize.utils.CameraUtil;
import com.example.licenseplaterecognize.utils.PathUtil;
import com.example.licenseplaterecognize.utils.RecognizeCallBack;
import com.google.gson.Gson;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    public static final int MSG_WHAT_RECOGNIZE_FAIL = 11;
    public static final int MSG_WHAT_RECOGNIZE_SUCCESS = 12;
    public static final int MSG_WHAT_RECOGNIZE_NO_RES = 13;

    private static final int REQ_CODE_PERMISSION_ON_START = 1001;
    private static final int REQ_CODE_PERMISSION_DO_RECOGNIZE = 1002;
    private static final int REQ_CODE_IMG_PICKER = 101;

    private static final String STR_PERMISSION_DENY = "存储权限未授权，识别功能将受影响";
    private static final String STR_RECOGNIZE_ERROR = "识别出错，请检查网络连接";
    private static final String STR_RECOGNIZE_SUCCESS = "识别成功";
    private static final String STR_RECOGNIZE_NO_RES = "未识别到车牌";
    private static final String STR_NO_PICTURE_SELECTED = "尚未选择图片";

    private static final String TAG = "MainActivity";

    private static final String secret = "your_api_secret";
    private static final String key = "your_api_key";

    private ImageInfo imageInfo = new ImageInfo();

    private LinearLayout progressBarHolder;
    private TextView textViewHint;
    private MyImageView imageViewSelected;
    private LinearLayout recognizeResHolder;
    private ArrayList<View> resViewList = new ArrayList<>();

    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            return handleMsg(msg);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 申请权限
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(permission);
        }
        int sz = permissionsToRequest.size();
        if (sz > 0) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[sz]), REQ_CODE_PERMISSION_ON_START);
        }

        Button btnDoRecognize = findViewById(R.id.btn_do_recognize);
        progressBarHolder = findViewById(R.id.progressbar_holder);
        textViewHint = findViewById(R.id.text_view_hint);
        imageViewSelected = findViewById(R.id.image_view_selected);
        recognizeResHolder = findViewById(R.id.ll_res_holder);

        // 申请权限 - 启动识别
        btnDoRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_CODE_PERMISSION_DO_RECOGNIZE);
                } else {
                    doRecognize();
                }
            }
        });
    }

    // 启动Matisse选择图片
    private void startSelectImage() {
        Matisse.from(MainActivity.this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(1)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .imageEngine(new GlideEngine())
                .thumbnailScale(0.85f)
                .forResult(REQ_CODE_IMG_PICKER);
    }

    // 检查图片信息是否正常然后上传
    private void doRecognize() {
        if (imageInfo.isOk()) {
            progressBarHolder.setVisibility(View.VISIBLE);
            startRecognize();
        } else {
            Toast.makeText(this, STR_NO_PICTURE_SELECTED, Toast.LENGTH_SHORT).show();
        }
    }

    // 执行网络请求，发送图片
    private void startRecognize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int timeoutSec = 20;
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(timeoutSec, TimeUnit.SECONDS)
                        .readTimeout(timeoutSec, TimeUnit.SECONDS)
                        .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                        .callTimeout(timeoutSec, TimeUnit.SECONDS)
                        .build();

                byte[] bytes = CameraUtil.compressImage2ByteArray(imageInfo.path);
                RequestBody image = RequestBody.create(bytes, MediaType.parse("image/jpg"));
                // RequestBody image = RequestBody.create(new File(imageInfo.path), MediaType.parse("image/jpg"));
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image_file", imageInfo.path, image)
                        .addFormDataPart("api_key", key)
                        .addFormDataPart("api_secret", secret)
                        .build();
                Request request = new Request.Builder()
                        .url("https://api-cn.faceplusplus.com/imagepp/v1/licenseplate")
                        .post(requestBody)
                        .build();
                RecognizeCallBack callBack = new RecognizeCallBack(handler);
                Call call = client.newCall(request);
                Response response;
                try {
                    response = call.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    callBack.onFailure(call, e);
                    return;
                }
                callBack.onResponse(call, response);
//                client.newCall(request).enqueue(new RecognizeCallBack(handler));
            }
        }).start();
    }

    // Matisse选择图片返回后，隐藏文章，显示图片
    private void onImageSelectDone(Intent data) {
        List<Uri> tmpUriList = Matisse.obtainResult(data);
        List<String> tmpPathList = Matisse.obtainPathResult(data);
        if (tmpUriList.size() > 0) {
            imageInfo.update(tmpUriList.get(0), tmpPathList.get(0));
            textViewHint.setVisibility(View.GONE);
            imageViewSelected.setImageURI(imageInfo.uri);
            removeOldRes();
        }
//        Log.d(TAG, "Uris: " + Matisse.obtainResult(data));
//        Log.d(TAG, "Paths: " + Matisse.obtainPathResult(data));
//        Log.e(TAG, "Use the selected photos with original: " + Matisse.obtainOriginalState(data));
    }

    private void onCameraDown(CameraUtil.CameraResultType cameraResult) {
        if (!cameraResult.ok) {
            Toast.makeText(this, STR_NO_PICTURE_SELECTED, Toast.LENGTH_SHORT).show();
        } else {
            if (cameraResult.useUri) {
                imageInfo.update(cameraResult.uri, PathUtil.getPath(MainActivity.this, cameraResult.uri));
                imageViewSelected.setImageURI(cameraResult.uri);
            } else {
                imageInfo.update(null, cameraResult.path);
                imageViewSelected.setImageBitmap(BitmapFactory.decodeFile(cameraResult.path));
            }
            textViewHint.setVisibility(View.GONE);
            removeOldRes();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_IMG_PICKER && resultCode == RESULT_OK && data != null)
            onImageSelectDone(data);
        if (requestCode == CameraUtil.REQUEST_CODE_CAMERA) {
            CameraUtil.CameraResultType result = CameraUtil.onResult(resultCode);
            onCameraDown(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_img:
                startSelectImage();
                break;
            case R.id.menu_camera:
                CameraUtil.openCamera(MainActivity.this);
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE_PERMISSION_ON_START:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, STR_PERMISSION_DENY, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQ_CODE_PERMISSION_DO_RECOGNIZE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doRecognize();
                } else {
                    Toast.makeText(MainActivity.this, STR_PERMISSION_DENY, Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    // 处理其它线程的消息
    private boolean handleMsg(Message msg) {
        switch (msg.what) {
            case MSG_WHAT_RECOGNIZE_FAIL:
                Toast.makeText(MainActivity.this, STR_RECOGNIZE_ERROR, Toast.LENGTH_SHORT).show();
                break;
            case MSG_WHAT_RECOGNIZE_SUCCESS:
                updateRes(msg.obj);
                break;
            case MSG_WHAT_RECOGNIZE_NO_RES:
                Toast.makeText(this, STR_RECOGNIZE_NO_RES, Toast.LENGTH_SHORT).show();
                break;
        }
        progressBarHolder.setVisibility(View.GONE);
        return true;
    }

    // 移除上一次的结果
    private void removeOldRes() {
        for (View view : resViewList)
            recognizeResHolder.removeView(view);
        imageViewSelected.clearBounds();
    }

    // 识别成功后展示结果
    private void updateRes(Object obj) {
        ArrayList<LicenseInfo> infoList = ((RecognizeResInfo) obj).results;
        for (View view : resViewList)
            recognizeResHolder.removeView(view);

        // 计算缩放比例
        final float rateCalcToRaw = imageViewSelected.getWidth() / (float) imageInfo.width;
        ArrayList<Bound> boundsToDraw = new ArrayList<>();

        int index = 1;
        for (LicenseInfo info : infoList) {
            LicenseInfoView view = new LicenseInfoView(MainActivity.this);
            boundsToDraw.add(Bound.getComputedBound(info.bound, rateCalcToRaw));
            view.update(index, info);
            resViewList.add(view);
            recognizeResHolder.addView(view);
            index++;
        }
        imageViewSelected.setBoundsToDraw(boundsToDraw);
        imageViewSelected.invalidate();

        Toast.makeText(this, STR_RECOGNIZE_SUCCESS, Toast.LENGTH_SHORT).show();
    }

    // 保存图片信息的类
    static class ImageInfo {
        int width, height;
        Uri uri;
        String path;

        boolean isOk() {
            return uri != null || path != null;
        }

        void update(Uri uri, String path) {
            this.uri = uri;
            this.path = path;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(path).getAbsolutePath(), options);
            height = options.outHeight;
            width = options.outWidth;
        }
    }
}