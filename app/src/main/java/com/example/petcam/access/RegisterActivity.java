package com.example.petcam.access;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: RegisterActivity
 *
 * Comment
 * 이 액티비티는 신규 사용자들의 회원가입 용도로 만들어졌습니다.
 **/


public class RegisterActivity extends AppCompatActivity {

    private ColorStateList colorStateList;
    private String userInputEmail;
    private TextView mLinkify, mDialogUserEmail;
    private EditText mName, mEmail, mPassword, mAuthKey;
    private SimpleDateFormat mRegTime;
    private Button mJoinButton, mDialogResendButton, mDialogAuthButton;
    private MaterialButton mAuthDialogButton;
    private ImageView mDialogCloseButton;
    private ServiceApi mServiceApi;

    // 다이알로그
    private LayoutInflater dialog; // LayoutInflater
    private View dialogLayout; // Layout을 담을 View
    private Dialog authDialog; // Dialog 객체

    // 클릭 리스너
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // 뒤로 가기를 클릭했을 경우
                case R.id.iv_backToIntro:
                    finish(); // 이 액티비티를 닫는다.
                    break;

                // 이메일 인증하기 버튼을 클릭했을 경우
                case R.id.btn_auth_dialog:
                    // 중복 이메일 체크 후 가능한 이메일을 입력했을 경우 다이알로그를 띄운다.
                    startEmailCheck();
                    break;

                // 이메일 인증 다이알로그 - 다이알로그 닫기
                case R.id.iv_dialog_close:
                    authDialog.dismiss();
                    break;

                // 이메일 인증 다이알로그 - 이메일 재발송 버튼을 클릭했을 경우
                case R.id.btn_resend:
                    userInputEmail = mEmail.getText().toString(); // 사용자가 입력한 이메일
                    sendMail(userInputEmail); // 입력한 이메일로 메일을 다시 보낸다.
                    break;

                // 이메일 인증 다이알로그 - 이메일 인증 후 완료 버튼을 클릭했을 경우
                case R.id.btn_auth_email:
                    // 입력한 키를 서버로 보내서 일치 여부 확인한다.
                    attemptKey();
                    break;

