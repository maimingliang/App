package com.miaml.wxplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.Map;

/**
 * author   maimingliang
 */


public class WxPlayer extends FrameLayout implements WxMediaController.WxMediaControll{


    private Context mContext;
    private FrameLayout mContainer;

    // all possible internal states
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_COMPLETED = 5;

    public static final int STATE_BUFFERING_PLAYING = 6;

    public static final int STATE_BUFFERING_PAUSED = 7;

    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;


    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;

    private MediaPlayer mMediaPlayer = null;
    private TextureView mTextureView = null;
    private WxMediaController mWxMediaController;
    private SurfaceTexture mSurfaceTexture = null;


    private int mBufferPercentage;

    private int mVideoWidth;
    private int mVideoHeight;
     private RelativeLayout mRlTextueView;

    public WxPlayer(Context context) {
        this(context,null);
    }

    public WxPlayer(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WxPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initContainer();

    }

    /**
     * 创建FrameLayout容器
     */
    private void initContainer() {

        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.setLayoutParams(params);
        this.addView(mContainer);

    }

    /**
     * 设置UI 控制器
     * @param controller
     */
    public void setMediaController(WxMediaController controller){

        Log.e("tag", " setMediaController ");

        mWxMediaController = controller;
        mWxMediaController.setWxPlayer(this);
        mContainer.removeView(mWxMediaController);

        LayoutParams params =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        mContainer.addView(mWxMediaController,-1,params);
    }

    @Override
    public void start(){

        if(mCurrentState == STATE_ERROR
                || mCurrentState == STATE_IDLE
                || mCurrentState == STATE_COMPLETED){
            initMediaPlayer();
            initTextureView();
            addTextureView();
        }

    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {

        if(TextUtils.isEmpty(path)){
            Log.e("setVideoPath", " 视频路径 为不合法");
           return;
        }
        String proxyUrl = path;
        if(path.startsWith("http://") || path.startsWith("https://")){
            proxyUrl  = VideoCacheManager.getInstance().getProxy(mContext).getProxyUrl(path);
        }


        Log.e("setVideoPath", "---- . proxyUrl = " + proxyUrl);
        setVideoURI(Uri.parse(proxyUrl));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    private void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }


    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        start();
    }


