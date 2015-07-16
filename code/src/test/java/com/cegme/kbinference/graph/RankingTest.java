
package com.cegme.kbinference.graph;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.cegme.kbinference.graph.Ranking;
import com.cegme.kbinference.graph.GraphService;

import java.io.IOException;
import java.util.ArrayList;

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

    log.debug("Loading the Graph.");    
    TransactionalGraph graph = GraphService.loadDb();


    log.debug("Building the path using the khop algorithm.");    
    ArrayList<String> stringPaths = GraphService.buildPath(graph, "Obama", "pot", 2); 

    log.debug("Translating paths from string to objects.");
    ArrayList<Path> paths = new ArrayList<Path>();
    for (String p : stringPaths) {
      paths.add(Path.buildPath(p, 0.5));
    }

    log.debug("Ranking the paths");
    try { 
      Ranking.RankPaths(graph, paths);
    }
    catch(IOException ioe) {
      log.error("Error ranking the paths", ioe);
    }
    
    log.debug("Creating a sankey diagram");
    Sankey sankey = Path.toSankey(paths);
    
    log.debug("Printing the sankey json.");
    System.out.println("\n" + sankey.toJson());

    graph.shutdown();
  }


  public static void main (String[] args) {

  }

}
