package com.nuhkoca.worker.workerdemo.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.nuhkoca.worker.workerdemo.Constants;

import java.io.FileNotFoundException;

import androidx.work.Data;
import androidx.work.Worker;

public class BlurWorker extends Worker {

    private static final String TAG = BlurWorker.class.getSimpleName();

    @NonNull
    @Override
    public WorkerResult doWork() {

        Context context = getApplicationContext();

        WorkerUtils.makeStatusNotification("Blurring image", context);
        WorkerUtils.sleep();

        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI, null);
        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }

            ContentResolver contentResolver = getApplicationContext().getContentResolver();

            Bitmap bitmap = BitmapFactory.decodeStream(
                    contentResolver.openInputStream(Uri.parse(resourceUri)));

            Bitmap output = WorkerUtils.blurBitmap(bitmap, context);

            Uri outputUri = WorkerUtils.writeBitmapToFile(context, output);

            setOutputData(new Data.Builder().putString(
                    Constants.KEY_IMAGE_URI, outputUri.toString()).build());

            return WorkerResult.SUCCESS;

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to decode input stream", e);
            throw new RuntimeException("Failed to decode input stream", e);
        } catch (Throwable throwable) {
            Log.e(TAG, "Error applying blur", throwable);
            return WorkerResult.FAILURE;
        }
    }
}
