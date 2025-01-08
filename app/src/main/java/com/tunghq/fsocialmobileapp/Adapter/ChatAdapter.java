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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.tunghq.fsocialmobileapp.Models.Chat;
import com.tunghq.fsocialmobileapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tunghq.fsocialmobileapp.Util.AESEncryption;
import com.tunghq.fsocialmobileapp.Util.DigitalSignatureUtil;
import com.tunghq.fsocialmobileapp.Util.KeystoreUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.SecretKey;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder>{


    private static final int MSG_TYPE_LEF = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    FirebaseUser fUser;

    Context context;
    List<Chat> chatList;
    String imageUrl;
    SecretKey secretKey;
    public ChatAdapter(Context context, List<Chat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
        try {
            secretKey = KeystoreUtils.getKey();
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent,false);
            return new MyHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent,false);
            return new MyHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") final int position) {
        //get data
        System.out.println("chat: " + chatList.get(position));
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimeStamp();
        String type = chatList.get(position).getType();
        String date = chatList.get(position).getDate();
        String time = chatList.get(position).getTime();
        String publicKey = (chatList.get(position).getPublicKey() != null) ? chatList.get(position).getPublicKey() : "";
        String signature = (chatList.get(position).getSignature() != null) ? chatList.get(position).getSignature() : "";

        if(type.equals("text")){
            //text Message
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            String textMess;
            try {
                System.out.println(secretKey);
                System.out.println(message);
                textMess = AESEncryption.decrypt(message, secretKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            holder.messageTv.setText(textMess);

        }else if(type.equals("image")){
            //image message
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.baseline_image_24).into(holder.messageIv);
        } else if(type.equals("file")) {
            holder.messageTv.setVisibility(View.VISIBLE); // Hiển thị TextView
            holder.messageIv.setVisibility(View.VISIBLE); // Hiển thị ImageView

            // Đặt lại quy tắc RelativeLayout cho ImageView để nằm trên
            if (holder.messageIv.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageIv.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                holder.messageIv.setLayoutParams(params);
            }

            // Đặt TextView xuống dưới ImageView
            if (holder.messageTv.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams tvParams = (RelativeLayout.LayoutParams) holder.messageTv.getLayoutParams();
                tvParams.addRule(RelativeLayout.BELOW, holder.messageIv.getId());
                holder.messageTv.setLayoutParams(tvParams);
            }

            // Tải ảnh vào ImageView
            Picasso.get().load(message).placeholder(R.drawable.file).into(holder.messageIv);

            // Set text cho TextView
            String[] messArray = message.split("\\+");
            holder.messageTv.setText(messArray[messArray.length - 1]);
        }
        //set
        //holder.messageTv.setText(message);
        holder.timeTv.setText(date);
        holder.timeHoursTv.setText(time);
        try{
            Picasso.get().load(message).placeholder(R.drawable.file).into(holder.messageIv);
        }catch (Exception e){

        }

        //set textSeen
        if(position == chatList.size()-1){
            if(!chatList.get(position).isSeen()){
                holder.isSeenTv.setText("Not Seen");

            }else{
                holder.isSeenTv.setText("Seen");

            }
        }else
        {
            holder.isSeenTv.setVisibility(View.GONE);
        }

        //click to show delete mess dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position);
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
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.timeHoursTv.setVisibility(View.VISIBLE);
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
                        System.out.println("signature: " + signature);
                        System.out.println("publicKey: " + publicKey);
                        downloadFileAction(message, signature, publicKey);

                    }
                });
                return false;
            }
        });
        holder.messageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.timeHoursTv.setVisibility(View.VISIBLE);
            }
        });

    }
    private void downloadFileAction(String url, String signature, String publicKey){
        if(url.contains("\\+")) {
            DigitalSignatureUtil.receiveAndVerifyFile(url, signature, publicKey, new DigitalSignatureUtil.VerificationCallback() {
                @Override
                public void onVerificationComplete(boolean isValid) {
                    System.out.println(isValid);
                    if (isValid) {
                        downloadFile(url);
                    } else {
                        System.out.println("Can't download!!!");
                    }
                }
            });
        } else {
            downloadFile(url);
        }
    }

    private void deleteMessage(int position) {

        String myUid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*
        * get timestamp of clicked message
        * Compare the timestamp of the clicked message with all  message in Chats
        * Where both value matches delete that message
        * This will allow sender to delete his and receiver's message*/

        String msgTimeStamp = chatList.get(position).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){

                    if(ds.child("sender").getValue().equals(myUid)){
                        //ds.getRef().removeValue();
                        HashMap<String,Object> hashMap = new HashMap<>();
                        try {
                            hashMap.put("message",AESEncryption.encrypt("This message was deleted...", secretKey));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
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

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEF;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv,timeHoursTv;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            messageIv = itemView.findViewById(R.id.messageIv);
            timeHoursTv = itemView.findViewById(R.id.timeHoursTv);

        }
    }

    public void downloadFile(String url) {
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
}

