# ArcMediaPlayer-Android #
## 阅读对象 ##
本文档面向所有使用当虹云Android播放SDK的开发、测试人员等, 要求读者具有一定的Android编程开发经验。
## 1.产品概述 ##
杭州当虹科技股份有限公司的Android平台多媒体播放引擎v3.5版本SDK主要提供了本地音视频的播放和网络流媒体的播放功能，能够实现较低的性能开销达到较好播放体验。本参考文档将对该SDK的主要函数及使用进行详细的描述，以便开发者能够参考该文档进行快速的开发。本文档中提到的所有接口和定义都是针对java语言。
## 2.版权信息 ##
当虹云Android播放SDK版权信息：[LICENSE](https://github.com/Arcloud/ArcMediaPlayer-Android/blob/master/LICENSE),

当虹云提供的Android播放SDK可以用于商业应用，不会收取任何SDK使用费用。但基于当虹云Android播放SDK的其他商业服务，由服务提供商收取费用。

## 3.ArcMediaPlayer SDK特性 ##
- 支持 RTMP 和 HLS 协议的直播流媒体播放
- 支持首屏秒开
- 支持直播累计延迟优化
- 支持倍数播放
- 支持纯音频播放
- 支持后台音频播放
- 支持播放重连功能
- 支持多种画面填充模式
- 支持音量调节功能   
- 支持点播循环播放
- 支持rtsp播放
- 支持 HTTPS 协议
- 支持 http 的 DNS 异步解析
- 支持cache内seek
- 支持低功耗硬解模式
- 支持mp4 本地缓存
- 支持当虹云私有 DRM
- 支持常见的音视频文件和编码格式（MP4、mp3、flv 等）
- 支持预缓冲和多个视频同时播放
- 支持软解和MediaCodec硬解，且支持自动切换
- 接口定义与Android系统播放器MediaPlayer保持一致
- 提供[ArcVideoView播放控件](https://github.com/Arcloud/ArcMediaPlayer-Android/wiki/ArcVideoView%E6%8E%A7%E4%BB%B6)

## 4.开发准备 ##
### 4.1 设备以及系统要求 ###
- 系统要求：Android 4.0 (API 14) 及其以上
- 架构体系：ARM

### 4.2 Android播放SDK下载 ###
- https://github.com/Arcloud/ArcMediaPlayer-Android

### 4.3 在当虹云后台进行注册账号，然后按照指定的方式获取AccessKey/Secret

## 5.快速集成 ##
- [当虹云Android播放SDK主页](https://github.com/Arcloud/ArcMediaPlayer-Android/wiki)

## 6.参考代码 ##
Sample APP：[ArcPlayer](https://github.com/Arcloud/ArcMediaPlayer-Android/tree/master/sample/sampleplayer)

Sample code中的ArcPlayerSample.java，是一个完整的简单播放应用，除了调用ArcMediaPlayer的播放接口实现播放逻辑之外，还牵涉到一些错误处理，界面元素状态转换和控制，Activity的消息监听和处理，控件和屏幕点击事件处理，整个测试程序是按照一个功能完整的普通播放器逻辑来实现的。

## 7.注意事项(权限，与其他SDK、第三方库的冲突等) ##
### 7.1代码混淆 ###
为了保证正常使用 SDK ，请在混淆配置proguard文件中添加以下代码：
```
-keep class com.arcvideo.MediaPlayer.**{ *; }
```

如果使用到arcvideoview播放控件，或者VR播放，则还需要添加

```
-keep class com.arcvideo.arcvideoview.**{ *; }
-keep class com.arcvideo.vrkit.**{ *; }
```

## 8.接口说明 ##

请查看doc目录下的[接口说明文档](https://github.com/Arcloud/ArcMediaPlayer-Android/tree/master/doc/)

## 9.高级应用 ##
关于播放器的高级应用VR播放、VideoView控件等等，请查看[Wiki这里](https://github.com/Arcloud/ArcMediaPlayer-Android/wiki/%E9%AB%98%E7%BA%A7%E5%BA%94%E7%94%A8)

## 10.FAQ ##
查看Wiki中的[FAQ](https://github.com/Arcloud/ArcMediaPlayer-Android/wiki/FAQ)页，不断更新中

## 11.反馈及意见 ##
### 11.1联系方式 ###
- 主页：[当虹云](http://www.danghongyun.com/)
- 邮箱：video_engine@arcvideo.com
- 可以通过在GitHub的repo提交issues来反馈问题，反馈问题时建议你用如下格式，有助于快速解决问题。
##### 格式 #####

|  类型   |           描述           |
| :---: | :--------------------: |
| SDK版本 |        v3.5.0.1        |
| 设备型号  |      XiaoMi Note2      |
| OS版本  | Android 6.0.1/MIUI 9.2 |
| 问题描述  |  描述问题出现的现象，操作步骤，播放内容等  |
|  附件   |     log,trace,截屏等      |

Issue:[https://github.com/Arcloud/ArcMediaPlayer-Android/issues](https://github.com/Arcloud/ArcMediaPlayer-Android/issues)