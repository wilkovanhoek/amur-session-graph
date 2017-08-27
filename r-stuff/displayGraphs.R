# set the working directory for relative paths
setwd(dirname(sys.frame(tail(grep('source',sys.calls()),n=1))$ofile))

if(!require(igraph)){
  install.packages('igraph')
  library(igraph)
}


plotGraph <- function(g, fileName, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex){
  
  #maxWeight = max(el$weight)
  #numRows = nrow(el)
  dfs <- dfs(g, root=1, dist=TRUE)
  if(toFile){
    pdf(paste(fileName,".pdf",sep=""),width = width, height = height)
    #png(filename=paste(fileName,".png"),width = width, height = height)  
  }
  par(mar=c(1.5,1.5,1.5,1.5))
  plot(g,
       #layout = layout.lgl(g),
       layout=layout.reingold.tilford(g, root=1, mode="out"),
       edge.width=E(g)$weight*weightFactor, 
       edge.arrow.size=0.0,
       vertex.label="",
       edge.label=ifelse(E(g)$weight> threshold, E(g)$weight, NA),
       edge.label.cex=labelCex,
       edge.label.color="black",
       #edge.color="black",
       
       margin=-0.00,
       # special margin and asp for threshold graph 12 of phds 
       # uncomment the following two lines and comment the line above (margin)
       # margin = c(1.5, 0,0.0,0),
       # asp= 0.6 
       
       vertex.color="white",
       vertex.size=vertexSize
       )
  
  #plot
  if(showLegend){
    #theme(legend.text.align = 1)
    legend("bottomright",cex = legendCex, bty="n",  xjust=1,
           legend = c(paste("number of nodes =", vcount(g)),
                      #paste("mean distance =",round(mean_distance(g), 2)),
                      paste("diameter =", round(diameter(g, directed = FALSE, weights =  NA), 2)),
                      #paste("closeness =", round(max(closeness(g)),6)),
                      paste("root distance =", max(dfs$dist))))
  }
  if(toFile){
    dev.off()
  }
}

plotCSVFiles <- function(folder, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex, thresholds){
  files <- list.files(path=folder, pattern="*.csv", full.names=T, recursive=FALSE)
  lapply(files, function(curFile) {
    fileName = substr(curFile, 1, nchar(curFile)-4)
    print(curFile)
    el=read.csv(curFile) 
    g=graph.data.frame(el)
    if(is.null(thresholds)){
      plotGraph(g, fileName,  vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex)
      createBarChart(el, fileName, toFile)  
    }else{
      for(i in thresholds){
        #print(el)
        thresholdList <- subset(el, weight >= i) 
        g <- graph.data.frame(thresholdList)
        newFile <- paste(fileName,"_threshold_",i,sep="")
        plotGraph(g, newFile, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex)
        
      }
    }
  })
}


createBarChart <- function(el, fileName, toFile) {
  width = 10
  height = 10
  n <- length(el)
  # get the second highest weight to create an upper bound for thresholding
  max <- max(el$weight)
  #curFile(paste("max:",max))
  min=1
  threshChart <- matrix( nrow = max-min+1, ncol = 2)
  colnames(threshChart) <- c("threshold","n")
  for(i in min:max) {
    threshold <- subset(el, weight >= i) 
    pos <- i-min+1
    threshChart[pos,1] <- i
    threshChart[pos,2]<- nrow(threshold)
  }
  if(toFile){
    pdf(paste(fileName,"_dist.pdf",sep=""),width = width, height = height/1.5)
  }
  par(mar=c(4.8, 5.5,1.5,0.0))
  barplot(threshChart[,2], 
          names.arg=threshChart[,1], 
          ylab = "number of nodes", 
          xlab = "weight threshold",
          cex.lab=2.8,cex.axis=2.6,cex.names = 2.6)
           
  
  #axis(1,cex.axis=2)
  #axis(2,cex.axis=2)
  if(toFile){
    dev.off()
  }
}



weight="../data/subtreeweight_test/results"
examples_graphs="../data/examples/results"
#examples_merged="../data/examples/results/merged"

#study="../data/study/results"
study_all="../data/study/all/results"
study_students="../data/study/students/results"
study_phds="../data/study/postdocs/results"

# currently only using square layouts
size = 60
# plotCSVFiles <- function(folder, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend)


legendCexOne = 12
legendCexTwo = 24
legendCexThree = 28

# plot examples
plotCSVFiles(examples_graphs, 10, 6, 100, 50, size, size, TRUE, FALSE, legendCexTwo, NULL)
#plotCSVFiles(examples_merged, 10, 40, 70, 0, size, size, TRUE, FALSE, legendCexThree, NULL)

#plotCSVFiles(study, 2.3, 6, 1.5, 50, size, size, TRUE, TRUE, legendCexOne, NULL)
# plots for complete dataset
plotCSVFiles(study_all, 2.3, 6, 1.5, 50, size, size, TRUE, TRUE, legendCexOne, NULL)
plotCSVFiles(study_all, 10.5, 6, 5, 50, size, size, TRUE, TRUE, legendCexThree, c(6,11,17))

# plots for students
plotCSVFiles(study_students, 2.3, 6, 3, 50, size, size, TRUE, TRUE, legendCexTwo, NULL)
plotCSVFiles(study_students, 10.5, 6, 8, 50, size, size, TRUE, TRUE, legendCexThree, c(5,9,12))

# plots for postdocs
plotCSVFiles(study_phds, 2.3, 6, 3, 50, size, size, TRUE, TRUE, legendCexTwo,NULL)
plotCSVFiles(study_phds, 10.5, 6, 8, 50, size, size, TRUE, TRUE, legendCexThree, c(5,9,12))
# In the threshold graph (12) for phds, the graph is drawn into the legend, 
# as a fix, there is a seperate command and in the plotGraph function an commented margin line
#plotCSVFiles(study_phds, 10.5, 6, 8, 50, size, size, TRUE, TRUE, legendCexThree, c(12))