package com.ming.hellogif.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = null;
        try {
            int width = view.getWidth();
            int height = view.getHeight();
            if (width != 0 && height != 0) {
                //可以改成RGB_565，相比ARGB_8888将节省一半的内存开销
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);

                bitmap = bitmapCompress(bitmap);
            }
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;

    }

    public static Bitmap crateBitmapByView(View view) {
        Bitmap bitmap = null;
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(measureSpec, measureSpec);

        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();

        if (measuredWidth != 0 && measuredHeight != 0) {
            view.layout(0, 0, measuredWidth, measuredHeight);
            bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            try {
                bitmap = bitmapCompress(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static Bitmap bitmapCompress(Bitmap bitmap) throws IOException {
        //采样率压缩
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = true;//true为不加载图片，只有宽高这类的数据,默认是false
        options.inSampleSize = computeSize(bitmap);//采样率  通过调节其inSampleSize参数，比如调节为2，宽高会为原来的1/2，内存变回原来的1/4.
        //质量压缩
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 70, stream);
        bitmap.recycle();

        byte[] bytes = stream.toByteArray();
        stream.close();
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return bitmap;
    }

    private static int computeSize(Bitmap bitmap) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        bitmapWidth = bitmapWidth % 2 == 1 ? bitmapWidth + 1 : bitmapWidth;
        bitmapHeight = bitmapHeight % 2 == 1 ? bitmapHeight + 1 : bitmapHeight;

        int longSide = Math.max(bitmapWidth, bitmapHeight);
        int shortSide = Math.min(bitmapWidth, bitmapHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            }
            if (longSide < 4990) {
                return 2;
            }
            if (longSide > 4990 && longSide < 10240) {
                return 4;
            }
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        }
        if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        }
        return (int) Math.ceil(longSide / (1280.0 / scale));
    }

    public static boolean compare2BitmapIsEqual(Bitmap bmp1, Bitmap bmp2) {
        int iteration = 0;
        int width = bmp1.getWidth();
        int height = bmp1.getHeight();
        if (width != bmp2.getWidth()) {
            return false;
        }
        if (height != bmp2.getHeight()) {
            return false;
        }
        iteration = Math.min(width, height);
        for (int i = 0; i < iteration; ++i) {
            if (bmp1.getPixel(i, i) != bmp2.getPixel(i, i)) {
                return false;
            }
        }
        return true;
    }

    private static Bitmap addWatermark(Bitmap src, Bitmap watermark) {
        if (src == null || watermark == null) {
            return src;
        }

        int sWid = src.getWidth();
        int sHei = src.getHeight();
        int wWid = watermark.getWidth();
        int wHei = watermark.getHeight();
        if (sWid == 0 || sHei == 0) {
            return null;
        }

        if (sWid < wWid || sHei < wHei) {
            return src;
        }

        Bitmap bitmap = Bitmap.createBitmap(sWid, sHei, Bitmap.Config.ARGB_8888);
        try {
            Canvas cv = new Canvas(bitmap);
            cv.drawBitmap(src, 0, 0, null);
            cv.drawBitmap(watermark, sWid - wWid - 5, sHei - wHei - 5, null);
            //cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }

    public static InputStream Bitmap2InputStream(Bitmap bm, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    /**
     * Android利用BitMap获得图片像素数据的方法
     *
     * @param bitmap
     * @param x
     * @param y
     */
    public static int[] getPixel(Bitmap bitmap, int x, int y) {
        int color = bitmap.getPixel(x, y);

        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return new int[]{a, r, g, b};
    }

    /**
     * 缩放图片
     *
     * @param source
     * @param sx
     * @param sy
     * @return
     */
    public static Bitmap scale(Bitmap source, float sx, float sy) {
        Matrix matrix = new Matrix();
        matrix.setScale(sx, sy);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * 保存图片
     * @param context
     * @param bitmap
     * @param fileName
     * @return
     */
    public static boolean saveBitmap(Context context, Bitmap bitmap, String fileName) {
        try {
            File imageFile = getFileDir(context, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 70, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @NonNull
    public static File getFileDir(Context context, String name) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!externalFilesDir.exists()) {
            boolean isSuccess = externalFilesDir.mkdirs();
            if (!isSuccess) {
                externalFilesDir.mkdirs();
            }
        }
        return new File(externalFilesDir, name);
    }
}
