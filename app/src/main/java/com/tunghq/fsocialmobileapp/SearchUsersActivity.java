package com.tunghq.fsocialmobileapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.SearchAdapter;
import com.tunghq.fsocialmobileapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;



public class SearchUsersActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<User> dataList;
    SearchAdapter adapter;
    Toolbar toolbar;
    EditText et_search;
    FirebaseUser user;
    FirebaseAuth auth;
    String myUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        //init view
        et_search=findViewById(R.id.et_search);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        dataList=new ArrayList<>();
        adapter=new SearchAdapter(this,dataList);
        recyclerView.setAdapter ( adapter );

        //init firebase
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        myUid = user.getUid();

        //show user
        readUsers();

        //check editTextChange
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




    }

    private void readUsers()
    {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (et_search.getText().toString().equals("")) {
                    dataList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User data=dataSnapshot.getValue(User.class);
                        if(!data.getUserId().equals(user.getUid())){
                            dataList.add(data);

                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchUsersActivity.this, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void searchUsers(String a)
    {
        Query query;
        if(et_search.getText().toString().startsWith("@")){
            query= FirebaseDatabase.getInstance ().getReference ("Users").orderByChild ( "featuredName" )
                    .startAt (  et_search.getText().toString())
                    .endAt ( et_search.getText().toString()+"\uf8ff" );
        }else{
            query= FirebaseDatabase.getInstance ().getReference ("Users").orderByChild ( "userName" )
                    .startAt (  et_search.getText().toString())
                    .endAt ( et_search.getText().toString()+"\uf8ff" );
        }
        query.addValueEventListener ( new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataList.clear ();
                for (DataSnapshot snapshot : dataSnapshot.getChildren ()){
                    User user2=snapshot.getValue (User.class);
                    if(!user2.getUserId().equals(user.getUid())){
                        dataList.add ( user2 );
                    }
                }

                adapter.notifyDataSetChanged ();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(SearchUsersActivity.this, "Error "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } );


    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startActivity(new Intent(SearchUsersActivity.this,HomeActivity.class));
//    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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
