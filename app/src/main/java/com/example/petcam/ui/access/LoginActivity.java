package com.example.petcam.ui.access;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcam.databinding.ActivityIntroBinding;
import com.example.petcam.databinding.ActivityLoginBinding;
import com.example.petcam.ui.main.MainActivity;
import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.AUTO_LOGIN_KEY;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_EMAIL;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: LoginActivity
 * <p>
 * Comment
 * 이 액티비티는 사용자들의 로그인을 위해 만들어졌습니다.
 **/


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ServiceApi mServiceApi;
    private EditText mEmail, mPassword;
    private SharedPreferences pref;
    private String mName, mUid, mPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // EditText 선언
        mEmail = binding.etLoginEmail;
        mPassword = binding.etLoginPassword;

        binding.btnForgotPassword.setOnClickListener(v -> startForgotPasswordActivity());
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.ivBackToIntro.setOnClickListener(v -> finish());

    }


    // 로그인 시도
    private void attemptLogin() {
        mEmail.setError(null);
        mPassword.setError(null);

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
        if (email.isEmpty()) { // 이메일 칸이 비어있는 경우
            mEmail.setError("Please enter your email");
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) { // 이메일 형식이 아닌 경우
            mEmail.setError("Please enter a valid email address");
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startLogin(email, password); // 오류가 없을 경우, 로그인한다.
        }
    }

    // =========================================================================================================

    private void startForgotPasswordActivity() {
        Intent intent_forgot_pw = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent_forgot_pw);
    }

    // 로그인 (서버 통신)
    private void startLogin(String email, String password) {
        mServiceApi.userLogin(email, password).enqueue(new Callback<ResultModel>() {

            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();

                // [error] 이메일 - 존재하지 않는 계정입니다.
                if (result.getResult().equals("error")) {
                    mEmail.setError(result.getMessage());

                    // [error] 비밀번호 - 비밀번호가 틀렸습니다.
                } else if (result.getResult().equals("pwd_error")) {
                    mPassword.setError(result.getMessage());

                    // [success] 로그인 성공했을 경우
                } else if (result.getResult().equals("success")) {
                    mName = result.getUserName();
                    mUid = result.getUid();
                    mPhoto = result.getUserPhoto();

                    // 쉐어드 프리퍼런스에 로그인 정보를 저장한다.
                    pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(AUTO_LOGIN_KEY, "success"); // 자동 로그인 상태 저장
                    editor.putString(USER_UID, mUid);
                    editor.putString(USER_IMAGE, mPhoto);
                    editor.putString(USER_EMAIL, mEmail.getText().toString());
                    editor.putString(USER_NAME, mName);
                    editor.commit();

                    // 로그인을 완료하고 홈(메인) 화면으로 넘어간다.
                    startMainActivity();
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "로그인 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("로그인 에러 발생", t.getMessage());
                t.printStackTrace(); // 에러 발생시 에러 발생 원인 단계별로 출력해 준다.
            }
        });
    }
    // =========================================================================================================

    private void startMainActivity() {
        // 로그인을 완료하고 홈(메인) 화면으로 넘어간다.
        Intent intent_login = new Intent(getApplicationContext(), MainActivity.class);
        intent_login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent_login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent_login);
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

