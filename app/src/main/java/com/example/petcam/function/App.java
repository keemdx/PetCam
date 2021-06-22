package com.example.petcam.function;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;

import com.google.gson.annotations.SerializedName;

/**
 * Class: App
 *
 * Comment
 * 이 클래스는 Application 클래스로 언제 어디서나 변수/메소드를 불러들일 수 있습니다.
 * 간편한 변수/메소드 접근을 위해 만들었습니다.
 * ex) 로그인 액티비티에서 받아온 사용자 아이디와 닉네임, 프사 등 저장 => 어디서나 사용가능
 **/

public class App extends Application {

    // 로그 찍을 때 필요한 TAG
    public static String TAG ="LOG";

    // 로그인한 유저를 전역에 사용할 변수
    public static String USER_ID; // 로그인한 유저의 고유 ID 번호
    public static String USER_NAME; // 로그인한 유저의 이름

    // Shared Preference 키값
    public static String LOGIN_STATUS = "login_status"; // 유저 로그인 상태

    public static String AUTO_LOGIN_KEY = "auto_login_key"; // 유저가 자동로그인을 한 상태 인지 아닌지 확인 (Value: Success / Fail)
    public static String USER_UID = "user_uid"; // 로그인한 유저의 ID를 꺼낼 때 사용 (Value: User ID 값 -> DB에 key값을 보낸 후 유저 정보 받을 때 사용)
    public static String USER_NICKNAME = "user_nickname"; // 로그인한 유저의 이름
    public static String USER_EMAIL = "user_email";
    public static String USER_IMAGE = "user_image_url"; // 로그인한 유저의 프로필 사진 주소
    public static String USER_STATUS = "user_status"; // 유저 상태 메시지
    public static String CHANNEL_ID = "channel_id"; // 채널 아이디 (상대 유저 아이디)

    public static String NOTICE_TITLE = "notice_title"; // 공지사항 제목
    public static String NOTICE_CONTENTS = "notice_contents"; // 공지사항 컨텐츠 (내용)
    public static String NOTICE_CREATE_AT = "notice_create_at"; // 공지사항 작성 날짜
    public static String NOTICE_PIN = "notice_pin"; // 공지사항 상단 고정 여부
    public static String NOTICE_ID = "notice_id"; // 공지사항 글 ID 번호
    public static String WRITER_ID = "writer_id"; // 작성자 ID 번호

    public static String NOTICE_COMMENT_CONTENTS = "notice_comment_contents"; // 공지사항 댓글 컨텐츠 (내용)
    public static String NOTICE_COMMENT_ID = "notice_comment_id"; // 공지사항 댓글 ID 번호

    /* 팬보드 */
    public static String FANBOARD_CONTENT = "fanboard_content"; // 팬보드 컨텐츠 (내용)
    public static String FANBOARD_ID = "fanboard_id"; // 팬보드 ID 번호

    /* 채팅 */
    public static String ROOM_ID = "room_id"; // 채팅 방 번호 ID
    public static String ROOM_NAME = "room_name"; // 채팅 방 이름
    public static String ROOM_STATUS = "room_status"; // 채팅 방 상태
    public static String ROOM_USER_COUNT = "room_user_count"; // 채팅 방 유저 수
    public static String NEW_CHATROOM_ID = "new_chatroom_id";
    public static String MESSAGE_TEXT = "message_text"; // 채팅 메시지 내용
    public static String MESSAGE_TYPE = "message_type"; // 채팅 메시지 타입
    public static String CHAT_DATA = "chat_data"; // 보낸 채팅 메시지 정보
    public static String RECEIVE_DATA = "receive_data"; // 받은 채팅 메시지 정보
    public static String SEND_TIME = "send_time"; // 채팅 메시지 전송 시간
    public static String NOTIFI_ROOM_ID = "notifi_room_id"; // 알림 클릭 시 전달되는 채팅 방 번호 ID
    public static String USER_INVITE = "user_invite"; // 초대한 유저 닉네임

    /** 스트리밍 **/
    public static String STREAMING_ROOM_ID = "streaming_room_id"; // 채팅 방 번호 ID
    public static String BROADCAST_LIVE_MSG = "broadcast_live_message"; // 브로드캐스트 보내는 메시지

    /*-------------------------- AWS S3 --------------------------*/
    public static String BUCKET_NAME = "petcam";
    public static String ACCESS_KEY = "xxxxxxxxxx";
    public static String SECRET_KEY = "xxxxxxxxxx";

    // URI
    public static String USER_PROFILE_URL = "https://petcam.s3.ap-northeast-2.amazonaws.com/profile-image/";
    public static String STREAMING_URL = "https://petcam.s3.ap-northeast-2.amazonaws.com/streaming/";
    /*-----------------------------------------------------------*/

    // 해당 액티비티에서 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
    public static void makeStatusBarBlack(Activity activity) {

        if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            activity.getWindow().setStatusBarColor(Color.BLACK);
        }
    }

}
