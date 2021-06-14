package com.example.petcam.profile.notice;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.widget.TimeString;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.example.petcam.function.App.NOTICE_COMMENT_CONTENTS;
import static com.example.petcam.function.App.NOTICE_COMMENT_ID;
import static com.example.petcam.function.App.NOTICE_ID;

public class NoticeCommentAdapter extends RecyclerView.Adapter<NoticeCommentAdapter.ViewHolder> {

    private List<NoticeCommentItem> mList;
    private Context mContext;
    private final String userID;
    private final NoticeCommentAdapter.OnListItemSelectedInterface mListener;

    // 리사이클러뷰를 클릭했을 때 동작할 메소드를 붙여줄 인터페이스
    public interface OnListItemSelectedInterface {
        void onItemSelected(View view, int position, String commentID, String commentContents);
    }

    public NoticeCommentAdapter(List<NoticeCommentItem> mList, Context mContext, NoticeCommentAdapter.OnListItemSelectedInterface mListener, String userID) {
        this.mList = mList;
        this.mContext = mContext;
        this.mListener = mListener;
        this.userID = userID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_notice_comment, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        NoticeCommentItem item = mList.get(position);

        if (item.getComment_profile_url() == null) {
            // 프로필 이미지가 없을 경우 기본 프로필 사진
            Glide.with(mContext)
                    .load(R.drawable.ic_user)
                    .centerCrop()
                    .into(holder.iv_comment_profile);
        } else {
            // 댓글 작성자 프로필 사진
            Glide.with(mContext)
                    .load(item.getComment_profile_url())
                    .centerCrop()
                    .into(holder.iv_comment_profile);
        }

        // 댓글 작성자 이름
        holder.tv_comment_name.setText(item.getComment_user_name());

        // 댓글 내용
        holder.tv_comment_text.setText(item.getComment_text());

        // 글 작성 시간 (create at)
        TimeString timeString = new TimeString();
        long getTime = Long.parseLong(item.getComment_create_at());
        holder.tv_comment_create_at.setText(TimeString.formatTimeString(getTime));

        if (item.getComment_user_id().equals(userID)) {
            holder.iv_comment_more.setVisibility(View.VISIBLE);
        } else {
            holder.iv_comment_more.setVisibility(View.GONE);
        }

        // 댓글 더보기 드롭 메뉴 - 수정, 삭제
        holder.iv_comment_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(ACTIVITY_SERVICE, view + ", " + position + ", " + item.getComment_id() + ", " + item.getComment_text());
                mListener.onItemSelected(view, position, item.getComment_id(), item.getComment_text());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_comment_name, tv_comment_text, tv_comment_create_at;
        public CircleImageView iv_comment_profile;
        public ImageView iv_comment_more;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_comment_name = itemView.findViewById(R.id.tv_comment_name);
            tv_comment_text = itemView.findViewById(R.id.tv_comment_text);
            tv_comment_create_at = itemView.findViewById(R.id.tv_comment_createAt);
            iv_comment_profile = itemView.findViewById(R.id.civ_comment_profile);
            iv_comment_more = itemView.findViewById(R.id.iv_comment_more);
        }
    }
}