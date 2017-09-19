package org.gesis.wts.amur.graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Config {
	final static Logger log = LoggerFactory.getLogger(Config.class);
	private static Config instance;
	// path to settings is hardcoded, if changeability is needed, an additional
	// getInstance or changeSettings method could be implemented.
	private static final String CONFIGFOLDER = "config/";
	private static final String DEFAULTCONFIG = "settings.conf";
	private static final String CONFIGPATH = CONFIGFOLDER + DEFAULTCONFIG;
	private List<String> parse;
	private Map<String, String> dataFolders;
	private List<String> classify;
	private Map<String, String> classificationFolders;
	private Map<String, String> parameters;
	private Map<String, String> settings;
	public static final String LINEDELIMITER = "\n\n----------\n";

	public enum Mode {
		DEFAULT, CLUSTER, CLASSIFY
	}

	private Config() {
	}

	public static Config getInstance() {
		return getInstance(DEFAULTCONFIG);
	}

	public static Config getInstance(String configPath) {
		// Add folder path or replace if no config specified
		configPath = configPath == "" ? CONFIGPATH : CONFIGFOLDER + configPath;
		if (Config.instance == null) {
			Gson gson = new Gson();
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(configPath));
				Config.instance = gson.fromJson(br, Config.class);
			} catch (FileNotFoundException e) {
				log.error("could find config under: " + configPath);
				log.info("using default path: " + CONFIGPATH);
				try {
					br = new BufferedReader(new FileReader(CONFIGPATH));
					Config.instance = gson.fromJson(br, Config.class);
				} catch (FileNotFoundException exception) {
					log.error("could find config under: " + CONFIGPATH);
					exception.printStackTrace();
				}
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

	public List<String> getClassify() {
		return classify;
	}

	public void setClassify(List<String> classify) {
		this.classify = classify;
	}

	public Map<String, String> getClassificationFolders() {
		return classificationFolders;
	}

	public void setClassificationFolders(Map<String, String> classificationFolders) {
		this.classificationFolders = classificationFolders;
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

	public Mode getMode() {
		String modeString = parameters.get("mode");
		switch (modeString) {
		case "cluster":
			return Mode.CLUSTER;
		case "classify":
			return Mode.CLASSIFY;
		default:
			return Mode.DEFAULT;
		}

	}

}
