package com.example.petcam.ui.profile.fanboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.widget.TimeString;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FanboardAdapter extends RecyclerView.Adapter<FanboardAdapter.ViewHolder> {

    private final List<FanboardItem> mList;
    private final Context mContext;
    private final String userID;
    private final String channelID;
    private final FanboardAdapter.OnListItemSelectedInterface mListener;

    public interface OnListItemSelectedInterface {
        void onItemSelected(View v, int position, String writerID);
    }

    public FanboardAdapter(List<FanboardItem> mList, Context mContext, FanboardAdapter.OnListItemSelectedInterface listener, String myID, String channelID) {
        this.mList = mList;
        this.mContext = mContext;
        this.mListener = listener;
        this.userID = myID;
        this.channelID = channelID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_fanboard, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FanboardItem item = mList.get(position);

        // 팬보드 아이디
        holder.tv_id.setText(item.getFanboard_id());

        // 팬보드 닉네임
        holder.tv_name.setText(item.getWriter_name());

        // 팬보드 텍스트
        holder.tv_text.setText(item.getFanboard_contents());

        // 글 작성 시간 (create at)
        TimeString timeString = new TimeString();
        long getTime = Long.parseLong(item.getCreate_at());
        holder.tv_time.setText(TimeString.formatTimeString(getTime));

        // 이미지
        if (item.getWriter_photo() == null) {
            // 프로필 이미지가 없을 경우 기본 프로필 사진
            Glide.with(mContext)
                    .load(R.drawable.ic_user)
                    .centerCrop()
                    .into(holder.civ_profile);
        } else {
            // 댓글 작성자 프로필 사진
            Glide.with(mContext)
                    .load(item.getWriter_photo())
                    .centerCrop()
                    .into(holder.civ_profile);
        }

        if (userID.equals(item.getWriter_id())) {
            holder.iv_more.setVisibility(View.VISIBLE);
        } else if (channelID.equals(userID)) {
            holder.iv_more.setVisibility(View.VISIBLE);
        } else {
            holder.iv_more.setVisibility(View.GONE);
        }

        // 더보기 드롭 메뉴 - 수정, 삭제
        holder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemSelected(view, position, item.getWriter_id());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name, tv_text, tv_time, tv_id;
        public CircleImageView civ_profile;
        public ImageView iv_more;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_id = itemView.findViewById(R.id.tv_id);
            tv_name = itemView.findViewById(R.id.tv_fanboard_name);
            tv_text = itemView.findViewById(R.id.tv_fanboard_text);
            tv_time = itemView.findViewById(R.id.tv_fanboard_createAt);
            civ_profile = itemView.findViewById(R.id.civ_fanboard_profile);
            iv_more = itemView.findViewById(R.id.iv_more);
        }
    }
}