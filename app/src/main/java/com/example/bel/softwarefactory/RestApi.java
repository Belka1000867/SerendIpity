package com.example.bel.softwarefactory;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by bel on 30.03.16.
 */

public interface RestApi {

    @POST("upload_file.php")
    Observable<ResponseBody> upload(@Body RequestBody file);
}
