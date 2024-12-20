package com.tunghq.fsocialmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;


public class SignUpActivity extends AppCompatActivity {

    TextView goToLogin;
    EditText username, featuredName, email, password, confirmPass;
    Button signUp;
    ProgressDialog progressDialog;

    Random random;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    ///
    String backgroundImg="https://firebasestorage.googleapis.com/v0/b/fsocial-media-app.appspot.com/o/backgroundd.jpg?alt=media&token=b8358e10-dc68-4549-944b-7ee22ffcbe5a";

    String profileImg="https://firebasestorage.googleapis.com/v0/b/fsocial-media-app.appspot.com/o/profileImage.png?alt=media&token=f21eeb08-f483-4646-88e7-e40a58bb1962";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //init view
        init();

        //init firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");


        //event click
        clickListener();



    }

    private void clickListener() {
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));

            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = username.getText().toString();
                String m = featuredName.getText().toString();
                String e = email.getText().toString();
                String p = password.getText().toString();
                String c = confirmPass.getText().toString();

                //validate
                if (n.isEmpty()) {
                    username.setError("Please enter name...");
                } else if (m.isEmpty()) {
                    featuredName.setError("Please enter featured name...");
                } else if (e.isEmpty()) {
                    email.setError("Please enter email..");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {

                    email.setError("Please enter correct email format(Ex: abc@gmail.com)...");
                } else if (p.isEmpty() ) {
                    password.setError("Please enter password");
                }else if (p.length() < 6) {
                    password.setError("Password length at least 6 characters..");
                } else if (c.isEmpty()) {
                    confirmPass.setError("Please enter confirm password...");
                } else if (!c.equals(p)){
                    confirmPass.setError("Password does not match...");
                }
                else {
                    progressDialog = new ProgressDialog(SignUpActivity.this);
                    progressDialog.setTitle("Registering....");
                    progressDialog.setMessage("Please wait..We're creating account for you a short while");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    //check featured name
                    Query query = reference.orderByChild("featuredName").equalTo("@" + m);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // dataSnapshot is the "issue" node with all children with id 0
                                Toast.makeText(SignUpActivity.this, "Featured name already exists, please enter another featured name", Toast.LENGTH_SHORT).show();
                                featuredName.setText("");
                                progressDialog.dismiss();
                            } else {
                                auth.createUserWithEmailAndPassword(e, p).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            auth = FirebaseAuth.getInstance();
                                            user = auth.getCurrentUser();

                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("userName", n);
                                            map.put("userId", user.getUid());
                                            map.put("featuredName", "@" + m);
                                            map.put("email", e);
                                            map.put("profileImg", profileImg);
                                            map.put("backgroundImg", backgroundImg);
                                            map.put("status", "Hello");


                                            reference.child(user.getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                                                        Toast.makeText(SignUpActivity.this, "Account created!!!", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(SignUpActivity.this, "Something went wrong " + task.getException(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(SignUpActivity.this, "Unable to register.." + task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    private void init() {
        goToLogin = findViewById(R.id.goToLogin);
        username = findViewById(R.id.username);
        featuredName = findViewById(R.id.memer);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPass = findViewById(R.id.confirmPassword);
        signUp = findViewById(R.id.signUp);
        random = new Random();
    }
}
