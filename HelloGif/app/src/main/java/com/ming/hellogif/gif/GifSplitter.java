package com.ming.hellogif.gif;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.ming.hellogif.gif.decoder.GifHeader;
import com.ming.hellogif.gif.decoder.GifHeaderParser;
import com.ming.hellogif.gif.decoder.StandardGifDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Gif拆分
 */
public class GifSplitter {

    public GifSplitter() {

    }

    /**
     * 将gif分解成多张Bitmap
     *
     * @param inputStream
     * @throws IOException
     */
    public List<Bitmap> decoderGifToBitmaps(InputStream inputStream) throws IOException {
        List<Bitmap> bitmapList = new ArrayList<>();

        byte[] data = isToBytes(inputStream);

        MockProvider provider = new MockProvider();
        GifHeaderParser headerParser = new GifHeaderParser();
        headerParser.setData(data);
        GifHeader header = headerParser.parseHeader();

        StandardGifDecoder decoder = new StandardGifDecoder(provider);
        decoder.setData(header, data);
        //帧数
        int frameCount = decoder.getFrameCount();
        for (int i = 0; i < frameCount; i++) {
            decoder.advance();
            Bitmap nextFrame = decoder.getNextFrame();
            bitmapList.add(nextFrame);
        }
        return bitmapList;
    }

    private static byte[] isToBytes(@NonNull InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        try {
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        } finally {
            is.close();
        }
        return os.toByteArray();
    }

    /**
     * 私有实现获取Bitmap
     */
    private static class MockProvider implements StandardGifDecoder.BitmapProvider {

        @NonNull
        @Override
        public Bitmap obtain(int width, int height, @NonNull Bitmap.Config config) {

            return Bitmap.createBitmap(width, height, config);
        }

        @Override
        public void release(@NonNull Bitmap bitmap) {
            // Do nothing.
        }

        @NonNull
        @Override
        public byte[] obtainByteArray(int size) {
            return new byte[size];
        }

        @Override
        public void release(@NonNull byte[] bytes) {
            // Do nothing.
        }

        @NonNull
        @Override
        public int[] obtainIntArray(int size) {
            return new int[size];
        }

        @Override
        public void release(@NonNull int[] array) {
            // Do Nothing
        }
    }
}