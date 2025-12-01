package com.example.driverapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CourseResponse {
    @SerializedName("success")
    public boolean success;
    
    @SerializedName("course")
    public Course course;
    
    @SerializedName("courses")
    public List<Course> courses;
    
    @SerializedName("error")
    public String error;
    
    public static class Course {
        @SerializedName("id")
        public int id;
        
        @SerializedName("point_embarquement")
        public String pointEmbarquement;
        
        @SerializedName("destination")
        public String destination;
        
        @SerializedName("motif")
        public String motif;
        
        @SerializedName("date_souhaitee")
        public String dateSouhaitee;
        
        @SerializedName("statut")
        public String statut;
        
        @SerializedName("demandeur")
        public Demandeur demandeur;
        
        @SerializedName("vehicule")
        public Vehicule vehicule;
        
        @SerializedName("date_validation")
        public String dateValidation;
        
        @SerializedName("distance_parcourue")
        public double distanceParcourue;
        
        public static class Demandeur {
            @SerializedName("id")
            public int id;
            
            @SerializedName("username")
            public String username;
            
            @SerializedName("first_name")
            public String firstName;
            
            @SerializedName("last_name")
            public String lastName;
        }
        
        public static class Vehicule {
            @SerializedName("id")
            public int id;
            
            @SerializedName("immatriculation")
            public String immatriculation;
            
            @SerializedName("marque")
            public String marque;
            
            @SerializedName("modele")
            public String modele;
        }
    }
}
