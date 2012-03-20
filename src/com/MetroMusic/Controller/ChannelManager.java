package com.MetroMusic.Controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.MetroMusic.Data.Channel;

public class ChannelManager implements Serializable{
	
	private List<Channel> channelList = new ArrayList<Channel>();
	private Channel		  currentChannel;
	
	public ChannelManager()
	{
		
	}
	
	/***
	 * ʹ��JSON�����ʼ��Ƶ�����������Ƶ���б�
	 * @param jsonArray JSON����
	 */
	public ChannelManager(JSONArray jsonArray)
	{
		Channel favorite = new Channel();
		favorite.setId(-3);
		favorite.setName("�����׺�");
		favorite.setSeqId(-1);
		channelList.add(favorite);
		for(int i = 0; i < jsonArray.length(); i++ )
		{
			JSONObject object = jsonArray.optJSONObject(i);
			channelList.add(new Channel(object));
		}
	}
	
	public List<Channel> getChannelList() {
		return channelList;
	}

	public void setChannelList(JSONArray jsonArray)
	{
		for(int i = 0; i < jsonArray.length(); i++ )
		{
			try {
				JSONObject object = jsonArray.getJSONObject(i);
				channelList.add(new Channel(object));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void setChannelList(List<Channel> channelList)
	{
		this.channelList = channelList;
	}
	
	public Channel changeChannelById(int channelId)
	{
		return this.currentChannel = getChannelById(channelId);
	}
	
	public Channel changeChannelByName(String channelName)
	{
		return this.currentChannel = getChannelByName(channelName);
	}
	
	public void initCurrentChannel()
	{
		this.currentChannel = this.currentChannel == null ? this.getChannelByName("�¸�") : this.currentChannel;
	}
	
	public Channel getCurrentChannel()
	{
		return this.currentChannel;
	}
	
	public Channel getChannelById(int channelId)
	{
		for(Channel channel : channelList )
		{
			if( channel.getId() == channelId )
			{
				return channel;
			}
		}
		return null;
	}
	
	public Channel getChannelByName(String channelName)
	{
		for(Channel channel : channelList )
		{
			if( channel.getName().equals(channelName) )
			{
				return channel;
			}
		}
		return null;
	}
	
}
