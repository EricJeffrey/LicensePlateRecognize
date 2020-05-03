package com.example.licenseplaterecognize;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;

import com.example.licenseplaterecognize.models.Bound;
import com.example.licenseplaterecognize.models.LicenseInfo;

import java.util.ArrayList;


public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {
    private ArrayList<Bound> boundsToDraw;
    private Paint p;

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.RED);
        p.setStrokeWidth(8);
        p.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (boundsToDraw != null) {
            for (int i = 0; i < boundsToDraw.size(); i++) {
                Bound tmpBound = boundsToDraw.get(i);
                Point lb = tmpBound.getLeft_bottom(), lt = tmpBound.getLeft_top();
                Point rb = tmpBound.getRight_bottom(), rt = tmpBound.getRight_top();
                canvas.drawLine(lb.x, lb.y, lt.x, lt.y, p);
                canvas.drawLine(lb.x, lb.y, rb.x, rb.y, p);
                canvas.drawLine(rt.x, rt.y, lt.x, lt.y, p);
                canvas.drawLine(rt.x, rt.y, rb.x, rb.y, p);
            }
        }
    }

    public void setBoundsToDraw(ArrayList<Bound> boundsToDraw) {
        this.boundsToDraw = boundsToDraw;
    }

    public void clearBounds() {
        this.boundsToDraw = null;
    }
}
