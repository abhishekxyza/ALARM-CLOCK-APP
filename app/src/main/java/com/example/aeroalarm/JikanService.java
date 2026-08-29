package com.example.aeroalarm;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JikanService {
    @GET("random/characters")
    Call<JikanResponse> getRandomCharacter();
}
