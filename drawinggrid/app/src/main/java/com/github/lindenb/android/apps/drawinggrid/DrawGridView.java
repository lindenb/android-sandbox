package com.github.lindenb.android.apps.drawinggrid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawGridView extends View {

    DrawGridView(final Context ctx) {
        super(ctx);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawRect(10,10,100,150,paint);
        paint.setColor(Color.RED);
        canvas.drawLine(15,15,100,150,paint);
    }

}
