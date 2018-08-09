package cn.talkcloud.talksdkdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.talkcloud.roomsdk.TKRoomManager;

import cn.talkcloud.talksdkdemo.utils.ToastUtils;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{


    private EditText mAccountlView;
    private EditText meditText;
    private EditText mPasswordView;
    private EditText mHostView;
    private CheckBox logcat;
    private CheckBox autoav;
    private CheckBox audioonly;

    private boolean islogcat = false;
    private boolean isautoav = false;
    private boolean isaudio = false;

    private SharedPreferences sp  = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAccountlView = (EditText) findViewById(R.id.account);
        meditText = (EditText) findViewById(R.id.et_login_type);
        mPasswordView = (EditText) findViewById(R.id.password);
        mHostView = (EditText) findViewById(R.id.host);

        logcat = (CheckBox) findViewById(R.id.logcat);
        autoav = (CheckBox) findViewById(R.id.autoav);
        audioonly = (CheckBox) findViewById(R.id.audio);

        sp = getSharedPreferences("User", Context.MODE_PRIVATE);

        logcat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                islogcat = isChecked ;
            }
        });

        autoav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isautoav = isChecked ;
            }
        });

        audioonly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isaudio = isChecked ;
            }
        });


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        if (mAccountlView.getText().toString()!=null)
            mAccountlView.setText(sp.getString("account",""));
        if (meditText.getText().toString()!=null)
            meditText.setText(sp.getString("type",""));
        if (mPasswordView.getText().toString()!=null)
            mPasswordView.setText(sp.getString("password",""));
        if (mHostView.getText().toString()!=null)
            mHostView.setText(sp.getString("host",""));

    }


    private void attemptLogin() {

        String account = mAccountlView.getText().toString();
        String type = meditText.getText().toString();
        String password = mPasswordView.getText().toString();
        String host = mHostView.getText().toString();

        if (TextUtils.isEmpty(account)) {
            ToastUtils.showToast(this,"房间号不能为空！");
            return;
        }
        if (TextUtils.isEmpty(type)) {
            ToastUtils.showToast(this,"角色类型不能为空");
            return;
        }
        if (TextUtils.isEmpty(host)) {
            ToastUtils.showToast(this,"域名不能为空");
            return;
        }


//        if (isaudio)
//            TKRoomManager.getInstance().switchOnlyAudioRoom(!TKRoomManager.getInstance().isAudioOnlyRoom());
//        else
//            TKRoomManager.getInstance().switchOnlyAudioRoom(TKRoomManager.getInstance().isAudioOnlyRoom());

        Intent intent = new Intent(LoginActivity.this, VideoChatSampleActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("type", type);
        intent.putExtra("password", password);
        intent.putExtra("isLogcat", islogcat);
        intent.putExtra("host",host);
        startActivity(intent);


        //保存

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("account", account);
        editor.putString("type", type);
        editor.putString("password", password);
        editor.putString("host",host);
        editor.commit();

    }
}

