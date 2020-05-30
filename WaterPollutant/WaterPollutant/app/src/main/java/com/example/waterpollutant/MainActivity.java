package com.example.waterpollutant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    TextView tempreture,ph_value,salinityTextView;
    Button pureWater;
    DatabaseReference reference;
    DatabaseReference ph_value_reference;
    DatabaseReference is_motor_reference;
    DatabaseReference salinity;
    private CustomGauge gauge1;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();

        reference          = firebaseDatabase.getReference("tem");
        ph_value_reference = firebaseDatabase.getReference("ph_value");
        is_motor_reference = firebaseDatabase.getReference("is_motor_active");
        salinity           = firebaseDatabase.getReference("salinity");

        gauge1 = findViewById(R.id.gauge1);
        tempreture = findViewById(R.id.tempreture);
        ph_value = findViewById(R.id.ph_value);
        pureWater = findViewById(R.id.pure_water);
        salinityTextView = findViewById(R.id.salinity);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tempreture.setText("Tempreture: "+dataSnapshot.getValue().toString()+" C");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        ph_value_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String phValue = dataSnapshot.getValue().toString();
                ph_value.setText(dataSnapshot.getValue().toString());
                if (phValue.equals("7")){
                    gauge1.setPointStartColor(Color.parseColor("#00FF00"));
                }else {
                    gauge1.setPointStartColor(Color.parseColor("#ff0000"));
                }
                gauge1.setValue(Integer.parseInt(dataSnapshot.getValue().toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        salinity.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                salinityTextView.setText("Salinity:" +dataSnapshot.getValue().toString() );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        logout();
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onStart() {
        super.onStart();

        pureWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                is_motor_reference.setValue(1);

            }
        });


    }

    private void logout(){
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor  editor = mPreferences.edit();
        editor.putBoolean(getString(R.string.isLogged),false);

        editor.apply();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();

    }
}
