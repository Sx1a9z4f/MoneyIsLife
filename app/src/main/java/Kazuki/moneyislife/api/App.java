package Kazuki.moneyislife.api;

import android.annotation.SuppressLint;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import Kazuki.moneyislife.BuildConfig;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressLint("Registered")
public class App extends Application {

    private static final String PREFS_NAME = "shared_prefs";
    private static final String KEY_TOKEN = "auth_token";

    private static final String TAG = "App";

    private Api api;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(
                BuildConfig.DEBUG
                        ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.BODY
        );


        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new AuthInterceptor())
                .build();


        Gson gson = new GsonBuilder()
                .create();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://loftschool.com/android-api/basic/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();


        api = retrofit.create(Api.class);
    }

    public Api getApi() {
        return api;
    }


    public void saveAuthToken(String token) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public String getAuthToken() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    public boolean isAuthorized() {
        return !TextUtils.isEmpty(getAuthToken());
    }

    private class AuthInterceptor implements Interceptor {

        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            HttpUrl url = request.url();
            HttpUrl.Builder urlBuilder = url.newBuilder();
            HttpUrl newUrl = urlBuilder.addQueryParameter("auth-token", getAuthToken()).build();
            Request.Builder requestBuilder = request.newBuilder();
            Request newRequest = requestBuilder.url(newUrl).build();
            return chain.proceed(newRequest);
        }
    }
}
