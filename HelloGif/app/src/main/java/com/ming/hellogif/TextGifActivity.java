package com.ming.hellogif;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ming.hellogif.gif.BitmapUtil;
import com.ming.hellogif.gif.GifMaker;
import com.ming.hellogif.util.GifTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TextGifActivity extends AppCompatActivity {
    private static final String TAG = TextGifActivity.class.getSimpleName();

    private static final int NEED_BITMAP_SIZE = 10;

    private final GifTimer gifTimer = new GifTimer();
    private ScheduledFuture<?> scheduledFuture = null;
    private final List<Bitmap> bitmapList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_gif);
        //开启动画，改变字体颜色
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
        valueAnimator.addUpdateListener(animation -> changeTvColor());
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(-1);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.start();

        //点击摄取10张Bitmap,生成Gif
        findViewById(R.id.btn_make_gif).setOnClickListener(view -> {
            takeAndMakeGif();
        });
    }

    private void changeTvColor() {
        TextView textContent = findViewById(R.id.textContent);
        textContent.setTextColor(Color.rgb(
                new Random().nextInt(255),
                new Random().nextInt(255),
                new Random().nextInt(255)));
    }

    private void takeAndMakeGif() {
        if (scheduledFuture == null) {
            bitmapList.clear();
            scheduledFuture = gifTimer.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    takeBitmap();
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    private void takeBitmap() {
        Bitmap bitmap = BitmapUtil.getBitmapFromView(findViewById(R.id.fl_text_layout));
        bitmapList.add(bitmap);
        if (bitmapList.size() == NEED_BITMAP_SIZE) {
            GifMaker gifMaker = new GifMaker();
            try {
                boolean makeGifResult = gifMaker.makeGif(bitmapList, BitmapUtil.getFileDir(TextGifActivity.this, "textColor.gif").getAbsolutePath());
                Log.d(TAG, "takeBitmap() called makeGifResult:" + makeGifResult);
                clearGif();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "takeBitmap() called bitmapList.size():" + bitmapList.size());
        }
        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(),
                "生成文件在sdcard ->Android->data->com.ming.hellogif->files->Pictures下",
                Toast.LENGTH_SHORT
        ).show());
    }

    private void clearGif() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (gifTimer != null) {
            gifTimer.cancel();
        }
        if (bitmapList != null) {
            bitmapList.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearGif();
    }
}
