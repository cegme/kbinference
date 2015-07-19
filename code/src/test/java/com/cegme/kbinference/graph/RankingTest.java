
package com.cegme.kbinference.graph;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.cegme.kbinference.graph.Ranking;
import com.cegme.kbinference.graph.GraphService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class RankingTest {


  @Test
  public void testLoadGraph () {
    TransactionalGraph graph = GraphService.loadDb();
    assertNotNull("The graph is properly loaded", graph);
    graph.shutdown();
  }


  @Test
  public void testCapitalLetters () {
    String test1 = "[Fox News:170612, needs more people like:1ULQL-InO-5GiG, Judge Napolitano:4437396, appears daily on:2owil-iCmU-gq1E, The Big Story:5403516]";
    double conf1 = Ranking.capitalLetters(test1);
    assertEquals("First test should return 7/5", 7.0/5, conf1, 0.001);

  }


  @Test
  public void testRanking () {
    
    Properties props = new Properties();
    try {
      props.load(Ranking.class.getResourceAsStream("/app.properties"));
    }
    catch (IOException ioe) {
      log.error("Error loading the properties page", ioe);
    }

    log.info("Loading the Graph.");    
    TransactionalGraph graph = GraphService.loadDb();

    Set<String> srcs = new HashSet<String>();
      //srcs.add("Hillary");
      //srcs.add("Palin");
      srcs.add("Marijuana");
      //srcs.add("Pot");
    Set<String> dsts = new HashSet<String>();
      dsts.add("CNN");
      dsts.add("Fox News");

    log.info("Building the path using the khop algorithm.");    
    ArrayList<String> stringPaths = GraphService.buildPath(graph, "Marijuana", "Fox News", 4, 0.9); 
    //ArrayList<String> stringPaths = GraphService.buildPath(graph, "Obama", "Fox News", 3, 0.5); 
    //ArrayList<String> stringPaths = GraphService.buildPath(graph, "Obama", null, 3, 0.008); 
    //ArrayList<String> stringPaths = GraphService.buildPath(graph, "Obama", "Fox News", 4, 0.1); 
    //ArrayList<String> stringPaths = GraphService.buildPath(graph, srcs, dsts, 3, 0.9); 
    //ArrayList<String> stringPaths = GraphService.buildPathMultiK(graph, srcs, dsts, 3, 0.9); 

    log.info("Translating paths from string to objects.");
    ArrayList<Path> paths = new ArrayList<Path>();
    for (String p : stringPaths) {
      paths.add(Path.buildPath(p, 0.5));
    }

    log.info("Ranking the paths");
    try { 
      Ranking.RankPaths(graph, paths);
    }
    catch(IOException ioe) {
      log.error("Error ranking the paths", ioe);
    }
    
    log.info("Creating a sankey diagram");
    Sankey sankey = Path.toSankey(paths);
    
    log.info("Printing the sankey json.");
    //System.out.println("\n" + sankey.toJson());

    log.info("Writing the page");
    String page = SankeyBuilder.buildPage(sankey.toJson(), paths);
    SankeyBuilder.writePage(page, props.getProperty("sankeyout.file"));

    graph.shutdown();
  }


  public static void main (String[] args) {

  }

}
