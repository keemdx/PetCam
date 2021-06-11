package com.example.petcam.profile.notice;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.widget.TimeString;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.NOTICE_CONTENTS;
import static com.example.petcam.function.App.NOTICE_CREATE_AT;
import static com.example.petcam.function.App.NOTICE_ID;
import static com.example.petcam.function.App.NOTICE_PIN;
import static com.example.petcam.function.App.NOTICE_TITLE;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.WRITER_ID;
import static com.example.petcam.function.App.makeStatusBarBlack;

public class NoticeDetailActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private ServiceApi mServiceApi;
    private TextView mNoticeTitle, mNoticeContents, mNoticeCreateAt, mUploadComment, mWriterName;
    private CircleImageView mWriterProfile;
    private EditText mComment;
    private LinearLayout mEmpty;
    private RecyclerView rv_notice_comment;
    private List<NoticeCommentItem> mDataList;
    private List<NoticeContents> mContentsList;
    private NoticeCommentAdapter.CommentRecyclerViewClickListener mListener;
    public int noticeID;
    private Boolean editNoticePin;
    private String uid, writerID, writerProfileImage, writerName, noticeTitle, noticeContents, noticeCreateAt, noticePin;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    finish();
                    break;

                case R.id.iv_more:
                    popupMenu(view, noticeID);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        // 액티비티 시작후 키보드 감추기
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);
        findViewById(R.id.iv_more).setOnClickListener(onClickListener);

        // 공지사항 관련 선언
        mNoticeTitle = (TextView) findViewById(R.id.tv_notice_title); // 공지사항 타이틀
        mNoticeContents = (TextView) findViewById(R.id.tv_notice_contents); // 공지사항 내용
        mNoticeCreateAt = (TextView) findViewById(R.id.tv_notice_createAt); // 공지사항 작성 날짜
        mWriterName = (TextView) findViewById(R.id.tv_writer_name); // 공지사항 작성자 닉네임
        mWriterProfile = (CircleImageView) findViewById(R.id.civ_writer_profile); // 공지사항 작성자 프로필 이미지

        // 댓글 관련 선언
        mEmpty = (LinearLayout) findViewById(R.id.layout_none);
        mComment = (EditText) findViewById(R.id.et_comment); // 댓글 입력하는 부분
        mUploadComment = (TextView) findViewById(R.id.tv_add_comment); // 댓글 등록 버튼

        // 인텐트를 통해 넘어온 NOTICE_ID 값을 받는다.
        Intent intent = getIntent();
        noticeID = intent.getExtras().getInt(NOTICE_ID);

        // 댓글 리사이클러뷰
        mDataList = new ArrayList<>();

        rv_notice_comment = findViewById(R.id.rv_notice_comment);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_notice_comment.setLayoutManager(linearLayoutManager); // LayoutManager 등록

        commentUploadButton(); // 댓글 업로드 시 빈칸 여부 확인 후 업로드 버튼 활성화
        }

    @Override
    protected void onStart() {
        super.onStart();
        getNoticeContents(); // 공지사항 내용 가져오기
        getNoticeComment(); // 댓글 리스트 가져오기
    }

    // =========================================================================================================
    // 공지사항 내용 가져오기
    private void getNoticeContents(){

        mContentsList = new ArrayList<>();

        Call<List<NoticeContents>> call = mServiceApi.getNoticeContents(noticeID);
        call.enqueue(new Callback<List<NoticeContents>>() {
            @Override
            public void onResponse(Call<List<NoticeContents>> call, Response<List<NoticeContents>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mContentsList = response.body();
                    if(!mContentsList.isEmpty()) {

                       noticeCreateAt = mContentsList.get(0).getCreate_at();
                       noticeTitle = mContentsList.get(0).getNotice_title();
                       noticeContents = mContentsList.get(0).getNotice_contents();
                       noticePin = mContentsList.get(0).getPin();
                       writerID = mContentsList.get(0).getWriter_id();
                       writerName = mContentsList.get(0).getWriter_name();
                       writerProfileImage = mContentsList.get(0).getWriter_profile();

                       mNoticeContents.setText(noticeContents);
                       mNoticeTitle.setText(noticeTitle);
                       mWriterName.setText(writerName);

                        if (!writerProfileImage.isEmpty()) {
                            // 현재 유저 프로필 이미지 보여주기
                            Glide.with(NoticeDetailActivity.this).load(writerProfileImage).centerCrop().into(mWriterProfile);

                        } else {
                            // 현재 유저 프로필 이미지 보여주기
                            Glide.with(NoticeDetailActivity.this).load(R.drawable.ic_user).centerCrop().into(mWriterProfile);
                        }

                       // 글 작성 시간 (create at)
                        TimeString timeString = new TimeString();
                        long getTime = Long.parseLong(noticeCreateAt);
                        mNoticeCreateAt.setText(TimeString.formatTimeString(getTime));
                    }
                }
            }
            @Override
            public void onFailure(Call<List<NoticeContents>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 댓글 리스트 가져오기
    private void getNoticeComment(){

        Call<List<NoticeCommentItem>> call = mServiceApi.getNoticeComment(noticeID);
        call.enqueue(new Callback<List<NoticeCommentItem>>() {
            @Override
            public void onResponse(Call<List<NoticeCommentItem>> call, Response<List<NoticeCommentItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mDataList = response.body();
                    if(!mDataList.isEmpty()) {
                        mEmpty.setVisibility(View.GONE);
                    }
                    NoticeCommentAdapter noticeCommentAdapter = new NoticeCommentAdapter(mDataList, NoticeDetailActivity.this, mListener);
                    rv_notice_comment.setAdapter(noticeCommentAdapter);
                    noticeCommentAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<NoticeCommentItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 댓글 업로드 시 빈칸 여부 확인
    private void commentUploadButton() {

        mComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 공지사항 제목 입력 여부 확인
                if (s.toString().trim().length()==0) {
                    mUploadComment.setTextColor(getColor(R.color.darkGray));
                } else {
                    mUploadComment.setTextColor(getColor(R.color.colorPrimary));
                    mUploadComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           saveNoticeComment();
                        }
                    });
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
    }

    // =========================================================================================================
    // 공지사항 댓글 DB 통신 후 업로드
    @SuppressLint("SimpleDateFormat")
    private void saveNoticeComment() {
        // DATE FORMAT
        String commentCreateAt =  String.valueOf(System.currentTimeMillis());
        sharedPreferences= NoticeDetailActivity.this.getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        uid = sharedPreferences.getString(USER_UID,"");
        String commentContents = mComment.getText().toString();

        Log.d("uid : ", uid);
        Log.d("noticeID : ", String.valueOf(noticeID));
        Log.d("commentContents : ", commentContents);
        Log.d("commentCreateAt : ", commentCreateAt);

        mServiceApi.saveNoticeComment(noticeID, uid, commentContents, commentCreateAt).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(NoticeDetailActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                if(result.getResult().equals("success")) {
                mComment.setText("");
                // 현재 액티비티 새로고침
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(NoticeDetailActivity.this, "회원가입 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 공지사항 글 상단 고정 해제, 수정, 삭제 기능
    private void popupMenu(View view, int noticeID) {
            // Display option menu
            PopupMenu popupMenu = new PopupMenu(NoticeDetailActivity.this, view);
            Menu menu = popupMenu.getMenu();

        // 공지사항 상단 고정 관련 메뉴 생성
        if(noticePin.equals("true")){
            menu.add(0, 0, 0, "상단 고정 해제");
        } else {
            menu.add(0, 1, 0, "상단 고정");
        }

            // 수정, 삭제 기능 메뉴
            popupMenu.getMenuInflater().inflate(R.menu.menu_notice, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        // 상단 고정 해제
                        case 0:
                            editNoticePin = false;
                            editFixTopNotice(editNoticePin);
                            break;

                        // 상단 고정
                        case 1:
                            editNoticePin = true;
                            editFixTopNotice(editNoticePin);
                            break;

                        // 글 수정
                        case R.id.menu_edit:
                            Intent intent = new Intent(NoticeDetailActivity.this, EditNoticeActivity.class);
                            intent.putExtra(NOTICE_ID, noticeID);
                            intent.putExtra(NOTICE_TITLE, noticeTitle);
                            intent.putExtra(NOTICE_CONTENTS, noticeContents);
                            intent.putExtra(NOTICE_PIN, noticePin);
                            startActivity(intent);
                            break;

                        // 글 삭제
                        case R.id.menu_delete:
                            AlertDialog.Builder alert = new AlertDialog.Builder(NoticeDetailActivity.this);
                            alert.setMessage("정말 삭제하시겠어요?");
                            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                     removeNotice();
                                }
                            });
                            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            alert.create().show();
                            break;

                        default:
                            break;
                    }
                    return false;
                }
            });
            popupMenu.show();
    }
    // =========================================================================================================
    // DB 연결 후 공지사항 글 삭제
    private void removeNotice() {

        Log.d("remove", String.valueOf(noticeID));

            mServiceApi.removeNotice(noticeID).enqueue(new Callback<ResultModel>() {
                // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    // 정상적으로 네트워크 통신 완료
                    ResultModel result = response.body();
                    Toast.makeText(NoticeDetailActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    // 성공적으로 DB 내 공지사항 삭제를 완료했을 경우 액티비티를 닫는다.
                    if(result.getResult().equals("success")) {
                       finish();
                    }
                }
                // 통신이 실패했을 경우 호출된다.
                @Override
                public void onFailure(Call<ResultModel> call, Throwable t) {
                    Toast.makeText(NoticeDetailActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                    Log.e("에러 발생", t.getMessage());
                }
            });
    }

    // =========================================================================================================
    // 공지사항 DB 통신 후 상단 고정 및 해제 수정
    @SuppressLint("SimpleDateFormat")
    private void editFixTopNotice(boolean editNoticePin) {

        mServiceApi.editFixTopNotice(noticeID, editNoticePin).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(NoticeDetailActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                if(result.getResult().equals("success")) {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(NoticeDetailActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }
    // =========================================================================================================
}