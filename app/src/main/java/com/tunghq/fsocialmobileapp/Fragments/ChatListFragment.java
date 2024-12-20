package com.tunghq.fsocialmobileapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.ChatListAdapter;
import com.tunghq.fsocialmobileapp.Models.User;
import com.tunghq.fsocialmobileapp.Models.Chat;
import com.tunghq.fsocialmobileapp.Models.ChatList;
import com.tunghq.fsocialmobileapp.NotificationActivity;
import com.tunghq.fsocialmobileapp.R;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tunghq.fsocialmobileapp.SearchUsersActivity;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {


    RecyclerView recyclerViewchatlist;
    FirebaseAuth firebaseAuth;
    List<ChatList> chatlists;
    List<User> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    ChatListAdapter adapterChatlist;

    public static ImageView note4;
    ImageView search;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        //init view
        note4 = view.findViewById(R.id.note4);
        search = view.findViewById(R.id.search);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerViewchatlist = view.findViewById(R.id.recyclerViewchatlist);

        chatlists = new ArrayList<>();
        checkNotifications();

        //show chat list
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlists.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatList chatlist = ds.getValue(ChatList.class);
                    chatlists.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        note4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seenNotifications();
                note4.setImageResource(R.drawable.ic_notifications);
                Intent intent=new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), SearchUsersActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (ChatList chatlist : chatlists) {
                        if (user.getUserId() != null && user.getUserId().equals(chatlist.getChatListId())) {
                            userList.add(user);
                            break;
                        }
                    }
                    //adapter
                    adapterChatlist = new ChatListAdapter(getContext(), userList);
                    recyclerViewchatlist.setAdapter(adapterChatlist);
                    //set last mess
                    for (int i = 0; i < userList.size(); i++) {
                        lastMessage(userList.get(i).getUserId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid())
                            && chat.getSender().equals(userId)
                            || chat.getReceiver().equals(userId)
                            && chat.getSender().equals(currentUser.getUid())) {
                        if(chat.getType().equals("image")){
                            theLastMessage = "Sent a photo....";
                        }else{
                            theLastMessage = chat.getMessage();

                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                note4.setImageResource(R.drawable.ic_baseline_notifications_active);
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


}