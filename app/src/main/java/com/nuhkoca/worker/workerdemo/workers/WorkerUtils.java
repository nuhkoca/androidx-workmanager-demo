package com.nuhkoca.worker.workerdemo.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.nuhkoca.worker.workerdemo.Constants;
import com.nuhkoca.worker.workerdemo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

final class WorkerUtils {

    private static final String TAG = WorkerUtils.class.getSimpleName();

    static void makeStatusNotification(@NonNull String message,
                                       @NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME;
            String description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(Constants.CHANNEL_ID, name, importance);

            channel.setDescription(description);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }


            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(Constants.NOTIFICATION_TITLE)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[0]);

            NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, builder.build());
        }
    }

    static void sleep() {
        try {
            Thread.sleep(Constants.DELAY_TIME_MILLIS, 0);
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @WorkerThread
    static Bitmap blurBitmap(@NonNull Bitmap bitmap,
                             @NonNull Context context) {
        RenderScript rsContext = null;

        try {
            Bitmap output = Bitmap.createBitmap(
                    bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

            rsContext = RenderScript.create(context, RenderScript.ContextType.DEBUG);
            Allocation inAlloc = Allocation.createFromBitmap(rsContext, bitmap);
            Allocation outAlloc = Allocation.createTyped(rsContext, inAlloc.getType());
            ScriptIntrinsicBlur theIntrinsic =
                    ScriptIntrinsicBlur.create(rsContext, outAlloc.getElement());
            theIntrinsic.setRadius(10f);
            theIntrinsic.setInput(inAlloc);
            theIntrinsic.forEach(outAlloc);
            outAlloc.copyTo(output);

            return output;
        } finally {
            if (rsContext != null) {
                rsContext.finish();
            }
        }
    }

    static Uri writeBitmapToFile(
            @NonNull Context context,
            @NonNull Bitmap bitmap) throws FileNotFoundException {

        String name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString());
        File outputDir = new File(context.getFilesDir(), Constants.OUTPUT_PATH);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, name);
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return Uri.fromFile(outputFile);
    }

    public WorkerUtils() {
    }
}
