package org.gesis.wts.amur.graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

public class Config {	
	private static Config instance;
	// path to settings is hardcoded, if changeability is needed, an additional getInstance or changeSettings method could be implemented.
	private static final String CONFIGPATH = "config/settings.conf";
	private List<String> parse;
	private Map<String,String> dataFolders;
	private Map<String,String> parameters;
	private Map<String,String> settings;
	public static final String LINEDELIMITER = "\n\n----------\n";
	
	private Config() {}
	
	public static Config getInstance() {
		if(Config.instance == null){
			Gson gson = new Gson();
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(CONFIGPATH));
				Config.instance = gson.fromJson(br, Config.class);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return Config.instance;
	}
	
	public List<String> getParse() {
		return parse;
	}
	public void setParse(List<String> parse) {
		this.parse = parse;
	}
	public Map<String, String> getDataFolders() {
		return dataFolders;
	}
	public void setDataFolders(Map<String, String> dataFolders) {
		this.dataFolders = dataFolders;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	public Map<String, String> getSettings() {
		return settings;
	}
	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}
	
	public boolean getBooleanSetting(String string) {
		return Boolean.parseBoolean(settings.get(string));
	}
	
	public boolean getBooleanParameter(String string) {
		return Boolean.parseBoolean(parameters.get(string));
	}
	
	public int getIntParameter(String parameter) {		
		return Integer.valueOf(parameters.get(parameter));
	}
	
	public String getStringParameter(String parameter) {		
		return String.valueOf(parameters.get(parameter));
	}
	
//	public static Config getConfig(String fileName){
//		Config newConf = new Config();
//		Gson gson = new Gson();
//		BufferedReader br;
//		try {
//			br = new BufferedReader(new FileReader(fileName));
//			newConf = gson.fromJson(br, Config.class);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		return newConf;
//	}

	

}
