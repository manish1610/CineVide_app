package com.oxoo.spagreen.network.apis;

import com.oxoo.spagreen.network.model.LiveTvCategory;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LiveTvApi {

    @GET("get_all_tv_channel_by_category")
    Call<List<LiveTvCategory>> getLiveTvCategories(@Query("api_secret_key") String key);

}
