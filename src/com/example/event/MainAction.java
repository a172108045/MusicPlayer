package com.example.event;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.example.Global.GlobalVariable;
import com.example.Global.PlayState;
import com.example.controller.*;
import com.example.controller.Page.SearchPage;
import com.example.entity.Song;
import com.example.gui.GUI;
import com.example.gui.MusicUtils;
import com.example.service.*;
import com.example.util.SongUtil;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.util.Callback;

import static com.example.Global.GlobalVariable.*;

public class MainAction {
	
	//-----------------------------------------Top------------------------
	public boolean back() {
 		PageQueue pq = gui.getPageManager();
 		IntegerProperty i = gui.getIndex(); IntegerProperty s = gui.getSize();
 		int ii = i.get();
 		if(ii < 1)
 			return false;
		Page p;int ss = s.get();
		if(ss < 2) 
			return false;
		p = pq.backward();
		show(p);
		return true;
	}
	
	public void fore() {
 		PageQueue pq = gui.getPageManager();
 		IntegerProperty i = gui.getIndex(); IntegerProperty s = gui.getSize();
		int ii = i.get();int ss = s.get();
 		if(ii >= ss - 1)
 			return;
		Page p = pq.forward();
		show(p);
	}
	
	public void search(Button b, TextField tf, ActionEvent e) {//FIXME
		String key = tf.getText(); 
		ArrayList<MusicUtils> sl = searchsong(key);
		//FIXME
		Page p = giveSearch(key);
		((Page.SearchPage)p).setKey(key);
		pq.add(p);
		show(p);
	}
	
	//-----------------------------------------Bottom---------------------
 	public void last() throws InterruptedException {
		play();
		Thread.sleep(100);
		ps.setState_PREMUSIC();
		Thread.sleep(100);
		play();		
	}
	
	public void play() {
		int s = PlayState.getPlayState().getCurrent_state();
		ps.setState_PLAYMUSIC();
		progressFeedBack(0);
//		if(s == PLAYMUSIC) {
//			ps.setState_PLAYMUSIC();
//		}
//		else if(s == PAUSEMUSIC) {
//			ps.setState_PAUSEMUSIC();
//		}
//		System.out.println(SongMenuOperate.getSongsByMenuName(currentMenu).size());
		refresh(s);
	}
	
	public void pause() {
		int s = PlayState.getPlayState().getCurrent_state();
		if(s == GlobalVariable.PAUSINGMUSIC) {
			System.out.println("播放");
			ps.setState_PLAYMUSIC();
		}
		else if(s == GlobalVariable.PLAYINFOMUSIC) {
			System.out.println("暂停");
			ps.setState_PAUSEMUSIC();
		}
//		System.out.println(SongMenuOperate.getSongsByMenuName(currentMenu).size());
		refresh(s);
	}
	
	public void next() throws InterruptedException {
		play();
		Thread.sleep(100);
		ps.setState_NEXTMUSIC();
		Thread.sleep(100);
		play();
	}
	
	public void modiProgress(double progress) {
		System.out.println("用户点击了:"+progress);
		ps.setProgress(progress);
	}
	
	public void modiVolume(double volume) {
		int v = Math.round((float)volume);
		ps.setCurrent_volume(v);
	}
	
	public void modeSwitch(Button b) {
		String mode = b.getStyleClass().get(0);
		b.getStyleClass().remove(0);
		switch (mode) {
		case "buttonRepeatInOne":
			b.getStyleClass().add("buttonRepeat");	
			ps.setPlAYMODE_LISTLOOP();
			break;
		case "buttonRepeat":
			b.getStyleClass().add("buttonOrderPlay");
			ps.setPlAYMODE_SEQUENCEPLAY();
			break;
		case "buttonOrderPlay":
			b.getStyleClass().add("buttonRandomPlay");
			ps.setPlAYMODE_RAMDOMPLAY();
			break;
		case "buttonRandomPlay":
			b.getStyleClass().add("buttonRepeatInOne");
			ps.setPlAYMODE_SINGLELOOP();
			break;
		default:	break;
		}
	}
	
	public static void progressFeedBack(double progress) {
		ct.setText(progressCal(tt.getText(), progress));
		pl.setValue(progress * 100);
	}
	
	private static String progressCal(String timeLength, double progress) {
//		System.out.println("currentdouble:" + progress);
		int sum = 0, mc = 0, sc =0;
		String m, s;
		String[] t = timeLength.split(":");
		m = t[0];s = t[1];
		mc = Integer.valueOf(m);sc = Integer.valueOf(s);
		sum = mc * 60 + sc;
		sum *= progress;
		int rm = sum / 60, rs = sum % 60;
		String r = String.format("%02d:%02d", rm, rs);
//		System.out.println("currenttime:" + r);
		return r;
	}
	
