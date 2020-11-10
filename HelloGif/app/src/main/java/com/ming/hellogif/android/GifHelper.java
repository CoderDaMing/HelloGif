package com.ming.hellogif.android;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * android包下Gif帮助实现
 * @author: ming
 * @date: 2020/9/21 16:01
 * version:1.0
 */
public class GifHelper {
    private static final String TAG = "GifHelper";

    private GifEncoder gifEncoder;
    private GifDecoder gifDecoder;

    public GifHelper() {
        this.gifEncoder = new GifEncoder();
        this.gifDecoder = new GifDecoder();
    }

    /**
     * Bitmap制作Gif
     */
    public boolean makeGif(List<Bitmap> bitmaps, String outputGifName) {
        File gifFile = new File(outputGifName);
        if (!gifFile.exists())
            try {
                gifFile.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        OutputStream os;
        try {
            os = new FileOutputStream(gifFile);
            gifEncoder.start(os);  //注意顺序
            gifEncoder.setDelay(100 * bitmaps.size());
            gifEncoder.setFrameRate(10);
            gifEncoder.setRepeat(0);
            gifEncoder.setTransparent(Color.TRANSPARENT);
            for (Bitmap bitmap : bitmaps) {
                gifEncoder.addFrame(bitmap);
            }
            boolean isOK = gifEncoder.finish();
            Log.d(TAG, "makeGif()  isOK = [" + isOK + "]");
            return isOK;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gif获取Bitmap
     */
    public List<Bitmap> getBitmapsFormGif(InputStream fis) {
        List<Bitmap> bitmaps = new ArrayList<>();
        try {
            int read = gifDecoder.read(fis);
            if (read == GifDecoder.STATUS_OK) {
                int frameCount = gifDecoder.getFrameCount();
                for (int i = 0; i < frameCount; i++) {
                    Bitmap frame = gifDecoder.getFrame(i);
                    bitmaps.add(frame);
                }
            }
            gifDecoder.complete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmaps;
    }
}
