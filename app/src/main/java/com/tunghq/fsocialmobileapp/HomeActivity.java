package com.tunghq.fsocialmobileapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.tunghq.fsocialmobileapp.Fragments.ChatListFragment;
import com.tunghq.fsocialmobileapp.Fragments.FavouriteFragment;
import com.tunghq.fsocialmobileapp.Fragments.FeedFragment;
import com.tunghq.fsocialmobileapp.Fragments.FollowingFragment;
import com.tunghq.fsocialmobileapp.Fragments.GroupListFragment;
import com.tunghq.fsocialmobileapp.Fragments.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;

    FirebaseAuth auth;
    FirebaseUser user;

    CircleImageView profile_image;
    FirebaseAuth firebaseAuth;
    String myUid;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        frameLayout = findViewById(R.id.frameLayout);
        bottomNavigationView=findViewById(R.id.bottom_nav);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        myUid = user.getUid();





        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.following) {
                    FollowingFragment followingFragment = new FollowingFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, followingFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.feed) {
                    FeedFragment feedFragment = new FeedFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, feedFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.fav) {
                    FavouriteFragment favouriteFragment = new FavouriteFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, favouriteFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.profile) {
                    UserFragment userFragment = new UserFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, userFragment);
                    fragmentTransaction.commit();
                    SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUid());
                    editor.apply();
                } else if (id == R.id.chatlist) {
                    showMoreChats();
//                    ChatListFragment chatListFragment = new ChatListFragment();
//                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                    fragmentTransaction.replace(R.id.frameLayout, chatListFragment);
//                    fragmentTransaction.commit();

                }
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.following);



    }

    private void showMoreChats() {
        pd = new ProgressDialog(this);

        String options[] = {"Chat List", "Groups Chat"};
        //alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    Toast.makeText(HomeActivity.this, "Chat List", Toast.LENGTH_SHORT).show();

                    ChatListFragment chatListFragment = new ChatListFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, chatListFragment);
                    fragmentTransaction.commit();
                } else if (which == 1) {

                    Toast.makeText(HomeActivity.this, "Groups Chat", Toast.LENGTH_SHORT).show();
                    GroupListFragment groupsChatFragment = new GroupListFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, groupsChatFragment);
                    fragmentTransaction.commit();
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",status);
        //update
        dbRef.updateChildren(hashMap);
    }
    @Override
    protected void onStart() {
        //checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get time stamp
        Calendar ccDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveDatee = currentDate.format(ccDate.getTime());

        Calendar ccTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        final String saveTimee = currentTime.format(ccTime.getTime());

        String timeStamp = saveDatee +":"+ saveTimee;
        //set offline with last seen time stamp
        checkOnlineStatus(timeStamp);

    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();

    }



}