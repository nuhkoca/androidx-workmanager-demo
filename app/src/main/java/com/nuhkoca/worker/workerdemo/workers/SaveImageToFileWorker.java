package com.nuhkoca.worker.workerdemo.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.nuhkoca.worker.workerdemo.Constants;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.work.Data;
import androidx.work.Worker;

public class SaveImageToFileWorker extends Worker {

    private static final String TAG = SaveImageToFileWorker.class.getSimpleName();

    private static final String TITLE = "Blurred Image";
    private static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault());

    @NonNull
    @Override
    public WorkerResult doWork() {
        Context context = getApplicationContext();


        WorkerUtils.makeStatusNotification("Saving image", context);
        WorkerUtils.sleep();

        ContentResolver contentResolver = context.getContentResolver();

        try {
            String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI, null);
            Bitmap bitmap = BitmapFactory.decodeStream(
                    contentResolver.openInputStream(Uri.parse(resourceUri)));

            String imageUrl = MediaStore.Images.Media.insertImage(
                    contentResolver, bitmap, TITLE, DATE_FORMATTER.format(new Date()));

            if (TextUtils.isEmpty(imageUrl)) {
                Log.e(TAG, "Writing to MediaStore failed");
                return WorkerResult.FAILURE;
            }

            Data output = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, imageUrl).build();

            setOutputData(output);
            return WorkerResult.SUCCESS;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return WorkerResult.FAILURE;
        }
    }
}
