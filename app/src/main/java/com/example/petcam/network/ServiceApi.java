package com.example.petcam.network;

import com.example.petcam.chatting.ChatMemberItem;
import com.example.petcam.chatting.ChatroomItem;
import com.example.petcam.chatting.ChattingDataItem;
import com.example.petcam.chatting.ChattingItem;
import com.example.petcam.chatting.SearchUserItem;
import com.example.petcam.main.FollowingItem;
import com.example.petcam.main.PopularItem;
import com.example.petcam.profile.FollowListItem;
import com.example.petcam.profile.fanboard.FanboardItem;
import com.example.petcam.profile.notice.FixTopNoticeItem;
import com.example.petcam.profile.notice.NoticeCommentItem;
import com.example.petcam.profile.notice.NoticeContents;
import com.example.petcam.profile.notice.NoticeItem;
import com.example.petcam.profile.vod.VODItem;
import com.example.petcam.streaming.LiveChatItem;
import com.example.petcam.streaming.ViewersItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/** 서버에 어떤 식으로 요청을 보내고 응답 받을 것인지 정의한다. **/

public interface ServiceApi {

    // =========================================================================================================

    /** [로그인, 회원가입 등 유저 관련] **/

    // 회원가입 - 가입 가능한 이메일인지 확인
    @FormUrlEncoded
    @POST("email-check.php")
    Call<ResultModel> userEmailCheck(@Field("userEmail") String userEmail);

    // 회원가입 - 이메일 발송 (인증)
    @FormUrlEncoded
    @POST("send-email.php")
    Call<ResultModel> userSendEmail(@Field("userEmail") String userEmail);

    // 비밀번호 재설정 - 비밀번호 재설정이 가능한 이메일인지 확인
    @FormUrlEncoded
    @POST("email-check.php")
    Call<ResultModel> FixPasswordEmailCheck(@Field("fixPasswordUserEmail") String userEmail);

    // 비밀번호 재설정 - 이메일 발송
    @FormUrlEncoded
    @POST("send-email.php")
    Call<ResultModel> passwordResetSendEmail(@Field("passwordResetEmail") String passwordResetEmail);

    // 회원가입 - 이메일 인증 (키 대조)
    @FormUrlEncoded
    @POST("key-check.php")
    Call<ResultModel> userSendKey(@Field("userEmail") String userEmail,
                                  @Field("userKey") String userKey);

    // 회원가입
    @FormUrlEncoded
    @POST("join.php")
    Call<ResultModel> userJoin(@Field("userName") String userName,
                               @Field("userEmail") String userEmail,
                               @Field("userPwd") String userPwd,
                               @Field("userRegtime") String userRegtime);

    // 로그인
    @FormUrlEncoded
    @POST("login.php")
    Call<ResultModel> userLogin(@Field("userEmail") String userEmail,
                                @Field("userPwd") String userPwd);

    // 구글 연동 로그인
    @FormUrlEncoded
    @POST("google-login.php")
    Call<ResultModel> userGoogleLogin(@Field("userPhoto") String userPhoto,
                                      @Field("userName") String userName,
                                      @Field("userEmail") String userEmail);

    // 유저 프로필 수정
    @FormUrlEncoded
    @POST("edit-profile.php")
    Call<ResultModel> editProfile(@Field("userPhoto") String userPhoto,
                                  @Field("userName") String userName,
                                  @Field("userUid") String userUid,
                                  @Field("userStatus") String userStatus);

    // =========================================================================================================

    /** [공지사항 관련] **/

    // 공지사항 업로드
    @FormUrlEncoded
    @POST("notice.php")
    Call<ResultModel> saveNotice(@Field("userUid") String uid,
                                 @Field("noticeTitle") String noticeTitle,
                                 @Field("noticeContents") String noticeContents,
                                 @Field("noticeRegTime") String noticeRegTime,
                                 @Field("noticePin") boolean noticePin);

    // 공지사항 수정
    @FormUrlEncoded
    @POST("notice-edit.php")
    Call<ResultModel> editNotice(@Field("noticeID") int noticeID,
                                 @Field("userUid") String uid,
                                 @Field("noticeTitle") String noticeTitle,
                                 @Field("noticeContents") String noticeContents,
                                 @Field("noticeRegTime") String noticeRegTime,
                                 @Field("noticePin") boolean noticePin);

    // 공지사항 상단 설정, 해제 수정
    @FormUrlEncoded
    @POST("notice-fix-top-edit.php")
    Call<ResultModel> editFixTopNotice(@Field("noticeID") int noticeID,
                                       @Field("noticePin") boolean noticePin);

    // 공지사항 내용 가져오기
    @GET("get-notice-contents.php")
    Call<List<NoticeContents>> getNoticeContents (@Query("noticeID") int noticeID);

    // 공지사항 리스트 가져오기
    @GET("get-notice.php")
    Call<List<NoticeItem>> getNotice (@Query("userUid") String uid);

