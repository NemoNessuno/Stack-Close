library(igraph)
library("ggplot2")

graphBuilding <- function(year, monthsC = c(1:12)){
  base <<- '/mnt/hgfs/Stackoverflow/Out/'
  user <- 'UserOut'
  question <- 'QuestionsOut'
  
  user <- read.csv(paste(base, user, year, '.csv', sep=""))
  img_count <<- 0
  
  months_t <- lapply(monthsC, function(month){
    read.csv(paste(base,question,year,'_',getMonth(month),'.csv', sep=""))
    })
  all <- do.call(rbind, months_t)
    
  user_g <- graph.empty()
  user_g <- add.vertices(user_g, length(user$ID))
  V(user_g)$ID  <- user$ID
  V(user_g)$Name <- as.character(user$Display.Name)
  V(user_g)$Reputation <- user$Reputation
  
  built_edges <- buildEdges(all, user_g)
  #edge_matrix <- as.matrix(t(built_edges)[c(1, 2),])
  edge_matrix <- built_edges[,c(1,2)]
  user_g <- add.edges(user_g, edge_matrix)
  
  subgraphs <- decompose.graph(user_g)
  subgraphs <- subgraphs[lapply(subgraphs, function(graph) length(V(graph))) > 5]
  lapply(subgraphs, function(graph) {
    E(graph)$time <- c(1:length(E(graph)))
    printToSVG(year, graph)
    })
  do.call(max(length(V), subgraphs))
}

getMonth <- function (month){
 if (month < 10) paste('0',month,sep="")
 else month
}

printToSVG <- function(year, graph){
  img_count <<- img_count+1
  svg(file=paste(base, year, img_count, '.svg', sep=""),width=21,height=21)
  printGraph(graph)
  dev.off()
}

printGraph <- function(graph){
  plot.igraph(graph, vertex.label=NA, vertex.size=log(V(graph)$Reputation)*(100/length(V(graph))), edge.width=0.5,
              layout=layout.kamada.kawai, vertex.label.color="black",edge.color="black", edge.arrow.size=.2)
  
}

printTimeGraph <- function(graph){
  #generate a cool palette for the graph
  YlOrBr <- c("#FFFFD4", "#FED98E", "#FE9929", "#D95F0E", "#993404")
  YlOrBr.Lab <- colorRampPalette(YlOrBr, space = "Lab")
  #colors for the nodes are chosen from the very beginning
  vcolor <- rev(YlOrBr.Lab(vcount(g)))
  
  #time in the edges goes from 1 to 300. We kick off at time 3
  ti <- 3
  #weights of edges formed up to time ti is 1. Future edges are weighted 0
  E(g)$weight <- ifelse(E(g)$time < ti,1,0)
  #generate first layout using weights.
  layout.old <- layout.kamada.kawai(g,params=list(weights=E(g)$weight))
  
  #total time of the dynamics
  total_time <- max(E(g)$time)
  #This is the time interval for the animation. In this case is taken to be 1/10 
  #of the time (i.e. 10 snapshots) between adding two consecutive nodes 
  dt <- 0.1
  #Output for each frame will be a png with HD size 1600x900 :)
  png(file="example%03d.png", width=1600,height=900)
  nsteps <- max(E(g)$time)
  #Time loop starts
  for(ti in seq(3,total_time,dt)){
    #define weight for edges present up to time ti.
    E(g)$weight <- ifelse(E(g)$time < ti,1,0) 
    #Edges with non-zero weight are in gray. The rest are transparent
    E(g)$color <- ifelse(E(g)$time < ti,"gray",rgb(0,0,0,0))
    #Nodes with at least a non-zero weighted edge are in color. The rest are transparent
    V(g)$color <- ifelse(graph.strength(g)==0,rgb(0,0,0,0),vcolor)
    #given the new weights, we update the layout a little bit
    layout.new <- layout.fruchterman.reingold(g,params=list(niter=10,start=layout.old,weights=E(g)$weight,maxdelta=1))
    #plot the new graph
    plot(g,layout=layout.new,vertex.label="",vertex.size=1+2*log(graph.strength(g)),vertex.frame.color=V(g)$color,edge.width=1.5,asp=9/16,margin=-0.15)
    #use the new layout in the next round
    layout.old <- layout.new 
  }
  dev.off()
}

extractUsers <- function(string){
  result <- strsplit(substr(string, 2, nchar(string)-1), ";")
  result
}

buildEdges <- function(frame, graph){
  counter <<- 0
  
  result <- do.call(rbind, do.call(rbind,mapply(function(owner, cVs) {
    rows <<- sapply(extractUsers(as.character(cVs)), function(voters) {
      lapply(voters, function(voter){
        counter<<-counter+1
        c(V(graph)[V(graph)$ID == voter],
          V(graph)[V(graph)$ID == owner], 
          counter)
      })
    })
  }, as.character(frame$Owner), frame$Closed.Voters)
  ))
  result
}