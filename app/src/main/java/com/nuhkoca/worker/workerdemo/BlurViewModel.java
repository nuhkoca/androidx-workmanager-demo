package com.nuhkoca.worker.workerdemo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.text.TextUtils;

import com.nuhkoca.worker.workerdemo.workers.BlurWorker;
import com.nuhkoca.worker.workerdemo.workers.CleanupWorker;
import com.nuhkoca.worker.workerdemo.workers.SaveImageToFileWorker;

import java.util.List;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

public class BlurViewModel extends ViewModel {

    private WorkManager mWorkManager;
    private Uri mImageUri;
    private Uri mOutputUri;
    private LiveData<List<WorkStatus>> mSavedWorkStatus;

    public BlurViewModel() {
        mWorkManager = WorkManager.getInstance();

        mSavedWorkStatus = mWorkManager.getStatusesByTag(Constants.TAG_OUTPUT);
    }

    void applyBlur(int blurLevel) {
        WorkContinuation workContinuation = mWorkManager
                .beginUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanupWorker.class));

        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder builder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            if (i == 0) {
                builder.setInputData(createInputDataForUri());
            }

            workContinuation = workContinuation.then(builder.build());
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .build();

        OneTimeWorkRequest save = new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                .setConstraints(constraints)
                .addTag(Constants.TAG_OUTPUT)
                .build();

        workContinuation = workContinuation.then(save);

        workContinuation.enqueue();
    }

    public void cancelWork() {
        mWorkManager.cancelUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME);
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(Constants.KEY_IMAGE_URI, mImageUri.toString());
        }

        return builder.build();
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    public void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    public void setOutputUri(String uri) {
        mOutputUri = uriOrNull(uri);
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public Uri getOutputUri() {
        return mOutputUri;
    }

    public LiveData<List<WorkStatus>> getOutputStatus() {
        return mSavedWorkStatus;
    }
}