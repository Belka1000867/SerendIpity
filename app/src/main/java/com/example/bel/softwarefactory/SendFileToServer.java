package com.example.bel.softwarefactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.bel.softwarefactory.api.Api;
import com.trello.rxlifecycle.components.RxActivity;

import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Send file to server
 *
 */
public class SendFileToServer extends RxActivity implements ProgressRequestBody.UploadCallbacks{

    private static final String TAG = "Debug_SendToServer";

    private ProgressDialog progressDialog;
    private Context context;

    public SendFileToServer(Context context){
        this.context = context;
    }

    /* private methods */

    public void uploadFile(File file, String owner, String latitude, String longitude) {
        Log.d(TAG, "uploadFile()");

        ProgressRequestBody requestFile = new ProgressRequestBody(file, this);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), requestFile)
                .addFormDataPart("owner", owner)
                .addFormDataPart("latitude", latitude)
                .addFormDataPart("longitude", longitude)
                .build();

        Log.d(TAG, "Owner : " + owner);
        Log.d(TAG, "Latitude : " + latitude);
        Log.d(TAG, "Longitude : " + longitude);

        Api api = new Api();
        api.upload(requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .doOnError(this::handleError)
                .subscribe(this::uploadFinished, this::handleError);
    }

    private void uploadFinished(ResponseBody responseBody) {
        Log.d(TAG, "uploadFinished()");

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        try {
            final String finalResponse = responseBody.string();
            Log.d(TAG, finalResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleError(Throwable throwable) {
        Log.e(TAG, throwable.getMessage());
    }

    //1
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProgressUpdate(int percentage) {
        Log.d(TAG, "onProgressUpdate()");
        if (progressDialog != null) {
            progressDialog.setProgress(percentage);
        } else {
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(percentage);
            progressDialog.show();
        }
    }

}
