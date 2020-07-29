package com.github.lindenb.android.apps.cropimage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private final static int PICK_IMAGE = 101;
    private static final String DEBUG_TAG = "MainActivity";


    public static class DrawingArea extends View {
        private Bitmap bitmap = null;
        final PointF drags[] = new PointF[]{new PointF(),new PointF()};
        PointF prevMouse = null;
        int selectedDragIndex = -1;

        public DrawingArea(final Context ctx, android.util.AttributeSet attributeSet) {
            super(ctx, attributeSet);
        }

        private RectF getSelectRectangle() {
            return new RectF(
                    Math.min(drags[0].x,drags[1].x),
                    Math.min(drags[0].y,drags[1].y),
                    Math.max(drags[0].x,drags[1].x),
                    Math.max(drags[0].y,drags[1].y)
                    )
                    ;
        }

        private double distance(float x1,float y1,float x2,float y2) {
            return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
        }


        int findDragIndexAt(float x2, float y2) {
            int best_idx = -1;
            double best_d = -1;
            for (int i = 0; i < drags.length; i++) {
                final PointF drag = drags[i];
                final double d = distance(drag.x,drag.y, x2, y2);
                if (d > 50) continue;
                if (best_idx < 0 || d < best_d) {
                    best_idx = i;
                    best_d = d;
                }
            }
            return best_idx;
        }


        @Override
        protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);
            final Paint paint = new Paint();
            paint.setColor(this.bitmap == null ? Color.DKGRAY : Color.LTGRAY);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

            if (this.bitmap == null) return;
            final Matrix matrix = getTransform();
            canvas.drawBitmap(this.bitmap, matrix, paint);

            final Xfermode oldMode = paint.getXfermode();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
            paint.setColor(Color.YELLOW);
            paint.setStrokeWidth(3.f);


            canvas.drawLine(drags[0].x,drags[0].y,drags[1].x,drags[0].y, paint);
            canvas.drawLine(drags[1].x,drags[0].y,drags[1].x,drags[1].y, paint);
            canvas.drawLine(drags[1].x,drags[1].y,drags[0].x,drags[1].y, paint);
            canvas.drawLine(drags[0].x,drags[1].y,drags[0].x,drags[0].y, paint);


            paint.setStrokeWidth(3.f);
            canvas.drawCircle(drags[0].x,drags[0].y,8,paint);
            canvas.drawCircle(drags[1].x,drags[1].y,8,paint);

            paint.setXfermode(oldMode);
        }

        private Matrix getTransform() {
            if (this.bitmap == null) return null;
            int panelWidth = this.getWidth();
            int panelHeight = this.getHeight();
            int imgWidth = this.bitmap.getWidth();
            int imgHeight = this.bitmap.getHeight();
            if (imgWidth == 0 || imgHeight == 0 || panelWidth == 0 || panelHeight == 0) return null;
            final Matrix tr = new Matrix();
            if (imgWidth != panelWidth || imgHeight != panelHeight) {
                final double r1 = (double) panelWidth / imgWidth;
                final double r2 = (double) panelHeight / imgHeight;
                final double r = Math.min(r1, r2);
                tr.postScale((float) r, (float) r);
                imgWidth = (int) (imgWidth * r);
                imgHeight = (int) (imgHeight * r);
            }

            final int dx = (panelWidth - imgWidth) / 2;
            final int dy = (panelHeight - imgHeight) / 2;
            tr.postTranslate(dx, dy);
            return tr;
        }

        void setBitMap(Bitmap bitmap) {
            this.bitmap = bitmap;
            invalidate();
            if (this.bitmap == null) return;
            final Matrix matrix = getTransform();
            RectF rect = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
            matrix.mapRect(rect);
            this.drags[0].x = rect.left;
            this.drags[0].y = rect.top;
            this.drags[1].x = rect.right;
            this.drags[1].y = rect.bottom;

        }


        Bitmap createCropped() {
            if(this.bitmap==null) return null;
            final Matrix tr = getTransform();
            final  Matrix inverseCopy = new Matrix();
            if(tr.invert(inverseCopy)){
                final RectF rect = getSelectRectangle();
                if(inverseCopy.mapRect(rect)) {
                    final int x = (int) Math.min(Math.max(0, rect.left), bitmap.getWidth());
                    final int y = (int) Math.min(Math.max(0, rect.top), bitmap.getHeight());
                    final int w = (int) Math.min(Math.max(0, rect.right - x), bitmap.getWidth()-x);
                    final int h = (int) Math.min(Math.max(0, rect.bottom - y), bitmap.getWidth()-y);
                    if(w<=0 || h<=0) return null;
                    return Bitmap.createBitmap(this.bitmap, x, y, w, h);
                    }
                }

            return null;
            }




        @Override
        public boolean onTouchEvent(final MotionEvent event) {
            final Matrix matrix = getTransform();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(matrix==null) break;
                    this. prevMouse = null;
                    this.selectedDragIndex = this.findDragIndexAt(event.getX(), event.getY());
                    if (this.selectedDragIndex == -1) {

                        final RectF rect = new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
                        if(matrix.mapRect(rect) && rect.contains(event.getX(), event.getY())) {
                            this.prevMouse = new PointF(event.getX(), event.getY());
                        }
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (matrix == null) break;
                    final RectF rect = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
                    matrix.mapRect(rect);

                    if (selectedDragIndex != -1) {
                        drags[selectedDragIndex].x = Math.max(rect.left, Math.min(rect.right, event.getX()));
                        drags[selectedDragIndex].y = Math.max(rect.top, Math.min(rect.bottom, event.getY()));
                        invalidate();
                    } else if (prevMouse != null) {
                        final double dx = event.getX() - prevMouse.x;
                        final double dy = event.getY() - prevMouse.y;
                        boolean ok = true;
                        for (PointF drag : this.drags) {
                            if (drag.x + dx < rect.left ||
                                    drag.x + dx > rect.right ||
                                    drag.y + dy < rect.top ||
                                    drag.y + dy > rect.bottom) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            for (PointF drag : this.drags) {
                                drag.x += dx;
                                drag.y += dy;
                            }
                        }

                        prevMouse = new PointF(event.getX(), event.getY());
                        invalidate();
                    }


                    return true;
                }
                case MotionEvent.ACTION_UP:
                    selectedDragIndex = -1;
                    prevMouse = null;
                    invalidate();
                    return true;
                default:
                    break;
            }
            return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void  openImage(final View view) {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
    public void  cropImage(final View view) {
        if(getDrawingArea().bitmap==null) return;
        final Bitmap bm = rotateBitMap(getDrawingArea().createCropped());
        if(bm==null) return;
        getDrawingArea().setBitMap(bm);
        alert("Image cropped",this);
    }

    public void  saveImage(final View view) {
        if(getDrawingArea().bitmap==null) return;
        FileOutputStream fOut = null;
        try {
            // Assume block needs to be inside a Try/Catch block.
            String path = Environment.getExternalStorageDirectory().toString();
            int counter = 0;
            File file = null;
            final int counter_max = 10000;
            while(counter < counter_max) {
               file =  new File(path, "Cropped."+counter+".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                counter++;
                if(!file.exists()) break;
            }
            if(file==null || counter==counter_max) return;
            fOut = new FileOutputStream(file);

            getDrawingArea().bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
            fOut=null;
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
            alert("Image Saved "+file.getName(),this);
        } catch(final Throwable err) {
            Log.d(DEBUG_TAG,"Canot save",err);
        }
        finally {
        if(fOut!=null) try {
            fOut.close();} catch (IOException err2) {}
         }
    }

    private void alert(final String msg,final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Message");
        final AlertDialog d=builder.setMessage(msg).create();
        d.show();
    }

    private boolean isPortrait(float w,float h) {
        return h>w;
    }

    private Bitmap rotateBitMap(Bitmap bitmap) {
        if(bitmap==null) return null;
        final int screen_width = getDrawingArea().getWidth();
        final int screen_height = getDrawingArea().getHeight();
        if(isPortrait(screen_width,screen_height)!=isPortrait(bitmap.getWidth(),bitmap.getHeight())) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        else
        {
            return bitmap;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE ) {
                Log.d(DEBUG_TAG,"PickImage");
                final Uri pickedImage = data.getData();
                if(pickedImage!=null) {

                    Log.d(DEBUG_TAG,"PickImage is " + pickedImage);

                    InputStream is =null;
                    try
                    {
                        is = getContentResolver().openInputStream(pickedImage);
                        if(is!=null) {
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            is.close();
                            is=null;
                            Log.d(DEBUG_TAG,"Update bit map:"+(bitmap==null?"null":"ok"));
                            bitmap = rotateBitMap(bitmap);
                            getDrawingArea().setBitMap(bitmap);
                        }
                    }
                    catch(Throwable err ) {
                        Log.d(DEBUG_TAG,"cannot open",err);
                    }
                    finally
                    {
                        if(is!=null)  try{is.close();}catch(Throwable err2) {}
                    }
                }


            }
        }
    }
    private DrawingArea getDrawingArea() {
        return (DrawingArea)findViewById(R.id.drawingArea);
    }

}