package com.example.licenseplaterecognize.models;

import android.graphics.Point;

public class Bound {
    private Point left_bottom, right_bottom, left_top, right_top;

    private Bound(Point left_bottom, Point right_bottom, Point left_top, Point right_top) {
        this.left_bottom = left_bottom;
        this.right_bottom = right_bottom;
        this.left_top = left_top;
        this.right_top = right_top;
    }

    public static Bound getComputedBound(Bound raw, float rateCalcToRaw) {
        return new Bound(
                getComputedPoint(raw.left_bottom, rateCalcToRaw),
                getComputedPoint(raw.right_bottom, rateCalcToRaw),
                getComputedPoint(raw.left_top, rateCalcToRaw),
                getComputedPoint(raw.right_top, rateCalcToRaw)
        );
    }

    private static Point getComputedPoint(Point point, float rateCalcToRaw) {
        return new Point((int) (point.x * rateCalcToRaw), (int) (point.y * rateCalcToRaw));
    }

    public Point getLeft_bottom() {
        return left_bottom;
    }

    public void setLeft_bottom(Point left_bottom) {
        this.left_bottom = left_bottom;
    }

    public Point getRight_bottom() {
        return right_bottom;
    }

    public void setRight_bottom(Point right_bottom) {
        this.right_bottom = right_bottom;
    }

    public Point getLeft_top() {
        return left_top;
    }

    public void setLeft_top(Point left_top) {
        this.left_top = left_top;
    }

    public Point getRight_top() {
        return right_top;
    }

    public void setRight_top(Point right_top) {
        this.right_top = right_top;
    }


}
