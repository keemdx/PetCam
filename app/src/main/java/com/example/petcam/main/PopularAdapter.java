package com.example.petcam.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.profile.fanboard.FanboardAdapter;
import com.example.petcam.streaming.StreamingPlayerActivity;
import com.example.petcam.widget.TimeString;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import static com.example.petcam.function.App.STREAMING_ROOM_ID;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder> {

    private List<PopularItem> mList;
    private Context mContext;

    public PopularAdapter(List<PopularItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_popular, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        PopularItem item = mList.get(position);

        holder.tv_title.setText(item.getStreamer_title());
        holder.tv_name.setText(item.getStreamer_name());

        // 글 작성 시간 (create at)
        TimeString timeString = new TimeString();
        long getTime = Long.parseLong(item.getCreate_at());
        holder.tv_time.setText(TimeString.formatTimeString(getTime));

        if (item.getThumbnail_image() != null) {
            // 썸네일 이미지 보여주기
            Glide.with(mContext).load(item.getThumbnail_image()).centerCrop().into(holder.iv_popular);
        } else {
            Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(holder.iv_popular);
        }

        holder.layout_popular.setOnClickListener(new View.OnClickListener(){
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
        public TextView tv_title, tv_name, tv_time;
        public ImageView iv_popular;
        public RelativeLayout layout_popular;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_title = itemView.findViewById(R.id.tv_popular_title);
            this.tv_name = itemView.findViewById(R.id.tv_popular_name);
            this.tv_time= itemView.findViewById(R.id.tv_popular_time);
            this.iv_popular = itemView.findViewById(R.id.iv_polular);
            this.layout_popular = itemView.findViewById(R.id.layout_popular);
        }
    }
}