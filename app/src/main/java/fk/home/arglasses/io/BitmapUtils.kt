package fk.home.arglasses.io

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import fk.home.arglasses.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
fun persistBitmap(context: Context, bitmap: Bitmap): Uri {

    //  String path = Environment.getExternalStorageDirectory().toString() +  "/Pictures/Screenshots/";
    val videoDirectory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + "/Screenshots"
    )

    val c: Calendar = Calendar.getInstance()
    val df = SimpleDateFormat("yyyy-MM-dd HH.mm.ss")
    val formattedDate: String = df.format(c.getTime())
    val mediaFile = File(videoDirectory, "FieldVisualizer$formattedDate.jpeg")
    val fileOutputStream = FileOutputStream(mediaFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream)
    fileOutputStream.flush()
    fileOutputStream.close()
    return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID +".provider",
        mediaFile)
}