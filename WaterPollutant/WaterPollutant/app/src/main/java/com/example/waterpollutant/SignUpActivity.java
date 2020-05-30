package com.example.waterpollutant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.MinLength;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;

import static eu.inmite.android.lib.validations.form.annotations.RegExp.EMAIL;

public class SignUpActivity extends AppCompatActivity {



    @NotEmpty(messageId = R.string.sigup_validation_message)
    private EditText firstNameEditText ;

    @NotEmpty(messageId = R.string.sigup_validation_message)
    private EditText lastNameEditText  ;

    @NotEmpty(messageId = R.string.sigup_validation_message)
    private EditText confirmPasswordEditText;

    @NotEmpty(messageId = R.string.sigup_validation_message)
    @RegExp(value = EMAIL,messageId = R.string.email_validation_message)
    private EditText emailEditText;

    @MinLength(value =8 , messageId = R.string.validation_password_length, order = 2)
    @NotEmpty(messageId = R.string.sigup_validation_message)
    private EditText passwordEdiText;

    private Button signUpBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        initViewComponents();
        initFirebaseObjectsAndReferences();



        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



//                if (firstNameEditText.getText().toString().trim().equals("")) {
//
//
//                    firstNameEditText.setError("please enter first name");
//
//                }
//                if (lastNameEditText.getText().toString().trim().equals("")) {
//
//
//                    lastNameEditText.setError("please enter last name");
//
//                }
//                if (passwordEdiText.getText().toString().trim().equals("")) {
//
//
//                    passwordEdiText.setError("please enter password");
//
//                }
//                if (confirmPasswordEditText.getText().toString().trim().equals("")) {
//
//
//                    confirmPasswordEditText.setError("Confirm password field is required");
//
//                }
//                if (emailEditText.getText().toString().trim().equals("")) {
//
//
//                    emailEditText.setError("email field is required");
//
//                }

                if (validate()){

                    if (!firstNameEditText.getText().toString().trim().equals("") && !lastNameEditText.getText().toString().trim().equals("") && !passwordEdiText.getText().toString().trim().equals("") && !confirmPasswordEditText.getText().toString().trim().equals("") && !emailEditText.getText().toString().trim().equals("") ) {
                        if (passwordEdiText.getText().toString().trim().equals(confirmPasswordEditText.getText().toString().trim())) {

                            String email = emailEditText.getText().toString().trim();
                            signupWithCredentials(email,passwordEdiText.getText().toString().trim());


                        }

                        else {

                            confirmPasswordEditText.setError("password did not match");
                        }
                    }
                }




            }
        });


    }

    private boolean validate(){

        return FormValidator.validate(this, new SimpleErrorPopupCallback(this));
    }



    private void initViewComponents(){
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.lastname_edit_text);
        passwordEdiText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        signUpBtn = findViewById(R.id.sign_up_btn);
        progressBar = findViewById(R.id.progress_bar);

    }

    private void initFirebaseObjectsAndReferences(){


        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

    }


    private void signupWithCredentials(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            ProfileModel profileModel = new ProfileModel(firstNameEditText.getText().toString().trim(),lastNameEditText.getText().toString().trim(),emailEditText.getText().toString().trim());

                            String userId = mAuth.getCurrentUser().getUid();

                             firebaseDatabase.getReference("users").child(userId).setValue(profileModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {

                                     Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                     progressBar.setVisibility(View.GONE);

                                     Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                     startActivity(intent);

                                 }
                             });

                        }
                        else {

                            Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                        }

                    }

                });

    }

}
