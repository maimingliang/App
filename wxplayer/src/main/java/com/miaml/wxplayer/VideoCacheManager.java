package com.miaml.wxplayer;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;


/**
 * author   maimingliang
 */


public class VideoCacheManager {

    private HttpProxyCacheServer mProxyCacheServer;

    private static VideoCacheManager instance;

    private VideoCacheManager(){}


    public static VideoCacheManager getInstance(){

        if(instance == null){
            instance = new VideoCacheManager();
        }
        return instance;
    }


    public  HttpProxyCacheServer getProxy(Context context){
        if(mProxyCacheServer == null){
            mProxyCacheServer = newProxy(context);
        }

        return mProxyCacheServer;
    }

    private  HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer.Builder(context.getApplicationContext())
                .maxCacheSize(100 * 1024 * 1024)
                .build();
    }
}
