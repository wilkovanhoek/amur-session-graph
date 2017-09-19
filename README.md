User Session Tree Analysis Tool used in the DFG Project AMUR
==============

This tool can be used to analyse user behaviour based on a tree based represenation of the user's search session in a search system. There are three modes in which the tool can be used:

* default (merge): Multiple user sessions can be merged into a single combined graph. 

* cluster: clusters all graphs into n groups with at least m members. This mode is in experimental status and was never finished.


Create graphs used in TPDL 2017 Paper (Building User Groups based on a Structural Representation of User Search Sessions)
--------------

You can create the graphs used in our paper following the steps described below. You would need Java, Maven and R installed on your system. Also we recommend using IDEs (Eclipse, R-Studio).

 1. run the java project. The main is in org.gesis.wts.amur.graph.Start. Pass 'tpdl-settings.conf' as only argument (alternatively, you can omit an argument but would have to rename the *config/tpdl-settings.conf* to *config/settings.conf*). We recommend to do this in an IDE (e.g. Eclipse). The project will process various folders and create results in: 
 
 * *data/study/all/results*
 combined graph for all graphs and the individual graphs
 
 * *data/study/grouping/non_archetype_graphs*
 combined graph of all graphs that were not grouped and the individual graphs
 
 * *data/study/grouping/groups/[ecobreadth|exhbreadth|exhepth|syssupport]/results*
 combined graph per group and the individual graphs
 
 The creation of the individual graphs is supposed to help to generate the ability to inspect each session individually.
 
 2. copy the combined graphs into the archetypes folder for further processing (executed from the projects root folder):
 
 ```
cp data/study/all/results/study_* data/study/grouping/archetypes/
cp data/study/grouping/groups/ecobreadth/results/study_* data/study/grouping/archetypes/
cp data/study/grouping/groups/exhbreadth/results/study_* data/study/grouping/archetypes/
cp data/study/grouping/groups/exhdepth/results/study_* data/study/grouping/archetypes/
cp data/study/grouping/groups/syssupport/results/study_* data/study/grouping/archetypes/
 ```

 3. run the R script *r-stuff/displayGraphs.R* (No arguments needed). This will create pdf files for all individual graphs in the folder described in (1) (and stores them in the same folders *data/study/grouping/groups/[ecobreadth|exhbreadth|exhepth|syssupport]/results*) and the combined and thresholded graphs in *data/study/grouping/archetypes/*.

If this process does not work for you or you have any additional questions, please don't hesitate to start an issue or contact the corresponding authors of the paper.


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
