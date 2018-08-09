package cn.talkcloud.talksdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fatangare.logcatviewer.utils.LogcatViewer;
import com.talkcloud.roomsdk.TKRoomManager;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "kdemo";

    private Button _btnJoin = null;
    private Button _btnAutoAV = null;
    private Button btn_is_audio_only = null;
    private boolean autoAV = true;
    private static boolean isLogcat = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btnJoin = (Button) findViewById(R.id.buttonJoin);
        _btnAutoAV = (Button) findViewById(R.id.btn_switch_auto_av);
        btn_is_audio_only = (Button) findViewById(R.id.btn_is_audio_only);
        if(_btnJoin != null) {
            _btnJoin.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Start();
                }
            });
        }
        _btnAutoAV.setText(autoAV?"AutoAV":"NotAutoAV");
        _btnAutoAV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoAV = !autoAV;
                _btnAutoAV.setText(autoAV?"AutoAV":"NotAutoAV");
            }
        });
        btn_is_audio_only.setText(TKRoomManager.getInstance().isAudioOnlyRoom()?"AudioOnly":"Normal");
        btn_is_audio_only.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TKRoomManager.getInstance().switchOnlyAudioRoom(!TKRoomManager.getInstance().isAudioOnlyRoom());
                btn_is_audio_only.setText(TKRoomManager.getInstance().isAudioOnlyRoom()?"AudioOnly":"Normal");
            }
        });

        findViewById(R.id.btn_test_logcat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LogcatViewer.showLogcatLoggerView(getApplicationContext());
            }
        });


        Log.d(TAG, "Start!");
    }

    public void Start() {
        Intent intent =new Intent(this, VideoChatSampleActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
