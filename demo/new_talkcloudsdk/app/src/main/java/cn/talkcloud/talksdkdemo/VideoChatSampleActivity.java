package cn.talkcloud.talksdkdemo;

import android.Manifest;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fatangare.logcatviewer.service.LogcatViewerFloatingView;
import com.fatangare.logcatviewer.utils.LogcatViewer;
import com.talkcloud.roomsdk.TKAudioFrame;
import com.talkcloud.roomsdk.TKMediaFrameObserver;
import com.talkcloud.roomsdk.TKRoomManagerObserver;
import com.talkcloud.roomsdk.TKRoomManager;
import com.talkcloud.roomsdk.RoomUser;
import com.talkcloud.roomsdk.TKVideoFrame;
import com.talkcloud.roomsdk.TkAudioStatsReport;
import com.talkcloud.roomsdk.TkVideoStatsReport;
import com.talkcloud.roomsdk.VideoProfile;

import org.json.JSONException;
import org.json.JSONObject;
import org.tkwebrtc.EglBase;
import org.tkwebrtc.EglRenderer;
import org.tkwebrtc.RendererCommon;
import org.tkwebrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cn.talkcloud.talksdkdemo.adapter.ChatMessageAdapter;
import cn.talkcloud.talksdkdemo.entity.ChatMessageModel;
import cn.talkcloud.talksdkdemo.utils.ToastUtils;
import wei.mark.standout.StandOutWindow;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


/**
 * Created by Administrator on 2018/4/10/010.
 */

public class VideoChatSampleActivity extends AppCompatActivity implements TKRoomManagerObserver,TKMediaFrameObserver {

    private static final String TAG = VideoChatSampleActivity.class.getSimpleName();
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    private ArrayList<VideoItem> videoItems = new ArrayList<VideoItem>();
    HashMap<String, Boolean> playingMap = new HashMap<String, Boolean>();
    ArrayList<RoomUser> playingList = new ArrayList<RoomUser>();
    PowerManager pm;
    PowerManager.WakeLock mWakeLock;
    private PopupWindow pw =null;
    private ChatMessageAdapter mChatAdapter =null;
    private RecyclerView rv;
    //消息
    private List<ChatMessageModel> mMessagelist = null;
    ImageView iv_camer, iv_mute, iv_finsh;
    ImageView iv_switch,iv_av,iv_disting,iv_hands,iv_message;
    LinearLayout containerSelf;
    LinearLayout containerOther;
    Button btn_switch_av;
    Spinner sp_videoprofiles;
    boolean videoIsPublish = true;
    boolean audioIsPublish = true;
    private boolean isclean = false;
    private AlertDialog.Builder builder = null;
    List<String> videoProfiles = new ArrayList<String>();
    private final static int[][] VIDEO_DEFINES = {{80, 60}, {176, 144}, {320, 180}, {320, 240}, {640, 480}, {1280, 720}, {1920, 1080}};

