package com.sergeybutorin.quester.network;

import com.sergeybutorin.quester.model.LoginRequest;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;
import com.sergeybutorin.quester.model.SignupRequest;
import com.sergeybutorin.quester.model.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

interface Api {
    @POST("user/login")
    Call<UserProfile> login(@Body LoginRequest request);

    @POST("user/signup")
    Call<UserProfile> signup(@Body SignupRequest request);

    @POST("quest/new")
    Call<Quest> newQuest(@Header("User-Token") String userToken, @Body Quest quest);

    @POST("quest/get_all")
    Call<List<QuestBase>> getAllQuests();

    @POST("quest/{id}/details")
    Call<Quest> getQuestDetails(@Path("id") int questId);
}
