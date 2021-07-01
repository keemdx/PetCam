package com.example.petcam.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcam.R;
import com.example.petcam.access.IntroActivity;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: SettingsActivity
 *
 * Comment
 * 이 액티비티는 설정 목록을 보여줍니다.
 **/

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences pref;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 뒤로가기 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    finish(); // 이 액티비티 화면을 닫는다.
                    break;

                // 로그아웃 버튼을 클릭했을 경우,
                case R.id.layout_logout:
                    // 로그아웃 다이알로그를 띄운다.
                    alertDialog(view);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);
        findViewById(R.id.layout_logout).setOnClickListener(onClickListener); // 로그아웃 버튼
    }

    // 로그아웃 - 팝업 다이알로그
    public void alertDialog(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
        alert.setMessage("Are you sure?");
        alert.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                // editor.clear()는 auto에 들어있는 모든 정보를 기기에서 지웁니다.
                editor.clear();
                editor.commit();
                startWelcomeActivity();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // 취소
            }
        });
        alert.create().show();
    }

    // 로그아웃 후 인트로 페이지로 이동
    private void startWelcomeActivity() {
        Toast.makeText(SettingsActivity.this, "Logout", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SettingsActivity.this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}