	//-----------------------------------------Left-----------------------
	public void local() {
		if(pq.getSize() > 0 && pq.getPage() instanceof Page.LocalPage ) 
			return;
		Page p = giveLocal();
		pq.add(p);
		show(p);
	}
	
	public void musiclist(String key) {
		if(GlobalVariable.currentMenu == key) 
			return;
		GlobalVariable.currentMenu = key;
//		SongMenu sm = SongMenu.findSongmenuByName(key);
//		String name = sm.getName();FIXME
//		Date d = sm.getCreateDate();
//		
		String date = df.format(new Date());//FIXME
		Page p = giveMusicList(key, date);
		((Page.MusicListPage)p).setKey(key);
		pq.add(p);
		((Page.MusicListPage)p).getController().getLabel_ListName().setText(key);
		show(p);
	}
	
	public void addMusicList(Button b, ListView<Button> l) {
		String t = b.getStyleClass().get(0);
		b.getStyleClass().remove(0);
		if(t.equals("AddList")) {
			vb.getChildren().add(2, hb);
			b.getStyleClass().add("CloseList");
			//tf.requestFocus();
		}
		if(t.equals("CloseList")) {
			vb.getChildren().removeAll(hb);
			b.getStyleClass().add("AddList");
			tf.clear();
		}
	}
	
