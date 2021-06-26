package com.example.petcam.streaming;

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

import java.util.List;


public class ViewersAdapter extends RecyclerView.Adapter<ViewersAdapter.ViewHolder> {

    private List<ViewersItem> mList;
    private Context mContext;

    public ViewersAdapter(List<ViewersItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_viewers, parent, false);

        // 뷰를 가져온다
        return new ViewersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewersAdapter.ViewHolder holder, int position) {
        ViewersItem item = mList.get(position);

        if (item.getViewer_image() == null) {
            Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(holder.iv_profile);
        } else {
            // 유저 프로필 이미지 보여주기
            Glide.with(mContext).load(item.getViewer_image()).centerCrop().into(holder.iv_profile);
        }

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
            this.iv_profile = itemView.findViewById(R.id.iv_viewer);
        }
    }
}