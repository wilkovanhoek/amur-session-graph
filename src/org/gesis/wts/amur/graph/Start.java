package org.gesis.wts.amur.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {
	final static Logger log = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		GraphAnalyser graphAnalyser = new GraphAnalyser();
		GraphClusterer graphClusterer = new GraphClusterer();

		for (String curData : Config.getInstance().getParse()) {
			System.out.println("processing " + curData + " data in folder " + Config.getInstance().getDataFolders().get(curData) + Config.LINEDELIMITER);
			log.info("processing " + curData + " data in folder " + Config.getInstance().getDataFolders().get(curData) + Config.LINEDELIMITER);

			if (Config.getInstance().getBooleanParameter("clustering")) {
				// TODO: move grouping number to config file
				graphClusterer.startClustering(Config.getInstance().getDataFolders().get(curData), (byte) 2);
			} else {
				graphAnalyser.startAnalysis(Config.getInstance().getDataFolders().get(curData));
			}

			log.info("processing is done!" + Config.LINEDELIMITER);
			System.out.println("processing is done! - You can find more detailed information in log/session-graph.log" + Config.LINEDELIMITER);
		}
	}
}