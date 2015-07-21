
package com.cegme.kbinference.graph;

import com.cegme.kbinference.graph.Ranking;
import com.cegme.kbinference.graph.GraphService;
import com.cegme.kbinference.graph.Pair;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.gremlin.groovy.Gremlin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class DissertationResultTest {

    private static Properties props = new Properties();
    static TransactionalGraph graph;

  @BeforeClass
  public static void loadGraph () {
    Gremlin.load();
    try {
      props.load(Ranking.class.getResourceAsStream("/app.properties"));
    }
    catch (IOException ioe) {
      log.error("Error loading the properties page", ioe);
    }

    log.info("Loading the Graph.");    
    graph = GraphService.loadDb();
  }

  @AfterClass
  public static void shutdownGraph () { 
    graph.shutdown();
  }


  @Test
  public void testRanking () {

    StopWatch watch = new StopWatch();

    ArrayList<Pair<String,String>> experiments = new ArrayList<Pair<String,String>>() ;
    //experiments.add(new Pair<String,String>("Marijuana", "Fox News"));
    //experiments.add(new Pair<String,String>("Abortion", "Hillary"));
    //experiments.add(new Pair<String,String>("Brutality", "Biden"));
    //experiments.add(new Pair<String,String>("Reddit", "Fox News"));
    experiments.add(new Pair<String,String>("Marijuana", "CNN"));

    ArrayList<Integer> hops = new ArrayList<Integer>();
    //hops.add(2);
    hops.add(3);
    hops.add(4);


    System.out.println("hops,src,dst,time");
    // Repeat experiment
    //for (int repeat = 0; repeat < 1; ++repeat) { 
      for (Pair<String,String> p : experiments) {
        for (Integer hop : hops) {

          Set<String> srcs = new HashSet<String>();
          srcs.add(p.left);
          Set<String> dsts = new HashSet<String>();
          dsts.add(p.right);

          watch.start();
          GraphService.buildPathNoSave(graph, srcs, dsts, hop, 1.0); 
          watch.stop();
          long duration = watch.getTime();
          System.out.println(hop + ",\"" + p.left + "\",\"" + p.right + "," + duration);
          watch.reset();
        }
      } 
    //}
  }


  public static void main (String[] args) {

  }

}
