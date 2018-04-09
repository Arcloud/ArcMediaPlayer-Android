/*----------------------------------------------------------------------------------------------
 *
 * This file is Arcvideo's property. It contains Arcvideo's trade secret, proprietary and 		
 * confidential information. 
 * 
 * The information and code contained in this file is only for authorized Arcvideo employees 
 * to design, create, modify, or review.
 * 
 * DO NOT DISTRIBUTE, DO NOT DUPLICATE OR TRANSMIT IN ANY FORM WITHOUT PROPER AUTHORIZATION.
 * 
 * If you are not an intended recipient of this file, you must not copy, distribute, modify, 
 * or take any action in reliance on it. 
 * 
 * If you have received this file in error, please immediately notify Arcvideo and 
 * permanently delete the original and any copy of any file and any printout thereof.
 *
 *-------------------------------------------------------------------------------------------------*/
package com.arcvideo.sampleplayer;

///////////////////
//must implement 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.arcvideo.MediaPlayer.ArcMediaPlayer;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.ARC_BANDWIDTH_SWITCH_MODE;
import com.arcvideo.MediaPlayer.ArcSeamlessPlayer;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.OnBufferingUpdateListener;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.OnCompletionListener;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.OnErrorListener;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.OnInfoListener;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.OnPreparedListener;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.OnSeekCompleteListener;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.OnVideoSizeChangedListener;
import com.arcvideo.MediaPlayer.MV2Config;
import com.arcvideo.MediaPlayer.MV2Config.MEDIAFILE;
import com.arcvideo.MediaPlayer.MV2Config.VALIDATE;
import com.arcvideo.MediaPlayer.ArcMediaPlayer.onMessageListener;
import com.arcvideo.MediaPlayer.VideoThumbnailUtils;
import com.arcvideo.sampleplayer.R;

/***
 * @author wzp2189
 *本测试程序，除了调用ArcMediaPlayer的播放接口实现播放逻辑之外，还牵涉到一些错误处理，界面元素状态转换和控制，
 *Activity的消息监听和处理，控件和屏幕点击事件处理，整个测试程序时按照一个功能完整的普通播放器逻辑来实现的。所以
 *除了集成ArcMediaPlayer播放器接口之外还有很多其它代码，如果使用者只想知道ArcMediaPlayer播放器接口如何调用，则可
 *参考如下几个函数中对于mMediaPlayer的使用即可：
 *1.onCreate
 *2.openFileStr
 *3.mRefreshHandler.handleMessage
 *4.onPrepared
 *5.onVideoSizeChanged
 *6.onInfo
 *7.onError
 *8.onSeekComplete
 *9.onCompletion
 *10.stopPlayback
 *11.还需参考ArcPlayerSampleApplication中的copyPlayerIni()和LoadLibraray()函数
 *
 *ArcMediaPlayer的调用时序如下(具体请参考API文档)：
 *1.创建player对象： ArcMediaPlayer mMediaPlayer = new ArcMediaPlayer();
 *2.设置权限认证参数：mMediaPlayer.validate(this, accessKey, secretKey, appKey);
 *2.设置context和configfile: mMediaPlayer.setConfigFile(context, configFile);
 *3.设置播放url之前调用：mMediaPlayer.reset();
 *4.设置播放地址 mMediaPlayer.setDataSource(m_strURL,headers); headers可以为空
 *5.设置各种监听Listener：
 *			mMediaPlayer.setOnCompletionListener(this);
 *			mMediaPlayer.setOnPreparedListener(this);
 *			mMediaPlayer.setOnVideoSizeChangedListener(this);
 *			mMediaPlayer.setOnInfoListener(this);
 *			mMediaPlayer.setOnErrorListener(this);
 *		    mMediaPlayer.setOnSeekCompleteListener(this);
 *6.设置SurfaceHolder给播放器，用于播放显示：
 *			mMediaPlayer.setDisplay(SurfaceHolder);
 *7.开始播放准备(使用前面设置的m_strURL和其它参数进行播放准备工作):mMediaPlayer.prepareAsync(),
 *  准备工作结束后会通过onPrepared通知APP, APP收到onPrepared消息之后即可调用mMediaPlayer.start()开始播放；
 *			
 *8.如果播放过程中需要seek，则可调用：mMediaPlayer.seekTo(position),seek成功后会通过onSeekComplete通知APP；
 *9.播放过程中出错会通过onError通知，有提示信息通过onInfo通知，播放结束通过onCompletion通知；
 *10.停止播放调用mMediaPlayer.stop()；
 *11.进程退出调用mMediaPlayer.release()释放所有资源，调用该函数后下次播放必须全部重新来。
 *
 *
 */
