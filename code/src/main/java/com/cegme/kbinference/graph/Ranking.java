
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
import java.util.HashMap;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Ranking {


  public static ArrayList<Path> RankPaths(TransactionalGraph g, ArrayList<Path> paths) throws IOException {

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
    //log.info("Path size: " + paths.size());
    Pair<Map<String,Integer>, Map<String,Integer>> t = GetLocalStats(paths);
    Map<String,Integer> localEdgeMap = t.left;
    Map<String,Integer> localNodeMap = t.right;

    //log.info("Local Edge Map size: " + localEdgeMap.size());
    //log.info("Local Node Map size: " + localNodeMap.size());


    double global_emax = Collections.max(globalEdgeMap.values());
    double global_emin = Collections.min(globalEdgeMap.values());
    double global_nmax = Collections.max(globalNodeMap.values());
    double global_nmin = Collections.min(globalNodeMap.values());

    double local_emax = Collections.max(localEdgeMap.values());
    double local_emin = Collections.min(localEdgeMap.values());
    double local_nmax = Collections.max(localNodeMap.values());
    double local_nmin = Collections.min(localNodeMap.values());

    //log.info("Maxes: " + global_emax + " " + global_emax + " " + local_emax + " " + local_nmax);
    //log.info("Mins: " + global_emin + " " + global_nmin + " " + local_emin + " " + local_nmin);

    double max_ge = 0.0, min_ge = Integer.MAX_VALUE;
    double max_re = 0.0, min_re = Integer.MAX_VALUE;
    double max_gn = 0.0, min_gn = Integer.MAX_VALUE;
    double max_rn = 0.0, min_rn = Integer.MAX_VALUE;
    double max_pcl = 0.0, min_pcl = Integer.MAX_VALUE;
    double max_pl = 0.0, min_pl = Integer.MAX_VALUE;
      
    int min_wc = Integer.MAX_VALUE;
    int max_wc = 0;
      

    // Count within the result paths
    for (int i = 0; i < paths.size(); ++i) {
      double geidf = 0.0;
      double reidf = 0.0;
      double gnidf = 0.0;
      double rnidf = 0.0;
      double pcl = 1.0;
      double pl = 1.0;

      int wordcount = 0;
      double edge_count = 0;
      double node_count = 0;

      for (int j = 0; j < paths.get(i).size(); ++j) {
        String term = paths.get(i).get(j).term.trim();

        try {
          if (paths.get(i).get(j).edge) {
            max_ge = Math.max(max_ge, globalEdgeMap.get(term));
            max_re = Math.max(max_re, localEdgeMap.get(term));
            min_ge = Math.min(min_ge, globalEdgeMap.get(term));
            min_re = Math.min(min_re, localEdgeMap.get(term));
            
            geidf += (globalEdgeMap.get(term) - global_emin) / (global_emax - global_emin + Double.MIN_VALUE);
            reidf += (localEdgeMap.get(term) - local_emin) / (local_emax - local_emin + Double.MIN_VALUE);
            //log.info(term + " geidf --> " + (globalEdgeMap.get(term) - global_emin) / (global_emax - global_emin));
            //log.info(term + " reidf --> " + (localEdgeMap.get(term) - local_emin) / (local_emax - local_emin));
            ++edge_count;
          }
          else {
            max_gn = Math.max(max_gn, globalNodeMap.get(term));
            max_rn = Math.max(max_rn, localNodeMap.get(term));
            min_gn = Math.min(min_gn, globalNodeMap.get(term));
            min_rn = Math.min(min_rn, localNodeMap.get(term));

            gnidf += (globalNodeMap.get(term) - global_nmin) / (global_nmax - global_nmin + Double.MIN_VALUE);
            rnidf += (localNodeMap.get(term) - local_nmin) / (local_nmax - local_nmin + Double.MIN_VALUE);
            //log.info(term + " gnidf --> " + globalNodeMap.get(term) + " - " + global_nmin + ") / (" + global_nmax + " - " +  global_nmin + " )");
            //log.info(term + " gnidf --> " + (globalNodeMap.get(term) - global_nmin) / (global_nmax - global_nmin));
            //log.info(term + " rnidf --> " + (localNodeMap.get(term) - local_nmin) / (local_nmax - local_nmin));

            ++node_count;
          }
        }
        catch (java.lang.NullPointerException npe) {
          log.error("term '" + term + "' was not found in the map ", npe);
        }

        int localWordcount = term.split("[\\s']+").length;
        wordcount += localWordcount;
        pcl += Ranking.capitalLetters(term)/localWordcount;
      }
      //scoreVectors.get(i).pathLength = paths.get(i).size();
      scoreVectors.get(i).pathLength = 1;

      min_wc = Math.min(wordcount, min_wc);
      max_wc = Math.max(wordcount, max_wc);

      scoreVectors.get(i).globalEdgeIDF = Math.max(0.0, Math.min(1.0, geidf / edge_count));
      scoreVectors.get(i).resultEdgeIDF = Math.max(0.0, Math.min(1.0, reidf / edge_count));
      scoreVectors.get(i).globalNodeIDF = Math.max(0.0, Math.min(1.0, gnidf / node_count));
      scoreVectors.get(i).resultNodeIDF = Math.max(0.0, Math.min(1.0, rnidf / node_count));
      scoreVectors.get(i).pathCapitalLetters =  pcl / wordcount; 
      scoreVectors.get(i).words = wordcount; 
      //log.info("scoreVectors: " + scoreVectors.get(i).toString());
    }

    for (int i = 0; i < paths.size(); ++i) {

      // Update wordcount
      scoreVectors.get(i).words = (scoreVectors.get(i).words - min_wc) / (max_wc - min_wc); 

      // Set the score
      paths.get(i).setConf(scoreVectors.get(i).score());
    }
    
    return paths;
  }
 
  public static Pair<Map<String,Integer>, Map<String,Integer>> GetLocalStats(final ArrayList<Path> paths) {

    Map<String,Integer> localEdgeMap = new HashMap<String,Integer>();
    Map<String,Integer> localNodeMap = new HashMap<String,Integer>();
    // Get local stats 
    for (int i = 0; i < paths.size(); ++i) {
      //log.debug(">>> " + paths.get(i).size());
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
    log.debug("localEdgeMap.size(): " + localEdgeMap.size());
    log.debug("localNodeMap.size(): " + localNodeMap.size());

    return new Pair<Map<String,Integer>,Map<String,Integer>>(localEdgeMap, localNodeMap);
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
