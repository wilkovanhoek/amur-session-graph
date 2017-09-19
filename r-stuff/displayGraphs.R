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
       vertex.color="white",
       vertex.size=vertexSize,
       #margin=-0.00,
       # special margin for threshold graph 12 of phds
       #margin = c(2, 0,0.0,0),
       asp= 0.6)
  
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

plotCSVFiles <- function(folder, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex, thresholds, plotBarCharts){
  files <- list.files(path=folder, pattern="*.csv", full.names=T, recursive=FALSE)
  print(files)
  lapply(files, function(curFile) {
    fileName = substr(curFile, 1, nchar(curFile)-4)
    print(curFile)
    el=read.csv(curFile) 
    g=graph.data.frame(el)
    if(is.null(thresholds)){
      plotGraph(g, fileName,  vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex)
      if(plotBarCharts){
        createBarChart(el, fileName, toFile)    
      }
    }else{
      if(!is.null(thresholds)){
        for(i in thresholds){
          #print(el)
          thresholdList <- subset(el, weight >= i) 
          g <- graph.data.frame(thresholdList)
          newFile <- paste(fileName,"_threshold_",i,sep="")
          plotGraph(g, newFile, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex)
          
        }  
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
  print(paste("max:",max))
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
    pdf(paste(fileName,"_dist.pdf",sep=""),width = width, height = height/2)
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

### folders 
subtreeweight_test="../data/subtreeweight_test/results"
study="../data/study/results"
study_all="../data/study/all/results"
study_students="../data/study/results/students"
study_phds="../data/study/results/phds"
examples_graphs="../data/examples/results/graphs"
examples_merged="../data/examples/results/merged"

### grouping related folders
study_non_archetype_graphs="../data/study/grouping/non_archetype_graphs/results"
study_syssupport="../data/study/grouping/groups/syssupport/results"
study_exhbreadth="../data/study/grouping/groups/exhbreadth/results"
study_exhdepth="../data/study/grouping/groups/exhdepth/results"
study_ecobreadth="../data/study/grouping/groups/ecobreadth/results"
study_archetypes="../data/study/grouping/archetypes"

### defining plot variables 
# plotCSVFiles <- function(folder, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex, thresholds, plotBarCharts )

vertexSize = 5
labelCex = 6
weightFactor =  6
threshold = 50
width = 60
height = width/1.5
legendCexOne = 10
legendCexTwo = 20
legendCexThree = 24


# plotCSVFiles <- function(folder, vertexSize, labelCex, weightFactor, threshold, width, height, toFile, showLegend, legendCex, thresholds, plotBarCharts )

### plotting all session graphs, so they can be compared on a visual basis
plotCSVFiles(study_all, vertexSize, labelCex, weightFactor, threshold, width, height, TRUE, TRUE, legendCexOne+6, NULL, FALSE)
plotCSVFiles(study_non_archetype_graphs, vertexSize, labelCex, weightFactor, threshold, width, height, TRUE, TRUE, legendCexOne+6, NULL, FALSE)

### plots for study group systm support
plotCSVFiles(study_syssupport, vertexSize, labelCex, weightFactor, threshold, width, height, TRUE, TRUE, legendCexTwo, NULL, FALSE)

### plots for study goup exhaustive breadth
plotCSVFiles(study_exhbreadth, vertexSize, labelCex, weightFactor, threshold, width, height, TRUE, TRUE, legendCexTwo, NULL, FALSE)

### plots for study group exhaustive depth
plotCSVFiles(study_exhdepth, vertexSize, labelCex, weightFactor, threshold, width, height, TRUE, TRUE, legendCexTwo, NULL, FALSE)

### plots for study group economic breadth
plotCSVFiles(study_ecobreadth, vertexSize, labelCex, weightFactor, threshold, width, height, TRUE, TRUE, legendCexTwo, NULL, FALSE)

### plotting archetypes and thresholds
plotCSVFiles(study_archetypes, 2.3, labelCex, 3, threshold, width, height, TRUE, TRUE, legendCexTwo, NULL, TRUE)
plotCSVFiles(study_archetypes, 2.3, labelCex, 3, threshold, width, height, TRUE, TRUE, legendCexTwo, c(2,3,4), FALSE)