public class ArcPlayerSample extends Activity implements
		SurfaceHolder.Callback, View.OnClickListener, OnSeekBarChangeListener,
		OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
		OnVideoSizeChangedListener, OnInfoListener, OnErrorListener,
		OnSeekCompleteListener, onMessageListener {

	//Handler中处理的各种操作类型
	public static final int MSG_GET_POSITION = 1; //获取当前播放位置
	public static final int MSG_GET_BUFFERINGPERCENT = 2;//获取当前缓冲百分比
	public static final int MSG_DELAYED_OPEN_EXT = 3; //打开本地文件
	public static final int MSG_DELAYED_OPEN = 4; //延迟打开播放器
	public static final int MSG_GET_BITRATE = 5;
	public static final int MSG_SWTICH_END = 6; //清晰度切换完毕

	/**
	 * 播放过程中的播放状态
	 * @author wzp2189
	 *
	 */
	enum AMMF_STATE {
		IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, STOPPED, PLAYCOMPLETED
	};

	private static final String TAG = "ArcPlayerSample";
	AMMF_STATE m_PlayerState = AMMF_STATE.IDLE;

	public String m_streamURL;
	public int m_urlcount = 0;
	public ListView m_listView = null;
	SurfaceView m_surfaceView = null;
	public String m_strURL;
	public String m_strURL_header;
	long m_lDuration = 0;

	private RelativeLayout m_mainLayout;
	private RelativeLayout m_selLayout;

	protected ArcMediaPlayer mMediaPlayer;
	private int m_iCurOrientation = 0;
	private RelativeLayout m_bottomBar = null;
	private RelativeLayout m_topBar = null;
	private boolean m_bShowBottomBar = true;
	private boolean m_bShowTopBar = false;
	private ImageButton m_imgBtnPlayPause = null;
//	private ImageButton m_imgBtnBackButton = null;
	private ImageButton m_imgBtnStop = null;
	private ImageButton m_imgBtnAdd = null;

	private CheckBox m_cbLooping = null;
	private TextView m_tvDuration = null;
	private TextView m_tvCurrentTime = null;
	private TextView m_tvBitrate = null;
	private TextView m_tvStatus = null;
	protected TextView m_tvcMetadata = null;
	private TextView m_tvaMetadata = null;
	private TextView m_tvvMetadata = null;
	private TextView m_tvdMetadata = null;
	private CheckBox m_cbUseHardwareMode = null;
	private ImageButton m_imgBtnSpeedown = null;
	private ImageButton m_imgBtnSpeedup = null;
	private Toast mToast = null;



	private SeekBar m_seekBar = null;
	private SeekBar m_volumBar = null;
	private boolean m_bSeekBarTouch = false;
	private boolean m_bVolumBarTouch = false;
	private boolean m_bBuffering = false;

	private Uri m_uri;
	private int m_lLoadType = 0;
	private int m_lCurrentSelet = 0;
	private boolean m_bManualPaused = false;


	int m_pixelFormat = android.graphics.PixelFormat.YCbCr_420_SP;

	int m_surfaceType = android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;

	int m_nClipWidth = 0;
	int m_nClipHeight = 0;

	int m_frameWidth = 0;
	int m_frameHeight = 0;

	int m_nRotation = 0;
	int mConnectTimeout = 5;
	int mReceiveTimeout = 10;
	int mReconnectCount = 0;
	
	float m_fPlaybackRate = 1.0f;

	/**
	 * 0----fillin显示效果为保证视频所有内容都完全显示在屏幕中且不会拉伸变形，
	 *            如果视频宽高比与设备屏幕宽高比不一致则显示不会完全全屏; 
	 * 1---fillout显示效果为保证视频能够全屏显示且不会拉伸变形，
	 *            如果视频宽高比与设备屏幕宽高比不一致则显示全屏，同时会有部分视频内容会超出屏幕范围从而被截掉; 
	 * 2------fullcreen显示效果为保证视频所有内容都完全显示在屏幕中全屏显示，
	 *                 如果视频宽高比与设备屏幕宽高比不一致则显示会被拉伸变形; 
	 */
	int mDisplayType = 0;
	
	private static final int SEEK_STEP = 5000; //seek往前跳动的最小长度

	//鉴权参数:需要app去获取和提供
//private String accessKey = "Place";
//private String secretKey = "YourCode";
//private String appKey = "Here";
	
	private AudioManager mAudioManager = null; //用来控制后台音乐暂停与恢复
	
	private boolean mbPrintLog = true;
	/**
	 * 用来控制日志打印
	 * @param strLog
	 */
	protected void output(String strLog) {
		if (mbPrintLog) {
			Log.d(TAG, strLog);
		}
	}
	
	/**
	 * Handler用来进行各种异步处理
	 */
	@SuppressLint("HandlerLeak")
	protected Handler mRefreshHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DELAYED_OPEN_EXT:
				try {
					openUri(m_uri);
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case MSG_DELAYED_OPEN: {
				m_lCurrentSelet++;
				if (m_lCurrentSelet >= m_aryUrl.size()) {
					m_lCurrentSelet = 0;
				}
				if (null != m_aryUrl && 0 != m_aryUrl.size())
					try {
						openFileStr(m_streamURL);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
				break;
			case MSG_GET_POSITION: {
				mRefreshHandler.removeMessages(MSG_GET_POSITION);

				int nCurPos = 0;
				if (null != mMediaPlayer) {
					try {
						nCurPos = mMediaPlayer.getCurrentPosition();
					} catch (IllegalStateException e) {
						// stop getting position
						break;
					}
					m_tvCurrentTime.setText(stringForTime(nCurPos));

					if (!m_bSeekBarTouch
							&& !m_seekBar.isPressed()
							&& m_PlayerState != AMMF_STATE.PAUSED
							&& !m_bBuffering) {
						// Log.d(TAG, "key: update progress to "+nCurPos);
						m_seekBar.setProgress(nCurPos);
					}

					mRefreshHandler.sendEmptyMessageDelayed(MSG_GET_POSITION,
							500);

					if (mMediaPlayer.isPlaying() && !m_bBuffering
							&& m_tvStatus.getText().toString().equals("Opened"))
						m_tvStatus.setText("Playing");
				}
			}
				break;
			case MSG_GET_BUFFERINGPERCENT: {
				mRefreshHandler.removeMessages(MSG_GET_BUFFERINGPERCENT);

				int nCurPercent = 0;
				if (null != mMediaPlayer) {
					try {
						nCurPercent = mMediaPlayer.getCurrentBufferingPercent();
					} catch (IllegalStateException e) {
						// stop getting position
						break;
					}
					m_tvStatus.setText("Buf:" + nCurPercent);
					mRefreshHandler.sendEmptyMessageDelayed(
							MSG_GET_BUFFERINGPERCENT, 300);
				}
			}
				break;
			case MSG_GET_BITRATE: {
				mRefreshHandler.removeMessages(MSG_GET_BITRATE);
				int nCurBitrate = 0;
				if(null != mMediaPlayer){
					nCurBitrate = mMediaPlayer.getCurrentTransBitrate();
					m_tvBitrate.setText("bits:"+(nCurBitrate/8)+"B/s");
					mRefreshHandler.sendEmptyMessageDelayed(MSG_GET_BITRATE, 1000);
				}
			}
			break;
			}
		}
	};
	
	/**
	 * 初始化surface 类型，根据android API level进行设置
	 */
	private void initSurfaceType() {

		output("initSurfaceType 1"
				+ Integer.valueOf(android.os.Build.VERSION.SDK));
		if (Integer.valueOf(android.os.Build.VERSION.SDK) < 14) {

			m_pixelFormat = android.graphics.PixelFormat.RGBA_8888;
			m_surfaceType = android.view.SurfaceHolder.SURFACE_TYPE_NORMAL;//

		} else {
			m_pixelFormat = android.graphics.PixelFormat.YCbCr_420_SP;// YCbCr_420_SP;//
			m_surfaceType = android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;//

		}
	}

	/**
	 * Called with the activity is first created.
	 */
	@SuppressLint({ "NewApi", "DefaultLocale" })
	@Override
		
	/*******************************************************************/
	//2015-01-20 yurong update for batch test  begine
	/*******************************************************************/
	
	

	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		//设置程序界面全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set the layout for this activity. You can find it
		// in res/layout/streamingplayer.xml
		
		setContentView(R.layout.streamingplayer);

		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//触发创建surface，并注册surface的回调
		createPorSurface();

		//用来记录旋转方向
		m_iCurOrientation = this.getResources().getConfiguration().orientation;

		//用来放置底部进度条，播放操作按钮等按键的Layout
		m_bottomBar = (RelativeLayout) findViewById(R.id.bottoml);
		m_topBar = (RelativeLayout) findViewById(R.id.top1);

		m_imgBtnPlayPause = (ImageButton) findViewById(R.id.but_play_pausel);
		m_imgBtnPlayPause.setOnClickListener(this);

//		m_imgBtnBackButton = (ImageButton) findViewById(R.id.btn_back);
//		m_imgBtnBackButton.setOnClickListener(this);
		
		m_imgBtnStop = (ImageButton) findViewById(R.id.but_stop);
		m_imgBtnStop.setOnClickListener(this);

		m_imgBtnAdd = (ImageButton) findViewById(R.id.btn_add);
		m_imgBtnAdd.setOnClickListener(this);

		m_cbLooping = (CheckBox) findViewById(R.id.cycle);
		m_cbLooping.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (m_cbLooping.isChecked()) {
					mMediaPlayer.setLooping(true);
				} else {
					mMediaPlayer.setLooping(false);
				}
			}
		});
		m_cbLooping.setChecked(false);
		
		m_cbUseHardwareMode = (CheckBox) findViewById(R.id.cbhardware);
		m_cbUseHardwareMode.setChecked(true);
		
		m_imgBtnSpeedown = (ImageButton)findViewById(R.id.but_speeddown);
		m_imgBtnSpeedown.setOnClickListener(this);
		m_imgBtnSpeedup = (ImageButton)findViewById(R.id.but_speedup);
		m_imgBtnSpeedup.setOnClickListener(this);
		
		m_tvCurrentTime = (TextView) findViewById(R.id.time1);
		m_tvCurrentTime.setText(stringForTime(0));

		m_tvDuration = (TextView) findViewById(R.id.time2);
		m_tvDuration.setText(stringForTime(0));
		
		m_tvBitrate = (TextView)findViewById(R.id.bitrate);
		m_tvBitrate.setText("bitrate");

		m_tvStatus = (TextView) findViewById(R.id.status);
		m_tvStatus.setText("Idle");

		m_tvcMetadata = (TextView) findViewById(R.id.metadata);
		m_tvaMetadata = (TextView) findViewById(R.id.audiometadata);
		m_tvvMetadata = (TextView) findViewById(R.id.videometadata);
		m_tvdMetadata = (TextView) findViewById(R.id.decodemetadata);

		m_seekBar = (SeekBar) findViewById(R.id.seekBar);
		m_seekBar.setOnSeekBarChangeListener(this);
		m_seekBar.setProgress(0);
		m_seekBar.setSecondaryProgress(0);

		m_volumBar = (SeekBar) findViewById(R.id.volumBar);
		m_volumBar.setOnSeekBarChangeListener(this);
		m_volumBar.setProgress(30);
		m_volumBar.setSecondaryProgress(0);
		m_volumBar.setVisibility(View.GONE);
		
		m_mainLayout = (RelativeLayout) findViewById(R.id.MainLayout);
		m_selLayout = (RelativeLayout) findViewById(R.id.SelectLayout);
		
		m_listView = (ListView) findViewById(R.id.listitem);

		getUrlList();
		m_urlcount = m_aryUrl.size();
					
		m_listView.setAdapter(new SimpleAdapter(this, m_aryUrl, R.layout.footer_selectview, new String[] {"url"}, new int[] {R.id.url}));			
		m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//选择播放列表中一个播放地址后，会触发这儿click事件，然后将url传给player进行播放
				TextView tv = (TextView)view.findViewById(R.id.url);
				m_streamURL = tv.getText().toString().trim();
				try {
					m_mainLayout.setVisibility(View.VISIBLE);
					m_selLayout.setVisibility(View.GONE);
					m_surfaceView.setVisibility(View.VISIBLE);
					openFileStr(m_streamURL);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}	
		});
		
		m_mainLayout.setVisibility(View.VISIBLE);
		m_selLayout.setVisibility(View.GONE);
		m_surfaceView.setVisibility(View.VISIBLE);

		Log.e(TAG, "new ArcMediaPlayer " + getFilesDir().getAbsolutePath() + "/MV3Plugin.ini");
		//创建player
		mMediaPlayer = new ArcMediaPlayer();
		//设置鉴权参数--必须
		
		//设置必须的参数，必须在创建player后立马调用
		mMediaPlayer.setConfigFile(this, getFilesDir().getAbsolutePath() + "/MV3Plugin.ini");
		mMediaPlayer.validate(this, accessKey, secretKey, appKey);
		mMediaPlayer.reset();

		Intent intent = getIntent();
		if (intent != null) {
			m_uri = intent.getData();
			if (m_uri != null) {
				// Loaded from intent, delayed open
				mRefreshHandler.removeMessages(MSG_DELAYED_OPEN_EXT);
				mRefreshHandler.sendEmptyMessageDelayed(MSG_DELAYED_OPEN_EXT,
						200);

				m_lLoadType = 1;
			}
		}
	}
	
	/**
	 * 当前activity退出时系统会自动触发该函数
	 * 在函数中进行player的关闭和释放
	 */
	protected void onDestroy() {
		Log.d(TAG, "Destory");

		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		m_surfaceView = null;
		//恢复被暂停的后台音乐
		abandonAudioFocus();
		super.onDestroy();
	}

	/**
	 * 将当前播放时间生成固定格式的时间字符串，以便用于屏幕上显示当前播放时间
	 * @param timeMs    当前播放时间，单位是毫秒
	 * @return   时间格式：hh:mm:ss
	 */
	private String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		return String.format("%02d:%02d:%02d", hours, minutes, seconds)
				.toString();
	}

	/**
	 * 控制屏幕画面显示
	 * @param PixelFormat   颜色空间类型
	 * @param x             画面在屏幕上起始点X轴坐标位置offset x
	 * @param y             画面在屏幕上起始点Y轴坐标位置offset y
	 * @param w             视频宽width
	 * @param h             视频高height
	 */
	@SuppressWarnings("deprecation")
	public void SetSurfaceRect(int PixelFormat, int x, int y, int w, int h) {
		AbsoluteLayout.LayoutParams lp;

		{
			if (w <= 1 && h <= 1) {
				lp = (AbsoluteLayout.LayoutParams) (m_surfaceView
						.getLayoutParams());
				lp.x = getWindow().getWindowManager().getDefaultDisplay()
						.getWidth();
				lp.y = getWindow().getWindowManager().getDefaultDisplay()
						.getHeight();
				lp.width = 0;
				lp.height = 0;
				m_surfaceView.setLayoutParams(lp);
				output("[0]x=" + lp.x + "y=" + lp.y + "w=" + lp.width + "h="
						+ lp.height);
			} else {
				output("[1]x=" + x + "y=" + y + "w=" + w + "h=" + h);
				lp = (AbsoluteLayout.LayoutParams) (m_surfaceView
						.getLayoutParams());
				output("[2]x=" + lp.x + "y=" + lp.y + "w=" + lp.width + "h="
						+ lp.height);
				lp.x = x;
				lp.y = y;
				lp.width = w;
				lp.height = h;
				m_surfaceView.setLayoutParams(lp);
				output("[3]x=" + lp.x + "y=" + lp.y + "w=" + lp.width + "h="
						+ lp.height);

			}
		}
	}

	/**
	 * 触发创建surface，并注册surface的回调
	 * 调用该函数后，surface创建和使用和销毁过程中就会触发
	 * surfaceCreated\surfaceDestroyed\surfaceChanged这些回调，
	 * 使用者就可以根据自己的需求进行相关的处理
	 */
	public void createPorSurface() {
		output("createPorSurface");
		initSurfaceType();
		SurfaceHolder sh = null;
		m_surfaceView = (SurfaceView) findViewById(R.id.PorView);

		if (m_surfaceView != null) {
			m_surfaceView.setVisibility(android.view.View.VISIBLE);
			sh = m_surfaceView.getHolder();
		}

		if (sh != null) {
			sh.addCallback(this);
			sh.setType(m_surfaceType);
			sh.setFormat(m_pixelFormat);
		}
	}

	/**
	 * surface 变化时会被系统自动触发
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		output("surfaceChanged");
		output("setDisplay surfaceChanged sh = " + holder);
		if (!m_surfaceView.getHolder().getSurface().isValid()) {
			Log.e("surfaceCheck", "surfaceChanged Surface is invalid");
		}
	}

	/**
	 * surface创建时会被系统自动触发，
	 * 可以在里面进行一些与surface相关的初始化操作
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		output("surfaceCreated");
		Surface sf = null;
		sf = m_surfaceView.getHolder().getSurface();
		m_iCurOrientation = this.getResources().getConfiguration().orientation;

		if (!sf.isValid()) {
			Log.e("surfaceCheck", "surfaceCreated,Surface is invalid");
			//says there was a bug in SurfaceView until it has been fixed in Android 4.2
			//that SurfaceView may pass an invalid Surface to surfaceCreated
			//http://stackoverflow.com/questions/18451854/the-surface-has-been-released-inside-surfacecreated
			m_surfaceView.setVisibility(View.GONE);
			m_surfaceView.setVisibility(View.VISIBLE);
			return;
		}
		
		//如果mMediaPlayer已经被创建了，则使用视频宽高来初始化显示区域
		//如果mMediaPlayer还没被创建则使用屏幕宽高来初始化显示区域
		if (mMediaPlayer != null) {
			mMediaPlayer.setDisplay(m_surfaceView.getHolder());
			onVideoSizeChanged(mMediaPlayer, mMediaPlayer.getVideoWidth(),
					mMediaPlayer.getVideoHeight());
		} else {
			
			Log.e("surface Created", "Set surface begin ");
			int nScreenWidth = getWindow().getWindowManager()
					.getDefaultDisplay().getWidth();
			int nScreenHeight = getWindow().getWindowManager()
					.getDefaultDisplay().getHeight();

			output("surfaceCreated:w" + nScreenWidth + "h=" + nScreenHeight);
			SetSurfaceRect(m_pixelFormat, 0, 0, nScreenWidth, nScreenHeight);
			Log.e("surface Created", "Set surface end ");
		}
	}

	/**
	 * surface销毁时系统会自动调用该函数，
	 * 可以进行surface相关的释放操作
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		output("surfaceDestroyed");

		if (null != mMediaPlayer) {
			mMediaPlayer.setDisplay(null);
		}

		if (null != m_surfaceView && null != m_surfaceView.getHolder()
				&& null != m_surfaceView.getHolder().getSurface()) {
			Log.e("surfaceCheck", "surfaceDestroyed,holder :" + holder
					+ " m_surfaceView.getHolder" + m_surfaceView.getHolder());
			if (!m_surfaceView.getHolder().getSurface().isValid()) {
				Log.e("surfaceCheck", "surfaceDestroyed,Surface is invalid");
			}
		}
	}

	/**
	 * seekBar被点击时会自动触发，该函数必须存在(尽管里面没有任何操作)，
	 * 否则seek后的播放位置无法拿到
	 * 需要设置监听OnSeekBarChangeListener
	 */
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
	}

	/**
	 * 这里记录seekbar开始被点击或者拖动了，
	 * 当点击拖动结束后会去计算seek的位置；
	 */
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.d(TAG, "start tracking touch");
		// TODO Auto-generated method stub
		if (seekBar == m_seekBar) {
			m_bSeekBarTouch = true;
		} else if (seekBar == m_volumBar) {
			m_bVolumBarTouch = true;
		}

	}

	/**
	 * 这里记录seekbar被点击或者拖动了操作结束了，
	 * 计算seek的位置；
	 */
	public void onStopTrackingTouch(SeekBar seekBar) {

		Log.d(TAG, "stop tracking touch");
		// TODO Auto-generated method stub
		if (seekBar == m_seekBar && m_bSeekBarTouch) {
			mMediaPlayer.seekTo(m_seekBar.getProgress());
			m_bSeekBarTouch = false;
		}else if (seekBar == m_volumBar && m_bVolumBarTouch) {
			float volum = (float) m_volumBar.getProgress() / 100;
			mMediaPlayer.setVolume(volum, volum);
			m_bVolumBarTouch = false;

		}	
	}

	/**
	 * 控制是否需要屏幕一直亮着
	 * @param bOn 
	 *           true-----表示保持屏幕亮着
	 *           false----表示屏幕可以睡眠黑掉
	 */
	public void KeepScreenOn(boolean bOn) {
		SurfaceHolder sfhold;
		sfhold = m_surfaceView.getHolder();
		if (sfhold != null) {
			sfhold.setKeepScreenOn(bOn);
		}
	}

	/**
	 * Activity起来时系统会触发该事件，这是系统函数，
	 * 是在AndroidManifest.xml中配置了
	 * android:configChanges="orientation|keyboardHidden"才会起作用，
	 * 目的是可以防止设备旋转时activity频繁的销毁和创建
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		output("onConfigurationChanged, " + newConfig.orientation + ","
				+ newConfig.keyboardHidden);
		//进行程序界面的重新布局配置
		if (newConfig.orientation != m_iCurOrientation) {
			if (m_bShowBottomBar)
				m_bottomBar.setVisibility(View.GONE);

			int nScreenWidth = getWindow().getWindowManager()
					.getDefaultDisplay().getWidth();
			// int nScreenHeight = getWindow().getWindowManager()
			// .getDefaultDisplay().getHeight();
			m_iCurOrientation = newConfig.orientation;
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) m_bottomBar
					.getLayoutParams();
			if (null != lp) {
				lp.height = 100;
				lp.width = nScreenWidth;
				m_bottomBar.setLayoutParams(lp);
			}
			if (m_bShowBottomBar)
				m_bottomBar.setVisibility(View.VISIBLE);
			onVideoSizeChanged(mMediaPlayer, m_frameWidth, m_frameHeight);
		}
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * 用来判断播放地址(url)格式上是否合法
	 * @param strText 播放地址
	 * @return
	 */
	public static boolean isInputTextValid(String strText) {
		if (null == strText || 0 == strText.length())
			return false;

		String strForbidden = "\\*<>|\n";
		int nStrForbidLen = 0;
		boolean bValid = true;

		nStrForbidLen = strForbidden.length();
		for (int i = 0; i < strText.length(); i++) {
			for (int j = 0; j < nStrForbidLen; j++) {
				if (strText.charAt(i) == strForbidden.charAt(j)) {
					bValid = false;
					break;
				}
			}
		}

		return bValid;
	}

	private AlertDialog m_currDlg = null;
	private Button m_btnConfirm;
	private Button m_btnCancel;

	private ArrayList<Map<String, Object>> m_aryUrl = new ArrayList<Map<String, Object>>();
	private File m_fileUrl = new File("/data/local/tmp/url.txt");
	private String m_strSpitChar = "\r\n";

	/**
	 * 从播放url配置文件中读取配置好的播放地址(url)列表
	 * 播放地址列表配置文件由File m_fileUrl = new File("/data/local/tmp/url.txt")指定
	 */
	private void getUrlList() {
		Log.e("getUrlList", "getUrlList 0 ,m_fileUrl:" + m_fileUrl);
		try {
			Log.e("getUrlList", "getUrlList 1 ");
			if (null != m_fileUrl) {
				Log.e("getUrlList", "getUrlList 2 ");
				if (!m_fileUrl.exists() || !m_fileUrl.canRead()) {
					m_fileUrl = new File(Environment.getExternalStorageDirectory()
							.getPath() + "/url.txt");
				}
				if (m_fileUrl.exists() && m_fileUrl.canRead()) {
					Log.e("getUrlList", "getUrlList 3 ");
					FileInputStream is = new FileInputStream(m_fileUrl);
					Log.e("getUrlList", "getUrlList 4, is:" + is);
					int iSize = is.available();
					StringBuffer sb = new StringBuffer();
					if (0 != iSize) {
						byte[] buffer = new byte[iSize];
						Log.e("getUrlList", "getUrlList 5, buffer:" + iSize
								+ "  " + buffer);
						Log.e("getUrlList", "getUrlList 6, sb:" + sb);
						while (is.read(buffer) != -1) {
							Log.e("getUrlList", "getUrlList 7:" + iSize + "  "
									+ buffer);
							sb.append(new String(buffer));
						}
						Log.e("getUrlList", "getUrlList 8");
						System.out.println(sb.toString());
					}
					is.close();
					is = null;

					String[] ary = sb.toString().split(m_strSpitChar);
					if (ary != null) {
						for (int i = 0; i < ary.length; i++) {
							if (ary[i].compareToIgnoreCase("") != 0) {
								Map<String, Object> item = new HashMap<String, Object>();
								item.put("url", ary[i]);
								m_aryUrl.add(item);
							}
						}
					}
					ary = null;
					sb = null;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("getUrlList", "getUrlList 61");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("getUrlList", "getUrlList 62");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 将最新的播放地址列表保存到文件，以便下次启动可以直接选择不用重新输入
	 */
	private void saveUrlList() {
		Log.e("saveUrlList", "saveUrlList 0 ,m_fileUrl:" + m_fileUrl);
		if (null != m_fileUrl) {
			if (m_fileUrl.exists())
				m_fileUrl.delete();

			try {
				m_fileUrl.createNewFile();

				StringBuffer sb = new StringBuffer();
				Log.e("saveUrlList", "saveUrlList 1 ,sb:" + sb);
				for (int i = 0; i < m_aryUrl.size(); i++) {
					sb.append(m_aryUrl.get(i).get("url"));
					sb.append(m_strSpitChar);
				}

				FileOutputStream os = new FileOutputStream(m_fileUrl);
				Log.e("saveUrlList", "saveUrlList 2 ,os:" + os);
				System.out.println(sb.toString());
				os.write(sb.toString().getBytes());
				os.flush();

				os.close();
				os = null;

				// m_fileUrl = null;
			} catch (IOException e) {
				Log.e("saveUrlList", "saveUrlList 71:");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将最新的播放地址添加到播放列表中
	 */
	private void addNewUrl(String strNewUrl) {
		boolean bHaveSame = false;
		
		for (int i = 0; i < m_aryUrl.size(); i++) {
			if (strNewUrl.compareToIgnoreCase((String)m_aryUrl.get(i).get("url")) == 0) {
				bHaveSame = true;
				/*Map<String, Object> obj = m_aryUrl.get(i);
				m_aryUrl.remove(i);
				m_aryUrl.add(0, obj);*/
				break;
			}
		}

		if (!bHaveSame) {			
			strNewUrl = m_strURL_header + strNewUrl;
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("url", strNewUrl);
			m_aryUrl.add(0, item);
		}
	}

	/**
	 * 用来进行在界面上跳出提示信息，一会后会自动消失的那种
	 * @param str           要提示的信息
	 * @param bShowLong     是否需要久一点的显示模式
	 */
	public void showToast(String str, boolean bShowLong) {
		Toast toast = null;
		int nDuration = bShowLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		toast = Toast.makeText(this, str, nDuration);
		toast.show();
	}

	/**
	 * 用来弹出允许使用者手动输入url的对话框
	 * @param strTitle    对话框抬头
	 * @param strContent  对话框中TextView显示的内容
	 */
	public void showOpenURLDlg(String strTitle, String strContent) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_edit_text,
				null);

		EditText etCon = (EditText) textEntryView
				.findViewById(R.id.edittext_edit);
		etCon.getEditableText().insert(0, strContent);
		m_btnConfirm = (Button) textEntryView.findViewById(R.id.plsBtnConfirm);
		m_btnCancel = (Button) textEntryView.findViewById(R.id.plsBtnCancel);
		m_btnConfirm.requestFocus();

		m_currDlg = new AlertDialog.Builder(this).create();
		m_currDlg.setView(textEntryView, 0, 0, 0, 0);
		m_currDlg.show();
        //make max width to display more text
		WindowManager.LayoutParams params = m_currDlg.getWindow().getAttributes();
		params.width = WindowManager.LayoutParams.FILL_PARENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		m_currDlg.getWindow().setAttributes(params);

		//用来监听输入播放url后点击OK和Cancel的操作事件
		View.OnClickListener clickListener = new View.OnClickListener() {
			public void onClick(View v) {
				long id = v.getId();
				if (id == R.id.plsBtnConfirm) {
					output("showOpenURLDlg 5");
					EditText etPl = (EditText) textEntryView
							.findViewById(R.id.edittext_edit);
					output("showOpenURLDlg 6");
					String str = etPl.getText().toString().trim();
					try {
						m_mainLayout.setVisibility(View.VISIBLE);
						m_selLayout.setVisibility(View.GONE);
						m_surfaceView.setVisibility(View.VISIBLE);
						openFileStr(str);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (id == R.id.plsBtnCancel) {
					output("showOpenURLDlg 15");
					m_currDlg.cancel();
				}

			}
		};

		m_btnConfirm.setOnClickListener(clickListener);
		m_btnCancel.setOnClickListener(clickListener);
	}

	/**
	 * 当播放按钮被按下是触发，根据播放状态进行一些界面元素的控制和播放器的操作
	 */
	private void onClickPlayButton() {
		switch (m_PlayerState) {
		case IDLE:
			output("onClick,but_open:begin");
			m_mainLayout.setVisibility(View.GONE);
			m_selLayout.setVisibility(View.VISIBLE);
			m_surfaceView.setVisibility(View.INVISIBLE);

			output("onClick,but_open:End");
			break;
		case STARTED:
			KeepScreenOn(true);
			output("onClick,but_pause:begin");
			m_tvStatus.setText("Pause");
			m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_play);
			m_PlayerState = AMMF_STATE.PAUSED;
			mMediaPlayer.pause();
			m_bManualPaused = true;
			output("onClick,but_pause:end");
			
			/*
			 * Bitmap bitmap = null; bitmap = mMediaPlayer.captureFrame(android
			 * .graphics.PixelFormat.RGB_565); if(null != bitmap) { ImageView
			 * OutputImageview = (ImageView)findViewById(R.id.OutputImageview);
			 * OutputImageview.setVisibility(View.VISIBLE);
			 * OutputImageview.bringToFront();
			 * OutputImageview.setImageBitmap(bitmap); }
			 */
			// OutputBitmap();
			break;
		case STOPPED:
			KeepScreenOn(false);
			output("onClick,but_play:begin");
			m_lDuration = 0;

			m_PlayerState = AMMF_STATE.IDLE;
			mMediaPlayer.reset(); // reset should be idle status,not stop
			m_bManualPaused = false;
			m_tvStatus.setText("Idle");

			m_mainLayout.setVisibility(View.GONE);
			m_selLayout.setVisibility(View.VISIBLE);
			m_surfaceView.setVisibility(View.INVISIBLE);
		
			output("onClick,but_play:end");
			break;
		case PAUSED:
			output("onClick,but_play:begin");

			mMediaPlayer.start();
			m_bManualPaused = false;
			m_PlayerState = AMMF_STATE.STARTED;
			m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_pause);
			m_tvStatus.setText("Playing");
			output("onClick,but_play:end");

			ImageView OutputImageview1 = (ImageView) findViewById(R.id.OutputImageview);
			OutputImageview1.setVisibility(View.GONE);

			break;
		default:
			break;
		}

	}

	/**
	 * 使用者按回退键时触发
	 */
	private void onClickBackButton() {	
		if(m_selLayout.getVisibility() == View.VISIBLE) {
			m_mainLayout.setVisibility(View.VISIBLE);
			m_selLayout.setVisibility(View.GONE);
			m_surfaceView.setVisibility(View.VISIBLE);
		}		
		else {
			stopPlayback();
			
			m_mainLayout.setVisibility(View.GONE);
			m_selLayout.setVisibility(View.VISIBLE);
			m_surfaceView.setVisibility(View.INVISIBLE);
			finish();
		}
	}
	
	/**
	 * 界面停止按钮被按下时触发，触发后停止播放，同时更新相关界面元素
	 */
	private void onClickStopButton() {
		int res = 0;
		output("onClick,but_stop:Begin");
		{
			m_tvCurrentTime.setText(stringForTime(0));
			m_tvDuration.setText(stringForTime(0));
			m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_play);
			m_seekBar.setProgress(0);
			m_seekBar.setSecondaryProgress(0);
			ImageView OutputImageview = (ImageView) findViewById(R.id.OutputImageview);
			OutputImageview.setVisibility(View.GONE);
		}
		stopPlayback();

		controlBackLight(this, false);

		saveUrlList();

		output("onClick,but_stop:end," + res);
		if (m_lLoadType == 1) {
			output("Intent Click stop out");
			finish();
		} else if (m_cbLooping.isChecked()) {
			mRefreshHandler.removeMessages(MSG_DELAYED_OPEN);
			mRefreshHandler.sendEmptyMessageDelayed(MSG_DELAYED_OPEN, 200);
		}
	}

	private void onClickSpeedUp(){
		if (mMediaPlayer != null) {
			Log.i(TAG, "onClickSpeedUp() 0001 m_fPlaybackRate = " + m_fPlaybackRate);
			BigDecimal bDecimal1 = new BigDecimal(1.0);
			BigDecimal bDecimal4 = new BigDecimal(8.0);
			BigDecimal bDecimaVar = new BigDecimal(m_fPlaybackRate);
			if (bDecimaVar.compareTo(bDecimal1) == 1 
					|| bDecimaVar.compareTo(bDecimal1) == 0) {
				m_fPlaybackRate += 0.5;
			}else {
				m_fPlaybackRate += 0.2;
			}
			
			BigDecimal bDecimaVar1 = new BigDecimal(m_fPlaybackRate);
			if (bDecimaVar1.compareTo(bDecimal4) == 1) {
				m_fPlaybackRate = 8.0f;
			}
			m_fPlaybackRate = (float)(Math.round(m_fPlaybackRate*10))/10;
			mMediaPlayer.setPlayerMode(m_fPlaybackRate);
			Log.i(TAG, "onClickSpeedUp() 0002 m_fPlaybackRate = " + m_fPlaybackRate);
			String speedString = m_fPlaybackRate + "x";			
			if (mToast == null) {
				mToast = Toast.makeText(this, speedString, Toast.LENGTH_SHORT);
			}else {
				mToast.setText(speedString);
			}
			mToast.show();
			
			Log.i(TAG, "onClickSpeedUp() 0003");
		}
	}
	
	private void onClickSpeedDown(){		
		if (mMediaPlayer != null) {
			Log.i(TAG, "onClickSpeedDown() 0001 m_fPlaybackRate = " + m_fPlaybackRate);
			BigDecimal bDecimal02 = new BigDecimal(0.2);
			BigDecimal bDecimal1 = new BigDecimal(1.0);
			BigDecimal bDecimaVar = new BigDecimal(m_fPlaybackRate);
			if (bDecimaVar.compareTo(bDecimal1) == 1) {
				m_fPlaybackRate -= 0.5;
			}else{
				m_fPlaybackRate -= 0.2f;
			}
			
			BigDecimal bDecimaVar1 = new BigDecimal(m_fPlaybackRate);
			if (bDecimaVar1.compareTo(bDecimal02) == -1) {
				m_fPlaybackRate = 0.2f;
			}
			m_fPlaybackRate = (float)(Math.round(m_fPlaybackRate*10))/10;
			mMediaPlayer.setPlayerMode(m_fPlaybackRate);
			Log.i(TAG, "onClickSpeedDown() 0002 m_fPlaybackRate = " + m_fPlaybackRate);
			String speedString = m_fPlaybackRate + "x";
			if (mToast == null) {
				mToast = Toast.makeText(this, speedString, Toast.LENGTH_SHORT);
			}else {
				mToast.setText(speedString);
			}
			mToast.show();
			Log.i(TAG, "onClickSpeedDown() 0003");
		}
	}

	/**
	 * View点击事件，需要继承View.OnClickListener；
	 * 可以判断点击的是View中的哪个按钮，然后进行相关操作
	 */
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.but_play_pausel:
			onClickPlayButton();
			break;
		case R.id.but_stop:
			onClickStopButton();
			break;
		case R.id.btn_add:
			showOpenURLDlg("", "");
			break;
		case R.id.but_speeddown:
			onClickSpeedDown();
			break;
		case R.id.but_speedup:
			onClickSpeedUp();
			break;
		default:
			break;
		}
	}

	/**
	 * 停止播放
	 */
	@SuppressLint("DefaultLocale")
	protected void stopPlayback() {
		m_tvCurrentTime.setText(stringForTime(0));
		m_tvDuration.setText(stringForTime(0));

		if (m_PlayerState != AMMF_STATE.IDLE) {
			m_tvStatus.setText("stopped");
			m_PlayerState = AMMF_STATE.STOPPED;
			m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_play);
			if (null != mMediaPlayer)
			{
				mMediaPlayer.stop();
				m_bManualPaused = false;
			}
			output("onClick,but_stop:end");
			controlBackLight(this, false);

			mRefreshHandler.removeMessages(MSG_GET_POSITION);
			mRefreshHandler.removeMessages(MSG_GET_BUFFERINGPERCENT);
			mRefreshHandler.removeMessages(MSG_GET_BITRATE);
		}

	}

	void showMetadata(String url) {
		Log.e(TAG, "Showing metadata");
		if (url == null) {
			return;
		}

		String strDecode = "Codec mode: "
				+ (true == mMediaPlayer.isHardware() ? "Hardware" : "Software");
		output("Video Decode: " + strDecode);
		if (null != m_tvdMetadata) {
			m_tvdMetadata.setText(strDecode);
		}

		if (url.contains("rtsp://")) {
			String text = String
					.format("%d x %d, RTSP", mMediaPlayer.getVideoWidth(),
							mMediaPlayer.getVideoHeight());
			m_tvcMetadata.setText(text);
			m_tvaMetadata.setVisibility(View.INVISIBLE);
			m_tvvMetadata.setVisibility(View.INVISIBLE);
		} else if (url.contains("http://")) {
			String text = String
					.format("%d x %d, HTTP ", mMediaPlayer.getVideoWidth(),
							mMediaPlayer.getVideoHeight());
			m_tvcMetadata.setText(text);
			m_tvaMetadata.setVisibility(View.INVISIBLE);
			m_tvvMetadata.setVisibility(View.INVISIBLE);		
			
		} else if (url.contains("udp://")) {
			String text = String
					.format("%d x %d, udp", mMediaPlayer.getVideoWidth(),
							mMediaPlayer.getVideoHeight());
			m_tvcMetadata.setText(text);
			m_tvaMetadata.setVisibility(View.INVISIBLE);
			m_tvvMetadata.setVisibility(View.INVISIBLE);
		} else if (url.contains("rtmp://")) {
			String text = String
					.format("%d x %d, RTMP", mMediaPlayer.getVideoWidth(),
							mMediaPlayer.getVideoHeight());
			m_tvcMetadata.setText(text);
			m_tvaMetadata.setVisibility(View.INVISIBLE);
			m_tvvMetadata.setVisibility(View.INVISIBLE);
		}
		else {
			
		}
	}

	@SuppressWarnings({ "deprecation", "unused" })
	private void OutputBitmap() {
		int i = 0;
		int width = 176;
		int height = 144;
		if (null == m_strURL || m_strURL.startsWith("http://")
				|| m_strURL.startsWith("rtsp://")) {
			return;
		}
		VideoThumbnailUtils vthutils = new VideoThumbnailUtils();
		vthutils.setConfigFile(getFilesDir().getAbsolutePath()
				+ "/MV3Plugin.ini");
		try {
			vthutils.setDataSource(m_strURL);
		} catch (IOException e) {

		} catch (IllegalArgumentException e) {

		}

		vthutils.setAllowBlankOutput(false);
		vthutils.setFillStyle(com.arcvideo.MediaPlayer.VideoThumbnailUtils.FIT_OUT);
		vthutils.setRotation(m_nRotation);

		if (90 == m_nRotation || 270 == m_nRotation) {
			int tmp = width;
			width = height;
			height = tmp;
		}

		vthutils.setTargetSize(width, height);
		vthutils.setOutputFormat(android.graphics.PixelFormat.RGB_565);
		vthutils.prepare();
		Bitmap mBitmap = null;
		try {
			mBitmap = vthutils.captureFrame(1000);
			int time = vthutils.getCaptureFrameTime();
			Log.v(TAG, "captureFrame time = " + time);
		} catch (Exception e) {
		}
		if (null != mBitmap) {
			Log.v(TAG, "[VTU]before output thumbnail");
			ImageView OutputImageview = (ImageView) findViewById(R.id.OutputImageview);
			if (null != OutputImageview) {
				OutputImageview.setVisibility(View.VISIBLE);
				AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) OutputImageview
						.getLayoutParams();
				if (null != lp) {
					lp.x = 0;
					lp.y = 0;
					lp.height = height;
					lp.width = width;
					OutputImageview.setLayoutParams(lp);
				}
				OutputImageview.bringToFront();
				OutputImageview.setImageBitmap(mBitmap);
			}
			String path = Environment.getExternalStorageDirectory().getPath()
					+ "/getThumbnail.jpg";
			File f = new File(path);
			if (null != f) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					Log.e(TAG, "[VTU]output file not found");
				}
				FileOutputStream fOut = null;
				try {
					fOut = new FileOutputStream(f);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				try {
					fOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				f.delete();
			}

			Log.v(TAG, "[VTU]after output thumbnail");
			// mBitmap.recycle();
			// mBitmap = null;
		} else {
			Log.e(TAG, "[VTU]bitmap is null");
		}
		vthutils.release();
		vthutils = null;

	}

	public void onBufferingUpdate(ArcMediaPlayer arg0, int percent) {
		Log.d(TAG, "onBufferingUpdate percent: " + percent);
		// String strText = String.format("Buffering... %d%%",
		// percent).toString();
		// m_tvStatus.setText(strText);
		int DLDPercent = (int) (percent * m_lDuration / 100);
		m_seekBar.setSecondaryProgress(DLDPercent);
	}

	/**
	 * 点播正常播放结束回调消息，
	 * 收到该消息说明点播已经正常播完了
	 * 需要app继承ArcMediaPlayer.OnCompletionListener
	 */
	public void onCompletion(ArcMediaPlayer arg0) {
		Log.d(TAG, "onCompletion called");

		mMediaPlayer.stop();
		m_bManualPaused = false;
		m_PlayerState = AMMF_STATE.STOPPED;
		m_tvCurrentTime.setText(stringForTime(0));
		m_tvStatus.setText("Stop");
		m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_play);
		m_seekBar.setProgress(0);
		m_seekBar.setSecondaryProgress(0);
		mRefreshHandler.removeMessages(MSG_GET_POSITION);
		mRefreshHandler.removeMessages(MSG_GET_BUFFERINGPERCENT);
		mRefreshHandler.removeMessages(MSG_GET_BITRATE);
		m_bVideoSizeChanged = false;
		ImageView OutputImageview = (ImageView) findViewById(R.id.OutputImageview);
		OutputImageview.setVisibility(View.GONE);

		saveUrlList();
		if (m_lLoadType == 1) {
			output("Intent out");
			finish();
		} else if (m_cbLooping.isChecked()) {
			mRefreshHandler.removeMessages(MSG_DELAYED_OPEN);
			mRefreshHandler.sendEmptyMessageDelayed(MSG_DELAYED_OPEN, 200);
		}

		controlBackLight(this, false);
	}

	/**
	 * 当视频宽高信息变化时会触发该函数，本测试程序中也有多处调用，
	 * 目的是为了加双重保险，保证屏幕能正确显示视频
	 * ArcMediaPlayer.OnVideoSizeChangedListener
	 */
	@SuppressWarnings("deprecation")
	public void onVideoSizeChanged(ArcMediaPlayer mp, int width, int height) {
		Log.v(TAG, "onVideoSizeChanged called: " + width + "x" + height);

		m_frameWidth = width;
		m_frameHeight = height;
		if (width != 0 && height != 0 && m_PlayerState == AMMF_STATE.STARTED)
			m_bVideoSizeChanged = true;
		Log.v(TAG, "onVideoSizeChanged m_bVideoSizeChanged: "
				+ m_bVideoSizeChanged);
		int nScreenWidth = getWindow().getWindowManager().getDefaultDisplay()
				.getWidth();
		int nScreenHeight = getWindow().getWindowManager().getDefaultDisplay()
				.getHeight();
		float aspect_ratio = mMediaPlayer.getAspectRatio();
		Log.v(TAG, "aspect_ratio=" + aspect_ratio);
		Log.v(TAG, "before adjuct aspect, w=" + m_frameWidth + ",h="
				+ m_frameHeight);
		
		if(aspect_ratio != 0.0) {
			m_frameWidth = Float.floatToIntBits((Float.intBitsToFloat(m_frameHeight) * aspect_ratio));

			Log.v(TAG, "after adjuct aspect, w=" + m_frameWidth + ",h="
					+ m_frameHeight);
		}
		/* start */
		if (m_frameWidth != 0 && m_frameHeight != 0) {
			int estimateW, estimateH;
			switch (mDisplayType) {
			case 0:
				if (nScreenWidth * m_frameHeight > nScreenHeight * m_frameWidth) {
					estimateW = nScreenHeight * m_frameWidth / m_frameHeight;
					estimateH = nScreenHeight;
					if (estimateW % 4 != 0)
						estimateW -= estimateW % 4;
				} else {
					estimateW = nScreenWidth;
					estimateH = nScreenWidth * m_frameHeight / m_frameWidth;
					if (estimateH % 4 != 0)
						estimateH -= estimateH % 4;
				}
				break;
			case 1:			
				if (nScreenWidth * m_frameHeight > nScreenHeight * m_frameWidth) {
					estimateW = nScreenWidth;
					estimateH = nScreenWidth * m_frameHeight / m_frameWidth;
					if (estimateH % 4 != 0)
						estimateH -= estimateH % 4;
				} else {

					estimateW = nScreenHeight * m_frameWidth / m_frameHeight;
					estimateH = nScreenHeight;
					if (estimateW % 4 != 0)
						estimateW -= estimateW % 4;
				}		
				break;
			default:
				estimateW = nScreenWidth;
				estimateH = nScreenHeight;
			}
			int xOffset = (nScreenWidth - estimateW) / 2;
			int yOffset = (nScreenHeight - estimateH) / 2;
			if (xOffset % 4 != 0)
				xOffset -= xOffset % 4;
			if (yOffset % 4 != 0)
				yOffset -= yOffset % 4;
			Log.d(TAG, xOffset + ", " + yOffset + ", " + estimateW + "x"
					+ estimateH);
			if (m_surfaceType == android.view.SurfaceHolder.SURFACE_TYPE_NORMAL) {
				mMediaPlayer.setDisplayRect(xOffset, yOffset, estimateW,
						estimateH);
				SetSurfaceRect(m_pixelFormat, xOffset, yOffset, estimateW,
						estimateH);
			} else {
				m_nClipWidth = estimateW;
				m_nClipHeight = estimateH;
				mMediaPlayer.setDisplayRect(xOffset, yOffset, estimateW,
						estimateH);
				SetSurfaceRect(m_pixelFormat, xOffset, yOffset, estimateW,
						estimateH);
			}
		} else {
			if (m_surfaceType == android.view.SurfaceHolder.SURFACE_TYPE_NORMAL) {
				mMediaPlayer.setDisplayRect(0, 0, nScreenWidth, nScreenHeight);
				SetSurfaceRect(m_pixelFormat, 0, 0, nScreenWidth, nScreenHeight);
			} else {
				// some implicit rules here..
				mMediaPlayer.setDisplayRect(0, 0, nScreenWidth, nScreenHeight);
				SetSurfaceRect(m_pixelFormat, 0, 0, nScreenWidth, nScreenHeight);
			}
		}

		// update info
		showMetadata(m_strURL);
		/* end */
	}

	boolean m_bVideoSizeChanged = false;

	/**
	 * 当播放器内部播放准备工作做好后会通过该函数通知调用者，
	 * 调用者收到该消息后可以调用mMediaPlayer.start()开始播放；
	 * 需要继承ArcMediaPlayer.OnPreparedListener
	 */	
	public void onPrepared(ArcMediaPlayer mediaplayer) {
		Log.d(TAG, "onPrepared called");
		m_PlayerState = AMMF_STATE.PREPARED;
		m_tvCurrentTime.setText(stringForTime(0));
		m_tvStatus.setText("Opened");
		m_seekBar.setProgress(0);
		m_seekBar.setSecondaryProgress(0);

		int nDuration = mMediaPlayer.getDuration();
		m_lDuration = nDuration;
		m_seekBar.setMax(nDuration);
		m_seekBar.setKeyProgressIncrement(SEEK_STEP);
		m_tvDuration.setText(stringForTime(nDuration));

		//播放开始后，使用Handler来不断获取和刷新当前播放时间
		mRefreshHandler.sendEmptyMessage(MSG_GET_POSITION); //

		// if player doesn't notify video size message before prepared
		if (!m_bVideoSizeChanged)
			onVideoSizeChanged(mMediaPlayer, mMediaPlayer.getVideoWidth(),
					mMediaPlayer.getVideoHeight());
				
		m_PlayerState = AMMF_STATE.STARTED;
		mMediaPlayer.start();
		m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_pause);
		

		addNewUrl(m_strURL);
		
	}

	/**
	 * 当播放器内部播放过程中有些必要的消息需要提示使用者时会通过该函数通知调用者，
	 * 调用者收到该消息后可以根据消息类型自动判断是否需要进行处理；
	 * 这些消息不处理也不会影响正常播放，MEDIA_INFO_BUFFERING_START和
	 * MEDIA_INFO_BUFFERING_END消息建议处理一下；
	 * 
	 * 需要继承ArcMediaPlayer.OnInfoListener
	 */
	public boolean onInfo(ArcMediaPlayer mp, int what, int extra) {
		switch (what) {
		case android.media.MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			Log.e(TAG,
					"Bad interleaving of media file, audio/video are not well-formed, extra is "
							+ extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_BUFFERING_END:
			m_bBuffering = false;
			if (mp.isPlaying()) {
				m_tvStatus.setText("Playing");
			} else {
				m_tvStatus.setText("Pause");
			}
			
			Log.e(TAG, "MEDIA_INFO_BUFFERING_END buffering type is " + extra);
			Log.e(TAG, "MEDIA_INFO_BUFFERING_END buffering play position = " + mMediaPlayer.getCurrentPosition());
			mRefreshHandler.removeMessages(MSG_GET_BUFFERINGPERCENT);
			Log.w(TAG,
					"Player is resuming playback after filling buffer(MEDIA_INFO_BUFFERING_END), buffering type is "
							+ extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_BUFFERING_START:
			m_bBuffering = true;
			m_tvStatus.setText("Buffering");
			Log.w(TAG,
					"Player is temporarily pausing playback internally to buffer more data(MEDIA_INFO_BUFFERING_START), buffering type is "
							+ extra);
			mRefreshHandler.sendEmptyMessage(MSG_GET_BUFFERINGPERCENT);
			mRefreshHandler.sendEmptyMessage(MSG_GET_BITRATE);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			Log.w(TAG, "A new set of metadata is available, extra is " + extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			Log.e(TAG, "The stream cannot be seeked, extra is " + extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			Log.w(TAG, "It's too complex for the decoder, extra is " + extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_UNKNOWN:
			Log.w(TAG, "Unknown info, extra is " + extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_RENDERING_START:
			Log.w(TAG, "video decode succeeded, start rendering");
			break;
		//warnings definition below
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_SPLITTER_NOAUDIO:
			Log.e(TAG, "MEDIA_INFO_SPLITTER_NOAUDIO ,Info type is "
					+ what + ", level is " + extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_SPLITTER_NOVIDEO:
			Log.e(TAG, "MEDIA_INFO_SPLITTER_NOVIDEO, Info type is "
					+ what + ", level is " + extra);
			break;
		/*case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_VCODEC_UNSUPPORTVIDEO:
			Log.e(TAG,
					"MEDIA_INFO_VCODEC_UNSUPPORTVIDEO, Info type is "
							+ what + ", level is " + extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_ACODEC_UNSUPPORTAUDIO:
			Log.e(TAG,
					"MEDIA_INFO_ACODEC_UNSUPPORTAUDIO, Info type is "
							+ what + ", level is " + extra);
			break;*/
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_VCODEC_DECODE_ERROR:
			Log.e(TAG, "MEDIA_INFO_VCODEC_DECODE_ERROR, Info type is "
					+ what + ", level is " + extra);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_INFO_ACODEC_DECODE_ERROR:
			Log.e(TAG, "MEDIA_INFO_ACODEC_DECODE_ERROR, Info type is "
					+ what + ", level is " + extra);
			break;
			
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.LICENSE_INFO:
			showLicenseInfo(extra);
			Log.e(TAG, "===license onInfo");
		default:
			Log.i(TAG, "Unknown info code: " + what + ", extra is " + extra);
			break;
		}
		return true;
	}

	/**
	 * 当播放器内部播放过程中出现了严重问题导致播放无法继续时会通过该函数通知调用者，
	 * 调用者收到该消息后必须调用stopPlayback()停止播放，同时也可以进行一些其它的操作；
	 * 
	 * 需要继承ArcMediaPlayer.OnErrorListener
	 */
	public boolean onError(ArcMediaPlayer mp, int what, int extra) {
		boolean bUpdate = false;
		String codecTypeString = "";
		switch (what) {
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_UNSUPPORTED_SCHEME:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_UNSUPPORTED_SCHEME, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_NETWORK_CONNECTFAIL:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_NETWORK_CONNECTFAIL, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_STREAM_OPEN:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_STREAM_OPEN, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_STREAM_SEEK:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_STREAM_SEEK, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DATARECEIVE_TIMEOUT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DATARECEIVE_TIMEOUT, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_FORMAT_UNSUPPORTED:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_FORMAT_UNSUPPORTED, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_FORMAT_MALFORMED:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_FORMAT_MALFORMED, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DNS_RESOLVE:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DNS_RESOLVE, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DNS_RESOLVE_TIMEOUT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DNS_RESOLVE_TIMEOUT, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_NETWORK_CONNECTIMEOUT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_NETWORK_CONNECTIMEOUT, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DATARECEIVE_FAIL:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DATARECEIVE_FAIL, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DATASEND_TIMEOUT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DATASEND_TIMEOUT, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DATAERROR_HTML:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DATAERROR_HTML, value = " + what);
			break;	
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DATASEND_FAIL:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DATASEND_FAIL, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_PLAYER_DISPLAY_INIT_FAILED:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_PLAYER_DISPLAY_INIT_FAILED, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_PLAYER_NOAUDIO_VIDEOUNSUPPORT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_PLAYER_NOAUDIO_VIDEOUNSUPPORT, value = " + what);
			codecTypeString = mMediaPlayer.getMediaMetadata(MV2Config.METADATA.METADATA_KEY_VIDEO_TYPE,bUpdate);
			Log.e(TAG, "onError: unsupproted video codec is " + codecTypeString);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_PLAYER_NOVIDEO_AUDIOUNSUPPORT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_PLAYER_NOVIDEO_AUDIOUNSUPPORT, value = " + what);
			codecTypeString = mMediaPlayer.getMediaMetadata(MV2Config.METADATA.METADATA_KEY_AUDIO_TYPE,bUpdate);
			Log.e(TAG, "onError: unsupproted audio codec is " + codecTypeString);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_PLAYER_AVCODEC_UNSUPPORT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_PLAYER_AVCODEC_UNSUPPORT, value = " + what);
			codecTypeString = mMediaPlayer.getMediaMetadata(MV2Config.METADATA.METADATA_KEY_VIDEO_TYPE,bUpdate);
			Log.e(TAG, "onError: unsupproted video codec is " + codecTypeString);
			codecTypeString = mMediaPlayer.getMediaMetadata(MV2Config.METADATA.METADATA_KEY_AUDIO_TYPE,bUpdate);
			Log.e(TAG, "onError: unsupproted audio codec is " + codecTypeString);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_PLAYER_AVCODEC_AUDIOUNSUPPORT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_PLAYER_AVCODEC_AUDIOUNSUPPORT, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_PLAYER_AVCODEC_VIDEOUNSUPPORT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_PLAYER_AVCODEC_VIDEOUNSUPPORT, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_PLAYER_OPERATION_CANNOTEXECUTE:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_PLAYER_OPERATION_CANNOTEXECUTE, value = " + what);
			break;
			
		case android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_UNKNOWN, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_BUFFER_TIMEOUT:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_BUFFER_TIMEOUT, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_DATARECEIVE_NOBODY:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_DATARECEIVE_NOBODY, value = " + what);
			break;
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_ERROR_SOURCE_SEEK_BEYONDFILESIZE:
			Log.e(TAG, "onError: error type is MEDIA_ERROR_SOURCE_SEEK_BEYONDFILESIZE, value = " + what);
			break;
			
		case com.arcvideo.MediaPlayer.ArcMediaPlayer.LICENSE_ERR:
			Log.e(TAG, "===license onError");
			showLicenseErr(extra);
			break;
		default:	
			if (what >= 100400 && what <= 100599) {
				Log.e(TAG, "onError: error type is one of the http critical status code, value = " + what );
			}
			else {
				Log.e(TAG, "onError: what the Fxxxx!! error is " + what + "? code is " + extra);
			}
			break;
		}
		stopPlayback();
		if(what != com.arcvideo.MediaPlayer.ArcMediaPlayer.LICENSE_ERR)
		{
			showToast("Error code = "+what+", extra = "+extra,false);
		}
		
		return true;
	}

	/**
	 * seek操作成功后，播放器会通过该接口通知调用者
	 * 需要继承ArcMediaPlayer.OnSeekCompleteListener
	 */
	public void onSeekComplete(ArcMediaPlayer mp) {
		Log.d(TAG, "onSeekComplete, new position: " + mp.getCurrentPosition()
				+ "ms");
		if (mMediaPlayer != null && m_PlayerState == AMMF_STATE.PAUSED && !m_bManualPaused) {
			mMediaPlayer.start();
			m_PlayerState = AMMF_STATE.STARTED;
			m_tvStatus.setText("Playing");
			m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_pause);
		}
	}

	private int mKeyHoldCount = 0;

		/**
	 * 设备按键点击时间，Activity自带的函数，表示点击结束手收回的动作;
	 * 这里只监听了设备回退键
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyUp in");
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			onClickPlayButton();
			return true;
		case KeyEvent.KEYCODE_BACK:
			Log.d(TAG, "KEYCODE_BACK in");
			mRefreshHandler.removeMessages(MSG_GET_POSITION);
			onClickBackButton();
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			mKeyHoldCount = 0;

			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
				m_seekBar.setProgress(m_seekBar.getProgress()
						- m_seekBar.getKeyProgressIncrement());
			else
				m_seekBar.setProgress(m_seekBar.getProgress()
						+ m_seekBar.getKeyProgressIncrement());

			onStopTrackingTouch(m_seekBar);
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			onClickStopButton();
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			// m_cbLooping.setChecked(!m_cbLooping.isChecked());
			switchBarDisplay();
			return true;
		default:
			return super.onKeyUp(keyCode, event);
		}
	}

	/**
	 * 设备按键点击事件，Activity自带的函数，表示点击开始手刚点下去的动作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown in");
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			onStartTrackingTouch(m_seekBar);
			mKeyHoldCount++;

			Log.d(TAG,
					"KeyDown:" + m_seekBar.getProgress() + "/"
							+ m_seekBar.getMax() + "??"
							+ m_seekBar.getKeyProgressIncrement() + " flag="
							+ m_bSeekBarTouch + ", holding=" + mKeyHoldCount);

			if (m_seekBar.getMax() != 0) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					m_seekBar.setProgress(m_seekBar.getProgress()
							- m_seekBar.getKeyProgressIncrement());
				else
					m_seekBar.setProgress(m_seekBar.getProgress()
							+ m_seekBar.getKeyProgressIncrement());
			}
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 控制播放界面底部m_bottomBar的显示和隐藏
	 * m_bottomBar中包含了播放按钮\进度条等一些按钮
	 */
	public void switchBarDisplay() {
		int curDisplay = (m_bShowBottomBar ? 1 : 0) + (m_bShowTopBar ? 2 : 0);

		if (m_bShowBottomBar) {
			m_bottomBar.setVisibility(View.GONE);
			m_volumBar.setVisibility(View.INVISIBLE);
		} else {
			m_bottomBar.setVisibility(View.VISIBLE);
		}
		m_bShowBottomBar = !m_bShowBottomBar;

		if (curDisplay == 1 || curDisplay == 3) {
			if (null != m_topBar) {
				if (m_bShowTopBar) {
					// m_topBar.setVisibility(View.GONE);
					m_tvcMetadata.setVisibility(View.INVISIBLE);
					m_tvaMetadata.setVisibility(View.INVISIBLE);
					m_tvvMetadata.setVisibility(View.INVISIBLE);
					m_tvdMetadata.setVisibility(View.INVISIBLE);
				} else {
					showMetadata(m_strURL);
					// m_topBar.setVisibility(View.VISIBLE);
					m_tvcMetadata.setVisibility(View.VISIBLE);
					m_tvaMetadata.setVisibility(View.VISIBLE);
					m_tvvMetadata.setVisibility(View.VISIBLE);
					m_tvdMetadata.setVisibility(View.VISIBLE);
				}
				m_bShowTopBar = !m_bShowTopBar;
			}
		}
	}

	/**
	 * 屏幕触摸事件，activity自带，
	 * 这里只进行了m_bottomBar的显示和隐藏的操作
	 * 就是：点击屏幕如果m_bottomBar是显示的那就把它隐藏，
	 *      如果是隐藏的就把它显示
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			switchBarDisplay();
		}

		return super.onTouchEvent(event);
	}

	// -------------------------------------------------------------------------------------
	/**
	 * 将Uri对象中的播放地址拿出来，变成String，以便设置给player
	 * 只有在使用intent来获取local播放文件的时候才会用到
	 * @param activity
	 * @param uri
	 * @return
	 */
	private static String uriToFilePath(Context activity, Uri uri) {
		if (null == uri || null == activity) {
			return null;
		}
		Log.d(TAG, "uri=" + uri.toString());
		String tempString = null;
		Cursor cur = null;
		if (uri.toString().startsWith("http://")) {
			tempString = uri.toString();
			tempString = tempString.replaceAll("%20", " ");
		} else if (uri.toString().startsWith("file://")) {
			tempString = uri.toString().substring(7);
			tempString = tempString.replaceAll("%20", " ");
			tempString = tempString.replaceAll("%25", "%");
		} else {
			ContentResolver cr = activity.getContentResolver();

			cur = cr.query(uri, null, null, null, null);
			if (null != cur) {
				if (cur.moveToFirst()) {
					int idIndex = cur
							.getColumnIndex(MediaStore.Audio.Media.DATA);
					tempString = cur.getString(idIndex);
				}
			}
		}

		if (cur != null) {
			cur.close();
		}

		return tempString;
	}

	/**
	 * 只有在使用intent来获取local播放文件或者使用代码调用intent来传uri的时候才会用到该函数
	 * 使用场景：设备中有一个视频，手动选择点击这个视频，系统会跳出
	 * 一个界面让使用者选择一个播放器进行播放，这种情况下会调用该函数
	 * @param uri
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private void openUri(Uri uri) throws IllegalArgumentException,
			IllegalStateException, IOException {
		if (uri.toString().startsWith("rtsp://"))
			openFileStr(uri.toString());
		else {
			String path = uriToFilePath(this, uri);
			Log.v(TAG, "Convert URI " + uri + " to: " + path);
			openFileStr(path);
		}
	}
	
	/**
	 * 在直接拿到String类型的播放地址后，需要调用该函数来进行播放
	 * 该接口是最常用的打开播放地址的方法
	 * @param str
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@SuppressLint("NewApi")
	private void openFileStr(String str) throws IllegalArgumentException,
			IllegalStateException, IOException {
		boolean bValid = isInputTextValid(str);
		if (!bValid) {
			showToast(getString(R.string.str_invalid_url), false);
		} else {
			/*******************************************************************/
			//2015-01-19 yurong update for url order number  begine
			/*******************************************************************/
			//5,http://172.17.197.110/ammf/H264_BL_800X480_1500kbps_29.97FPS_AAC(LC).mp4
			String[] m_strURL_temp = str.split(",");
			if(m_strURL_temp[0].contains("/")) {
				m_strURL = str;
				m_strURL_header = "";
			} else if (m_strURL_temp.length == 2) {				
				m_strURL = m_strURL_temp[1];
				m_strURL_header = m_strURL_temp[0] + ",";
			} else if (m_strURL_temp.length == 1) {
			} else {
				Log.e("ammf", "--------------->this url is wrong!");
			}
//			m_strURL = str;
			Log.e("ammf", "--------openFileStr------->" + m_strURL);
			
			/*******************************************************************/
			//2015-01-19 yurong update for url order number  end
			/*******************************************************************/
			
			if (m_lLoadType == 0 && m_currDlg != null)
				m_currDlg.cancel();

			long res = -1;

			output("Opening " + m_strURL);

			m_lDuration = 0;		

			mMediaPlayer.reset();

			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("User-Agent", "Arcvideo Player/3.5");
			headers.put("Referer", "Arcvideo Sample Player");
			//设置播放地址	
			
			m_fPlaybackRate = 1;
			if (m_uri == null) {
				mMediaPlayer.setDataSource(m_strURL,headers);
			} else {
				output("Opening use uri:" + m_uri.toString());
				mMediaPlayer.setDataSource(this, m_uri, headers);
			}
			//设置各种监听Listener
			output("setOnBufferingUpdateListener");
			mMediaPlayer.setOnBufferingUpdateListener(this);

			output("setOnCompletionListener");
			mMediaPlayer.setOnCompletionListener(this);

			output("setOnPreparedListener");
			mMediaPlayer.setOnPreparedListener(this);

			output("setOnVideoSizeChangedListener");
			m_bVideoSizeChanged = false;
			mMediaPlayer.setOnVideoSizeChangedListener(this);

			mMediaPlayer.setOnInfoListener(this);

			mMediaPlayer.setOnErrorListener(this);

			mMediaPlayer.setOnSeekCompleteListener(this);

			mMediaPlayer.setOnMessageListener(this);
			
			//设置SurfaceHolder给播放器，用于播放显示
			output("setDisplay");
			SurfaceHolder sh = null;
			sh = m_surfaceView.getHolder();
			output("setDisplay sh = " + sh);
			mMediaPlayer.setDisplay(sh);
			if (m_cbUseHardwareMode.isChecked()) {
				mMediaPlayer.setHardwareMode(true);
			}else {
				mMediaPlayer.setHardwareMode(false);
			}
			
			//
			mMediaPlayer.setBenchmark(2);

			//mMediaPlayer.setMode(com.arcvideo.MediaPlayer.ArcMediaPlayer.MEDIA_PLAYER_MODE_FORWARD, 2);

			//设置播放器初始化缓冲的时间
			mMediaPlayer.setConfig(MEDIAFILE.INITIAL_BUFFERTIME_ID, 500);
			//设置播放器最少缓冲多少时间数据就可以开始播放，默认是5秒，播放直播流时设置，播放点播流不用设置
			//mMediaPlayer.setConfig(MEDIAFILE.PLAYINGTIME_ID, 1500);
			//设置播放器最大可以缓冲多少时间的数据，播放直播流设置，点播流不用设置
			//mMediaPlayer.setConfig(MEDIAFILE.MAX_BUFFERTIME_ID, 5000);
			//设置播放器缓冲超时时间，默认是5秒 根据需要设置
			//mMediaPlayer.setConfig(MV2Config.MEDIAFILE.BUFFERING_TIMEOUT_ID, 1800);
			//设置与服务器连接超时时间，mConnectTimeout单位为秒
			mMediaPlayer.setConfig(ArcMediaPlayer.CONFIG_NETWORK_CONNECT_TIMEOUT, mConnectTimeout * 1000);
			//设置与服务器连接超时时间，mConnectTimeout单位为秒			
			mMediaPlayer.setConfig(ArcMediaPlayer.CONFIG_NETWORK_RECEIVE_TIMEOUT, mReceiveTimeout * 1000);
			//设置失败重连次数			
			mMediaPlayer.setConfig(ArcMediaPlayer.CONFIG_NETWORK_RECONNECT_COUNT, mReconnectCount);
			
			//播放加密流需要设置当虹云 customid and contentid
			String customidString = "";
			String contentidString = "";

			if (customidString.length() > 0 
					&& contentidString.length() > 0 ) {
				output("customidString  = " + customidString);
				output("contentidString  = " + contentidString);
				mMediaPlayer.setUserInfo(MEDIAFILE.DRM_CUSTOM_ID, customidString);
				mMediaPlayer.setUserInfo(MEDIAFILE.DRM_CONTENT_ID, contentidString);
			}
			
			//开始播放准备(使用前面设置的url和其它参数进行播放准备工作)
			//准备工作结束后会通过onPrepared通知APP
			mMediaPlayer.prepareAsync();
			
			//使用视频宽高重新计算显示区域
			onVideoSizeChanged(mMediaPlayer, mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());

			//控制屏幕背景灯			
			controlBackLight(this, true);

			//当前App的一些界面元素状态的控制			
			m_PlayerState = AMMF_STATE.PREPARING;
			m_tvCurrentTime.setText(stringForTime(0));
			m_tvDuration.setText(stringForTime(0));
			m_lDuration = 0;
			m_tvStatus.setText("Connecting...");

			m_tvCurrentTime.setText(stringForTime(0));
			m_tvDuration.setText(stringForTime(0));
			m_tvStatus.setText("Opening...");
			output("showOpenURLDlg 12" + res);
			output("showOpenURLDlg 13");
			
			//暂停其它后台音乐
			getAudioFocus();
		}
	}

	
	private void showLicenseErr(int res) {
		String errorInfo = "";
		switch (res) {
			case VALIDATE.LICENSE_ERR_DISABLE_APP_NAME :
				errorInfo = "appname不一致，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_AUTHENTICATE_FAIL :
				errorInfo = "验证不通过，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_INVALID_PARAM :
				errorInfo = "账号无设置，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_MEM_NOT_ENOUGH :
				errorInfo = "内存不足，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_PARAMETER_DISACCORD :
				errorInfo = "多SDK账号信息不一致，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_DIRECTORY_ERR :
				errorInfo = "目录错误，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_SDK_NO_EXITS :
				errorInfo = "sdk信息不存在，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_SDK_PLANTFORM_NO_SUPPORT :
				errorInfo = "平台不支持，禁止";
				break;
				
			case VALIDATE.LICENSE_ERR_DISABLE_NO_UPDATE :
				errorInfo = "无更新，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_NETWORK :
				errorInfo = "网络错误，禁止";
				break;
			case VALIDATE.LICENSE_ERR_DISABLE_DATA_FORMAT :
				errorInfo = "license格式错误，禁止";
				break;	
			default :
				errorInfo = "未知错误!";
				break;
		}
		
		if (0 != res) {
			Toast.makeText(this, errorInfo, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	private void showLicenseInfo(int res) {
		String errorInfo = "";
		switch (res) {	
			case VALIDATE.LICENSE_INFO_ENABLE_SDK_EXPIREDATE :
				errorInfo = "sdk过期";
				break;	
			case VALIDATE.LICENSE_INFO_ENABLE_NO_UPDATE :
				errorInfo = "license没更新";
				break;
			case VALIDATE.LICENSE_INFO_ENABLE_NETWORK :
				errorInfo = "网络错误";
				break;
			case VALIDATE.LICENSE_INFO_ENABLE_DATA_FORMAT :
				errorInfo = "license格式错误";
				break;
			default :
				errorInfo = "未知错误!";
				break;
		}
		
		if (0 != res) {
			Toast.makeText(this, errorInfo, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	
	
	
	/**
	 * 控制屏幕背景灯
	 * true---保持屏幕一直亮着；
	 * false --- 取消屏幕一直亮着的设置
	 * @param activity
	 * @param flag
	 */
	public static void controlBackLight(Activity activity, boolean flag) {
		if (null == activity)
			return;

		Window win = activity.getWindow();
		if (flag) {
			win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public boolean onMessage(ArcMediaPlayer mp, int messageInfo, int level) {
		// TODO Auto-generated method stub
		switch (messageInfo) {
		default:
			Log.e(TAG, "unknown messageInfo  is " + messageInfo + ", level is "
					+ level);

			break;
		}
		return true;
	}

	// menu

	private static final int EXIT_ID = Menu.FIRST + 3;
	private static final int ROTATION_ID = Menu.FIRST + 5;
	private static final int VOLUM_ID = Menu.FIRST + 6;
	private static final int SETTING_ID = Menu.FIRST + 7;
	private Spinner m_spinner;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		boolean b = super.onCreateOptionsMenu(menu);

		menu.add(0, EXIT_ID, 3, R.string.exitaudiofx);
		menu.add(0, ROTATION_ID, 5, R.string.rotation);
		menu.add(0, VOLUM_ID, 6, R.string.volum);
		menu.add(0, SETTING_ID, 6, R.string.setting);
		return b;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// boolean bIsMediaPath = false;
		boolean b = super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (ROTATION_ID):
			setRotation();
			break;
		case (VOLUM_ID):
			setVolum();
			break;
		case SETTING_ID:
			showSettingDialog();
			break;
		}

		return b;
	}

	private LayoutInflater mInflater;
	private View mView;

	private List<String> mRotationList;

	public void setRotation() {
		try {
			mRotationList = new ArrayList<String>();
			mRotationList.add("0");
			mRotationList.add("90");
			mRotationList.add("180");
			mRotationList.add("270");

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, mRotationList);
			mInflater = LayoutInflater.from(ArcPlayerSample.this);
			mView = mInflater.inflate(R.layout.presets, null);
			m_spinner = (Spinner) mView.findViewById(R.id.spinner1);
			m_spinner.setAdapter(adapter);

			new AlertDialog.Builder(this)
					.setView(mView)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									try {
										String szPreset = m_spinner
												.getSelectedItem().toString();
										m_nRotation = Integer
												.parseInt(szPreset);

									} catch (Exception e) {
										e.printStackTrace();
										showToast(e.getMessage(), false);
									}
								}
							}).show();
		} catch (Exception e) {
			showToast(e.getMessage(), false);
		}
	}

	public void setVolum() {
		try {
			m_volumBar.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			showToast(e.getMessage(), false);
		}
	}

	public void trytoroot() {
		try {
			boolean retval = false;
			Process process = Runtime.getRuntime().exec("su");
			int suProcessRetval = process.waitFor();
			if (255 != suProcessRetval) {
				retval = true;
			} else {
				retval = false;
			}
			System.out.println("root: " + retval);
		} catch (Exception ex) {
			// Log.e("Error executing root action", ex);
		}
	}
	
	/**
	 * 系统函数，当Activity退后台或者关闭时会触发
	 * 比如：按back键，按home键，按电源键锁屏，或屏幕自动锁屏
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "KEYCODE_HOME in");
		if (mMediaPlayer != null && m_PlayerState == AMMF_STATE.STARTED) {
			m_tvStatus.setText("Pause");
			m_imgBtnPlayPause.setBackgroundResource(R.drawable.ic_videoview_play);
			mMediaPlayer.pause();
			m_PlayerState = AMMF_STATE.PAUSED;
		}

	}

	/**
	 * 系统函数，当Activity重新打开或者激活时触发
	 * 与onPause()相对
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (mMediaPlayer != null && m_PlayerState == AMMF_STATE.PAUSED) {
			mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition());
		}
	}

	private void showSettingDialog() {
		final Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar);
		dialog.setContentView(R.layout.dialog_setting);
		final EditText editConnectTimeout = (EditText) dialog.findViewById(R.id.edit_connect_timeout);
		final EditText editReceiveTimeout = (EditText) dialog.findViewById(R.id.edit_receive_timeout);
		final EditText editReconnectCount = (EditText) dialog.findViewById(R.id.edit_reconnect_count);
		final Button btnOk = (Button) dialog.findViewById(R.id.btn_setting_ok);
		final Button btnCancel = (Button) dialog.findViewById(R.id.btn_setting_cancel);
		
		editConnectTimeout.setText(String.valueOf(mConnectTimeout));
		editReceiveTimeout.setText(String.valueOf(mReceiveTimeout));
		editReconnectCount.setText(String.valueOf(mReconnectCount));
		
		btnOk.setOnClickListener(new View.OnClickListener(){
			@Override
            public void onClick(View v) {
				String strConnectTimeout = editConnectTimeout.getText().toString();
				String strReceiveTimeout = editReceiveTimeout.getText().toString();
				String strReconnectCount = editReconnectCount.getText().toString();
				if (strConnectTimeout.length() <= 0 
						|| strReceiveTimeout.length() <= 0
						|| strReconnectCount.length() <= 0) {
					Toast.makeText(ArcPlayerSample.this, "please input valid number", Toast.LENGTH_SHORT).show();
					return;
				}
				
				int connectTimeout = Integer.parseInt(strConnectTimeout);
				int receivtTimeout = Integer.parseInt(strReceiveTimeout);
				int reconnectCount = Integer.parseInt(strReconnectCount);
				
				if (connectTimeout <= 0 || receivtTimeout <= 0) {
					Toast.makeText(ArcPlayerSample.this, "timeout must bigger than 0", Toast.LENGTH_SHORT).show();
					return;
				}
				
				mConnectTimeout = connectTimeout;
				mReceiveTimeout = receivtTimeout;
				mReconnectCount = reconnectCount;
				mMediaPlayer.setConfig(ArcMediaPlayer.CONFIG_NETWORK_CONNECT_TIMEOUT, mConnectTimeout * 1000);
				mMediaPlayer.setConfig(ArcMediaPlayer.CONFIG_NETWORK_RECEIVE_TIMEOUT, mReceiveTimeout * 1000);
				mMediaPlayer.setConfig(ArcMediaPlayer.CONFIG_NETWORK_RECONNECT_COUNT, mReconnectCount);
				
				dialog.dismiss();
            }
		});
		
		btnCancel.setOnClickListener(new View.OnClickListener(){
			@Override
            public void onClick(View v) {
				dialog.cancel();
            }
		});
		
		dialog.show();
	}
	
	/**
	 * 获取音频焦点，成功获取后其他声音(后台音乐)将被暂停，只播出当前应用的声音
	 */
	private void getAudioFocus(){
		if (mAudioManager == null) {
			mAudioManager = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);
		}
		mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	}
	
	/**
	 * 释放音频焦点，成功释放后其他被暂停声音(后台音乐)将被恢复
	 */
	private void abandonAudioFocus(){
		if (mAudioManager == null) {
			mAudioManager = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);
		}
		mAudioManager.abandonAudioFocus(null);
	}
	
}
