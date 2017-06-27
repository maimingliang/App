# WxPlayer

# 简介

Android 仿微信视频播放


[详情请看](http://www.jianshu.com/p/872f9ea87817)


# 使用


```code

    mWxPlayer = (WxPlayer) findViewById(R.id.wx_player);

    String path = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";


    mWxPlayer.setVideoPath(path);

    WxMediaController controller = new WxMediaController(this);
    controller.setThumbImage("http://192.168.0.232:8089/group1/M00/00/90/wKgA6FlI5MqAJVGlAAAcfondTlY728.jpg").setThumbHeight(640);
    mWxPlayer.setMediaController(controller);

```



# 关于我

[简书maimingliang](http://www.jianshu.com/users/141bda5f1c5c/latest_articles)


# 最后

如果遇到问题或者好的建议，请反馈到我的邮箱，maimingliang8#163.com (# 换 @)

如果觉得对你有用的话，点一下Star赞一下吧!



