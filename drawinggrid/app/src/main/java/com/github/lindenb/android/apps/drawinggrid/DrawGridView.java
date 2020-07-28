package com.github.lindenb.android.apps.drawinggrid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class DrawGridView extends View {
    private boolean squareGrid=false;
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
        if(imgWidth==0 || imgWidth==0 || panelWidth==0 || panelHeight==0) return null;
        Matrix tr= new Matrix();

        if( isPortrait(panelWidth, panelHeight) != isPortrait(imgWidth,imgHeight)) {
            tr.postTranslate(imgHeight, 0);
            tr.postRotate((float)(Math.PI/2));
            int tmp = imgHeight;
            imgHeight = imgWidth;
            imgWidth = tmp;
        }

        if(imgWidth!= panelWidth || imgHeight!=panelHeight) {
            final double r1 = (double)panelWidth/imgWidth;
            final double r2 = (double)panelHeight/imgHeight;
            final double r = Math.min(r1, r2);
            final Matrix tr2 = new Matrix();
            tr2.postTranslate((float)r, (float)r);
            tr2.postConcat(tr);
            tr=tr2;
            imgWidth = (int)(imgWidth*r);
            imgHeight =(int)(imgHeight*r);
        }

        final int dx=(panelWidth-imgWidth)/2;
        final int dy=(panelHeight-imgHeight)/2;
        final Matrix tr3 = new Matrix();
        tr3.postTranslate(dx,dy);
        ;
        tr3.postConcat(tr);
        tr=tr3;
        return tr;
    }



    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final Paint paint = new Paint();
        paint.setColor(this.bitmap==null?Color.DKGRAY:Color.LTGRAY);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);



        Rect bounds = new Rect(0,0,getWidth(),getHeight());

        boolean needRotate = false;

        if(this.bitmap!=null) {
            Matrix matrix = getMatrix();
            if(matrix!=null) canvas.drawBitmap(this.bitmap,matrix,paint);
        }


        if (numDiv > 0) {
            float dw = bounds.width() / (float) numDiv;
            float dh = bounds.height() / (float) numDiv;
            if (this.squareGrid) {
                dw = Math.min(dw, dh);
                dh = dw;
                }



            int i=1;
            float p = bounds.left  + dw;
            while(p < bounds.right) {
                if(i%2==0) {
                    paint.setStrokeWidth(2f);
                    paint.setColor(Color.RED);
                }
                else
                {
                    paint.setStrokeWidth(1f);
                    paint.setColor(Color.BLUE);
                }
                canvas.drawLine(p, bounds.top,p, bounds.bottom,paint);
                p+= dw;
                i++;
            }

            i=1;
            p= bounds.top + dh;
            while(p < bounds.bottom) {
                if(i%2==0) {
                    paint.setStrokeWidth(2f);
                    paint.setColor(Color.RED);
                }
                else
                {
                    paint.setStrokeWidth(1);
                    paint.setColor(Color.BLUE);
                }
                canvas.drawLine(bounds.left , p, bounds.right,p,paint);
                p+= dh;
                i++;
            }
        }
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
