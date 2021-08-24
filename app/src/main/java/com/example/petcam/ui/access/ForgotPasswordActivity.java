package com.example.petcam.ui.access;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcam.R;
import com.example.petcam.databinding.ActivityForgotPasswordBinding;
import com.example.petcam.databinding.ActivityMainBinding;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: ForgotPasswordActivity
 * <p>
 * Comment
 * 유저의 비밀번호를 찾기 위해 비밀번호 재설정 링크를 보내는 화면입니다.
 **/

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private EditText mEmail;
    private ServiceApi mServiceApi;
    private String userInputEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        mEmail = binding.etUserEmail;
        binding.ivClose.setOnClickListener(v -> {
            finish();
        });

        binding.btnSendEmail.setOnClickListener(v -> {
            startEmailCheck();
        });

    }

    // 입력한 이메일이 가입되어 있는 계정인지 확인한다.
    private void startEmailCheck() {
        // 입력된 이메일 주소
        userInputEmail = mEmail.getText().toString(); // 사용자가 입력한 이메일
        boolean cancel = false;
        View focusView = null;

        // 이메일의 유효성 검사
        if (userInputEmail.isEmpty()) {
            mEmail.setError("이메일을 입력하세요.");
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(userInputEmail)) {
            mEmail.setError("올바른 이메일 주소를 입력하세요.");
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mServiceApi.FixPasswordEmailCheck(userInputEmail).enqueue(new Callback<ResultModel>() {
                // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    // 정상적으로 네트워크 통신 완료
                    ResultModel result = response.body();

                    // [error] 가입되어 있지 않은 이메일의 경우, 오류 메시지를 띄워준다.
                    if (result.getResult().equals("error")) {
                        mEmail.setError(result.getMessage());

                        // [success] 비밀번호 재설정이 가능한 계정일 경우 다이알로그를 띄워준다.
                    } else if (result.getResult().equals("success")) {
                        sendMail(userInputEmail); // 입력된 이메일 주소로 이메일을 발송한다.
                    }
                }

                // 통신이 실패했을 경우 호출된다.
                @Override
                public void onFailure(Call<ResultModel> call, Throwable t) {
                    Toast.makeText(ForgotPasswordActivity.this, "이메일 인증 에러 발생", Toast.LENGTH_SHORT).show();
                    Log.e("이메일 인증 에러 발생", t.getMessage());
                }
            });
        }
    }

    // 비밀번호 재설정 - 이메일 전송
    private void sendMail(String email) {

        // 사용자가 입력한 이메일로 재설정 메일을 보낸다.
        mServiceApi.passwordResetSendEmail(email).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();

                if (result.getResult().equals("success")) {
                    String sendEmail = result.getMessage();
                    showDialog(sendEmail);
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "이메일 전송 실패", Toast.LENGTH_SHORT).show();
                Log.e("이메일 전송 실패", t.getMessage());
            }
        });
    }

    void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this)
                .setTitle("이메일 발송 완료")
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // 정규식을 이용한 이메일 형식 체크
    private boolean isEmailValid(String email) {
        String mail = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(mail);
        Matcher m = p.matcher(email);
        return m.matches();
    }
}