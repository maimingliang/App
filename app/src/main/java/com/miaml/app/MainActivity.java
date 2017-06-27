package com.miaml.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.miaml.wxplayer.WxMediaController;
import com.miaml.wxplayer.WxPlayer;

public class MainActivity extends AppCompatActivity {

    private WxPlayer mWxPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mWxPlayer = (WxPlayer) findViewById(R.id.wx_player);

                String path = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
        //       String path = "/storage/emulated/0/DCIM/Camera/20170521_200117.mp4";
        //               String path = "/storage/emulated/0/sound.amr";
//        String path = "/sdcard/test.mp4";

        mWxPlayer.setVideoPath(path);

        WxMediaController controller = new WxMediaController(this);
        controller.setThumbImage("http://192.168.0.232:8089/group1/M00/00/90/wKgA6FlI5MqAJVGlAAAcfondTlY728.jpg").setThumbHeight(640);
        mWxPlayer.setMediaController(controller);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWxPlayer != null){
            mWxPlayer.release();
        }
    }
}
