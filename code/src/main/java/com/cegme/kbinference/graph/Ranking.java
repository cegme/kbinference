
package com.cegme.kbinference.graph;

import com.cegme.kbinference.graph.GraphService;
import com.cegme.kbinference.graph.Path;
import com.cegme.kbinference.graph.ScoreVector;

import com.tinkerpop.blueprints.TransactionalGraph;

import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Ranking {


  public static ArrayList<Path> RankPaths(TransactionalGraph g, ArrayList<Path> paths) throws IOException{

    ArrayList<ScoreVector> scoreVectors = new ArrayList<ScoreVector>(paths.size());
    for (int i = 0; i < paths.size(); ++i) {
      scoreVectors.add(new ScoreVector());
    }
    

    Properties props = new Properties();
    props.load(Ranking.class.getClass().getResourceAsStream("/test.properties"));
    // Get Global stats from serialized maps
    Map<String,Integer> globalEdgeMap = GraphService.deserializeCompressedMap(props.getProperty("edgemap.file"));
    Map<String,Integer> globalNodeMap = GraphService.deserializeCompressedMap(props.getProperty("vertexmap.file"));


    // Get the local counts
    Map<String,Integer> localEdgeMap = new TreeMap<String,Integer>();
    Map<String,Integer> localNodeMap = new TreeMap<String,Integer>();
    GetLocalStats(localEdgeMap, localNodeMap, paths);


    // Count within the result paths
    for (int i = 0; i < paths.size(); ++i) {
      double gidf = 1.0;
      double ridf = 1.0;
      double pcl = 1.0;
      double pl = 1.0;

      int wordcount = 0;
      
      for (int j = 0; j < paths.get(i).size(); ++j) {
        String term = paths.get(i).get(j).term.trim();

        if (paths.get(i).get(j).edge) {
          log.error("------> " + term + " " + i + " " + j + paths.get(i).toString());
          gidf += Math.ceil(1 / Math.log(globalEdgeMap.get(term)));
          ridf += Math.ceil(1 / Math.log(localEdgeMap.get(term)));
        }
        else {
          gidf += Math.ceil(1 / Math.log(globalNodeMap.get(term)));
          ridf += Math.ceil(1 / Math.log(localNodeMap.get(term)));
        }

       int localWordcount = term.split("[\\s']+").length;
        wordcount += localWordcount;
        pcl += Ranking.capitalLetters(term)/localWordcount;
      }
      scoreVectors.get(i).pathLength = paths.get(i).size();

      scoreVectors.get(i).globalIDF = gidf / scoreVectors.get(i).pathLength;
      scoreVectors.get(i).resultIDF = ridf / scoreVectors.get(i).pathLength;
      scoreVectors.get(i).pathCapitalLetters =  1.0*pcl / wordcount; 
    }

    for (int i = 0; i < paths.size(); ++i) {
      paths.get(i).setConf(scoreVectors.get(i).score());
    }
    
    return paths;
  }
 
  public static void GetLocalStats(Map<String,Integer> localEdgeMap, Map<String,Integer> localNodeMap, ArrayList<Path> paths) {
    // Get local stats 
    for (int i = 0; i < paths.size(); ++i) {
      for (int j = 0; j < paths.get(i).size(); ++j) {
        String term = paths.get(i).get(j).term.trim();
        if (paths.get(i).get(j).edge) {
          if(localEdgeMap.containsKey(term)) {
            localEdgeMap.put(term, localEdgeMap.get(term)+1);
          }
          else {
            localEdgeMap.put(term, 1);
          }
        }
        else {
          if(localNodeMap.containsKey(term)) {
            localNodeMap.put(term, localNodeMap.get(term)+1);
          }
          else {
            localNodeMap.put(term, 1);
          }
        }
      }
    }
  }
 
   /**
    * Count the number of capital letters in the path and return the count.
    * We want to know the number of capical letters because that correlates with the number of entities mentioned.
    */
  public static double capitalLettersRaw (final String path) {
    final String pattern = "([A-Z]\\w+[ \\s:$])";
    Matcher m = Pattern.compile(pattern).matcher(path);
    
    short words = 0;
    while(m.find()) {
      //System.err.println(">2> " + m.group());
      ++words;
    }

    short items = 0;
    for (int i = 0; i < path.length(); ++i) {
      if (path.charAt(i) == ':') {
        ++items;
      }
    }

    return (double) words / items;
  }
 
  public static int capitalLetters (final String path) {
    String [] words = path.split("[\\s',]+");
    
    short wordCount = 0;
    for (int i = 0; i < words.length; ++i) {
      if (words[i].length() > 0 && Character.isUpperCase(words[i].charAt(0))) {
        ++wordCount;
      }
    }
    return wordCount;
  }

  public static void main(String[] args) { }

}