	public void addLocalMusic() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("打开音乐文件");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP3", "*.mp3"),
				new FileChooser.ExtensionFilter("flac", "*.flac*"),
				new FileChooser.ExtensionFilter("所有文件", "*.*"));
		List<File> selectedFile = fileChooser.showOpenMultipleDialog(GUI.staticStage);
		List<MusicUtils> ml = new ArrayList<>();
		if(selectedFile != null)
			for(File file : selectedFile) {
				Song s = SongOperate.addSong(file.getAbsolutePath(),GlobalVariable.currentMenu);
				MusicUtils mu = SongUtil.songToMucic(s);
				ml.add(mu);
			}
		GlobalVariable.currentCtrl.getTableView_musicTable().getItems().addAll(FXCollections.observableArrayList(ml));
		for(MusicUtils m : ml)
			System.out.println(m.getMusicTitle());
	}
	
	public void addLocalDirectory() {
		DirectoryChooser fileChooser = new DirectoryChooser();
		fileChooser.setTitle("打开音乐文件夹");
		File selectedFile = fileChooser.showDialog(GUI.staticStage);
		if(selectedFile != null) {
			ArrayList<File> fl = new ArrayList<>();
			loopDirectory(selectedFile, fl);
			for(File file : fl)
			SongOperate.addSong(file.getAbsolutePath(),"我的最爱");
		}
	}
	
	private void loopDirectory(File file, ArrayList<File> fl) {
		for(File f : file.listFiles()) {
			if(f.isDirectory()) {
				loopDirectory(f, fl);
			}
			if(f.isFile()) {
				String s = f.getName();
				if(s.matches(".*\\.(?i)mp3$"))
					fl.add(f);
				if(s.matches(".*\\.(?i)flac$"))
					fl.add(f);
			}
		}
	}

	public void createMusicList(Button nb) {
		nb.getStyleClass().remove(0);
		nb.getStyleClass().add("listButton");
		ml.getItems().add(nb);
		nb.setOnAction(nbe -> {
			musiclist(nb.getText());
		});
		nb.setOnMouseClicked(tca);
	}

	private class Extension implements EventHandler<ActionEvent>{//FIXME
		@Override
		public void handle(ActionEvent event) {
			if(tf.getText().length() > 0) {
				try {
					String key = tf.getText();
					SongMenuOperate.addSongMenu(key/*, new Date()*/);
					Button nb = new Button(key);
					createMusicList(nb);
					addl.fire();
				} catch (RuntimeException e2) {
			        Alert _alert = new Alert(Alert.AlertType.ERROR,e2.getMessage(),new ButtonType("返回", ButtonBar.ButtonData.YES));
			        _alert.show();
				}
				
			}
		}
	}
	
	//-----------------------------------------Play------------------------
	public void reverse() {
		if(!back())
			show(null);
	}
	
	//-----------------------------------------List------------------------
		public void playAll() {
			tca.getCb().setMl(SongMenuOperate.getSongsByMenuName(currentMenu));
			ContextBox.play_all.fire();
		}
		
		public void playAllNext() {
			tca.getCb().setMl(SongMenuOperate.getSongsByMenuName(currentMenu));
			ContextBox.play_all_next.fire();
		}
	//-----------------------------------------Item------------------------
	private Page giveLocal() {
		LocalMusicPageController lmC = null;
		StackPane localmusic = null;
		try {
			FXMLLoader lm = new FXMLLoader(GUI.class.getResource("LocalMusicPage.fxml"),
					ResourceBundle.getBundle("ini"));
			localmusic = (StackPane) lm.load();
			lmC = lm.getController();
			List<MusicUtils> list = new ArrayList<MusicUtils>();
			//FIXME 读入
			lmC.initData(this,list);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Page.newPage(Controller.LOCAL, localmusic, lmC);
    }
    
	private Page giveMusicList(String name, String date) {
		MusicListPageController mlC = null;
		StackPane musiclist = null;
		List<MusicUtils> list = SongMenuOperate.getSongsByMenuName(name);
		try {
			FXMLLoader ml = new FXMLLoader(GUI.class.getResource("MusicListPage.fxml"),
					ResourceBundle.getBundle("ini"));
			musiclist = (StackPane) ml.load();
			mlC = ml.getController();
			mlC.initData(this, name, date, list);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Page.newPage(Controller.MUSICLIST, musiclist, mlC);
    }
 
	private Page giveSearch(String name) {
		StackPane searchpage = null;
		SearchPageController spC = null;
		try {
			FXMLLoader sp = new FXMLLoader(GUI.class.getResource("SearchPage.fxml"), ResourceBundle.getBundle("ini"));
			searchpage = (StackPane) sp.load();
			spC = sp.getController();
			List<MusicUtils> list = new ArrayList<MusicUtils>();
			//FIXME 搜索
			spC.initData(this, name, list);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Page.newPage(Controller.SEARCH, searchpage, spC);
    }
    
	public static boolean presskey(TextField t, KeyEvent e) {
		if(e.getCode() == KeyCode.ENTER) {
			t.deleteNextChar();
			if (t.getText().length() > 0) {
				return true;
			}
		}
		if(e.getCode() == KeyCode.TAB) {
			t.deleteNextChar();
		}
		return false;
	}
	
	public static void show(Page p) {
		if(p ==null) {
			gui.getPermanent().setCenter(null);
			gui.getPermanent().leftProperty().set(gui.getLeftlist());
		}
		else if(p instanceof Page.PlayPage) {
			gui.getPermanent().setCenter(gui.getPlaypage());
			gui.getPermanent().leftProperty().set(null);
			return;
		}
		else if(p instanceof Page.SettingPage) {
			gui.getPermanent().leftProperty().set(null);
			return;
		}
		else {
			gui.getPermanent().setCenter(p.getPage());
			gui.getPermanent().leftProperty().set(gui.getLeftlist());
		}
		refresh(p);
	}
	
	private static void refresh(Page p) {
		//back and fore
		int ii = i.get();
		int ss = s.get();
		if (ii > 0)
			b.setDisable(false);
		else
			b.setDisable(true);
		if (ii < ss - 1)
			f.setDisable(false);
		else
			f.setDisable(true);
		
		if(p == null) return;
		GlobalVariable.PageType = p.getType();
		//adds and addd
		if(GlobalVariable.PageType == Controller.LOCAL ||
				GlobalVariable.PageType == Controller.MUSICLIST) {
			adds.setDisable(false);addd.setDisable(false);
			GlobalVariable.currentCtrl = p.getCtrl();
		}else {
			adds.setDisable(true);addd.setDisable(true);
			GlobalVariable.currentCtrl = null;
		}
		if(p instanceof SearchPage) {
			String key =((SearchPage)p).getKey();
			if(GlobalVariable.currentSearch == key)
				return;
			GlobalVariable.currentSearch = key;
		}
	}
		
	private static void refresh(int s) {
		//pb and lb and nb
		if(s == PLAYMUSIC){
			pb.getStyleClass().remove(0);
			pb.getStyleClass().add("buttonPause");
		}
		if(s == PAUSEMUSIC){
			pb.getStyleClass().remove(0);
			pb.getStyleClass().add("buttonPlay");
		}
		List<MusicUtils> l = ps.getCurrent_songMenu();
		if(l == null || l.size() == 0) 
			buttonDis(true, pb, lb, nb);
		else 
			buttonDis(false, pb, lb, nb);
		//tt
		String n = ps.getCurrent_song().getMusicTimeLength();
		tt.setText(n);
	}

	public static ArrayList<MusicUtils> searchsong(String key){//FIXME
		ArrayList<MusicUtils> sl = new ArrayList<>();
		
		return sl;
	}
	
	public MainAction(GUI gui) {
		MainAction.gui = gui;
		tca = new TagClickAction(this, gui.getLlC().getListView_musicList());
		pq = gui.getPageManager();
		ps = PlayState.getPlayState();
		ps.setPlAYMODE_SEQUENCEPLAY();
		//--------main---------
		i = new SimpleIntegerProperty();
		s = new SimpleIntegerProperty();
		i.bind(gui.getIndex());
		s.bind(gui.getSize());
		b = gui.getTabC().getButton_back();
		f = gui.getTabC().getButton_forward();
		pb = gui.getTabC().getButton_pause();
		lb = gui.getTabC().getButton_last();
		nb = gui.getTabC().getButton_next();
		pl = gui.getTabC().getSlider_songProgress();
		ct = gui.getTabC().getLabel_currentTime();
		tt = gui.getTabC().getLabel_totalTime();
		cp = new SimpleDoubleProperty(0);
		cp.unbind();
		cp.bind(PlayOperate.cur_p);
		//--------left---------
		adds = gui.getLlC().getButton_addLocalMusic();
		addd = gui.getLlC().getButton_addLocalDirectory();
		addl = gui.getLlC().getButton_addMusicList();
		vb = gui.getLlC().getVBox_leftMainField();	
		ml = gui.getLlC().getListView_musicList();
		//iv = gui.getLlC().getImageView_albumCover();
		
		buttonDis(true, adds, addd, pb, lb, nb);
		
		tf.setPromptText("请输入歌单名称");
		hb.getChildren().addAll(tf, btn);
		btn.getStyleClass().remove(0); btn.getStyleClass().add("AddList");
		tf.getStyleClass().add("ListField");
		tf.setPrefHeight(80);tf.setPrefWidth(250);
		btn.setOnAction(new Extension());
		tf.setOnKeyPressed(new EnterAction(tf, btn));
		cp.addListener((o, ov, nv) ->{
			progressFeedBack((double)nv);
//			pl.setValue((double)nv);
		});
	}
	public static GUI gui;
	private static MainAction ma;
	public static PageQueue pq;
	public static PlayState ps;
	public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\u521b\u5efa");
	public TagClickAction tca;	
	private static Task<Double> task;
	
	static TextField tf = new TextField();//扩展输入栏
	static Button btn = new Button();//扩展添加按钮
	static HBox hb = new HBox();//扩展横箱
	
	static Button addl;//指定left中的添加歌单
	static Button adds, addd;//指定left中的添加歌曲和添加目录
	//static ImageView iv;//指定left中的专面
	static VBox vb;//指定left中的竖箱
	static ListView<Button> ml;//指定left中的列表
	static IntegerProperty i, s;//指定gui中的目录和大小
	static DoubleProperty cp;
	static Button b, f;//指定topandbottom中的后退和前进
	static Button pb,lb,nb;//指定topandbottom中的播放,上一首和下一首
	static Slider pl;//指定topandbottom中的进度条
	static Label ct, tt;//指定topandbottom中的当前时间和总时间

 	public static void buttonDis(boolean r, Button... bl) {
		for(Button b : bl) 
			b.setDisable(r);
	}
	
	public static class IndexFactory<S> implements Callback<CellDataFeatures<S, Integer>, ObservableValue<Integer>> {
		private TableView<S> tv;

		public TableView<S> getTv() {
			return tv;
		}

		public IndexFactory(TableView<S> tv) {
			super();
			this.tv = tv;
		}

		@Override
		public ObservableValue<Integer> call(CellDataFeatures<S, Integer> param) {
			S value = param.getValue();
			Integer i = new Integer(tv.getItems().indexOf(value) + 1);
			ObservableValue<Integer> index = new ReadOnlyObjectWrapper<Integer>(i);
			return index;
		}
	}
		
	public static class likeCheckBox implements Callback<Integer,ObservableValue<Boolean>>{
		
		private TableView<MusicUtils> tv;
		
		public TableView<MusicUtils> getTv() {
			return tv;
		}

		public likeCheckBox(TableView<MusicUtils> tv) {
			this.tv = tv;
		}
		
		@Override
		public ObservableValue<Boolean> call(Integer param) {
			MusicUtils m = tv.getItems().get(param);
			ObservableValue<Boolean> ob = new SimpleBooleanProperty(m.isLike());
			return ob;
		}
	}
	
	public static class TableCleaner<S> implements ChangeListener<Boolean>{
		private TableView<S> tv;
		public TableView<S> getTv() {
			return tv;
		}

		public TableCleaner(TableView<S> tv) {
			super();
			this.tv = tv;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if(newValue == false)
				tv.getSelectionModel().clearSelection();
		}
	}
	
	public GUI getGui() {
		//初始化播放服務d
		PlayOperate.getPlayOperate();
		return gui;
	}
	
	public static MainAction getMa() {
		if(ma == null)
			ma = new MainAction(GUI.getGui());
		return ma;
	}
}
