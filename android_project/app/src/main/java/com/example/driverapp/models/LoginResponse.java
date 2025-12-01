package com.example.driverapp.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    public boolean success;
    
    @SerializedName("token")
    public String token;
    
    @SerializedName("user")
    public User user;
    
    @SerializedName("userType")
    public String userType;
    
    @SerializedName("error")
    public String error;
    
    public static class User {
        @SerializedName("id")
        public int id;
        
        @SerializedName("username")
        public String username;
        
        @SerializedName("email")
        public String email;
        
        @SerializedName("first_name")
        public String firstName;
        
        @SerializedName("last_name")
        public String lastName;
        
        @SerializedName("role")
        public String role;
        
        @SerializedName("is_driver")
        public boolean isDriver;
        
        @SerializedName("userType")
        public String userType;
    }
}