    // 상단 고정 공지사항 리스트 가져오기
    @GET("get-fix-top-notice.php")
    Call<List<FixTopNoticeItem>> getFixTopNotice (@Query("userUid") String uid);

    // 공지사항 삭제 처리 요청
    @FormUrlEncoded
    @POST("notice-remove.php")
    Call<ResultModel> removeNotice(@Field("noticeID") int noticeID);

    // 공지사항 댓글 리스트 가져오기
    @GET("get-notice-comment.php")
    Call<List<NoticeCommentItem>> getNoticeComment (@Query("noticeID") int noticeID);

    // 공지사항 댓글 업로드
    @FormUrlEncoded
    @POST("notice-comment.php")
    Call<ResultModel> saveNoticeComment( @Field("noticeID") int noticeID,
                                         @Field("userUid") String uid,
                                         @Field("commentContents") String commentContents,
                                         @Field("commentCreateAt") String commentCreateAt);

    // 공지사항 댓글 수정
    @FormUrlEncoded
    @POST("edit-notice-comment.php")
    Call<ResultModel> editNoticeComment( @Field("commentID") int commentID,
                                         @Field("userUid") String uid,
                                         @Field("commentContents") String commentContents,
                                         @Field("commentEditAt") String commentEditAt);

    // 공지사항 댓글 삭제 처리 요청
    @FormUrlEncoded
    @POST("remove-notice-comment.php")
    Call<ResultModel> removeComment(@Field("noticeID") int noticeID);

    // =========================================================================================================

    /** [팬보드 관련] **/

    // 팬보드 리스트 가져오기
    @GET("get-fanboard.php")
    Call<List<FanboardItem>> getFanboard (@Query("userUid") String uid);

    // 팬보드 업로드
    @FormUrlEncoded
    @POST("save-fanboard.php")
    Call<ResultModel> saveFanboard (@Field("writerID") String writerID,
                                 @Field("channelID") String channelID,
                                 @Field("content") String content,
                                 @Field("createAt") String createAt);

    // 팬보드 게시물 수정
    @FormUrlEncoded
    @POST("edit-fanboard.php")
    Call<ResultModel> editFanboard( @Field("fanboardID") String fanboardID,
                                    @Field("content") String content);

    // 팬보드 게시물 수정
    @FormUrlEncoded
    @POST("remove-fanboard.php")
    Call<ResultModel> removeFanboard( @Field("fanboardID") String fanboardID);

    // =========================================================================================================

    /** [프로필 및 채널 관련] **/

    // 유저의 팔로잉, 팔로워 가져오기
    @FormUrlEncoded
    @POST("get-follow.php")
    Call<ResultModel> getFollow (@Field("ID") String ID);

    // 팔로잉 리스트 가져오기
    @FormUrlEncoded
    @POST("get-following-list.php")
    Call<List<FollowListItem>> getFollowingList(@Field("userID") String userID);

    // 팬 리스트 가져오기
    @FormUrlEncoded
    @POST("get-fan-list.php")
    Call<List<FollowListItem>> getFanList(@Field("userID") String userID);

    // 채널 정보 가져오기
    @FormUrlEncoded
    @POST("get-channel.php")
    Call<ResultModel> getChannel(@Field("channelID") String channelID,
                                 @Field("userID") String userID);

    // 팔로우 (팬 등록)
    @FormUrlEncoded
    @POST("save-follow.php")
    Call<ResultModel> saveFollow (@Field("userID") String userID,
                                    @Field("channelID") String channelID);

    // 언팔로우 (팬 해제)
    @FormUrlEncoded
    @POST("save-unfollow.php")
    Call<ResultModel> saveUnfollow (@Field("userID") String userID,
                                    @Field("channelID") String channelID);

    // =========================================================================================================

    /** [다이렉트 메시지 (개인 채팅) 관련] **/

    // 메시지 보낼 유저 리스트 가져오기
    @FormUrlEncoded
    @POST("get-users.php")
    Call<List<SearchUserItem>> getUsers( @Field("uid") String uid,
                                        @Field("roomID") String roomID);

    // 채팅 룸 생성, 및 채팅 유저 저장
    @FormUrlEncoded
    @POST("create-chatroom.php")
    Call<ResultModel> createChatroom (@FieldMap Map<String, Object> map);

    // 채팅 초대하기
    @FormUrlEncoded
    @POST("invite-chatroom.php")
    Call<ResultModel> inviteChatroom (@FieldMap Map<String, Object> map,
                                      @Field("roomID") String roomID);

    // 채팅 룸 리스트 가져오기
    @FormUrlEncoded
    @POST("get-chatroom.php")
    Call<List<ChatroomItem>> getChatroom (@Field("userUid") String uid);

