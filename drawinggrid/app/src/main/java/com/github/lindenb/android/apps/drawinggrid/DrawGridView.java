package com.github.lindenb.android.apps.drawinggrid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class DrawGridView extends View {
    private final static String TAG="DrawGridView";
    private boolean squareGrid=true;
    private int numDiv = 8;
    private Bitmap bitmap = null;

    public DrawGridView(final Context ctx, android.util.AttributeSet attributeSet) {
        super(ctx,attributeSet);
    }

    private boolean isPortrait(final int w,final int h) {
        return h>w;
    }

    private Matrix getTransform() {
        if(bitmap==null) return null;
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        int imgWidth = this.bitmap.getWidth();
        int imgHeight = this.bitmap.getHeight();
        if(imgWidth==0 || imgHeight==0 || panelWidth==0 || panelHeight==0) return null;
        final Matrix tr= new Matrix();


        if( isPortrait(panelWidth, panelHeight) != isPortrait(imgWidth,imgHeight)) {
            tr.postTranslate(-imgWidth/2f,-imgHeight/2f);
            tr.postRotate(90);
            tr.postTranslate(imgHeight/2f,imgWidth/2f);
            int tmp = imgHeight;
            imgHeight = imgWidth;
            imgWidth = tmp;
            }

        if(imgWidth!= panelWidth || imgHeight!=panelHeight) {
            final double r1 = (double)panelWidth/imgWidth;
            final double r2 = (double)panelHeight/imgHeight;
            final double r = Math.min(r1, r2);
            tr.postScale((float)r, (float)r);
            imgWidth = (int)(imgWidth*r);
            imgHeight =(int)(imgHeight*r);
        }

        final int dx=(panelWidth-imgWidth)/2;
        final int dy=(panelHeight-imgHeight)/2;
        tr.postTranslate(dx,dy);
        return tr;
    }



    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final Paint paint = new Paint();
        paint.setColor(this.bitmap==null?Color.DKGRAY:Color.LTGRAY);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        Matrix matrix = getTransform();
        if(matrix==null) return;

        if(this.bitmap!=null) {
            canvas.drawBitmap(this.bitmap,matrix,paint);
        }


        if (numDiv > 0) {
            final int old_alpha = paint.getAlpha();
            paint.setAlpha(160);


            RectF rect= new RectF(0f,0f,bitmap.getWidth(),bitmap.getHeight());
            matrix.mapRect(rect);

            float dw = rect.width() / (float) numDiv;
            float dh = rect.width() / (float) numDiv;
            if (this.squareGrid) {
                dw = Math.min(dw, dh);
                dh = dw;
                }



            int i=1;
            float p = rect.left+dw;
            while(p < rect.right) {
                if(i%2==0) {
                    paint.setStrokeWidth(1f);
                    paint.setColor(Color.RED);
                }
                else
                {
                    paint.setStrokeWidth(1f);
                    paint.setColor(Color.BLUE);
                }
                canvas.drawLine(p, rect.top,p,rect.bottom,paint);
                p+= dw;
                i++;
            }

            i=1;
            p= rect.top + dh;
            while(p < rect.bottom) {
                if(i%2==0) {
                    paint.setStrokeWidth(1f);
                    paint.setColor(Color.RED);
                }
                else
                {
                    paint.setStrokeWidth(1f);
                    paint.setColor(Color.BLUE);
                }
                canvas.drawLine(rect.left, p, rect.right,p,paint);
                p+= dh;
                i++;
            }
            paint.setAlpha(old_alpha);
        }
    }

    int getNumDiv() {
        return numDiv;
    }

    void setNumDiv(int numDiv) {
        if(numDiv==this.numDiv) return;
        this.numDiv = numDiv;
        invalidate();
    }

    void setBitmap(final Bitmap bitmap) {
        if(this.bitmap==null && bitmap==null) return;
        this.bitmap = bitmap;
        invalidate();
    }

    void setSquareGrid(boolean squareGrid) {
        if(this.squareGrid == squareGrid) return;
        this.squareGrid = squareGrid;
        invalidate();
    }
}
