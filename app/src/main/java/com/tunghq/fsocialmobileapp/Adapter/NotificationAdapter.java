package com.tunghq.fsocialmobileapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.tunghq.fsocialmobileapp.ChatActivity;
import com.tunghq.fsocialmobileapp.Models.User;
import com.tunghq.fsocialmobileapp.Models.Notifications;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.tunghq.fsocialmobileapp.OthersProfileActivity;
import com.tunghq.fsocialmobileapp.PostDetailsActivity;
import com.tunghq.fsocialmobileapp.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    public static PlayerView post_videos;

    Context context;
    List<Notifications> list;


    public NotificationAdapter(Context context, List<Notifications> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Notifications notifications = list.get(position);
        holder.textNotifications.setText(notifications.getTextNotifications());

        getUserInfo(holder.profile, holder.username, notifications.getUserId());

        //check display notification
        if (notifications.getCheck().equals("post")) {

            holder.post_image.setVisibility(View.VISIBLE);
            getPost(holder.post_image, notifications.getPostId());

        } else if(notifications.getCheck().equals("user")||notifications.getCheck().equals("group")){
            holder.post_image.setVisibility(View.GONE);
            post_videos.setVisibility(View.GONE);
            holder.accept.setVisibility(View.VISIBLE);
            holder.cancel.setVisibility(View.VISIBLE);

        } else if(notifications.getCheck().equals("chat")){
            holder.post_image.setVisibility(View.GONE);
            post_videos.setVisibility(View.GONE);
        }
        if(notifications.getStatus().equals("NoSeen")){
            holder.relativeLayout.setBackgroundColor(Color.parseColor("#DDF1F8"));

        }else {
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.accept.setVisibility(View.GONE);
            holder.cancel.setVisibility(View.GONE);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (notifications.getCheck()) {
                    case "post": {
                        Intent intent = new Intent(context, PostDetailsActivity.class);
                        intent.putExtra("postId", notifications.getPostId());
                        intent.putExtra("notificationId", notifications.getNotificationId());
                        intent.putExtra("affectedPersonId", notifications.getAffectedPersonId());
                        context.startActivity(intent);

                        break;
                    }
                    case "user": {
                        Intent intent = new Intent(context, OthersProfileActivity.class);
                        intent.putExtra("uid", notifications.getUserId());
                        intent.putExtra("notificationId", notifications.getNotificationId());
                        intent.putExtra("affectedPersonId", notifications.getAffectedPersonId());
                        context.startActivity(intent);

                        break;
                    }
                    case "chat": {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("chatuid", notifications.getUserId());
                        intent.putExtra("notificationId", notifications.getNotificationId());
                        intent.putExtra("affectedPersonId", notifications.getAffectedPersonId());
                        context.startActivity(intent);

                        break;
                    }
                }
            }
        });
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //remove check Sent
                FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                        .child(notifications.getUserId())
                        .child(notifications.getAffectedPersonId()).removeValue();
                if(notifications.getCheck().equals("user")){
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(notifications.getUserId())
                            .child("following").child(notifications.getAffectedPersonId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(notifications.getAffectedPersonId())
                            .child("followers").child(notifications.getUserId()).setValue(true);
                }else if(notifications.getCheck().equals("group")){
                    //setup user data
                    Calendar cDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                    final String saveDate = currentDate.format(cDate.getTime());

                    Calendar cTime = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                    final String saveTime = currentTime.format(cTime.getTime());

                    String time = saveDate +":"+ saveTime;


                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("uid", notifications.getAffectedPersonId());
                    hashMap.put("role", "participant");
                    hashMap.put("timeStamp", time);

                    //add that user in Group>groupId>Participants

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
                    ref.child(notifications.getGroupId()).child("Participants").child(notifications.getAffectedPersonId()).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //added Successfully
                                    Toast.makeText(context, "Added Successfully...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed adding user in group
                                    Toast.makeText(context, "Failed...", Toast.LENGTH_SHORT).show();
                                }
                            });
                }


                HashMap<String, Object> resultUpdate = new HashMap<>();

                resultUpdate.put("status", "seen");
                resultUpdate.put("textNotifications","Accept");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                        .child(notifications.getAffectedPersonId())
                        .child(notifications.getNotificationId());
                reference.updateChildren(resultUpdate);


            }
        });
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, Object> resultUpdate = new HashMap<>();

                resultUpdate.put("status", "seen");
                resultUpdate.put("textNotifications","Don't accepted follow");

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                        .child(notifications.getAffectedPersonId())
                        .child(notifications.getNotificationId());
                reference.updateChildren(resultUpdate);

                FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                        .child(notifications.getUserId())
                        .child(notifications.getAffectedPersonId()).removeValue();
            }
        });


    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView post_image;
        CircleImageView profile;
        TextView username, textNotifications;
        RelativeLayout relativeLayout;
        Button accept, cancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            post_image = itemView.findViewById(R.id.post_image_notifications);
            profile = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            textNotifications = itemView.findViewById(R.id.comment);
            post_videos = itemView.findViewById(R.id.post_videos);
            relativeLayout = itemView.findViewById(R.id.relativeNotification);
            accept = itemView.findViewById(R.id.acceptFollow);
            cancel = itemView.findViewById(R.id.cancelFollow);


        }
    }

    private void getUserInfo(final ImageView imageView, TextView username, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User data = snapshot.getValue(User.class);

                username.setText(data.getUserName());
                Picasso.get().load(data.getProfileImg()).into(imageView);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getPost(final ImageView imageView, String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        reference.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Posts posts = snapshot.getValue(Posts.class);
                try {
                    if (posts.getType().equals("image")) {
                        try {

                            Picasso.get().load(posts.getPostUrl()).into(imageView);

                        } catch (Exception e) {

                        }
                        NotificationAdapter.post_videos.setVisibility(View.GONE);

                    } else if (posts.getType().equals("video")) {
                        NotificationAdapter.post_videos.setVisibility(View.VISIBLE);
                        try {

                            SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
                            post_videos.setPlayer(simpleExoPlayer);
                            MediaItem mediaItem = MediaItem.fromUri(posts.getPostUrl());
                            simpleExoPlayer.addMediaItems(Collections.singletonList(mediaItem));
                            simpleExoPlayer.prepare();
                            simpleExoPlayer.setPlayWhenReady(false);

                        } catch (Exception e) {
                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                        }
                    } else if (posts.getType().equals("status")) {
                        NotificationAdapter.post_videos.setVisibility(View.GONE);

                    }
                } catch (Exception e) {

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
