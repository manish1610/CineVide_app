package com.oxoo.spagreen.network.apis;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ProfileApi {

    @POST("update_profile")
    @Multipart
    Call<ResponseBody> updateProfile(@Part("id") RequestBody id, @Part("name") RequestBody name, @Part("email") RequestBody email,
                                     @Part("password") RequestBody password, @Part("api_secret_key") RequestBody key,
                                     @Part MultipartBody.Part file);

}
