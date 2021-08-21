package com.example.petcam.ui.profile.notice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.NOTICE_COMMENT_CONTENTS;
import static com.example.petcam.function.App.NOTICE_COMMENT_ID;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

public class EditCommentActivity extends AppCompatActivity {

    private ServiceApi mServiceApi;
    private SharedPreferences sharedPreferences;
    private EditText mCommentContents;
    private CheckBox mPin;
    private int commentID;
    private String commentContents;
    private TextView tv_edit_comment;
    private InputMethodManager imm;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    finish(); // 이 액티비티 화면을 닫는다.
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // 인텐트를 통해 넘어온 NOTICE_COMMENT ID, CONTENTS 값을 받는다.
        Intent intent = getIntent();
        commentID = intent.getExtras().getInt(NOTICE_COMMENT_ID);
        commentContents = intent.getExtras().getString(NOTICE_COMMENT_CONTENTS);


        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);

        // EditText 선언
        tv_edit_comment = (TextView) findViewById(R.id.tv_edit_comment); // 댓글 등록 버튼
        mCommentContents = (EditText) findViewById(R.id.et_comment); // 댓글 내용 입력하는 부분
        mCommentContents.setText(commentContents);

    }

    @Override
    protected void onStart() {
        super.onStart();
        attenpt(); // 빈칸 여부 확인 후 업로드 버튼 활성화
    }

    // =========================================================================================================
    // 공지사항 업로드 시 빈칸 여부 확인
    private void attenpt() {

        mCommentContents.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                // 입력된 값이 null이 아닐 결우 입력값과 정해준 변수값이 같을 때 버튼을 활성화한다.
                if (s.toString().trim().length() == 0 || mCommentContents.getText().toString().equals(commentContents)) {
                    editButtonDisabled();
                } else {
                    editButtonEnabled();
                    // 댓글 수정 완료 버튼을 클릭했을 경우,
                    tv_edit_comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            uploadEditNoticeComment();
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
    // 수정한 공지사항 업로드
    public void uploadEditNoticeComment() {
        sharedPreferences = EditCommentActivity.this.getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        String uid = sharedPreferences.getString(USER_UID, "");
        String editNoticeComment = mCommentContents.getText().toString();

        // 데이터 베이스 저장
        editNoticeComment(uid, editNoticeComment);
    }

    // =========================================================================================================
    // 공지사항 DB 통신 후 업로드
    @SuppressLint("SimpleDateFormat")
    private void editNoticeComment(String uid, String editNoticeComment) {
        // DATE FORMAT - 회원가입 시간
        String editCommentTime = String.valueOf(System.currentTimeMillis());

        Log.e("commentID", String.valueOf(commentID));
        Log.e("editCommentTime", editCommentTime);

        mServiceApi.editNoticeComment(commentID, uid, editNoticeComment, editCommentTime).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(EditCommentActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                if (result.getResult().equals("success")) {
                    finish();
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(EditCommentActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 업로드 버튼 비활성화
    @SuppressLint("ResourceAsColor")
    private void editButtonDisabled() {
        tv_edit_comment.setClickable(false); // 업로드 버튼 비활성화
        tv_edit_comment.setTextColor(getColor(R.color.darkGray));
    }

    // 업로드 버튼 활성화
    @SuppressLint("ResourceAsColor")
    private void editButtonEnabled() {
        tv_edit_comment.setClickable(true); // 업로드 버튼 활성화
        tv_edit_comment.setTextColor(getColor(R.color.colorPrimary));
    }
}