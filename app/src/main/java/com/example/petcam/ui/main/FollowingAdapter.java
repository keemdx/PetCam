package com.example.petcam.ui.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.streaming.StreamingPlayerActivity;

import java.util.List;

import static com.example.petcam.function.App.STREAMING_ROOM_ID;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.ViewHolder> {

    private final List<FollowingItem> mList;
    private final Context mContext;

    public FollowingAdapter(List<FollowingItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_following, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FollowingItem item = mList.get(position);

        if (item.getStreamer_image() != null) {
            // 유저 프로필 이미지 보여주기
            Glide.with(mContext).load(item.getStreamer_image()).centerCrop().into(holder.iv_profile);
        } else {
            Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(holder.iv_profile);
        }
        holder.iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, StreamingPlayerActivity.class);
                intent.putExtra(STREAMING_ROOM_ID, item.getRoom_id());
                mContext.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_profile;

        public ViewHolder(View itemView) {
            super(itemView);
            this.iv_profile = itemView.findViewById(R.id.iv_profile);
        }
    }
}