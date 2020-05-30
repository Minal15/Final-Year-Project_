package com.example.hp.planter;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class Firebase {

    public static DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference();
    public static DatabaseReference motor  = mDatabase.child("is_motor_active");
    public static DatabaseReference mTemperature = mDatabase.child("tem");
    public static DatabaseReference ph_sensor  = mDatabase.child("ph_value");
    public static DatabaseReference salinity  = mDatabase.child("salinity");
    public static double ph_lower_limit;
    public static double ph_upper_limit;


    public static void setmTemperature(long b) {
        mTemperature.setValue(b);
    }

    public static boolean setPh_sensor(List<String> b) {
        if(b.isEmpty()){
            ph_sensor.child("value").setValue("false");

            ph_sensor.child(ph_sensor.push().getKey()).setValue("false");
            return false;
        }else {
            for (String value:b) {
                ph_sensor.child(ph_sensor.push().getKey()).setValue(value);
                return true;
            }
        }
        return false;
    }

    public static boolean setPh_sensor(double b) {
        if(b == 0){
            ph_sensor.child("value").setValue("false");
            return false;
        }else {

            ph_sensor.child("value").setValue(b);
                return true;
        }
    }

    public static void ph_lower_limit(){


        DatabaseReference ph_lower = mDatabase.child("ph_lower_limit");
        ph_lower.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ph_lower_limit = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static void ph_upper_limit(){

        //final int[] lower_limit = {(int) 0.1};
        DatabaseReference ph_lower = mDatabase.child("ph_upper_limit");
        ph_lower.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ph_upper_limit = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static void setph_status(Double s){

        DatabaseReference ph_status = mDatabase.child("ph_status");
        ph_status.setValue(s);
    }

    public static void setSalinity(long salinity) {
        Firebase.salinity.setValue(salinity);
    }
}
