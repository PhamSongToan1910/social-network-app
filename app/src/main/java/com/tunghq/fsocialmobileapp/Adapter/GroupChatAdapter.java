package com.tunghq.fsocialmobileapp.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tunghq.fsocialmobileapp.Models.GroupChat;
import com.tunghq.fsocialmobileapp.R;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatAdapter  extends RecyclerView.Adapter<GroupChatAdapter.HolderGroupChat>{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    FirebaseAuth firebaseAuth;
    Context context;
    FirebaseUser fUser;
    ArrayList<GroupChat> modelGroupChatList;

    public GroupChatAdapter(Context context, ArrayList<GroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;
        firebaseAuth = FirebaseAuth.getInstance();


    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent,false);
            return new HolderGroupChat(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent,false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, @SuppressLint("RecyclerView") int position) {
        GroupChat model = modelGroupChatList.get(position);
        String message = model.getMessage();//if text message then contain message, if image contain url
        String sender = model.getSender();
        String timeStamp = model.getTimeStamp();
        String type = model.getType();
        String date = model.getDate();
        String time = model.getTime();
        String groupId = model.getGroupId();

        //
        if(type.equals("text")){
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);

        }else{
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            try {
                Picasso.get().load(message).placeholder(R.drawable.baseline_image_24).into(holder.messageIv);
            }catch (Exception e){
                holder.messageIv.setImageResource(R.drawable.baseline_image_24);

            }

        }

        holder.timeTv.setText(date);
        setUserNameProfileImg(model,holder);
        holder.messageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.hoursTv.setText(time);
                holder.hoursTv.setVisibility(View.VISIBLE);
            }
        });
        holder.messageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.hoursTv.setText(time);
                holder.hoursTv.setVisibility(View.VISIBLE);
            }
        });
        holder.messageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Dialog dialog = new Dialog(view.getContext());
                dialog.setContentView(R.layout.dialog_image);

                ImageView imageViewMess = dialog.findViewById(R.id.imageViewImage);
                ImageView buttonCancel = dialog.findViewById(R.id.cancel_image);
                ImageView buttonDown = dialog.findViewById(R.id.download_image);

                Picasso.get().load(message).placeholder(R.drawable.baseline_image_24).into(imageViewMess);

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes(layoutParams);

                dialog.show();

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                buttonDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadFile(message);

                    }
                });
                return false;
            }
        });
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(groupId,position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    private void deleteMessage(String groupId, int position) {
        String myUid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*
         * get timestamp of clicked message
         * Compare the timestamp of the clicked message with all  message in Chats
         * Where both value matches delete that message
         * This will allow sender to delete his and receiver's message*/

        String msgTimeStamp = modelGroupChatList.get(position).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Groups Chat").child(groupId).child("Messages");
        Query query = dbRef.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){

                    if(ds.child("sender").getValue().equals(myUid)){
                        //ds.getRef().removeValue();
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("message","This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "You can delete only your message   ", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void downloadFile(String url){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String title = URLUtil.guessFileName(url,null,null);
        request.setTitle(title);
        request.setDescription("Downloading File please wait...");
        String cookie = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("cookie",cookie);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        Toast.makeText(context, "Downloading started...", Toast.LENGTH_SHORT).show();
    }

    private void setUserNameProfileImg(GroupChat model, HolderGroupChat holder) {
        //get sender info from uid in model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query query =  ref.orderByChild("userId").equalTo(model.getSender());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String name = "" + ds.child("userName").getValue();
                    String profileImg = "" + ds.child("profileImg").getValue();

                    holder.nameTv.setText(name);
                    try{
                        Picasso.get().load(profileImg).into(holder.profileIv);
                    }catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(modelGroupChatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }

    }

    class HolderGroupChat extends RecyclerView.ViewHolder{
        TextView nameTv, messageTv, timeTv, hoursTv;
        ImageView profileIv, messageIv;
        RelativeLayout messageLayout;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTvGRC);
            messageTv = itemView.findViewById(R.id.messageTvGRC);
            profileIv = itemView.findViewById(R.id.profileImgGRC);
            timeTv = itemView.findViewById(R.id.timeTvGRC);
            messageIv = itemView.findViewById(R.id.messageIv);
            hoursTv = itemView.findViewById(R.id.hoursTvGRC);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
