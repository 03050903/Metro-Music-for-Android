package com.MetroMusic.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.MetroMusic.activity.R;
import com.MetroMusic.dao.CookieDAO;
import com.MetroMusic.dbhelper.DataBaseHelper;
import com.MetroMusic.http.AsyncHttpClient;
import com.MetroMusic.http.RequestParams;

public class NetworkManager {
	private AsyncHttpClient httpClient;
	private Context			appContext;
	
	public NetworkManager(Context context)
	{
		appContext = context;
		httpClient = AsyncHttpClient.getInstance(context);
	}
	
	public InputStream executeInPost(String uri,RequestParams params)
	{
		HttpPost httpPost = null;
		httpPost = new HttpPost(uri);
		try {
			if (params != null)
			{
				httpPost.setEntity(new UrlEncodedFormEntity(params.getEntityList(),
						HTTP.UTF_8));
			}
			HttpResponse response = null;
			response = httpClient.executeSync(httpPost);
			return response.getEntity().getContent();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public InputStream execute(String uri,RequestParams params) throws IOException
	{
		HttpGet  httpGet = null;
		if(params != null)
		{
			URI 	 httpUri  = params.getUrlWithParams(uri);
			httpGet = new HttpGet(httpUri);
		}
		else
		{
			httpGet = new HttpGet(uri);
		}
		HttpResponse response = null;
		try {
			response = httpClient.executeSync(httpGet);
			return response.getEntity().getContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return null;
	}
	
	public JSONObject executeAndGetJson(String uri,RequestParams params) throws IOException
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader( execute(uri,params) ));
			StringBuilder sb = new StringBuilder("");
			String rd;
			while( (rd = br.readLine()) != null)
			{
				sb.append(rd);
			}
			return new JSONObject(sb.toString());
		}  catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject executeAndPutJson(String uri,RequestParams params) throws IOException
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader( executeInPost(uri,params) ));
			StringBuilder sb = new StringBuilder("");
			String rd;
			while( (rd = br.readLine()) != null)
			{
				sb.append(rd);
			}
			return new JSONObject(sb.toString());
		}  catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeExpiredConnection()
	{
		httpClient.connectionClosed();
	}
	
	public void saveCookie()
	{
		CookieStore cookieStore = httpClient.getCustomCookieStore();
		List<Cookie> cookieList = cookieStore.getCookies();
		String app_name			= appContext.getResources().getString(R.string.app_name);
		CookieDAO	 cookieDAO  = new CookieDAO(new DataBaseHelper(appContext,app_name).getWritableDatabase());
		cookieDAO.saveCookie(cookieList);
		cookieDAO.dbClose();
	}
	
	public void clearCookie()
	{
		String app_name			= appContext.getResources().getString(R.string.app_name);
		CookieDAO	 cookieDAO  = new CookieDAO(new DataBaseHelper(appContext,app_name).getWritableDatabase());
		cookieDAO.clearCookie();
		cookieDAO.dbClose();
		httpClient.setCustomeCookieStore(null);
	}

}
