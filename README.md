User Session Tree Analysis Tool used in the DFG Project AMUR
==============

This tool can be used to analyse user behaviour based on a tree based represenation of the user's search session in a search system. There are three modes in which the tool can be used:

* default (merge): Multiple user sessions can be merged into a single combined graph. 

* classify: when archetpyes are defined (see [tpdl 2017](https://github.com/wilkovanhoek/amur-session-graph/tree/tpdl2017)), this calculates distances between given graphs and archetypes.

* cluster: clusters all graphs into n groups with at least m members. This mode is in experimental status and was never finished.


IJDL and TPDL 2017
--------------

This tool was developed and used in the context of two publications. For each publication there is a specific branch:

[ijdl](https://github.com/wilkovanhoek/amur-session-graph/tree/ijdl) - Carevic, Zeljko, et al. "Investigating exploratory search activities based on the stratagem level in digital libraries." International Journal on Digital Libraries (2017): 1-21.

[tpdl 2017](https://github.com/wilkovanhoek/amur-session-graph/tree/tpdl2017) - van Hoek, Wilko, and Zeljko Carevic. "Building User Groups Based on a Structural Representation of User Search Sessions." International Conference on Theory and Practice of Digital Libraries. Springer, Cham, 2017.


Classifying Graphs
--------------

In addition to the published results in our TPDL paper, we experimented on assessing how good graphs not belonging to one of our groups fit into one of our archetypes. Therefore, we merge each archetpye and graph individually and assess the number of nodes of the resulting graph. In our assumption, graphs that merge well with an archetype are those, where the resulting graph conains a minimal number of nodes. This means, that most nodes could be merged and the graphs overlap very well. At the same time the number of nodes of both, the archetype and the graph in question should be close to each other. We planned to define a suitable metric for this. Anyhow, this work remains unfinished.

Our analysis can be triggered using the following steps:

 1. run the java project (master branch). In the way it is describe in [tpdl 2017](https://github.com/wilkovanhoek/amur-session-graph/tree/tpdl2017).
 
 2. change the config used as run parameter to 'tpdl-settings_(classify).conf' (in this config only the mode is changed to classify) and run the project again.
 
 The results of the analysis will be stored in *data/study/grouping/results/classifier.csv*. In addition, a pre-processed version is stored under *data/study/grouping/classification.csv*. 


Folder Structure
--------------

* *config*

contains all configuration files for the logging and the run paramters for the tool.

* *data*

contains the data files (user session json files). See more information in the section data structure.

* *log*

stores the log files

* *r-stuff*

R code to create images of the tool's results. Also the thresholding of trees is done with R.

* *src*

java source code


Data Structure
--------------

For each data set, their exists an individual subfolder in the *data* folder. It is assumed, that each data set folder has two subfolders *{data set folder}/graphs* and *{data set folder}/results*. The *graphs* folder should contain all sessions stored in individual json files (see json format below. 

json format
--------------

Each tree is stored in a single json file, the format is as follows:

Each session is a list of nodes (representing an user activity). Each activity can have the following properties:

```json
{
	"type": "nodeName",
	"weight": 1,
	"index":  1,
	"children":[ ... ]
}
```

The children property is an array of child nodes. The represent activity, that was executed from the parent node. In *data/study* some example trees are stored.

During the analysis, the childnodes are seperated into *ChildlessChildren* And *ChildrenWithKids*. These properties can also be set for the input data directly. Outputted files will not contain the original *children* array.


Data output
--------------

After processing, the *results* folder will contain the analysis' output. The output are json files and csv files. CSV files are used for printing the session trees (using the r-scripts).


Logging
--------------

The tool only generates a small amount of command line output. Most output is logged into the *log* folder. Logging can be set to *info* or *debug*.


settins.conf
--------------

To run the tool, you have to create a local version of a settings file. Just create a copy of *config/settings-default.conf* and rename it to *settings.conf*. This creates an running settings file. However, you should define your own configuration. Following settings can be set in the *config/settings.conf*:

### parse

a array of data folder aliases. All data folders listed here will be processed. (example: ["study"])

### dataFolders
 
a list of folder aliases assigned to folder paths. You can list all data sets, only those referenced in *parse* will be considered. (example: {"study":"data/study"})

### parameters

a list of parameters. This List specifies the mode in which the tool is running and what to do. You should define all parameters even if the do not affect your analysis.

  * *clustering*

boolean can be set to either *true* or *false*. If set to *true* *cluser* mode will be used, else *default* (merge) mode will be used.

  * *permutations*

boolean can be set to either *true* or *false*. If set to *true*, it tries to merge all possible permutations in which the graphs can be merged (CAUTION: this is very resource intense. If not needed, you should use *sorted* mergeMode)

  * *sorted*

boolean can be set to either *true* or *false*. Graphs will be sorted before they are merged. This mergeMode was used in all publications and has shown to be the most efficient, although a optimal merge can not be garanteed.

  * *sortMode*

can be set to either *ASC*, *DESC*, or *SHUFFLE*. Defines the order in which the trees are merged whe using *sorted* mergMode. Default is *ASC* and has been used in current publications.

  * *threaded*

boolean can be set to either *true* or *false*. Specifies if the program should run in multiple threads. Will be used for *default* (merging) and *cluster* modes. Especially useful for "permutations* mergeMode.

  * *nodeThreads*

boolean can be set to either *true* or *false*. Defines, whether node merging should be done in multiple threads as well (CAUTION: has not shown to be more efficient and does not run reliably)

  * *threadPoolSize*

integer value. Defines how many threads are allowed in threaded execution.

  * *writeThreadResults*

boolean can be set to either *true* or *false*. Defines if results of each threaded merge should be outputted as well. 

  * *writeIntermediateGraphs*

boolean can be set to either *true* or *false*. When merging mulitple graphs, should all intermediate merges be outputted es well.

  * *writeParsedGraphs*

boolean can be set to either *true* or *false*. Output the input graphs after processing. Helps to identify problems and can be used to create csv files for printing.

### settings

a list for system specific settings.

  * *rHome*

points to the home directory of the r installation. Currently not in use! (example: {"rHome":"/usr/bin/R"})

Funding
--------------

This work was funded by Deutsche Forschungsgemeinschaft (DFG), grant no. MA 3964/5-1; the AMUR project at GESIS
