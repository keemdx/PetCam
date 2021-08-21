package com.example.petcam.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.petcam.R;
import com.example.petcam.streaming.VODPlayerActivity;

import java.util.List;

import static com.example.petcam.function.App.STREAMING_ROOM_ID;

public class ChartVideosAdapter extends RecyclerView.Adapter<ChartVideosAdapter.ViewHolder> {

    private final List<ChartVideosItem> mList;
    private final Context mContext;

    public ChartVideosAdapter(List<ChartVideosItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_chart_videos, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ChartVideosItem item = mList.get(position);

        // vod 차트 랭킹 넘버
        holder.tv_rank.setText(String.valueOf(item.getNo()));
        if (item.getNo() > 3) {
            holder.tv_rank.setTextColor(Color.GRAY);
        }

        // vod 타이틀
        holder.tv_vod_title.setText(item.getRoomTitle());

        if (item.getThumbnail() != null) {
            // vod 썸네일 이미지
            Glide.with(mContext)
                    .load(item.getThumbnail())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_vod_thumbnail);
        }

        // vod 조회수 : 조회수가 없을 경우 0으로 표시한다.
        if (item.getViewer() <= 0) {
            holder.tv_hits.setText("0");
        } else {
            holder.tv_hits.setText(String.valueOf(item.getViewer()));
        }

        if (item.getUserProfileImage() == null) {

            Glide.with(mContext)
                    .load(R.drawable.ic_user)
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_profile);
        } else {
            Glide.with(mContext)
                    .load(item.getUserProfileImage())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_profile);
        }

        // vod 게시자 닉네임
        holder.tv_name.setText(item.getUserName());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, VODPlayerActivity.class);
                intent.putExtra(STREAMING_ROOM_ID, item.getRoomID());
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
        public RelativeLayout layout;
        public TextView tv_vod_title, tv_hits, tv_name, tv_rank;
        public ImageView iv_vod_thumbnail, iv_hitsImage, iv_profile;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout_video);
            tv_rank = itemView.findViewById(R.id.tv_rank);
            tv_vod_title = itemView.findViewById(R.id.tv_vod_title);
            tv_hits = itemView.findViewById(R.id.tv_hits);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_vod_thumbnail = itemView.findViewById(R.id.iv_vod_thumbnail);
            iv_hitsImage = itemView.findViewById(R.id.iv_hitsImage);
            iv_profile = itemView.findViewById(R.id.civ_profile);

        }
    }
}