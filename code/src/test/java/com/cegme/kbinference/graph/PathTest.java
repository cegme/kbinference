

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
    assertEquals("The test path is the same ", path1.toString(), testPath1);


    String testPath2 = "[Fox News:170612, calls the election for:1ULNP-InO-mEnA, McCain:33452, did n't have:2Fa2F-8Hy-C8e, a plan:171604]";
    Path path2 = Path.buildPath(testPath2, 0.5);
    assertEquals("The test path is the same ", path2.toString(), testPath2);
  }

}


