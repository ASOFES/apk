package com.example.driverapp;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.Map;

public interface ApiService {
    
    @FormUrlEncoded
    @POST("login-api/")
    Call<LoginResponse> login(
        @Field("username") String username,
        @Field("password") String password
    );
    
    @GET("api/driver/assigned-course/")
    Call<CourseResponse> getAssignedCourse(
        @Header("Authorization") String token
    );
    
    @GET("api/driver/course-history-real/")
    Call<CourseHistoryResponse> getCourseHistory(
        @Header("Authorization") String token
    );
    
    @POST("api/driver/course/{course_id}/start/")
    Call<ApiResponse> startCourse(
        @Header("Authorization") String token,
        @Path("course_id") int courseId,
        @Field("kilometrage") String kilometrage
    );
    
    @POST("api/driver/course/{course_id}/complete/")
    Call<ApiResponse> completeCourse(
        @Header("Authorization") String token,
        @Path("course_id") int courseId,
        @Field("kilometrage") String kilometrage
    );
}