    /**
     * 添加TextureView
     */
    private void addTextureView() {
        Log.e("tag", " addTextureView ");
        mContainer.removeView(mRlTextueView);
        LayoutParams params =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mRlTextueView,0,params);
    }

    private void initTextureView() {
        Log.e("initTextureView ","initTextureView");
        if(mTextureView == null){
            mTextureView = new TextureView(mContext);
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        if(mRlTextueView == null){
            mRlTextueView = new RelativeLayout(mContext);
        }

        mRlTextueView.removeView(mTextureView);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextureView.setLayoutParams(params);
        mRlTextueView.addView(mTextureView);
     }

    private void initMediaPlayer() {


        if(mMediaPlayer == null){
            Log.e("initMediaPlayer ","initMediaPlayer");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);

            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);

        }
    }


    private void openVideo() {

        if(mUri == null || mSurfaceTexture == null || mMediaPlayer == null){
            Log.e("openVideo", "打开播放器错误 mUri == null ||  mSurfaceTexture == null" );
            return;
        }

        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(),mUri,mHeaders);
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            setControllerState();
        } catch (IOException e) {
            Log.e("wxplayer", "打开播放器错误 msg = " + e.getMessage());
            mCurrentState = STATE_ERROR;
            setControllerState();
            e.printStackTrace();
        }
    }


    public void setControllerState(){
        if(mWxMediaController != null){
            mWxMediaController.setControllerState(mCurrentState);
        }
    }

    //=================================================== mediaplayer controller =====================================//
    @Override
    public void release() {

        if(mMediaPlayer != null){
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mContainer.removeView(mTextureView);
        if(mSurfaceTexture != null){
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        mCurrentState = STATE_IDLE;

    }

    @Override
    public void restart() {

        if(mCurrentState == STATE_PAUSED){
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            setControllerState();
        }


    }


    @Override
    public void pause() {


        if(mCurrentState == STATE_PLAYING){
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            setControllerState();
        }
    }

    @Override
    public int getDuration() {
        if(isInPlaybackState()){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(isInPlaybackState()){
            return mMediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    @Override
    public void seekTo(int pos) {

        if(isInPlaybackState()){
            mMediaPlayer.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {

        return mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isIDLE() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPause() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isBuffPause() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public void finish() {
        ((Activity) mContext).finish();
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }
    //================================================listener=================================================//

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {

            mCurrentState = STATE_PREPARED;
            setControllerState();
            Log.e("onPrepared","--- player.getVideoWidth() = " + mp.getVideoWidth() +"    player.getVideoHeight() = " + mp.getVideoHeight());
            Log.e("onPrepared","--- getDeviceWidth = " + getDeviceWidth() +"    getMeasuredHeight() = " + getMeasuredHeight());
            // 当prepare完成后，该方法触发，在这里我们播放视频

            //首先取得video的宽和高
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            //            if(mVideoWidth > getDeviceWidth() || mVideoHeight > getDeviceHeight()) {
            //                //如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
            //                float wRatio = (float) mVideoWidth / (float) getDeviceWidth();
            //                float hRatio = (float) mVideoHeight / (float) getDeviceHeight();
            //
            //                //选择大的一个进行缩放
            //                float ratio = Math.max(wRatio, hRatio);
            //
            //                mVideoWidth = (int) Math.ceil((float) mVideoWidth / ratio);
            //                mVideoHeight = (int) Math.ceil((float) mVideoHeight / ratio);
            //
            //                //设置surfaceView的布局参数
            //                mTextureView.setLayoutParams(new LinearLayout.LayoutParams(mVideoWidth, mVideoHeight));
            //
            //                //然后开始播放视频
            //            }


            // the size is fixed
            int width = getDeviceWidth(); // 对视频缩放
            int height = 0;

            if (mVideoWidth == 0 || mVideoHeight == 0) {
                height = getDeviceHeight();
            } else {
                height = width * mVideoHeight / mVideoWidth;
            }

//            // for compatibility, we adjust size based on aspect ratio
//            if ( mVideoWidth * height  < width * mVideoHeight ) {
//                width = height * mVideoWidth / mVideoHeight;
//            } else if ( mVideoWidth * height  > width * mVideoHeight ) {
//                height = width * mVideoHeight / mVideoWidth;
//            }
            //设置mTextureView的布局参数
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mTextureView.setLayoutParams(params);


            if(mMediaPlayer != null){
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
                setControllerState();
            }
        }
    };


    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {

            switch (what){
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: // 播放器渲染第一帧
                    mCurrentState = STATE_PLAYING;
                    setControllerState();
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:// MediaPlayer暂时不播放，以缓冲更多的数据

                    if(mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED){
                        mCurrentState = STATE_BUFFERING_PAUSED;
                    }else {
                        mCurrentState = STATE_BUFFERING_PLAYING;
                    }

                    setControllerState();


                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:  // 填充缓冲区后，MediaPlayer恢复播放/暂停

                    if(mCurrentState == STATE_BUFFERING_PLAYING){
                        mCurrentState = STATE_PLAYING;
                    }
                    if(mCurrentState == STATE_BUFFERING_PAUSED){
                        mCurrentState = STATE_PAUSED;
                    }
                    setControllerState();
                    break;


            }


            return false;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferPercentage = percent;
        }
    };


    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

            Log.e("onVideoSizeChanged"," width = " + width +" height = " + height);
//            mVideoWidth = width;
//            mVideoHeight = height;
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e("wxplayer", " what = " + what + " - - extra = " + extra);
            mCurrentState = STATE_ERROR;
            setControllerState();
            return false;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_COMPLETED;
            setControllerState();
        }
    };

    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {

        }
    };


    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            if(mSurfaceTexture == null){
                mSurfaceTexture = surface;
                openVideo();
            }else {
                mTextureView.setSurfaceTexture(mSurfaceTexture);
            }


        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return mSurfaceTexture == null;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    public  int getDeviceWidth() {
        return mContext.getResources().getDisplayMetrics().widthPixels;
    }

    public  int getDeviceHeight(){
        return mContext.getResources().getDisplayMetrics().heightPixels;
    }

}
