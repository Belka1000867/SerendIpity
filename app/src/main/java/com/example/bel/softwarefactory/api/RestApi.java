package com.example.bel.softwarefactory.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface RestApi {

    @POST("upload_file.php")
    Observable<ResponseBody> upload(@Body RequestBody file);
}
