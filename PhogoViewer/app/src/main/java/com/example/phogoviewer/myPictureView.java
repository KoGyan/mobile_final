package com.example.phogoviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class myPictureView extends View {
    private Bitmap bitmap;

    public myPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }
}
