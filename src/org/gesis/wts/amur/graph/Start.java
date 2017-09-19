package org.gesis.wts.amur.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {
	final static Logger log = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		GraphAnalyser graphAnalyser = new GraphAnalyser();
		GraphClusterer graphClusterer = new GraphClusterer();
		GraphClassifier graphClassifier = new GraphClassifier();

		System.out.println("Started session graph project (see log file for details)...\n");
		String configPath = "";
		if (args != null && args.length > 0) {
			configPath = args[0];
		}
		System.out.println(Config.getInstance(configPath).getMode());
		switch (Config.getInstance(configPath).getMode()) {
		case CLASSIFY:
			for (String curData : Config.getInstance().getClassify()) {
				graphClassifier.startClassifying(Config.getInstance().getClassificationFolders().get(curData));
			}
			break;
		default:
			for (String curData : Config.getInstance().getParse()) {
				log.info("processing " + curData + " data in folder "
						+ Config.getInstance().getDataFolders().get(curData) + Config.LINEDELIMITER);

				switch (Config.getInstance().getMode()) {
				case CLUSTER:
					graphClusterer.startClustering(Config.getInstance().getDataFolders().get(curData), (byte) 2);
					break;

				default:
					graphAnalyser.startAnalysis(Config.getInstance().getDataFolders().get(curData), curData);
					break;
				}

			}
			break;
		}

		log.info("processing is done!" + Config.LINEDELIMITER);
		System.out.println("... processing is done!");
	}
}
