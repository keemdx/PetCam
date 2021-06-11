package com.example.petcam.profile.vod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.petcam.R;
import com.example.petcam.widget.TimeString;

import java.util.List;

public class VODAdapter extends RecyclerView.Adapter<VODAdapter.ViewHolder> {

    private List<VODItem> mList;
    private Context mContext;

    public VODAdapter(List<VODItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_vod, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        VODItem item = mList.get(position);

        // vod 썸네일 이미지
        try {
            Glide.with(mContext)
                    .load(item.getThumbnail())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_vod_thumbnail);
        } catch (Exception e){

        }

        // vod 타이틀
        holder.tv_vod_title.setText(item.getTitle());

        // vod 날짜 (create at)
        TimeString timeString = new TimeString();
        long getTime = Long.parseLong(item.getCreateAt());
        holder.tv_createAt.setText(TimeString.formatTimeString(getTime));

        // 조회수 이미지
        holder.iv_hitsImage.setImageResource(item.getHitsImage());

        // vod 조회수 : 조회수가 없을 경우 0으로 표시한다.
        if(item.getHits() == null) {
            holder.tv_hits.setText("0");
        } else {
            holder.tv_hits.setText(item.getHits());
        }

        // vod 게시자 프로필 사진
        try {
            Glide.with(mContext)
                    .load(item.getProfileImage())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_profile);
        } catch (Exception e) {

        }
            // vod 게시자 닉네임
        holder.tv_name.setText(item.getName());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_createAt, tv_vod_title, tv_hits, tv_name;
        public ImageView iv_vod_thumbnail, iv_hitsImage, iv_profile;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_createAt = itemView.findViewById(R.id.tv_createAt);
            tv_vod_title = itemView.findViewById(R.id.tv_vod_title);
            tv_hits = itemView.findViewById(R.id.tv_comment_createAt);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_vod_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            iv_hitsImage = itemView.findViewById(R.id.iv_hitsImage);
            iv_profile= itemView.findViewById(R.id.civ_profile);

        }
    }
}