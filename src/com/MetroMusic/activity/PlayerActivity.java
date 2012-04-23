package com.MetroMusic.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import api.BroadcastCode;

import com.MetroMusic.aidl.PlayerServiceHelper;
import com.MetroMusic.aidl.PlayerUIHelper;
import com.MetroMusic.controller.PlayerController;
import com.MetroMusic.helper.PlayerState;
import com.MetroMusic.helper.SongInfomation;
import com.MetroMusic.helper.SystemState;
import com.MetroMusic.listener.PlayerAlbumGestureListener;
import com.MetroMusic.model.LyricModel;
import com.MetroMusic.service.PlayerService;
import com.MetroMusic.view.LrcView;
public class PlayerActivity extends MMAbstractActivity implements OnTouchListener{

	/* UIs  */
	private Button	playButton;
	private Button	nextButton;
	private Button	settingButton;
	private Button  loveButton;
	private ProgressBar waitProgressBar;
	private ImageView	songImageView;
	private TextView 	songTitle;
	private TextView	songTime;
	private ProgressBar musicProgressBar;
	private FrameLayout songImageLayout;
	private LrcView	lyricView;
	private Notification  notification;
	private NotificationManager nm;
	private GestureDetector mGestureDetector;
	private PlayerAlbumGestureListener gestureListener;
	
	/* BroadcastReceivers */
	private BroadcastReceiver lrcReceiver;
	
	/* Constraint */
	private final static int MENU_HATE			= 0x01;
	private final static int MENU_ABOUT			= 0x02;
	private final static int MENU_EXIT_PROCESS	= 0x03;
	/* ********** */
	
