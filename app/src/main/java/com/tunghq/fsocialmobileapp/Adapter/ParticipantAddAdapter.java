package com.tunghq.fsocialmobileapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tunghq.fsocialmobileapp.Models.User;
import com.tunghq.fsocialmobileapp.OthersProfileActivity;
import com.tunghq.fsocialmobileapp.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ParticipantAddAdapter extends RecyclerView.Adapter<ParticipantAddAdapter.HolderParticipantAdd>{
    Context context;
    ArrayList<User> userList;
    String groupId, myGroupRole; //Creator
    FirebaseUser user;


    public ParticipantAddAdapter(Context context, ArrayList<User> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add, parent,false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        //get data
        User model = userList.get(position);
        String name = model.getUserName();
        String featuredName = model.getFeaturedName();
        String image = model.getProfileImg();
        String uid = model.getUserId();

        user = FirebaseAuth.getInstance().getCurrentUser();

        //set data
        holder.userNameTv.setText(name);
        holder.featuredNameTv.setText(featuredName);
        try {
            Picasso.get().load(image).placeholder(R.drawable.firebase_logoo).into(holder.avatarIv);
        }catch (Exception e){
            holder.avatarIv.setImageResource(R.drawable.firebase_logoo);

        }

        if(userList.get(position).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);

        }else{
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);

        }

        checkIfAlreadyExists(model, holder);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    //user exists
                                    String hisPreviousRole = ""+snapshot.child("role").getValue();

                                    //options to display in dialog
                                    String[] options;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Options");
                                    if(myGroupRole.equals("creator")){
                                        if(hisPreviousRole.equals("admin")){
                                            //i am creator, he is admin
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //item click
                                                    if(i == 0){
                                                        //remove admin
                                                        removeAdmin(model);
                                                    }else{
                                                        //remove user
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")) {
                                            //i am creator, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //item click
                                                    if(i == 0){
                                                        //make admin
                                                        makeAdmin(model);
                                                    }else{
                                                        //remove user
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if (myGroupRole.equals("admin")) {
                                        if(hisPreviousRole.equals("creator")){
                                            //i am admin, he is creator
                                            Toast.makeText(context, "Creator of Group....", Toast.LENGTH_SHORT).show();
                                        }
                                        else if(hisPreviousRole.equals("admin")) {
                                            //i am admin, he is admin too
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //item click
                                                    if(i == 0){
                                                        //remove admin
                                                        removeAdmin(model);
                                                    }else{
                                                        //remove user
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")) {
                                            //i am admin, he is participant
                                            //i am creator, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //item click
                                                    if(i == 0){
                                                        //make admin
                                                        makeAdmin(model);
                                                    }else{
                                                        //remove user
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }

                                    }
                                }else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group")
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    addParticipant(model);
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

        holder.avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid", model.getUserId());
                context.startActivity(intent);
            }
        });
    }

    private void addParticipant(User model) {

        HashMap map = new HashMap();
        map.put("checkSentFollow",true);
        FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                .child(user.getUid())
                .child(model.getUserId()).setValue(map);
        addNotifications(model.getUserId(),groupId);
        Toast.makeText(context, "Sent a request", Toast.LENGTH_SHORT).show();
//        //setup user data
//        Calendar cDate = Calendar.getInstance();
//        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
//        final String saveDate = currentDate.format(cDate.getTime());
//
//        Calendar cTime = Calendar.getInstance();
//        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
//        final String saveTime = currentTime.format(cTime.getTime());
//
//        String time = saveDate +":"+ saveTime;
//
//
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("uid", model.getUserId());
//        hashMap.put("role", "participant");
//        hashMap.put("timeStamp", time);
//
//        //add that user in Group>groupId>Participants
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
//        ref.child(groupId).child("Participants").child(model.getUserId()).setValue(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        //added Successfully
//                        Toast.makeText(context, "Added Successfully...", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //failed adding user in group
//                        Toast.makeText(context, "Failed...", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void makeAdmin(User model) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");

        //update role
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId).child("Participants").child(model.getUserId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //success
                        Toast.makeText(context, "The user is now Admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeParticipants(User model) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId).child("Participants").child(model.getUserId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Removed...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdmin(User model) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");

        //update role
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId).child("Participants").child(model.getUserId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //success
                        Toast.makeText(context, "The user is no longer Admin...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExists(User model, HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId).child("Participants").child(model.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //already exists
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }else{
                            //doesn't exists
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder {

        ImageView avatarIv, onlineStatusIv;
        TextView userNameTv, featuredNameTv, statusTv;
        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            userNameTv = itemView.findViewById(R.id.nameTv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            featuredNameTv = itemView.findViewById(R.id.featuredNameTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
    private void addNotifications(String userid, String groupId)
    {
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Notifications").child(userid);
        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("checkNotification").child(userid);

        String notificationId = reference.push().getKey();

        HashMap<String,Object> map=new HashMap<>();
        HashMap<String,Object> map2=new HashMap<>();

        map.put("notificationId", notificationId);
        map.put("affectedPersonId", userid);
        map.put("userId",user.getUid());
        map.put("groupId", groupId);
        map.put("textNotifications","Invite you to Group Chat");
        map.put("postId","");
        map.put("check","group");
        map.put("status", "NoSeen");
        map2.put("seen", true);
        reference.child(notificationId).setValue(map);
        reference2.setValue(map2);





    }


}
