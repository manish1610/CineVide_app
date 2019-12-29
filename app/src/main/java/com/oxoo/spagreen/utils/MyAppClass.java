package com.oxoo.spagreen.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.onesignal.OneSignal;
import com.oxoo.spagreen.Config;
import com.oxoo.spagreen.NotificationClickHandler;
import com.oxoo.spagreen.network.RetrofitClient;
import com.oxoo.spagreen.network.apis.AppConfigApi;
import com.oxoo.spagreen.network.model.AppConfig;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyAppClass extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext=this;

        // OneSignal Initialization
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationClickHandler(mContext))
                //.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        SharedPreferences preferences=getSharedPreferences("push",MODE_PRIVATE);
        if (preferences.getBoolean("status",true)){
            OneSignal.setSubscription(true);
        }else {
            OneSignal.setSubscription(false);
        }

        //

        if (!getFirstTimeOpenStatus()) {
            if (Config.DEFAULT_DARK_THEME_ENABLE) {
                changeSystemDarkMode(true);
            } else {
                changeSystemDarkMode(false);
            }
            saveFirstTimeOpenStatus(true);
        }

        getAppConfigInfo();

    }

    public void changeSystemDarkMode(boolean dark) {

        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("dark", dark);
        editor.apply();

    }

    public void saveFirstTimeOpenStatus(boolean dark) {

        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("firstTimeOpen", true);
        editor.apply();

    }

    public boolean getFirstTimeOpenStatus() {
        SharedPreferences preferences=getSharedPreferences("push",MODE_PRIVATE);
        return preferences.getBoolean("firstTimeOpen",false);
    }

    public static Context getContext(){
        return mContext;
    }

    public void getAppConfigInfo() {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        AppConfigApi appConfigApi = retrofit.create(AppConfigApi.class);
        Call<AppConfig> call = appConfigApi.getAppConfig(Config.API_KEY);
        call.enqueue(new Callback<AppConfig>() {
            @Override
            public void onResponse(Call<AppConfig> call, Response<AppConfig> response) {

                if (response.code() == 200) {

                    AppConfig appConfig = response.body();

                    // save app config info to shared preference
                    saveAppConfigInfo(appConfig);

                }

            }

            @Override
            public void onFailure(Call<AppConfig> call, Throwable t) {
                t.printStackTrace();

                SharedPreferences preferences = getSharedPreferences("appConfig", MODE_PRIVATE);
                Constants.NAV_MENU_STYLE = preferences.getString("navMenuStyle", "grid");
                Constants.IS_ENABLE_PROGRAM_GUIDE = preferences.getBoolean("enableProgramGuide", false);
                Constants.IS_LOGIN_MANDATORY = preferences.getBoolean("loginMandatory", false);
                Constants.IS_GENRE_SHOW = preferences.getBoolean("genreShow", true);
                Constants.IS_COUNTRY_SHOW = preferences.getBoolean("countryShow", true);
            }
        });

    }

    public void saveAppConfigInfo(AppConfig appConfig) {

        Constants.NAV_MENU_STYLE = appConfig.getMenu();
        Constants.IS_ENABLE_PROGRAM_GUIDE = appConfig.isProgramEnable();
        Constants.IS_LOGIN_MANDATORY = appConfig.isLoginMandatory();
        Constants.IS_GENRE_SHOW = appConfig.isGenreVisible();
        Constants.IS_COUNTRY_SHOW = appConfig.isCountryVisible();

        SharedPreferences.Editor editor = getSharedPreferences("appConfig", MODE_PRIVATE).edit();
        editor.putString("navMenuStyle", appConfig.getMenu());
        editor.putBoolean("enableProgramGuide", appConfig.isProgramEnable());
        editor.putBoolean("loginMandatory", appConfig.isLoginMandatory());
        editor.putBoolean("genreShow", appConfig.isGenreVisible());
        editor.putBoolean("countryShow", appConfig.isCountryVisible());
        editor.apply();

    }

}
