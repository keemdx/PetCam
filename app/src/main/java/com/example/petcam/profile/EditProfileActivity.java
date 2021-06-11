package com.example.petcam.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.function.App;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.ACCESS_KEY;
import static com.example.petcam.function.App.BUCKET_NAME;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.SECRET_KEY;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_PROFILE_URL;
import static com.example.petcam.function.App.USER_STATUS;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: EditProfileActivity
 *
 * Comment
 * 사용자가 프로필 정보를 수정하는 화면입니다.
 **/

public class EditProfileActivity extends AppCompatActivity {

    private ServiceApi mServiceApi;
    private SharedPreferences pref;
    private CircleImageView mProfileImage;
    private EditText et_edit_nickname, et_edit_status;
    private String EditImageUrl, editName, editStatus, fileName, userUid, userName, userPhoto, userStatus;
    private Uri imageUri;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 뒤로가기 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    finish(); // 이 액티비티 화면을 닫는다.
                    break;

                // 프로필 이미지 추가/수정 버튼을 클릭했을 경우, (카메라)
                case R.id.civ_add_profile_image:
                    // 이미지를 선택할 수 있도록 image picker를 띄워준다.
                    onAddImageClicked();
                    // (싱글 이미지) 선택된 이미지를 Glide에 담는다.

                    break;

                // 수정하기 버튼을 클릭했을 경우,
                case R.id.btn_edit_profile:

                    if (imageUri != null) { // 프로필 이미지가 수정되었다면,
                        uploadImageFile(); // 프로필 이미지를 스토리지에 저장한다.
                        EditImageUrl = USER_PROFILE_URL + fileName; // 스토리지에 저장 후 수정된 이미지 url

                    } else { // 프로필 이미지가 수정되지 않은 경우,
                        EditImageUrl = userPhoto; // 기존 이미지 url
                    }
                    editName = et_edit_nickname.getText().toString(); // 유저가 작성한 닉네임
                    editStatus = et_edit_status.getText().toString(); // 유저가 작성한 상태 메시지

                    // 쉐어드 프리퍼런스에 프로필 수정 정보를 저장한다.
                    SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(USER_IMAGE, EditImageUrl);
                    editor.putString(USER_NAME, editName);
                    editor.putString(USER_STATUS, editStatus);
                    editor.commit();

                    // DB 프로필 정보 수정
                    startEditProfile(EditImageUrl, editName, userUid, editStatus);

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // UI 선언
        mProfileImage = findViewById(R.id.civ_profile_image);
        et_edit_nickname = findViewById(R.id.et_edit_nickname);
        et_edit_status = findViewById(R.id.et_edit_status);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.btn_edit_profile).setOnClickListener(onClickListener);
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);
        findViewById(R.id.civ_add_profile_image).setOnClickListener(onClickListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // 'SharedPreferences'에 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userPhoto = pref.getString(USER_IMAGE,""); // 유저 프로필 이미지
        userName = pref.getString(USER_NAME,""); // 유저 닉네임
        userUid = pref.getString(USER_UID,""); // 유저 UID
        userStatus = pref.getString(USER_STATUS,""); // 유저 UID

        // 유저 프로필 이미지가 있는 경우,
        if (!userPhoto.isEmpty()) {
            // 현재 유저 프로필 이미지 보여주기
            Glide.with(EditProfileActivity.this).load(userPhoto).centerCrop().into(mProfileImage);

        } else {
            // 기본 이미지 보여주기
            Glide.with(EditProfileActivity.this).load(R.drawable.ic_user).centerCrop().into(mProfileImage);
        }

        // 현재 유저 닉네임 보여주기
        et_edit_nickname.setText(userName);
        // 현재 유저 상태 메시지 보여주기
        et_edit_status.setText(userStatus);
    }

    // 프로필 이미지 선택 (TedBottomPicker)
    public void onAddImageClicked() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                //TedBottomPicker 이미지 피커 라이브러리
                TedBottomPicker.with(EditProfileActivity.this)
                        .setPeekHeight(5000) // 피커 액티비티가 올라오는 높이 설정
                        .showTitle(false)
                        .setCompleteButtonText("완료")
                        .setPreviewMaxCount(1000)
                        .setSelectMinCount(0)
                        .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                imageUri = uri;
                                Glide.with(EditProfileActivity.this).load(uri).centerCrop().into(mProfileImage);
                            }
                        });
            }
            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(EditProfileActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

            }
        };
        checkPermission(permissionlistener); // 권한 체크
    }

    // 권한 체크
    private void checkPermission(PermissionListener permissionlistener) {

        TedPermission.with(EditProfileActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("사진 및 파일을 저장하기 위해서는 접근 권한이 필요합니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    // AWS s3 스토리지에 선택한 이미지 업로드
    public void uploadImageFile() {

        // 이미지 이름 지정
        fileName = "image_" + System.currentTimeMillis();

        // 선택한 이미지 파일
        File file = new File(imageUri.getPath());

        // AWS s3 스토리지 설정
        AWSCredentials awsCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(EditProfileActivity.this).build();
        TransferNetworkLossHandler.getInstance(EditProfileActivity.this);

        // profile-image 경로에 이미지 업로드
        TransferObserver uploadObserver = transferUtility.upload(BUCKET_NAME + "/profile-image", fileName, file);
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(App.TAG, "onStateChanged: " + id + ", " + state.toString());
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;
                Log.d(App.TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(App.TAG, ex.getMessage());
            }
        });
    }

    // DB 유저 프로필 수정 요청 (서버 통신)
    @SuppressLint("SimpleDateFormat")
    private void startEditProfile(String uri, String name, String uid, String status) {

        mServiceApi.editProfile(uri, name, uid, status).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(EditProfileActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                if(result.getResult().equals("success")) {
                    finish(); // 이 액티비티 닫기
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }
}