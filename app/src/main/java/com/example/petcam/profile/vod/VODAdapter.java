package com.example.petcam.profile.vod;

import android.content.Context;
import android.content.Intent;
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
import com.example.petcam.streaming.StreamingPlayerActivity;
import com.example.petcam.streaming.VODPlayerActivity;
import com.example.petcam.widget.TimeString;

import java.util.List;

import static com.example.petcam.function.App.STREAMING_ROOM_ID;

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
        if (item.getThumbnail() == null) {
            Glide.with(mContext)
                    .load(R.drawable.ic_live_sm)
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_vod_thumbnail);

        } else {
            Glide.with(mContext)
                    .load(item.getThumbnail())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_vod_thumbnail);
        }

        // vod 타이틀
        holder.tv_vod_title.setText(item.getTitle());

        // vod 날짜 (create at)
        TimeString timeString = new TimeString();
        long getTime = Long.parseLong(item.getCreateAt());
        holder.tv_createAt.setText(TimeString.formatTimeString(getTime));

        // vod 조회수 : 조회수가 없을 경우 0으로 표시한다.
        if(item.getHits() <= 0) {
            holder.tv_hits.setText("0");
        } else {
            holder.tv_hits.setText(String.valueOf(item.getHits()));
        }

        // vod 게시자 프로필 사진
        // 프로필 이미지가 등록되어 있는지 여부 확인
        if (item.getProfileImage() == null) {
            Glide.with(mContext)
                    .load(R.drawable.ic_user)
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_profile);

        } else {
            Glide.with(mContext)
                    .load(item.getProfileImage())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_profile);
        }
            // vod 게시자 닉네임
        holder.tv_name.setText(item.getName());

        holder.layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String roomID = String.valueOf(item.getRoomID());
                Intent intent = new Intent(mContext, VODPlayerActivity.class);
                intent.putExtra(STREAMING_ROOM_ID, roomID);
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
        public TextView tv_createAt, tv_vod_title, tv_hits, tv_name;
        public ImageView iv_vod_thumbnail, iv_profile;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout_vod);
            tv_createAt = itemView.findViewById(R.id.tv_create_at);
            tv_vod_title = itemView.findViewById(R.id.tv_vod_title);
            tv_hits = itemView.findViewById(R.id.tv_hits);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_vod_thumbnail = itemView.findViewById(R.id.iv_vod_thumbnail);
            iv_profile= itemView.findViewById(R.id.civ_profile);

        }
    }
}