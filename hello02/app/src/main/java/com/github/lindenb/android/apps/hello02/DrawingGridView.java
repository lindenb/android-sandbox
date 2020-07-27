package com.github.lindenb.android.apps.hello02;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawingGridView extends View {
    DrawingGridView(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawRect(10,10,100,150,paint);
        paint.setColor(Color.RED);
        canvas.drawLine(15,15,100,150,paint);
    }
}