    int testCount = 0;
    boolean isTest = false;
    private static String host = "";
    private static String account = "";
    private static String type = "";
    private static String password = "";
    private static boolean islogcat = false;
    private static boolean isSwithc = false;
    private static boolean ishands = false;
    private static boolean ischat = false;
    private static boolean isJoinroom = false;
    private int checkedItemId = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.videochatsample);

        account = getIntent().getStringExtra("account");
        type = getIntent().getStringExtra("type");
        password = getIntent().getStringExtra("password");
        islogcat = getIntent().getBooleanExtra("isLogcat", false);
        host = getIntent().getStringExtra("host");
        if (islogcat)
            LogcatViewer.showLogcatLoggerView(getApplicationContext());

        iv_camer = (ImageView) findViewById(R.id.iv_camer);
        iv_mute = (ImageView) findViewById(R.id.iv_mute);
        iv_finsh = (ImageView) findViewById(R.id.iv_finsh);

        iv_switch = (ImageView) findViewById(R.id.iv_switch);
        iv_av = (ImageView) findViewById(R.id.iv_av);
        iv_disting = (ImageView) findViewById(R.id.iv_disting);
        iv_hands = (ImageView) findViewById(R.id.iv_hands);
        iv_message = (ImageView) findViewById(R.id.iv_message);


        containerSelf = (LinearLayout) findViewById(R.id.local_video_view_container);
        containerOther = (LinearLayout) findViewById(R.id.remote_video_view_container);
        iv_hands.setImageResource(R.mipmap.handsonclick);
        //初始化聊天界面
        InitPopView();
        //切换摄像头
        iv_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSwithc)
                    TKRoomManager.getInstance().selectCameraPosition(true);
                else
                    TKRoomManager.getInstance().selectCameraPosition(false);
                isSwithc = !isSwithc;
            }
        });
        //音视频切换
        iv_av.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TKRoomManager.getInstance().switchOnlyAudioRoom(!TKRoomManager.getInstance().isAudioOnlyRoom());
            }
        });

        //免提
        iv_hands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ishands) {
                    iv_hands.setImageResource(R.mipmap.handsonclick);
                    TKRoomManager.getInstance().useLoudSpeaker(true);
                } else {
                    iv_hands.setImageResource(R.mipmap.hands);
                    TKRoomManager.getInstance().useLoudSpeaker(false);
                }
                ishands = !ishands;
            }
        });

        //分辨率
        iv_disting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initList();
            }
        });
        //挂断/离开
        iv_finsh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isJoinroom) {
                    isExit = true;
                    TKRoomManager.getInstance().leaveRoom();
                } else {
                    finish();
                }

            }
        });
        //消息界面
        iv_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ischat) {
                    iv_message.setImageResource(R.mipmap.message_press);
                    ShowPopMessage();
                } else {
                    iv_message.setImageResource(R.mipmap.message_default);
                    if (pw!=null)
                        pw.dismiss();
                }
                ischat = !ischat;
            }
        });


        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            joinroom();
        }
    }

    //聊天界面
    private void ShowPopMessage() {
        pw.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
    }
    private void InitPopView(){
        Display dp = getWindowManager().getDefaultDisplay();
        int height = dp.getHeight();
        View msView = LayoutInflater.from(this).inflate(R.layout.activity_chatmessage, null, false);
        final EditText editText = (EditText) msView.findViewById(R.id.et_message);
        ImageView ivSend = (ImageView) msView.findViewById(R.id.iv_send);
        rv = (RecyclerView) msView.findViewById(R.id.rlv_message);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setNestedScrollingEnabled(true);
        mMessagelist = new ArrayList<>();
        mChatAdapter = new ChatMessageAdapter(mMessagelist);

        rv.setAdapter(mChatAdapter);
        //重写键盘按钮及监听键盘发送
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String content = editText.getText().toString().trim();
                    if (!TextUtils.isEmpty(content)) {
                        int state =TKRoomManager.getInstance().sendMessage(content, "__all", new HashMap<String, Object>());
                        if (state == 0) {
                            editText.setText("");
                        }
                    }
                }
                return false;
            }
        });
        //发送
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(content)) {
                    int state =TKRoomManager.getInstance().sendMessage(content, "__all", new HashMap<String, Object>());
                    if (state == 0) {
                        editText.setText("");
                    }
                }
            }
        });

        pw = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, height / 2);
        pw.setContentView(msView);
        //pop空白监听
        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                iv_message.setImageResource(R.mipmap.message_default);
            }
        });
        pw.setBackgroundDrawable(new ColorDrawable(0x00000000));
        pw.setAnimationStyle(R.style.popwin_anim_style);
        //pop中edittext不弹出软键盘
        pw.setFocusable(true);
        pw.setOutsideTouchable(true);
        pw.setTouchable(true);

    }

    private void initList() {
        if (!isclean) {
            videoProfiles.clear();
            for (int i = 0; i < VIDEO_DEFINES.length; i++) {
                videoProfiles.add(VIDEO_DEFINES[i][0] + "X" + VIDEO_DEFINES[i][1]);
            }

            isclean = true;
        }

        String items[] = (String[]) videoProfiles.toArray(new String[0]);
        builder = new AlertDialog.Builder(this,videoProfiles.size());

        builder.setSingleChoiceItems(items, checkedItemId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                checkedItemId = which;
                VideoProfile videoProfile = new VideoProfile();
                videoProfile.width = VIDEO_DEFINES[which][0];
                videoProfile.height = VIDEO_DEFINES[which][1];
                videoProfile.maxfps = 25;
                TKRoomManager.getInstance().setVideoProfile(videoProfile);
            }
        }).create().show();


    }

    private void joinroom() {
        //初始化操作
        HashMap<String,Object> initParams = new HashMap<>();
//        initParams.put("AudioSource", AudioManager.STREAM_MUSIC);
//        initParams.put("autoSubscribeAV",true);
        TKRoomManager.init(getApplicationContext(), "demo",initParams);
        TKRoomManager.getInstance().registerRoomObserver(this);
        TKRoomManager.getInstance().registerMediaFrameObserver(this);

        //加入课堂
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("serial", account);//课堂号
        params.put("password", password); // 进入课堂人员身份（例如：老师。学生）密码
        params.put("userrole", Integer.parseInt(type));//身份
//        TKRoomManager.getInstance().setTestServer("192.168.1.252",8890);
//        TKRoomManager.getInstance().joinRoom("global.talk-cloud.neiwang", 80, "ad", params, null, true);
        if(TextUtils.isEmpty(host))
            host = "global.talk-cloud.net";
//        TKRoomManager.getInstance().joinRoom("demo.talk-cloud.net", 80, "ad", params, null, true);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("xiao_01","beforeJoinRoom   ts = "+System.currentTimeMillis());
//            }
//        });

        TKRoomManager.getInstance().joinRoom(host, 80, "ad", params, null, true);
        ToastUtils.showToast(this,"请稍后...");
    }

    int heigth;
    int width;
    @Override
    protected void onStart() {
        super.onStart();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();
        doPlayVideo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWakeLock.release();
//        TKRoomManager.getInstance().pauseLocalCamera();
    }

    private boolean isExit = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isExit = true;
            if(islogcat){
                StandOutWindow.closeAll(getApplicationContext(), LogcatViewerFloatingView.class);
            }
            TKRoomManager.getInstance().leaveRoom();

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /***
     * 成功进入房间
     */
    @Override
    public void onRoomJoined() {
        Log.d(TAG, "roomManagerRoomJoined");
        //加入课堂成功之后开始发布自己的音视频
        isJoinroom = true;
        TKRoomManager.getInstance().publishAudio();
        TKRoomManager.getInstance().publishVideo();
//        TKRoomManager.getInstance().changeUserPublish(TKRoomManager.getInstance().getMySelf().peerId,3);
        TKRoomManager tk=TKRoomManager.getInstance();
        if (tk != null) {
            int height = tk.get_room_video_height();
            Log.e("分辨率高", String.valueOf(height));
            for (int i = 0; i < VIDEO_DEFINES.length; i++) {
                if (height == VIDEO_DEFINES[i][0]);
                    checkedItemId = i;
            }
        }



        if (isTest) {
            TKRoomManager.getInstance().leaveRoom();
        }
//        openSpeaker();

    }
    private void openSpeaker() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION) {
            try {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.ROUTE_SPEAKER);
                int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                TKRoomManager.getInstance().useLoudSpeaker(true);
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setSpeakerphoneOn(true);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            75,
                            AudioManager.STREAM_MUSIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * 离开课堂
     */
    @Override
    public void onRoomLeaved() {
        if (isTest) {
            if (testCount <= 100) {
                testCount++;
                Log.e("xiao_02", "testCount = " + testCount);
                joinroom();
                return;
            }
        }
        if (isExit) {
            TKRoomManager.getInstance().registerRoomObserver(null);
            for (int i = 0; i < videoItems.size(); i++) {
                videoItems.get(i).sf_video.release();
            }
            isExit = false;
            this.finish();
        }
    }

//    @Override
//    public void onConnectionLost() {
//
//    }

    private void getPlayingList() {
        playingList.clear();

        for (String p : playingMap.keySet()) {
            if (playingMap.get(p)) {
                RoomUser u = TKRoomManager.getInstance().getUser(p);
                if (playingList.size() >= 7) {
                    return;
                }
                playingList.add(u);
            }
        }
    }

    private int sitpos = -1;

    private void doPlayVideo() {

        for (int i = 0; i < playingList.size(); i++) {

            final RoomUser user = playingList.get(i);
            boolean hasSit = false;
            for (int j = 0; j < videoItems.size(); j++) {
                if (videoItems.get(j).peerid.equals(user.peerId)) {
                    hasSit = true;
                }
            }
            sitpos = -1;
            for (int z = 0; z < videoItems.size(); z++) {
                if (videoItems.get(z).peerid.equals(user.peerId)) {
                    hasSit = true;
                    sitpos = z;
                }
            }
            if (!hasSit) {
                final VideoItem vi = new VideoItem();
                vi.lin_parent = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video, null);
                vi.sf_video = (SurfaceViewRenderer) vi.lin_parent.findViewById(R.id.sf_video);
               /* vi.iv_display = (ImageView) vi.lin_parent.findViewById(R.id.iv_display);*/

                vi.btn_video = (AutoFitTextView) vi.lin_parent.findViewById(R.id.btn_video);
                vi.btn_audio = (AutoFitTextView) vi.lin_parent.findViewById(R.id.btn_audio);
                vi.btn_video.setVisibility(View.GONE);
                vi.btn_audio.setVisibility(View.GONE);

                vi.sf_video.init(EglBase.create().getEglBaseContext(), null);
                vi.peerid = user.peerId;

                if (user.peerId.equals(TKRoomManager.getInstance().getMySelf().peerId)) {

                    containerSelf.removeAllViews();

                    containerSelf.addView(vi.lin_parent);
                  /*  vi.iv_display.setVisibility(View.VISIBLE);*/
                    if (user.publishState > 1) {
                        TKRoomManager.getInstance().playVideo(user.peerId, vi.sf_video, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
                    }
                    if (user.publishState >= 1&&user.publishState!=2)
                        TKRoomManager.getInstance().playAudio(user.peerId);

                    vi.sf_video.setZOrderMediaOverlay(true);

                    iv_camer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (videoIsPublish) {
                                v.setSelected(false);
                                videoIsPublish = false;
//                                iv_camer.setImageDrawable(getResources().getDrawable(R.drawable.cameraoff));
//                                vi.sf_video.setVisibility(View.VISIBLE);
                                TKRoomManager.getInstance().publishVideo();
//                                TKRoomManager.getInstance().playVideo(user.peerId, vi.sf_video, RendererCommon.ScalingType.SCALE_ASPECT_FILL);

                            } else {
                                v.setSelected(true);
                                videoIsPublish = true;
//                                iv_camer.setImageDrawable(getResources().getDrawable(R.drawable.cameraon));
                                vi.sf_video.setVisibility(View.INVISIBLE);
//                                TKRoomManager.getInstance().unPlayVideo(user.peerId);
                                TKRoomManager.getInstance().unPublishVideo();
                            }
                        }
                    });

                    iv_mute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (audioIsPublish) {
                                audioIsPublish = false;
                                v.setSelected(false);
//                                iv_mute.setImageDrawable(getResources().getDrawable(R.drawable.mute));
//                                TKRoomManager.getInstance().playAudio(user.peerId);
                                TKRoomManager.getInstance().publishAudio();

                            } else {
                                audioIsPublish = true;
                                v.setSelected(true);
//                                iv_mute.setImageDrawable(getResources().getDrawable(R.drawable.unmute));
//                                TKRoomManager.getInstance().unPlayAudio(user.peerId);
                                TKRoomManager.getInstance().unPublishAudio();

                            }
                        }
                    });
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) vi.lin_parent.getLayoutParams();
                    params.gravity = Gravity.TOP;
                    vi.lin_parent.setLayoutParams(params);
                    RelativeLayout.LayoutParams vparams = (RelativeLayout.LayoutParams) vi.sf_video.getLayoutParams();
                    vparams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    vi.sf_video.setLayoutParams(vparams);
                    videoItems.add(vi);

                } else {

                    if (containerOther.getChildCount() < 4) {
                        vi.sf_video.setZOrderOnTop(true);
                        if (user.publishState > 1) {
                            TKRoomManager.getInstance().playVideo(user.peerId, vi.sf_video, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
                        }
                        if (user.publishState >= 1&&user.publishState!=2)
                            TKRoomManager.getInstance().playAudio(user.peerId);



                        containerOther.addView(vi.lin_parent);
                        TKRoomManager.getInstance().resumeLocalCamera();
                        DisplayMetrics dm = getResources().getDisplayMetrics();
                        heigth = dm.heightPixels;
                        width = dm.widthPixels;
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) vi.lin_parent.getLayoutParams();
                        params.height = 200;
//                        params.weight = 1;
                        params.width = width / 4;
                        params.gravity = Gravity.BOTTOM;
                        vi.lin_parent.setLayoutParams(params);
                        RelativeLayout.LayoutParams vparams = (RelativeLayout.LayoutParams) vi.sf_video.getLayoutParams();
                        vparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        vi.sf_video.setLayoutParams(vparams);
                        vi.sf_video.addFrameListener(new EglRenderer.FrameListener() {
                            @Override
                            public void onFrame(Bitmap bitmap) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("xiao_01", "firstFrame       ts = " + System.currentTimeMillis());
                                    }
                                });

                            }
                        }, 0);
                        videoItems.add(vi);
                    }
                }

            } else {

                if (user.publishState > 1 && user.publishState < 4) {
                    if (videoItems.get(sitpos).isPlayVideo) {
                        if (user.publishState > 1) {
                            videoItems.get(sitpos).sf_video.setVisibility(View.VISIBLE);
                            TKRoomManager.getInstance().playVideo(user.peerId, videoItems.get(sitpos).sf_video, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
                        }
                    }
                    if (videoItems.get(sitpos).isPlayAudio&&user.publishState >= 1&&user.publishState!=2) {
                        TKRoomManager.getInstance().playAudio(user.peerId);
                    }
                } else {
                    videoItems.get(sitpos).sf_video.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void doUnPlayVideo(RoomUser user, boolean isleave) {
        for (int i = 0; i < videoItems.size(); i++) {
            if (videoItems.get(i).peerid.equals(user.peerId)) {
                if (user.publishState == 0 || isleave) {
                    if (!user.peerId.equals(TKRoomManager.getInstance().getMySelf().peerId)) {
                        for (int j = 0; j < videoItems.size(); j++) {
                            if (videoItems.get(j).peerid.equals(user.peerId)) {
                                RoomUser u = TKRoomManager.getInstance().getUser(user.peerId);
                                if(u!=null){
                                    if(u.publishState<=1){
                                        TKRoomManager.getInstance().unPlayVideo(user.peerId);
                                        videoItems.get(i).sf_video.setVisibility(View.INVISIBLE);
                                    }else{
                                        if(u.publishState < 2)
                                            TKRoomManager.getInstance().unPlayVideo(user.peerId);
                                        if(u.publishState<1||u.publishState == 2) {
                                            TKRoomManager.getInstance().unPlayAudio(user.peerId);
                                        }
                                    }
                                }
                                containerOther.removeView(videoItems.get(j).lin_parent);
                            }
                        }

                    } else {
                        RoomUser u = TKRoomManager.getInstance().getUser(user.peerId);
                        if(u!=null){
                            if(u.publishState<=1){
                                TKRoomManager.getInstance().unPlayVideo(user.peerId);
                            }else{
                                if(u.publishState != 2)
                                    TKRoomManager.getInstance().unPlayVideo(user.peerId);
                                if(u.publishState<1||u.publishState == 2)
                                    TKRoomManager.getInstance().unPlayAudio(user.peerId);
                            }
                        }
                        containerSelf.removeAllViews();
                    }
                    videoItems.get(i).sf_video.release();
                    videoItems.remove(i);
                } else {
                    RoomUser u = TKRoomManager.getInstance().getUser(user.peerId);
                    if(u!=null){

                        if(u.publishState<=1){
                            TKRoomManager.getInstance().unPlayVideo(user.peerId);
                            videoItems.get(i).sf_video.setVisibility(View.INVISIBLE);
                        }else{
                            if(u.publishState < 2)
                                TKRoomManager.getInstance().unPlayVideo(user.peerId);
                            if(u.publishState<1||u.publishState == 2) {
                                TKRoomManager.getInstance().unPlayAudio(user.peerId);
                            }
                        }
                    }
                }
            }
        }
    }

    /***
     * @param user  用户对象
     * @param inList 在自己之前进入；false：在自己之后进入
     *  有用户进入房间
     */
    @Override
    public void onUserJoined(RoomUser user, boolean inList) {
        Log.d(TAG, "roomManagerPeerJoined " + user.nickName + (inList ? " inlist" : ""));
        /*if (inList && (user.role == 0 && RoomManager.getInstance().getMySelf().role == 0 ||
                (RoomManager.getInstance().getRoomType() == 0 && user.role == RoomManager.getInstance().getMySelf().role)))
            RoomManager.getInstance().evictUser(user.peerId);*/
    }

    /***
     *  有用户离开房间
     * user：用户对象
     */
    @Override
    public void onUserLeft(RoomUser user) {
        playingMap.remove(user.peerId);
        getPlayingList();
        doUnPlayVideo(user, true);
        doPlayVideo();
    }

    /***
     * @param user  用户对象
     * @param changedProperties 发生变化的属性
     *@param s
     *  有用户的属性发生了变化
     */
    @Override
    public void onUserPropertyChanged(RoomUser user, Map<String, Object> changedProperties, String s) {
//        Log.d(TAG, "roomManagerPeerChanged " + user.nickName + " " + changedProperties);
//        if (playingMap.containsKey(user.peerId) && user.publishState > 0) {
//            playingMap.put(user.peerId, user.publishState > 0);
//            if (user.publishState > 0) {
//                getPlayingList();
//                doPlayVideo();
//            } else {
//                getPlayingList();
//                doUnPlayVideo(user, false);
//            }
//        } else {
//            getPlayingList();
//            doUnPlayVideo(user, false);
//        }
    }

    /***
     * 用户视频状态发生改变
     */
    public void onUserPublishState(String peerId, int state) {
        //有人发布视频的时候播放视频
        if(!TKRoomManager.getInstance().getMySelf().peerId.equals(peerId)){
            Log.e("xiao_____2","onUserPublishState peerid = "+peerId+"  time="+System.currentTimeMillis());
        }
        if (state > 0) {
            playingMap.put(peerId, state > 0);
            getPlayingList();
            doUnPlayVideo(TKRoomManager.getInstance().getUser(peerId), false);
            doPlayVideo();
        } else {
            playingMap.remove(peerId);
            getPlayingList();
            doUnPlayVideo(TKRoomManager.getInstance().getUser(peerId), false);
        }
        if (peerId.equals(TKRoomManager.getInstance().getMySelf().peerId)) {
            if (state > 0 && state != 2 && state < 4) {
                iv_mute.setSelected(true);
                iv_mute.setImageDrawable(getResources().getDrawable(R.drawable.mute));
                audioIsPublish = false;
            } else {
                iv_mute.setSelected(false);
                iv_mute.setImageDrawable(getResources().getDrawable(R.drawable.unmute));
                audioIsPublish = true;
            }
            if (state > 1 && state < 4 && state != 1) {
                iv_camer.setSelected(true);
                iv_camer.setImageDrawable(getResources().getDrawable(R.drawable.cameraoff));
                videoIsPublish = false;
            } else {
                iv_camer.setSelected(false);
                iv_camer.setImageDrawable(getResources().getDrawable(R.drawable.cameraon));
                videoIsPublish = true;
            }

        }

    }

    /***
     * 自己被踢出房间
     * @param reason 被踢的原因 0 同身份踢掉，1 被老师请出房间
     */
    @Override
    public void onKickedout(int reason) {
        isExit = true;
        finish();
    }

    /***
     *    收到聊天消息
     */
    @Override
    public void onMessageReceived(RoomUser roomUser, JSONObject message, long ts) {
        String content =null;
        try {
            content = message.getString("msg");
            if (roomUser != null && !TextUtils.isEmpty(content)) {
                if (roomUser.peerId.equals(TKRoomManager.getInstance().getMySelf().peerId))
                    mMessagelist.add(new ChatMessageModel(roomUser.peerId,roomUser.nickName,content,1));
                else
                    mMessagelist.add(new ChatMessageModel(roomUser.peerId, roomUser.nickName, content, 0));

                mChatAdapter.notifyDataSetChanged();
                rv.scrollToPosition(mChatAdapter.getItemCount()-1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void onShareMediaState(String peerId, int state, Map<String, Object> attrs) {
        if (state > 0) {
            playingMap.put(peerId, true);
        } else {
            playingMap.remove(peerId);
        }
    }


    /***
     * 回放时清除所有信令的回调
     */
    @Override
    public void onPlayBackClearAll() {

    }

    /***
     * 更新回放进度
     * @param currentTime  当前时间
     */
    @Override
    public void onPlayBackUpdateTime(long currentTime) {

    }

    /***
     * 回放开始
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    @Override
    public void onPlayBackDuration(long startTime, long endTime) {

    }

    //回放结束
    @Override
    public void onPlayBackEnd() {

    }



    public void onShareScreenState(String peerId, int state) {
        if (state > 0) {
            playingMap.put(peerId, true);
        } else {
            playingMap.remove(peerId);
        }
    }

    /***
     * @param errorCode
     * 错误码：
     *10001 摄像头丢失
     *10002 socket 5次链接失败，应退出教室
     *10004 udp链接异常
     *
     * 进入房间的错误：
     *3001 服务器过期
     *3002 公司被冻结
     *3003 教室被删除或过期
     *4007 教室不存在
     *4008 教室密码错误
     *4110 该教室需要密码，请输入密码
     */
    public void onError(final int errorCode, final String errMsg) {
        Log.d("classroom", "errorcode = " + errorCode + "---" + errMsg);
        if (errorCode != 0)
            Toast.makeText(VideoChatSampleActivity.this,"进入教室失败，错误码："+errorCode,Toast.LENGTH_LONG).show();
        if(errorCode == 10002||errorCode == 3001||errorCode == 3002||errorCode == 3003||errorCode == 4007||errorCode == 4008||errorCode == 4110||errorCode == 4012){
//            isExit = true;
            TKRoomManager.getInstance().leaveRoom();
            this.finish();
        }
    }

    /***
     *
     * @param warning
     * 10001 摄像头打开
     * 10002 摄像头关闭
     */
    public void onWarning(int warning) {

    }


    public void onShareFileState(String peerId, int state) {
        if (state > 0) {
            playingMap.put(peerId, true);
        } else {
            playingMap.remove(peerId);
        }
    }

    @Override
    public void onAudioVolume(String s, int i) {
        if(s.equals(TKRoomManager.getInstance().getMySelf().peerId)){
            Log.e("xiao","peerid = "+s+"----volume = "+i);
        }
    }

    @Override
    public void onPlayBackRoomJson(int i, String s) {

    }

    @Override
    public void onVideoStatsReport(String peerId, TkVideoStatsReport statsReport) {
        Log.e(String.format("onVideoStatsReport peerid = %s", peerId),statsReport.toString());
        statsReport = null;
    }

    @Override
    public void onAudioStatsReport(String peerId, TkAudioStatsReport statsReport) {
        Log.e(String.format("onAudioStatsReport peerid = %s", peerId),statsReport.toString());
        statsReport = null;
    }

    @Override
    public void onGetRoomUsersBack(int i, ArrayList<RoomUser> arrayList) {

    }

    @Override
    public void onGetRoomUserNumBack(int i, int i1) {

    }

    @Override
    public void onAudioRoomSwitch(String peerid, boolean isSwitch) {
        RoomUser user = TKRoomManager.getInstance().getUser(peerid);
        if (user != null) {
            //ture 开启纯音教室 false关闭
            if (isSwitch)
                ToastUtils.showToast(this,user.nickName+"切换为纯音教室");
            else
                ToastUtils.showToast(this,user.nickName+"切换为音视频教室");


        }



    }


    @Override
    public void onFirstVideoFrame(String peerID, int width, int height, int mediaType) {
        if(!TKRoomManager.getInstance().getMySelf().peerId.equals(peerID)){
            Log.e("xiao_____2","onFirstVideoFrame peerid = "+peerID+"  time="+System.currentTimeMillis());
        }
        Log.e(TAG, "onFirstVideoFrame: "+"peerid="+peerID+"width="+width+"height="+height );
    }

    @Override
    public void onFirstAudioFrame(String peerID, int mediaType) {
        Log.e(TAG, "onFirstAudioFrame: "+"peerid="+peerID );
    }



    /***
     *   收到自定义信令消息
     */
    @Override
    public void onRemotePubMsg(String s, String s1, long l, Object o, boolean b1, String s2, String s3, String s4) {
        if (s.equals("OnlyAudioRoom")) {
            iv_av.setImageResource(R.mipmap.audio);
        }


    }

    /***
     *   删除自定义信令消息
     */
    @Override
    public void onRemoteDelMsg(String s, String s1, long l, Object o, boolean b1, String s2, String s3, String s4) {
        if (s.equals("OnlyAudioRoom")) {
            iv_av.setImageResource(R.mipmap.av);
        }

    }

    /***
     *   媒体视频播放暂停
     */
    @Override
    public void onUpdateAttributeStream(String s, long l, boolean b, HashMap<String, Object> hashMap) {

    }


    public boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    /*Tools.ShowAlertDialog(this, getString(R.string.mic_hint));*/
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    joinroom();
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA);
                    /*Tools.ShowAlertDialog(this, getString(R.string.camera_hint));*/
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCaptureAudioFrame(TKAudioFrame tkAudioFrame, String s, int i) {
        return false;
    }

    @Override
    public boolean onRenderAudioFrame(TKAudioFrame tkAudioFrame, String s, int i) {
        return false;
    }

    @Override
    public boolean onCaptureVideoFrame(TKVideoFrame tkVideoFrame, String s) {
        return false;
    }

    @Override
    public boolean onRenderVideoFrame(TKVideoFrame tkVideoFrame, String s, int i) {
        return false;
    }
}
