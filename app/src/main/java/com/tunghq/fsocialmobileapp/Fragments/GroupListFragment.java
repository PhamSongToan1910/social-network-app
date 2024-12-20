package com.tunghq.fsocialmobileapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tunghq.fsocialmobileapp.Adapter.GroupListAdapter;
import com.tunghq.fsocialmobileapp.GroupCreateActivity;
import com.tunghq.fsocialmobileapp.Models.GroupList;
import com.tunghq.fsocialmobileapp.NotificationActivity;
import com.tunghq.fsocialmobileapp.R;

import java.util.ArrayList;


public class GroupListFragment extends Fragment {

    public static ImageView note5;
    ImageView  createGroup;
    RecyclerView groupsRv;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    ArrayList<GroupList> groupChatList;
    GroupListAdapter adapterGroupChatList;
    String myUid;
    public GroupListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        //init
        init(view);
        //click
        clickListener();
        //check notify
        checkNotifications();
        //load data
        loadGroupsChatList();
        return view;
    }

    private void loadGroupsChatList() {
        groupChatList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups Chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    //if current user's uid exists in participant
                    if(ds.child("Participants").child(myUid).exists()){
                        GroupList model = ds.getValue(GroupList.class);
                        groupChatList.add(model);
                    }
                }
                adapterGroupChatList = new GroupListAdapter(getActivity(),groupChatList);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init(View view) {

        groupsRv = view.findViewById(R.id.recyclerViewGroupChat);

        createGroup = view.findViewById(R.id.createGroup);
        note5 = view.findViewById(R.id.note5);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        myUid = user.getUid();

    }

    private void clickListener() {
        note5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seenNotifications();
                note5.setImageResource(R.drawable.ic_notifications);
                Intent intent=new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
            }
        });
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), GroupCreateActivity.class);
                startActivity(intent);
            }
        });

    }
    private void checkNotifications(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("checkNotification")
                .child(firebaseUser.getUid());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // If any child is added to the database reference
                note5.setImageResource(R.drawable.ic_baseline_notifications_active);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // If any child is updated/changed to the database reference
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // If any child is removed to the database reference
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // If any child is moved to the database reference
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }
    private void seenNotifications(){
//        HashMap<String, Object> resultUpdate = new HashMap<>();
//        resultUpdate.put("seen", "yes");
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("checkNotification").child(firebaseUser.getUid());
        reference.removeValue();

    }
//    private void searchGroupsChatList(String query) {
//        groupChatList = new ArrayList<>();
//
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups Chat");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                groupChatList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()){
//                    //if current user's uid exists in participant
//                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
//                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
//                            GroupChat model = ds.getValue(GroupChat.class);
//                            groupChatList.add(model);
//                        }
//
//                    }
//                }
//                adapterGroupChatList = new GroupsChatAdapter(getActivity(),groupChatList);
//                groupsRv.setAdapter(adapterGroupChatList);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}