    // 채팅 룸 정보 가져오기
    @FormUrlEncoded
    @POST("get-chatroom-info.php")
    Call<ResultModel> getChatroomInfo (@Field("roomID") String roomID,
                                       @Field("userID") String userID);
    // 채팅 유저 가져오기
    @FormUrlEncoded
    @POST("get-chatroom-member.php")
    Call<List<ChatMemberItem>> getChatMember(@Field("roomID") String roomID,
                                             @Field("userID") String userID);

    // 채팅 메시지 리스트 가져오기
    @FormUrlEncoded
    @POST("get-message.php")
    Call<List<ChattingDataItem>> getChatMessage (@Field("userID") String userID,
                                                 @Field("roomID") String roomID);

    // 채팅 룸 마지막 메시지 시간 비교해서 boolean 값 가져오기
    @FormUrlEncoded
    @POST("chat-time-check.php")
    Call<ResultModel> checkCurrentTime (@Field("roomID") String roomID,
                                        @Field("date") String date);

    // 채팅 메시지 업로드
    @FormUrlEncoded
    @POST("save-message.php")
    Call<ResultModel> saveMessage(@Field("status") String status,
                                  @Field("userID") String userID,
                                  @Field("roomID") String roomID,
                                  @Field("message") String message,
                                  @Field("sendTime") String sendTime);

    // 채팅 룸 안 읽은 메시지 숫자 추가
    @FormUrlEncoded
    @POST("add-message-num.php")
    Call<ResultModel> addMessageNum (@Field("roomID") String roomID);

    // 채팅 룸 안 읽은 메시지 숫자 리셋
    @FormUrlEncoded
    @POST("reset-message-num.php")
    Call<ResultModel> resetMessageNum (@Field("roomID") String roomID,
                                       @Field("userID") String userID);

    // 채팅 나가기 시 -> 룸 멤버 삭제
    @FormUrlEncoded
    @POST("chat-member-remove.php")
    Call<ResultModel> removeChatMember(@Field("roomID") String roomID,
                                       @Field("userID") String userID);

    // =========================================================================================================

    /** [라이브 스트리밍 관련] **/

    // 스트리밍을 위한 룸 생성
    @FormUrlEncoded
    @POST("streaming-room.php")
    Call<ResultModel> createStreamingRoom (@Field("roomID") String roomID,
                                          @Field("streamerID") String streamerID,
                                          @Field("roomTitle") String roomTitle,
                                          @Field("roomStatus") String roomStatus,
                                          @Field("viewer") int viewer,
                                           @Field("createAt") String createAt);

    // 스트리밍 룸에 Thumbnail 전송 (방송 시작 후 캡쳐된 이미지)
    @FormUrlEncoded
    @POST("streaming-room.php")
    Call<ResultModel> saveThumbnail (@Field("uri") String uri,
                                  @Field("roomID") String roomID);

    //  라이브 중인 친구 리스트 가져오기
    @FormUrlEncoded
    @POST("get-live-friends.php")
    Call<List<FollowingItem>>getLiveNowFriends (@Field("userID") String userID);

    // 실시간 방송 뷰어(시청자) 순으로 가져오기
    @FormUrlEncoded
    @POST("get-live-rooms.php")
    Call<List<PopularItem>>getHotLiveRooms(@Field("userID") String userID);

    // DB 와 연결해서 roomStatus 를 (ON -> OFF)로 바꾼다.
    @FormUrlEncoded
    @POST("streaming-room.php")
    Call<ResultModel> saveRoomStatus (@Field("roomID") String roomID,
                                      @Field("status") String status);

    // 현재 viewer 수 가져오기
    @FormUrlEncoded
    @POST("get-viewer-count.php")
    Call<ResultModel> getViewerCount (@Field("roomID") String roomID);

    // viewer 저장하기
    @FormUrlEncoded
    @POST("set-viewer-count.php")
    Call<ResultModel> setViewerCount (@Field("viewerStatus") String viewerStatus,
                                      @Field("roomID") String roomID,
                                      @Field("viewer") String userID);

    // 라이브 종료 시 보여줄 시청자 프로필 사진, 룸 thumbnail 가져오기
    @FormUrlEncoded
    @POST("get-live-result.php")
    Call<List<ViewersItem>> getLiveResult (@Field("roomID") String roomID);

    // 채팅 저장
    @FormUrlEncoded
    @POST("save-live-chat.php")
    Call<ResultModel> saveLiveChat (@Field("roomID") String roomID,
                                    @Field("userID") String userID,
                                    @Field("message") String message,
                                    @Field("time") String time,
                                    @Field("liveTime") String liveTime);

    // 라이브 종료 시 보여줄 시청자 프로필 사진, 룸 thumbnail 가져오기
    @FormUrlEncoded
    @POST("get-live-chat.php")
    Call<List<LiveChatItem>> getLiveChat (@Field("roomID") String roomID);

    // =========================================================================================================

    /** [VOD 관련] **/
    @FormUrlEncoded
    @POST("get-vod-list.php")
    Call<List<VODItem>> getVODList (@Field("userID") String userID);


}