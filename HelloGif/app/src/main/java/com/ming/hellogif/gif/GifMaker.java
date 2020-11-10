package com.ming.hellogif.gif;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;

import androidx.annotation.DrawableRes;

import com.ming.hellogif.gif.encoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GifMaker {

    private OnGifListener mGifListener = null;

    public GifMaker() {

    }

    public boolean makeGif(List<Bitmap> source, String outputPath) throws IOException {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //注意顺序
        encoder.start(bos);
        encoder.setDelay(100);
        encoder.setFrameRate(10);
        encoder.setRepeat(0);
        encoder.setTransparent(Color.TRANSPARENT);
        final int length = source.size();
        for (int i = 0; i < length; i++) {
            Bitmap bmp = source.get(i);
            if (bmp == null) {
                continue;
            }
            //再次进行了缩放
            Bitmap thumb = ThumbnailUtils.extractThumbnail(bmp, bmp.getWidth(), bmp.getHeight(), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            try {
                encoder.addFrame(thumb);
                if (mGifListener != null) {
                    mGifListener.onMake(i, length);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.gc();
                break;
            }
            //TODO how about releasing bitmap after addFrame
        }
        encoder.finish();
        source.clear();
        byte[] data = bos.toByteArray();

        File file = new File(outputPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(data);
        fileOutputStream.flush();
        fileOutputStream.close();
        return file.exists();
    }

    public boolean makeGifFromPath(List<String> sourcePathList, String outputPath) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        final int length = sourcePathList.size();
        for (int i = 0; i < length; i++) {
            bitmaps.add(BitmapFactory.decodeFile(sourcePathList.get(i)));
        }
        return makeGif(bitmaps, outputPath);
    }

    public boolean makeGifFromFile(List<File> sourceFileList, String outputPath) throws IOException {
        List<String> pathArray = new ArrayList<String>();
        final int length = sourceFileList.size();
        for (int i = 0; i < length; i++) {
            pathArray.add(sourceFileList.get(i).getAbsolutePath());
        }
        return makeGifFromPath(pathArray, outputPath);
    }

    public boolean makeGif(Resources resources, @DrawableRes int[] sourceDrawableId, String outputPath) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        for (int i = 0; i < sourceDrawableId.length; i++) {
            bitmaps.add(BitmapFactory.decodeResource(resources, sourceDrawableId[i]));
        }
        return makeGif(bitmaps, outputPath);
    }

    public boolean makeGifFromVideo(String videoPath, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        return makeGifWithMediaMetadataRetriever(retriever, startMillSeconds, endMillSeconds, periodMillSeconds, outputPath);
    }

    public boolean makeGifFromVideo(Context context, Uri uri, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        return makeGifWithMediaMetadataRetriever(retriever, startMillSeconds, endMillSeconds, periodMillSeconds, outputPath);
    }

    private boolean makeGifWithMediaMetadataRetriever(MediaMetadataRetriever retriever, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        if (startMillSeconds < 0 || endMillSeconds <= 0 || periodMillSeconds <= 0 || endMillSeconds <= startMillSeconds) {
            throw new IllegalArgumentException("startMillSeconds may < 0 or endMillSeconds or periodMillSeconds may <= 0, or endMillSeconds <= startMillSeconds");
        }
        try {
            List<Bitmap> bitmaps = new ArrayList<Bitmap>();
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long duration = Long.parseLong(durationStr);
            long minDuration = Math.min(duration, endMillSeconds);
            for (long time = startMillSeconds; time < minDuration; time += periodMillSeconds) {
                bitmaps.add(retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST));
            }
            retriever.release();
            return makeGif(bitmaps, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setOnGifListener(OnGifListener listener) {
        mGifListener = listener;
    }

    public interface OnGifListener {
        public void onMake(int current, int total);
    }
}