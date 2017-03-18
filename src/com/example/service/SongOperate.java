package com.example.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.dao.SongDao;
import com.example.dao.impl.SongDaoImpl;
import com.example.entity.Song;
import com.example.entity.Tag;
import com.example.util.TagInfoUtil;
import com.sun.org.apache.bcel.internal.classfile.Field;

import javafx.scene.media.Media;


/** 
* @date 2017年3月18日 上午1:19:59 
* @version 1.0 
* @Description:   
*/
public class SongOperate {
	static SongDao songDao;
	static{
		songDao=new SongDaoImpl();
	}
	
	public static void addSong(String path,String menuName){
		//此处判断是否为合法路径以及合法音频格式
		
		Song song=new Song();
		song.setPath(path);
		Tag tag = TagInfoUtil.Mp3InfoRead(path);
		song.setTag(tag);
		songDao.addSong(song,menuName);
		
	}
	
	public static void addSongWithFile(String filePath,String menuName){
		File files=new File(filePath);
		List<Song> songList=new ArrayList<Song>();
		if(files.exists()&&files.isDirectory()){
			for(File file:files.listFiles()){
//				Media media=new Media(file.toString());
				String absolutePath = file.getAbsolutePath();
				if(file.isFile()){
					if(absolutePath.endsWith(".mp3")||absolutePath.endsWith(".flac")){
						Song song=new Song();
						song.setPath(absolutePath);
						Tag tag = TagInfoUtil.Mp3InfoRead(absolutePath);
						song.setTag(tag);
						songList.add(song);
					}
				}
			}
		}
		songDao.addSongWithFile(songList, menuName);
	}
	
}
 