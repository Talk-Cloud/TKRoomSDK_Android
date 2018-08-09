package cn.talkcloud.talksdkdemo;

import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.tkwebrtc.SurfaceViewRenderer;

/**
 * Created by Administrator on 2017/6/27.
 */

public class VideoItem {

    public RelativeLayout lin_parent;
    public SurfaceViewRenderer sf_video;
    public AutoFitTextView btn_video;
    public AutoFitTextView btn_audio;
    public boolean isPlayVideo = true;
    public boolean isPlayAudio = true;
    public String peerid;
   /* public ImageView iv_display;*/

}
