package com.tunghq.fsocialmobileapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;


public class SignInActivity extends AppCompatActivity {
    Button sign;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    TextView goToSignUp, forgotPass;

    EditText email,password;
    Button login;
    ProgressDialog progressDialog;

    FirebaseAuth auth;
    FirebaseUser user;

    String url="https://firebasestorage.googleapis.com/v0/b/fsocial-media-app.appspot.com/o/backgroundd.jpg?alt=media&token=b8358e10-dc68-4549-944b-7ee22ffcbe5a";
    String profile="https://firebasestorage.googleapis.com/v0/b/fsocial-media-app.appspot.com/o/profileImage.png?alt=media&token=f21eeb08-f483-4646-88e7-e40a58bb1962";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        FirebaseApp.initializeApp(SignInActivity.this);

        //init view
        init();

        //init firebase
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        //configure google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        //event click
        clickListener();


    }
    private void clickListener(){
        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));

            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e=email.getText().toString();
                String p=password.getText().toString();

                if (e.isEmpty())
                {
                    email.setError("Please enter email..");
                }else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()){
                    email.setError("Please enter correct email format(Ex: abc@gmail.com)...");
                }else if (p.isEmpty())
                {
                    password.setError("Please enter password..");
                } else if (p.length()<6) {
                    password.setError("Password length at least 6 characters..");
                } else
                {
                    progressDialog=new ProgressDialog(SignInActivity.this);
                    progressDialog.setTitle("Logging");
                    progressDialog.setMessage("Please wait..");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    auth.signInWithEmailAndPassword(e,p).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                startActivity(new Intent(SignInActivity.this,HomeActivity.class));
                                Toast.makeText(SignInActivity.this, "Login success...", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                finish();
                            }else
                            {
                                Toast.makeText(SignInActivity.this, "Account not registered", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });
    }
    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //set layout linearlayout
        LinearLayout linearLayout = new LinearLayout(this);

        //views to set in dialog
        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //set a min width of a EditView to fit a text of n 'M' letters regardless of the actual text extension and text size
        emailEt.setMinEms(10);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //input email
                String email = emailEt.getText().toString().trim();
                beginRecover(email);


            }
        });
        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();

            }
        });
        //show dialog
        builder.create().show();
    }
    private void beginRecover(String email) {
        progressDialog.setMessage("Sending Email....");
        progressDialog.show();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Toast.makeText(SignInActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(SignInActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //get and show proper error message
                        progressDialog.dismiss();
                        Toast.makeText(SignInActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Random rand = new Random();
        int upperBound = 1000;
        String random = String.valueOf(rand.nextInt(upperBound));
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //sign in success
                            FirebaseUser user = auth.getCurrentUser();


                            //if user is signing in first time then get and show user info from google account
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){


                                //When user is registered store user info in firebase realtime dtb too
                                //using HashMap
                                HashMap<Object, String> hashMap = new HashMap<>();

                                hashMap.put("userName", "newbie");//will add later (e.g. edit profile)
                                hashMap.put("userId", user.getUid());
                                hashMap.put("email",user.getEmail());
                                hashMap.put("featuredName", "@newbie"+random);
                                hashMap.put("profileImg", profile);
                                hashMap.put("status", "Hello");
                                hashMap.put("backgroundImg", url);


                                //firebase dtb instance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();

                                //path to store user data named "User"
                                DatabaseReference reference = database.getReference().child("Users");
                                //put data with hashmap in database
                                reference.child(user.getUid()).setValue(hashMap);

                            }

                            //show email in toast
                            Toast.makeText(SignInActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();

                            //go to profile atv after logged in
                            startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                            finish();
                            //updateUI(user);
                        }else{
                            Toast.makeText(SignInActivity.this, "Login Failed...", Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignInActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //result returned from launching the Intent from GoogleSignIn Api.getSignInIntent
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //was successful
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                //Failed
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void init()
    {
        sign=findViewById(R.id.signInGoogle);
        goToSignUp=findViewById(R.id.goToSignUp);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        forgotPass = findViewById(R.id.forgotPass);
        progressDialog = new ProgressDialog(this);

    }
}