	/* Listeners  */
	private View.OnClickListener startListener	  = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			PlayerController playerController = ((PlayerController)controller);
			playerController.onStart();
		}
	};
	
	private View.OnClickListener stopListener  	 = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			PlayerController playerController = ((PlayerController)controller);
			playerController.onPause();
		}
	};
	
	private View.OnClickListener nextListener		= new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			PlayerController playerController = ((PlayerController)controller);
			gestureListener.refreshImage();
			playerController.onNext();
		}
	};
	
	private View.OnClickListener loveListener		= new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			loveButton.setEnabled(false);
			PlayerController playerController = ((PlayerController)controller);
			playerController.onLove();
		}
	};
	
	private View.OnClickListener unloveListener		= new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			loveButton.setEnabled(false);
			PlayerController playerController = ((PlayerController)controller);
			playerController.onUnLove();
		}
	};
	
	private View.OnClickListener settingListener	= new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			PlayerController playerController = ((PlayerController)controller);
			playerController.onSetting();
		}
	};
	
	/* *********  */
	private Handler stateHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Resources res = PlayerActivity.this.getResources();
			switch(msg.what)
			{
			case PlayerState.PLAY:
			{
				playButton.setBackgroundDrawable(res.getDrawable(R.drawable.mp_pausebtn_style));
				playButton.setOnClickListener(stopListener);
				break;
			}
			case PlayerState.PAUSE:
			{
				playButton.setBackgroundDrawable(res.getDrawable(R.drawable.mp_playbutton_style));
				playButton.setOnClickListener(startListener);
				break;
			} 
			case PlayerState.STOP:
			{
				playButton.setBackgroundDrawable(res.getDrawable(R.drawable.mp_playbutton_style));
				playButton.setOnClickListener(startListener);
				break;
			}
			case PlayerState.WAIT:
			{
				waitProgressBar.setVisibility(View.VISIBLE);
				playButton.setEnabled(false);
				nextButton.setEnabled(false);
				loveButton.setEnabled(false);
				
				lyricView.clear(); // Clear lrc view
				break;
			}
			case PlayerState.READY:
			{
				waitProgressBar.setVisibility(View.GONE);
				playButton.setEnabled(true);
				nextButton.setEnabled(true);
				loveButton.setEnabled(true);
				break;
			}
			case PlayerState.PROGRESS_MAX:
			{
				int max = msg.arg1;
				musicProgressBar.setMax(max);
				break;
			}
			case PlayerState.PROGRESS:
			{
				int position = msg.arg1;
				musicProgressBar.setProgress(position);
				lyricView.updateIndex(position < 1 ? 100 : position * 1000);
				
				break;
			}
			case PlayerState.BUFFERING:
			{
				int position = msg.arg1;
				musicProgressBar.setSecondaryProgress(position);
				break;
			}
			case PlayerState.LOVE:
			{
				loveButton.setBackgroundDrawable(res.getDrawable(R.drawable.mp_islovebtn_style));
				loveButton.setOnClickListener(unloveListener);
				break;
			}
			case PlayerState.UNLOVE:
			{
				loveButton.setBackgroundDrawable(res.getDrawable(R.drawable.mp_lovebtn_style));
				loveButton.setOnClickListener(loveListener);
				break;
			}
			case PlayerState.ENABLE_LOVE:
			{
				loveButton.setEnabled(true);
				break;
			}
			case PlayerState.DISABLE_LOVE:
			{
				loveButton.setEnabled(false);
				break;
			}
			}
			super.handleMessage(msg);
		}
	};
	
	private Handler systemHandler =  new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case SystemState.NET_WORK_ERROR:
			{
				String message = (String)msg.obj;
				Toast.makeText(getApplicationContext(), "错误:\n"+message, Toast.LENGTH_LONG).show();
				break;
			}
			}
			super.handleMessage(msg);
		}
		 
	};
	
	private Handler songInfomationHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case SongInfomation.IMAGE: 
			{
				Bitmap bitmap = (Bitmap)msg.obj;
				songImageView.setImageBitmap(bitmap);
				
				/**
				 * Start album image animation
				 */
				Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mp_songimage_animation);
				songImageView.startAnimation(animation);
				break;
			}
			case SongInfomation.TITLE:
			{
				String title  = (String)msg.obj;
				
				/**
				 * Update the song information in activity
				 */
				songTitle.setText(title);
				
				notification.tickerText = "正在播放："+title;
				RemoteViews rv	= notification.contentView;
				rv.setTextViewText(R.id.notificationStatusText, "正在播放："+title);
				nm.cancel(R.string.app_name);
				nm.notify(R.string.app_name, notification);
				break;
			}
			case SongInfomation.TIME:
			{
				String time   = (String)msg.obj;
				songTime.setText("本曲时长："+time);
				break;
			}
			}
			super.handleMessage(msg);
		}
		
	};
	
	public Handler getStateHandler() {
		return stateHandler;
	}
	
	public Handler getSystemHandler()
	{
		return systemHandler;
	}

	public Handler getSongInfomationHandler()
	{
		return songInfomationHandler;
	}
	
	/* Service Interface */
	private PlayerServiceHelper serviceHelper;
	
	/* Connection */
	private ServiceConnection mConnection = new ServiceConnection() {  
    	public void onServiceConnected(ComponentName className,  IBinder service) {  
    		serviceHelper = PlayerServiceHelper.Stub.asInterface(service);  
    		try {
				serviceHelper.setPlayerUIHelper(new UIHelper());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		PlayerController playerController = ((PlayerController)controller);
    		playerController.setPlayHelper(serviceHelper);
    	}  
    	public void onServiceDisconnected(ComponentName className) {  
    		Log.i("Tag","disconnect service");  
    		serviceHelper = null;  
    	}  
	}; 
	
	public PlayerServiceHelper getServiceHelper() {
		return serviceHelper;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		setWindowProperties();
		setContentView(R.layout.player);
		setController(new PlayerController(this));
		setupViews();
		setupBroadcastReceiver();
	}

    private void setWindowProperties()
    {
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
        getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Intent serviceIntent = new Intent(getApplicationContext(),PlayerService.class);
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		
		IntentFilter intentFilter = new IntentFilter(BroadcastCode.DOWNLOAD_LRC);
		registerReceiver(lrcReceiver,intentFilter);
		
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		PlayerActivity.this.unbindService(mConnection);
		
		unregisterReceiver(lrcReceiver);
		
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(1, MENU_HATE, MENU_HATE, "不再播放");
		menu.add(1, MENU_ABOUT, MENU_ABOUT, "关于");
		menu.add(1, MENU_EXIT_PROCESS, MENU_EXIT_PROCESS, "退出程序");
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch( item.getItemId() )
		{
		case MENU_HATE:
		{
			PlayerController playerController = ((PlayerController)controller);
			playerController.neverPlay();
			break;
		}
		case MENU_EXIT_PROCESS:
		{
			new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("确认退出")
	        .setMessage("您真的要退出吗")
	        .setPositiveButton("是", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					PlayerActivity.this.finish();
					((PlayerController)controller).close();
					Intent intent = new Intent(getApplicationContext(),PlayerService.class);
					nm.cancelAll();
					getApplicationContext().stopService(intent);
					
				}
	        })
	        .setNegativeButton("否",null)
	        .show();
			break;
		}
		case MENU_ABOUT:
		{
			Intent intent = new Intent( this, AboutActivity.class );
			startActivity(intent);
		}
		default:
			return super.onMenuItemSelected( featureId, item);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent home = new Intent(Intent.ACTION_MAIN);  
		home.addCategory(Intent.CATEGORY_HOME);   
		startActivity(home); 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode == Activity.RESULT_OK)
		{
			Bundle bundle = data.getBundleExtra("bundle"); 
			PlayerController playerController = ((PlayerController)controller);
			playerController.setUserData(bundle);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}



	/***
	 * IPC interface for activity
	 * @author Coffee
	 */
	class  UIHelper extends PlayerUIHelper.Stub
	{
		@Override
		public void showWaitBar(boolean show) throws RemoteException {
			// TODO Auto-generated method stub
			Message msg = stateHandler.obtainMessage();
			msg.what = show ? PlayerState.WAIT : PlayerState.READY;
			stateHandler.sendMessage(msg);
		}

		@Override
		public void setMusicProgressBarMax(int max) throws RemoteException {
			// TODO Auto-generated method stub
			Message msg = stateHandler.obtainMessage();
			msg.what	= PlayerState.PROGRESS_MAX;
			msg.arg1	= max;
			stateHandler.sendMessage(msg);
		}

		@Override
		public void updateMusicProgress(int position) throws RemoteException {
			// TODO Auto-generated method stub
			Message msg = stateHandler.obtainMessage();
			msg.what	= PlayerState.PROGRESS;
			msg.arg1	= position;
			stateHandler.sendMessage(msg);
		}

		@Override
		public void updateBufferingProgress(int position)
				throws RemoteException {
			// TODO Auto-generated method stub
			Message msg = stateHandler.obtainMessage();
			msg.what	= PlayerState.BUFFERING;
			msg.arg1	= position;
			stateHandler.sendMessage(msg);
		}

		@Override
		public void updateLrctime(long time) throws RemoteException {
			// TODO Auto-generated method stub
			Message msg = stateHandler.obtainMessage();
			msg.what	= PlayerState.LRC_UPDATE;
			msg.obj		= time;
			stateHandler.sendMessage(msg);
		}
		
	}



	@Override
	protected void setupViews() {
		// TODO Auto-generated method stub
		this.playButton 		= (Button)findViewById(R.id.playbtn);
		this.nextButton 		= (Button)findViewById(R.id.nextbtn);
		this.settingButton		= (Button)findViewById(R.id.settingBtn);
		this.waitProgressBar 	= (ProgressBar)findViewById(R.id.waitProgressBar);
		this.songImageView		= (ImageView)findViewById(R.id.songimageview);
		this.songTime			= (TextView)findViewById(R.id.songtime);
		this.songTitle			= (TextView)findViewById(R.id.songtitle);
		this.musicProgressBar	= (ProgressBar)findViewById(R.id.musicProgressbar);
		this.loveButton			= (Button)findViewById(R.id.lovebtn);
		this.songImageLayout	= (FrameLayout)findViewById(R.id.songimageframe);
		this.lyricView			= (LrcView)findViewById(R.id.lrctextview);

		this.nm					= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		this.notification 		= new Notification(R.drawable.ic_launcher, "正在播放：", System.currentTimeMillis());
		
		this.songImageLayout.setOnTouchListener(this);
		this.songImageLayout.setLongClickable(true);
		
		gestureListener			= new PlayerAlbumGestureListener(songImageView,lyricView,this);
		mGestureDetector		= new GestureDetector(gestureListener);
		
		/***
		 * 初始化通知栏
		 */
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		Intent i = new Intent(getApplicationContext(), PlayerActivity.class);
		i.setAction(Intent.ACTION_MAIN);  
		i.addCategory(Intent.CATEGORY_LAUNCHER);  
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  
		PendingIntent contentIntent = PendingIntent.getActivity(
		        getApplication(),
		        R.string.app_name,
		        i,
		        PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentView = new RemoteViews(PlayerActivity.this.getPackageName(), R.layout.notification);
		notification.contentIntent = contentIntent;
		super.setupViews();
	}
	
	protected void setupBroadcastReceiver()
	{
		lrcReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				LyricModel lyric = (LyricModel)intent.getSerializableExtra("lyric");
				lyricView.setLyric(lyric);
				
			}
			
		};
	}

	@Override
	protected void setListeners() {
		// TODO Auto-generated method stub
		this.playButton.setOnClickListener(startListener);
		this.nextButton.setOnClickListener(nextListener);
		this.loveButton.setOnClickListener(loveListener);
		this.settingButton.setOnClickListener(settingListener);
	}

	/***
	 * 识别滑动手势
	 */
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event); 
	};
	
	
	
}
