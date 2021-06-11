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

import static com.example.petcam.function.App.NOTICE_COMMENT_CONTENTS;
import static com.example.petcam.function.App.NOTICE_COMMENT_ID;

public class NoticeCommentAdapter extends RecyclerView.Adapter<NoticeCommentAdapter.ViewHolder> {

    private List<NoticeCommentItem> mList;
    private Context mContext;
    private CommentRecyclerViewClickListener mListener;
    private ServiceApi mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

    // 리사이클러뷰를 클릭했을 때 동작할 메소드를 붙여줄 인터페이스
    public interface CommentRecyclerViewClickListener {
        void onEditCommentClick(int position);
    }

    public void setOnClickListener(CommentRecyclerViewClickListener listener) {
        this.mListener = listener;
    }

    public NoticeCommentAdapter(List<NoticeCommentItem> mList, Context mContext, CommentRecyclerViewClickListener mListener) {
        this.mList = mList;
        this.mContext = mContext;
        this.mListener = mListener;
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
        int commentID = Integer.parseInt(item.getComment_id());
        String commentContents = item.getComment_text();

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

        // 댓글 더보기 드롭 메뉴 - 수정, 삭제
        holder.iv_comment_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, holder.iv_comment_more);
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_comment);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_edit:
                                Intent intent = new Intent(mContext, EditCommentActivity.class);
                                intent.putExtra(NOTICE_COMMENT_ID, commentID);
                                intent.putExtra(NOTICE_COMMENT_CONTENTS, commentContents);
                                mContext.startActivity(intent);
                                break;
                            case R.id.menu_delete:
                                alertDialog(view, position, commentID); // 삭제하시겠습니까? 팝업창
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();

            }
        });
    }

    // 삭제 확인 팝업 다이알로그
    public void alertDialog(View view, final int position, int commentID) {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setMessage("선택한 댓글을 삭제하시겠습니까?");
        alert.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeComment(position, commentID);
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.create().show();
    }

    // =========================================================================================================
    // DB 연결 후 해당 댓글 삭제
    private void removeComment(int position, int commentID) {

        mServiceApi.removeComment(commentID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_SHORT).show();
                // 성공적으로 DB 내 공지사항 삭제를 완료했을 경우 액티비티를 닫는다.
                if(result.getResult().equals("success")) {
                    //arraylist에서 해당 인덱스의 아이템 객체를 지워주고
                    mList.remove(position);
                    //어댑터에 알린다.
                    notifyItemRemoved(position);
                    //이 notify도 반드시 해줘야 한다. 해주지 않으면 index오류
                    notifyDataSetChanged();
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(mContext, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }
    // =========================================================================================================

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
            this.tv_comment_name = itemView.findViewById(R.id.tv_comment_name);
            this.tv_comment_text = itemView.findViewById(R.id.tv_comment_text);
            this.tv_comment_create_at = itemView.findViewById(R.id.tv_comment_createAt);
            this.iv_comment_profile = itemView.findViewById(R.id.civ_comment_profile);
            this.iv_comment_more = itemView.findViewById(R.id.iv_comment_more);
        }
    }
}