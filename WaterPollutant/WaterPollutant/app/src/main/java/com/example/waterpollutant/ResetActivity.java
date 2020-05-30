package com.example.waterpollutant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;

import static eu.inmite.android.lib.validations.form.annotations.RegExp.EMAIL;
import static java.util.Objects.requireNonNull;

public class ResetActivity extends AppCompatActivity implements View.OnClickListener {

    @NotEmpty(messageId = R.string.null_validation_message)
    @RegExp(value = EMAIL,messageId = R.string.email_validation_message)
    private EditText resetPassword;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        initUI();
        firebaseAuth = FirebaseAuth.getInstance();
    }

   private void initUI(){

        resetPassword = findViewById(R.id.email);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (validate()){
            String email = resetPassword.getText().toString();
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Email has been sent to you!",Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                       String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean validate(){
        return FormValidator.validate(this, new SimpleErrorPopupCallback(this));
    }

}
