package com.tunghq.fsocialmobileapp.Adapter;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.AddPostActivity;
import com.tunghq.fsocialmobileapp.CommentActivity;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.tunghq.fsocialmobileapp.OthersProfileActivity;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tunghq.fsocialmobileapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    Context context;
    List<Posts> posts;

    public PostAdapter(Context context, List<Posts> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

        Posts postList=posts.get(position);

        //post's display settings
        if (postList.getDescription().isEmpty())
        {
            holder.description.setVisibility(View.GONE);
        }else
        {
            holder.description.setVisibility(View.VISIBLE);
        }

        if(posts.get(position).getType().equals("image")){
            try{
                Picasso.get().load(postList.getPostUrl()).into(holder.post_image);
            }catch (Exception e){

            }
            holder.description.setText(postList.getDescription());
            holder.playerView.setVisibility(View.GONE);
            holder.date.setText(postList.getTime());
            holder.username.setText(postList.getUserName());
            holder.featuredName.setText(postList.getFeaturedName());
            Picasso.get().load(postList.getProfileImg()).placeholder(R.drawable.profile_image).into(holder.profile);

        }else if(posts.get(position).getType().equals("video")){
            try{

                SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
                holder.playerView.setPlayer(simpleExoPlayer);
                MediaItem mediaItem = MediaItem.fromUri(postList.getPostUrl());
                simpleExoPlayer.addMediaItems(Collections.singletonList(mediaItem));
                simpleExoPlayer.prepare();
                simpleExoPlayer.setPlayWhenReady(false);


            }catch (Exception e){
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();;
            }

            holder.description.setText(postList.getDescription());
            holder.date.setText(postList.getTime());
            holder.username.setText(postList.getUserName());
            holder.featuredName.setText(postList.getFeaturedName());
            Picasso.get().load(postList.getProfileImg()).placeholder(R.drawable.profile_image).into(holder.profile);
        }else if(posts.get(position).getType().equals("status")){
            holder.relativeSave.setVisibility(View.GONE);
            holder.descriptionStatus.setVisibility(View.VISIBLE);
            holder.descriptionStatus.setText(postList.getDescription());
            holder.description.setVisibility(View.GONE);
            holder.playerView.setVisibility(View.GONE);
            holder.post_image.setVisibility(View.GONE);
            holder.date.setText(postList.getTime());
            holder.username.setText(postList.getUserName());
            holder.featuredName.setText(postList.getFeaturedName());
            Picasso.get().load(postList.getProfileImg()).placeholder(R.drawable.profile_image).into(holder.profile);
        }


        String pDescription = posts.get(position).getDescription();


        //check like
        isLiked(postList.getPostId(),holder.like);

        //get like count
        getLikesCount(holder.likes_count,postList.getPostId());

        //check save
        isSaved(postList.getPostId(),holder.save);

        //get comment count
        getCommentsCount(holder.comments_count,postList.getPostId());

        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(context,v);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.edit) {
                            Intent intent = new Intent(context, AddPostActivity.class);
                            intent.putExtra("key", "editPost");
                            intent.putExtra("editPostId", posts.get(position).getPostId());
                            context.startActivity(intent);
                            return true;
                        } else if(item.getItemId() == R.id.delete) {
                            FirebaseDatabase.getInstance().getReference().child("Posts")
                                    .child(postList.getPostId()).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                            }else
                                            {
                                                Toast.makeText(context, "Unable to delete", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            return true;
                        } else if(item.getItemId() == R.id.unfollow) {
                                FirebaseDatabase.getInstance().getReference().child("Follow")
                                        .child(user.getUid())
                                        .child("following").child(postList.getUserId()).removeValue();

                                FirebaseDatabase.getInstance().getReference().child("Follow")
                                        .child(postList.getUserId())
                                        .child("followers").child(user.getUid()).removeValue();
                                return true;
                        } else if(item.getItemId() == R.id.download) {
                            downloadFile(postList.getPostUrl());
                            return true;
                        }
                        return true;
                    }
                });

                popupMenu.inflate(R.menu.options);

                //set up display menu
                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference()
                        .child("Follow").child(user.getUid()).child("following");

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if(user.getUid().equals(postList.getUserId())){
                                popupMenu.getMenu().findItem(R.id.unfollow).setVisible(false);

                            }else {
                                popupMenu.getMenu().findItem(R.id.unfollow).setVisible(true);

                            }

                        }else{
                            popupMenu.getMenu().findItem(R.id.unfollow).setVisible(false);

                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                if (!postList.getUserId().equals(user.getUid()) )
                {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                }else{
                    popupMenu.getMenu().findItem(R.id.report).setVisible(false);
                }
                if(postList.getType().equals("status")){
                    popupMenu.getMenu().findItem(R.id.download).setVisible(false);
                }
                popupMenu.show();
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("Like"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(postList.getPostId())
                            .child(user.getUid()).setValue(true);

                    addNotifications(postList.getUserId(),postList.getPostId(),user.getUid());
                }else
                {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(postList.getPostId())
                            .child(user.getUid()).removeValue();
                }
            }
        });


        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.save.getTag().equals("save"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Favourites")
                            .child(user.getUid())
                            .child(postList.getPostId())
                            .setValue(true);
                }else
                {
                    FirebaseDatabase.getInstance().getReference().child("Favourites")
                            .child(user.getUid())
                            .child(postList.getPostId())
                            .removeValue();
                }
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.post_image.getDrawable();
                if(bitmapDrawable == null){
                    //post without image
                    shareTextOnly(pDescription);
                }else{
                    //post with image
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pDescription, bitmap);

                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, CommentActivity.class);
                intent.putExtra("postId",postList.getPostId());
                intent.putExtra("userId",postList.getUserId());
                context.startActivity(intent);
            }
        });


        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid",postList.getUserId());
                context.startActivity(intent);
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid",postList.getUserId());
                context.startActivity(intent);
            }
        });

        holder.featuredName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid",postList.getUserId());
                context.startActivity(intent);
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


    private void shareTextOnly(String pDescription) {
        String shareBory = pDescription;

        Intent sintent = new Intent(Intent.ACTION_SEND);
        sintent.setType("text/plan");
        sintent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sintent.putExtra(Intent.EXTRA_TEXT, shareBory);//text too share
        context.startActivity(Intent.createChooser(sintent,"Share Via"));
    }

    private void shareImageAndText(String pDescription, Bitmap bitmap) {
        String shareBody = pDescription;

        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context,"com.example.socialapp.fileprovider", file);

        }
        catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }



    @Override
    public int getItemCount() {
        return posts.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profile;
        ImageView post_image,options;
        ImageView like,liked,comment,share,save,saved;
        TextView username,date,likes_count,comments_count;
        TextView featuredName,description,descriptionStatus;
        ProgressDialog pd;
        RelativeLayout relativeSave;
        PlayerView playerView;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //init view
            profile=itemView.findViewById(R.id.profile_image);
            post_image=itemView.findViewById(R.id.post_image);
            options=itemView.findViewById(R.id.options);
            like=itemView.findViewById(R.id.like);
            liked=itemView.findViewById(R.id.liked);
            comment=itemView.findViewById(R.id.comments);
            share=itemView.findViewById(R.id.share);
            save=itemView.findViewById(R.id.save);
            saved=itemView.findViewById(R.id.saved);
            username=itemView.findViewById(R.id.username);
            date=itemView.findViewById(R.id.date);
            likes_count=itemView.findViewById(R.id.likes_count);
            comments_count=itemView.findViewById(R.id.comments_count);
            featuredName =itemView.findViewById(R.id.memer);
            description=itemView.findViewById(R.id.description);
            pd=new ProgressDialog(context);
            playerView = itemView.findViewById(R.id.exoplayer_item_post);
            descriptionStatus = itemView.findViewById(R.id.descriptionStatus);
            relativeSave = itemView.findViewById(R.id.relativeSave);

        }
    }


    private void isLiked(final String postid, final ImageView imageView) {
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists())
                {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("Liked");
                }else
                {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotifications(String userid, String postid, String uid)
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Notifications").child(userid);
        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("checkNotification").child(userid);
        String notificationId = reference.push().getKey();

        HashMap<String,Object> map=new HashMap<>();
        HashMap<String,Object> map2=new HashMap<>();

        if(!userid.equals(uid)){
            map.put("notificationId", notificationId);
            map.put("affectedPersonId", userid);
            map.put("userId",user.getUid());
            map.put("groupId", "");
            map.put("textNotifications","liked your post");
            map.put("postId",postid);
            map.put("check","post");
            map.put("status", "NoSeen");
            map2.put("seen", true);
            reference.child(notificationId).setValue(map);
            reference2.setValue(map2);
        }
    }

    private void getLikesCount(final TextView like,final String postid)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                like.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getCommentsCount(final TextView c,final String postid)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Comments")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                c.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isSaved(final String postid,final ImageView imageView)
    {
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Favourites")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists())
                {
                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("saved");
                }else
                {
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }




}
