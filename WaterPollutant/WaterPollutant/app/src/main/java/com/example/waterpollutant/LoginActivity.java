package com.example.waterpollutant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.prefs.Preferences;

import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.MinLength;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;

import static eu.inmite.android.lib.validations.form.annotations.RegExp.EMAIL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signUpBtn;
    private Button signInBtn;

    @MinLength(value =8 , messageId = R.string.validation_password_length, order = 2)
    @NotEmpty(messageId = R.string.null_validation_message)
    private EditText passwordEditText;

    @NotEmpty(messageId = R.string.null_validation_message)
    @RegExp(value = EMAIL,messageId = R.string.email_validation_message)
    private EditText emailEditText;

    private ProgressBar progressBar;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private TextView forgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        initViewComponents();
        initFirebaseObjectsAndReferences();


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                startActivity(new Intent(LoginActivity.this,SignUpActivity.class));


            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (emailEditText.getText().toString().trim().equals("")) {


//                    emailEditText.setError("email field is required");

                }
                if (passwordEditText.getText().toString().trim().equals("")) {


//                    passwordEditText.setError("password cannot be empty");

                }

                if (validate()){

                    if (!emailEditText.getText().toString().trim().equals("") && !passwordEditText.getText().toString().trim().equals("")){


                        signInWithCredentials(emailEditText.getText().toString().trim(),passwordEditText.getText().toString().trim());

                    }
                }



            }
        });

    }

    private boolean validate(){

        return FormValidator.validate(this, new SimpleErrorPopupCallback(this));
    }

    private void initViewComponents(){

        passwordEditText = findViewById(R.id.password_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        signInBtn = findViewById(R.id.sign_in_btn);
        progressBar = findViewById(R.id.progress_bar);
        signUpBtn = findViewById(R.id.sign_up_btn);
        findViewById(R.id.forget_password_text_view).setOnClickListener(this);

    }


    private void initFirebaseObjectsAndReferences(){

        mAuth = FirebaseAuth.getInstance();

    }

    private void signInWithCredentials(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            editor.putBoolean(getString(R.string.isLogged),true);
                            editor.commit();
                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            editor.putBoolean("isLogged",false);
                            editor.commit();
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });


    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.forget_password_text_view){
            Intent intent = new Intent(this,ResetActivity.class);
            startActivity(intent);
        }
    }
}
