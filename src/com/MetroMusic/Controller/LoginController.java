package com.MetroMusic.Controller;

import java.io.InputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.MetroMusic.Activity.LoginActivity;
import com.MetroMusic.Activity.SettingActivity;
import com.MetroMusic.Helper.LoginState;
import com.MetroMusic.Model.UserModel;

public class LoginController {
	private LoginActivity loginActivity;
	private UserModel	  userModel,loginUser;
	private UserManager   userManager;
	private String 		  captchaCode;
	private String		  captcha;
	private Context appContext;
	
	public LoginController(LoginActivity loginActivity)
	{
		this.loginActivity = loginActivity;
	}
	
	public void Bind()
	{
		appContext = loginActivity.getApplicationContext();
		userManager	= new UserManager(appContext);
		
		userModel	= new UserModel();
		
		
		loginActivity.setOnLoginClickListener(new LoginListenerImpl());
		loginActivity.setOnCaptchaImageClickListener(new CaptchaClickListenerImpl());
		
		
		userManager.setAppContext(appContext);
		userManager.setCaptchaCompeltionListener(new CaptchaLoadCompletion());
		userManager.loadCaptchaAsync();
	}
	
	class LoginListenerImpl	implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!loginActivity.checkUsernameAvaliable())
			{
				Toast.makeText(loginActivity.getApplicationContext(), "���������û���", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!loginActivity.checkPasswordAvaliable())
			{
				Toast.makeText(loginActivity.getApplicationContext(), "������������", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!loginActivity.chechCaptchaAvaliable())
			{
				Toast.makeText(loginActivity.getApplicationContext(), "����������֤��", Toast.LENGTH_SHORT).show();
				return;
			}
			String username = loginActivity.getUsername();
			String password = loginActivity.getPassword();
			captcha	= loginActivity.getCaptcha();
			UserModel user = new UserModel();
			user.setUsername(username);
			user.setPassword(password);
			new LoginTask().execute(user);
		}
	}
	
	class LoginTask	extends AsyncTask<UserModel, Void, String>
	{
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = new ProgressDialog(loginActivity);
			progressDialog.setCancelable(true);
			progressDialog.setIndeterminate(true);
			progressDialog.setTitle("���ڵ�½");
			progressDialog.setMessage("��½�У����Ժ�");
			progressDialog.show();
		}

		@Override
		protected String doInBackground(UserModel... users) {
			// TODO Auto-generated method stub
			String msg = "success";
			try{
				loginUser = userManager.userLogin(users[0], captcha, captchaCode);
			}
			catch(RuntimeException re)
			{
				msg = re.getMessage();
			}
			return msg;
		}
		
    	@Override
    	protected void onPostExecute(String result) { 
    		progressDialog.dismiss();
    		if(result == null )
    		{
    			Toast.makeText(loginActivity.getApplicationContext(), "��¼�ĵط����쳣û�в���", Toast.LENGTH_LONG).show();
    			return;
    		}
    		if(result.equals("success"))
    		{
    			Toast.makeText(loginActivity.getApplicationContext(), "��½�ɹ�", Toast.LENGTH_LONG).show();
    			Intent intent = new Intent(loginActivity,SettingActivity.class);
    			Bundle bundle = new Bundle();
    			bundle.putSerializable("loginuser", loginUser);
    			intent.putExtra("bundle", bundle);
    			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    			loginActivity.startActivity(intent);
    		}
    		else
    		{
    			Toast.makeText(loginActivity.getApplicationContext(), "��½ʧ�ܣ�"+result, Toast.LENGTH_LONG).show();
    		}
    	}
		
	}
	
	class CaptchaLoadCompletion implements UserManager.OnCaptchaCompletionListener
	{

		@Override
		public void onCompletion(InputStream is, String code) {
			// TODO Auto-generated method stub
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			loginActivity.updateCaptchaImage(bitmap);
			captchaCode	= code;
		}
		
	}
	
	class CaptchaClickListenerImpl implements View.OnClickListener
	{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			loginActivity.getUIHandler().sendEmptyMessage(LoginState.CAPTCHA_LOAD);
			userManager.loadCaptchaAsync();
		}
	}
	
}
