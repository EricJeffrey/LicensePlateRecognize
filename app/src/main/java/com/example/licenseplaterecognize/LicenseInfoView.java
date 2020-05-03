package com.example.licenseplaterecognize;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.licenseplaterecognize.models.LicenseInfo;

import java.util.ArrayList;

public class LicenseInfoView extends LinearLayout {
    public static final ArrayList<String> PlateColorTypeToStr = new ArrayList<String>() {{
        add("蓝牌");
        add("黄牌");
        add("黑牌");
        add("白牌");
        add("绿牌");
        add("小型新能源牌");
        add("大型新能源牌");
        add("未识别车牌");
    }};

    private TextView indexView;
    private TextView licenseView;
    private TextView plateTypeView;

    public LicenseInfoView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_license_info, this);
        indexView = findViewById(R.id.view_tv_index);
        licenseView = findViewById(R.id.view_tv_license_str);
        plateTypeView = findViewById(R.id.view_tv_plate_type);
    }

    public LicenseInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_license_info, this);

        indexView = findViewById(R.id.view_tv_index);
        licenseView = findViewById(R.id.view_tv_license_str);
        plateTypeView = findViewById(R.id.view_tv_plate_type);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LicenseInfoView);
        licenseView.setText(array.getText(R.styleable.LicenseInfoView_licenseText));
        final int defValue = PlateColorTypeToStr.size() - 1;
        plateTypeView.setText(PlateColorTypeToStr.get(array.getInteger(R.styleable.LicenseInfoView_plateType, defValue)));
        array.recycle();
    }

    public void update(int index, LicenseInfo info) {
        indexView.setText(String.valueOf(index));
        licenseView.setText(info.license_plate_number);
        plateTypeView.setText(PlateColorTypeToStr.get(info.color));
    }

}
