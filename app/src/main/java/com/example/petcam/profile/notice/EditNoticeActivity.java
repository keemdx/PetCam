package com.example.petcam.profile.notice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.NOTICE_CONTENTS;
import static com.example.petcam.function.App.NOTICE_CREATE_AT;
import static com.example.petcam.function.App.NOTICE_ID;
import static com.example.petcam.function.App.NOTICE_TITLE;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.NOTICE_PIN;
import static com.example.petcam.function.App.WRITER_ID;
import static com.example.petcam.function.App.makeStatusBarBlack;

public class EditNoticeActivity extends AppCompatActivity {

    private ServiceApi mServiceApi;
    private SharedPreferences sharedPreferences;
    private EditText mNoticeTitle, mNoticeContents;
    private CheckBox mPin;
    private int noticeID;
    private String noticeTitle, noticeContents, noticePin;
    private Button mUpdateButton;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    finish(); // 이 액티비티 화면을 닫는다.
                    break;

                // 공지 수정 완료 버튼을 클릭했을 경우,
                case R.id.btn_update_notice:
                    uploadEditNotice();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notice);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 인텐트를 통해 넘어온 NOTICE_ID 값을 받는다.
        Intent intent = getIntent();
        noticeID = intent.getExtras().getInt(NOTICE_ID);
        noticeTitle = intent.getExtras().getString(NOTICE_TITLE);
        noticeContents = intent.getExtras().getString(NOTICE_CONTENTS);
        noticePin = intent.getExtras().getString(NOTICE_PIN);


        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);
        findViewById(R.id.btn_update_notice).setOnClickListener(onClickListener);

        // EditText 선언
        mNoticeTitle = (EditText) findViewById(R.id.et_notice_title); // 공지사항 타이틀 입력하는 부분
        mNoticeContents = (EditText) findViewById(R.id.et_notice_contents); // 공지사항 내용 입력하는 부분
        mPin = (CheckBox) findViewById(R.id.cb_pin); // 공지사항 내용 입력하는 부분
        mUpdateButton = (Button) findViewById(R.id.btn_update_notice); // 공지사항 업로드 버튼

        mNoticeTitle.setText(noticeTitle);
        mNoticeContents.setText(noticeContents);

        // 상단 설정 (핀) 된 게시물일 경우,
        if(noticePin.equals("true")) {
            mPin.setChecked(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attenpt(); // 빈칸 여부 확인 후 업로드 버튼 활성화
    }

    // =========================================================================================================
    // 공지사항 업로드 시 빈칸 여부 확인
    private void attenpt() {

        mNoticeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                // 입력된 값이 null이 아닐 결우 입력값과 정해준 변수값이 같을 때 버튼을 활성화한다.
                if(s.toString().trim().length()==0 || mNoticeContents.getText().toString()==null) {
                    uploadButtonDisabled();
                } else {
                    uploadButtonEnabled();
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

        mNoticeContents.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // 입력된 값이 null이 아닐 결우 입력값과 정해준 변수값이 같을 때 버튼을 활성화한다.
                if(s.toString().trim().length()==0 || mNoticeTitle.getText().toString()==null) {
                    uploadButtonDisabled();
                } else {
                    uploadButtonEnabled();
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
    public void uploadEditNotice() {
        sharedPreferences= EditNoticeActivity.this.getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        String uid = sharedPreferences.getString(USER_UID,"");
        String editNoticeTitle = mNoticeTitle.getText().toString();
        String editNoticeContents = mNoticeContents.getText().toString();
        Boolean noticePin;

        // 상단 설정 (핀) 체크 박스가 체크 된 경우
        if(mPin.isChecked()) {
            noticePin = true;
        } else {   // 체크 박스가 해제 된 경우
            noticePin = false;
        }
        // 데이터 베이스 저장
        editNotice(noticeID, uid, editNoticeTitle, editNoticeContents, noticePin);
    }

    // =========================================================================================================
    // 공지사항 DB 통신 후 업로드
    @SuppressLint("SimpleDateFormat")
    private void editNotice(int noticeID, String uid, String noticeTitle, String noticeContents, boolean noticePin) {
        // DATE FORMAT - 회원가입 시간
        String noticeRegTime =  String.valueOf(System.currentTimeMillis());

        Log.e("uid : ", uid);
        Log.e("noticePin : ", String.valueOf(noticePin));
        Log.e("noticeTitle : ",noticeTitle);
        Log.e("noticeContents : ", noticeContents);
        Log.e("noticeRegTime : ", noticeRegTime);

        mServiceApi.editNotice(noticeID, uid, noticeTitle, noticeContents, noticeRegTime, noticePin).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(EditNoticeActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                if(result.getResult().equals("success")) {
                    finish();
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(EditNoticeActivity.this, "회원가입 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("회원가입 에러 발생", t.getMessage());
            }
        });
    }
    // =========================================================================================================
    // 업로드 버튼 비활성화
    private void uploadButtonDisabled() {
        mUpdateButton.setEnabled(false); // 업로드 버튼 비활성화
        mUpdateButton.setBackgroundDrawable(ContextCompat.getDrawable(EditNoticeActivity.this, R.drawable.btn_dg_round_corner));
    }

    // 업로드 버튼 활성화
    private void uploadButtonEnabled() {
        mUpdateButton.setEnabled(true); // 업로드 버튼 활성화
        mUpdateButton.setBackgroundDrawable(ContextCompat.getDrawable(EditNoticeActivity.this, R.drawable.btn_round_corner));
    }

}