                // 가입하기 버튼을 클릭했을 경우
                case R.id.btn_join:
                    // 회원가입을 시도한다.
                    attemptJoin();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 텍스트뷰 해당 텍스트에 링크 걸기
        mLinkify = (TextView) findViewById(R.id.tv_linkify); // 서비스 이용 약관
        String text = "I agree to the Terms of Service and Privacy Policy";
        mLinkify.setText(text);

        Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher match, String url) {
                return "";
            }
        };

        Pattern pattern1 = Pattern.compile("Terms of Service");
        Pattern pattern2 = Pattern.compile("Privacy Policy");

        /** 개인 프로젝트이므로 링크는 임의로 사용 **/
        Linkify.addLinks(mLinkify, pattern1, "https://play.google.com/intl/ko_kr/about/play-terms/", null, mTransform);
        Linkify.addLinks(mLinkify, pattern2, "https://varietn.tistory.com/10", null, mTransform);

        // 다이알로그 객체 만들기
        dialog = LayoutInflater.from(RegisterActivity.this);
        dialogLayout = dialog.inflate(R.layout.dialog_auth_mail, null);
        authDialog = new Dialog(RegisterActivity.this);
        authDialog.setContentView(dialogLayout);

        // 다이알로그 바깥 부분 선택해도 닫히지 않는다.
        authDialog.setCanceledOnTouchOutside(false);

        // ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // UI 선언
        mName = (EditText) findViewById(R.id.et_join_nickname);
        mEmail = (EditText) findViewById(R.id.et_join_email);
        mPassword = (EditText) findViewById(R.id.et_join_password);
        mAuthDialogButton = findViewById(R.id.btn_auth_dialog); // 이메일 인증하기 버튼
        mJoinButton = (Button) findViewById(R.id.btn_join); // 가입하기 버튼

        // 이메일 인증 다이알로그 UI 선언
        mDialogCloseButton = (ImageView) dialogLayout.findViewById(R.id.iv_dialog_close);
        mDialogUserEmail = (TextView) dialogLayout.findViewById(R.id.tv_user_email);
        mAuthKey = (EditText) dialogLayout.findViewById(R.id.et_auth_key);
        mDialogResendButton = (Button) dialogLayout.findViewById(R.id.btn_resend); // 이메일 재발송 버튼
        mDialogAuthButton = (Button) dialogLayout.findViewById(R.id.btn_auth_email); // 이메일 인증 완료 버튼


        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_backToIntro).setOnClickListener(onClickListener);
        mDialogCloseButton.setOnClickListener(onClickListener);
        mDialogResendButton.setOnClickListener(onClickListener);
        mDialogAuthButton.setOnClickListener(onClickListener);
        mAuthDialogButton.setOnClickListener(onClickListener);
        mJoinButton.setOnClickListener(onClickListener);

    }

    private void attemptKey() {

        mAuthKey.setError(null);

        String email = mEmail.getText().toString(); // 사용자가 입력한 이메일
        String key = mAuthKey.getText().toString(); // 사용자가 입력한 인증번호

        boolean cancel = false;
        View focusView = null;

        if (key.isEmpty()) { // 인증번호 칸이 비어있을 경우
            mAuthKey.setError("Please enter a verification code");
            focusView = mAuthKey;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            sendAuthKey(email, key); // 입력한 인증번호의 일치 여부를 서버와 통신하여 확인한다.
        }

    }

    // 회원가입을 시도한다.
    @SuppressLint("SimpleDateFormat")
    private void attemptJoin() {

        mName.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);

        String name = mName.getText().toString(); // 사용자가 입력한 닉네임
        String email = mEmail.getText().toString(); // 사용자가 입력한 이메일
        String password = mPassword.getText().toString(); // 사용자가 입력한 비밀번호

        boolean cancel = false;
        View focusView = null;

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            mPassword.setError("Please enter your password");
            focusView = mPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPassword.setError("Please enter your password with 6-10 digits including number and alphabet");
            focusView = mPassword;
            cancel = true;
        }

        // 이메일의 유효성 검사
        if (email.isEmpty()) {
            mEmail.setError("Please enter your email");
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail.setError("Please enter a valid email address");
            focusView = mEmail;
            cancel = true;
        }

        // 이름의 유효성 검사
        if (name.isEmpty()) {
            mName.setError("Please enter your nickname");
            focusView = mName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startJoin(name, email, password); // 준비가 완료되었을 경우, 회원가입을 한다.
        }
    }

    // 신규 사용자가 입력한 이메일이 이미 가입된 계정인지 확인한다.
    private void startEmailCheck() {
        // 입력된 이메일 주소
        userInputEmail = mEmail.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // 이메일의 유효성 검사
        if (userInputEmail.isEmpty()) {
            mEmail.setError("Please enter your email");
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(userInputEmail)) {
            mEmail.setError("Please enter a valid email address");
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mServiceApi.userEmailCheck(userInputEmail).enqueue(new Callback<ResultModel>() {
                // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    // 정상적으로 네트워크 통신 완료
                    ResultModel result = response.body();

                    // [error] 이미 가입된 계정일 경우, 이미 가입된 계정이라는 메시지 띄워준다.
                    if (result.getResult().equals("error")) {
                        mEmail.setError(result.getMessage());

                        // [success] 가입이 가능한 계정일 경우 다이알로그를 띄워준다.
                    } else if (result.getResult().equals("success")) {
                        sendMail(userInputEmail);
                    }
                }

                // 통신이 실패했을 경우 호출된다.
                @Override
                public void onFailure(Call<ResultModel> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "이메일 인증 에러 발생", Toast.LENGTH_SHORT).show();
                    Log.e("이메일 인증 에러 발생", t.getMessage());
                }
            });
        }
    }

    // 회원가입 - 이메일 인증을 위한 통신
    private void sendMail(String email) {

        // 사용자가 입력한 이메일로 인증 번호를 보낸다.
        mServiceApi.userSendEmail(email).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();

                if(result.getResult().equals("success")) {
                    authDialog.show(); // 인증 다이알로그를 띄운다.
                    mDialogUserEmail.setText(email); // 사용자가 입력한 이메일을 보여준다.
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "메일 전송 실패", Toast.LENGTH_SHORT).show();
                Log.e("메일 전송 실패", t.getMessage());
            }
        });
    }

    // 이메일 인증을 위한 통신
    private void sendAuthKey(String email, String key) {

        mServiceApi.userSendKey(email, key).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists"})
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();

                if(result.getResult().equals("success")) {
                    // 인증 완료 후 다이알로그 종료
                    authDialog.dismiss();
                    // 인증하기 -> 인증 완료로 버튼 텍스트 변경
                    colorStateList = getResources().getColorStateList(R.color.darkGray);
                    mAuthDialogButton.setStrokeColor(colorStateList);
                    mAuthDialogButton.setText("Verified");
                    mAuthDialogButton.setEnabled(false);
                    mAuthDialogButton.setTextColor(Color.parseColor("#A8A8A8"));

                } else if (result.getResult().equals("error")) {
                    // 인증이 실패했을 경우,
                    mAuthKey.setError(result.getMessage());
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "메일 전송 실패", Toast.LENGTH_SHORT).show();
                Log.e("메일 전송 실패", t.getMessage());
            }
        });
    }

    // 회원가입 진행
    @SuppressLint("SimpleDateFormat")
    private void startJoin(String name, String email, String password) {
        mRegTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // DATE FORMAT - 회원가입 시간
        String regTime =  mRegTime.format(new Date());

        mServiceApi.userJoin(name, email, password, regTime).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                if(result.getResult().equals("success")) { // 회원가입에 성공했을 경우,
                    finish(); // 이 액티비티를 닫는다 -> IntroActivity로 이동한다.
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "회원가입 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("회원가입 에러 발생", t.getMessage());
            }
        });
    }

    // 정규식을 이용한 이메일 형식 체크
    private boolean isEmailValid(String email) {
        String mail = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(mail);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    // 비밀번호 형식 체크 - 영문, 숫자 혼합하여 6~20자리 이내
    private boolean isPasswordValid(String password) {
        String pwd = "^.*(?=.{6,10})(?=.*[0-9])(?=.*[a-zA-Z]).*$";
        Pattern p = Pattern.compile(pwd);
        Matcher m = p.matcher(password);
        return m.matches();
    }

}