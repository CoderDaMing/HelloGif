package com.ming.hellogif

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ming.hellogif.gif.BitmapUtil
import com.ming.hellogif.gif.GifMaker
import com.ming.hellogif.gif.GifSplitter
import com.ming.hellogif.util.GifTimer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * 生成文件在sdcard ->Android->data->com.ming.hellogif->files->Pictures下
 */
class MainActivity : AppCompatActivity() {
    private val gifTimer: GifTimer = GifTimer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageGif = findViewById<ImageView>(R.id.imageGif)

        val btnGetImage = findViewById<Button>(R.id.btnGetImage)
        btnGetImage.setOnClickListener {
            gifTimer.execute {
                decoderGif()
            }
        }

        val btnGetGifFromImage = findViewById<Button>(R.id.btnGetGifFromImage)
        btnGetGifFromImage.setOnClickListener {
            gifTimer.execute {
                encoderGifFromImages()
            }
        }

        val btnGetGifFromVideo = findViewById<Button>(R.id.btnGetGifFromVideo)
        btnGetGifFromVideo.setOnClickListener {
            gifTimer.execute {
                encoderGifFromVideo()
            }
        }
    }

    private fun decoderGif() {
        val gifSplitter = GifSplitter()
        try {
            val inputStream: InputStream
            //1.资源文件读取InputStream
            inputStream = resources.assets.open("dance.gif")
            //2.File读取InputStream
//                FileInputStream(BitmapUtil.getFileDir(this, "dance.gif").absolutePath)
            //3.分解成bitmapList
            val bitmapList: List<Bitmap> = gifSplitter.decoderGifToBitmaps(inputStream)
            val size = bitmapList.size
            for (i in 0 until size) {
                val bitmap = bitmapList[i]
                //遍历存储到file
                BitmapUtil.saveBitmap(this, bitmap, "dance$i.webp")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "生成文件在sdcard ->Android->data->com.ming.hellogif->files->Pictures下",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun encoderGifFromImages() {
        val gifMaker = GifMaker()
        gifMaker.setOnGifListener { current, total ->
            Log.d(TAG, "onMake() called with: current = [$current], total = [$total]")
        }
        try {
            //使用资源图片生成
            val makeGif = gifMaker.makeGif(
                resources, intArrayOf(R.drawable.dance1, R.drawable.dance2, R.drawable.dance3),
                BitmapUtil.getFileDir(this, "dance.gif").absolutePath
            )
            Log.d(TAG, "encoderGifFromImages Result = [$makeGif]")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "生成文件在sdcard ->Android->data->com.ming.hellogif->files->Pictures下",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun encoderGifFromVideo() {
        val gifMaker = GifMaker()
        gifMaker.setOnGifListener { current, total ->
            Log.d(TAG, "onMake() called with: current = [$current], total = [$total]")
        }
        try {
            //使用视频提取生成
            val videoPath = BitmapUtil.getFileDir(this, "dance_girl.mp4").absolutePath
            val file = File(videoPath)
            if (!file.exists()) {
                //将assets内视频拷到file
                val inputStream = assets.open("dance_girl.mp4")
                copyToSd(inputStream, videoPath)
            }
            val uri = Uri.fromFile(File(videoPath))
            val makeGifFromVideo = gifMaker.makeGifFromVideo(
                this,
                uri,
                1000,
                6000,
                1000,
                BitmapUtil.getFileDir(this, "dance_girl.gif").absolutePath
            )
            Log.d(TAG, "makeGifFromVideo Result = [$makeGifFromVideo]")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "生成文件在sdcard ->Android->data->com.ming.hellogif->files->Pictures下",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun copyToSd(inputStream: InputStream, newPath: String) {
        val fos = FileOutputStream(File(newPath))
        val buffer = ByteArray(1024)
        var byteCount = 0
        while (inputStream.read(buffer)
                .also { byteCount = it } != -1
        ) { //循环从输入流读取 buffer字节
            fos.write(buffer, 0, byteCount) //将读取的输入流写入到输出流  
        }
        fos.flush() //刷新缓冲区
        inputStream.close()
        fos.close()
    }

    companion object {
        const val TAG: String = "MainActivity"
    }
}