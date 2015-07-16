

package com.cegme.kbinference.graph;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@Slf4j
public class PathTest {

  @Test
  public void testParsing () {
    String testPath1 = "[Fox News:170612, calls the election for:1ULNP-InO-mEnA, McCain:33452, may do well in:2FcbL-8Hy-Sa2, NH:1369296]";
    Path path1 = Path.buildPath(testPath1, 0.5);
    log.info("--->" + testPath1 + " " + path1.toString());
    assertEquals("The test path is the same ", testPath1, path1.toString());


    String testPath2 = "[Fox News:170612, calls the election for:1ULNP-InO-mEnA, McCain:33452, did n't have:2Fa2F-8Hy-C8e, a plan:171604]";
    Path path2 = Path.buildPath(testPath2, 0.5);
    log.info("--->" + testPath2 + " " + path2.toString());
    assertEquals("The test path is the same ", testPath2, path2.toString());

    String testPath3 = "[Fox News:170612,  was a reaction to:1ULWd-InO-8OAK,  the Clinton years:897572]";
    Path path3 = Path.buildPath(testPath3, 0.5);
    log.info("--->" + testPath3 + " " + path3.toString());
    assertEquals("The test path is the same ", testPath3, path3.toString());

    String testPath5 = "[N.C.:4852948, is expected to win in:2RTj1-4CY-7YRw, Obama:17792]";
    Path path5 = Path.buildPath(testPath5, 0.5);
    log.info("--->" + testPath5 + " " + path5.toString());
    assertEquals("The test path is the same ", testPath5, path5.toString());

    String testPath4 = "[CNN, Fox News:2010680, has been featured on:1KlNz-dfta-3U9M, Entertainment Drive:3157732]";
    Path path4 = Path.buildPath(testPath4, 0.5);
    log.info("--->" + testPath4 + " " + path4.toString());
    assertEquals("The test path is the same ", testPath4, path4.toString());
    
  }

}


