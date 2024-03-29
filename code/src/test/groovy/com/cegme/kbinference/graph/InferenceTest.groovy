
package com.cegme.kbinference.graph

import com.google.common.collect.Maps;

import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.groovy.Gremlin

import groovy.util.logging.Slf4j

import org.apache.commons.configuration.BaseConfiguration
import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

@Slf4j
public class InferenceTest {

    private static Properties props = new Properties()
    static TransactionalGraph graph

    @BeforeClass
    static void loadDb () {
        Gremlin.load()

        props.load(GraphTest.class.getResourceAsStream('/test.properties'))
        def path = props.getProperty('graphdb.path')

        def graphDir = new File(path)

        BaseConfiguration conf = new BaseConfiguration()
        //conf.setProperty("storage.batch-loading", true);
        //conf.setProperty("autotype", "none");
        conf.setProperty("storage.directory", path);
        conf.setProperty("storage.backend", "persistit");
        conf.setProperty("storage.transactions","false")
        //conf.setProperty("storage.buffer-size", "1073741824") // 1G
        //conf.setProperty("buffer.count.16384", "50000");
        graph = TitanFactory.open(conf)
    }

    @Test
    void testGraphLoaded(){
        def vertexCnt = graph.vertices.size()
        assertEquals('All vertices were loaded', 2197534, vertexCnt)

        def edgeCnt = graph.edges.size()
        assertEquals('All edges were loaded', 11302466, edgeCnt)
    }


    @Test 
    void testVertexSearchQuery () { 
      def startnodeVertices = graph.V('noun', 'Fox News').next()

      def inCnt = 0
      def outCnt = 0
      def totalCnt = 0
      // FIXME probably not idiomatic groovy
      startnodeVertices.outE.each {
        ++totalCnt
        ++outCnt 
      }
      startnodeVertices.inE.each {
        ++totalCnt
        ++inCnt
      }
      assertNotNull("We have start nodes", startnodeVertices as Object)
      assertNotNull("We have outE", outCnt as Object)
      assertNotNull("We have inE", inCnt as Object)
    }

    @Test
    void testOneHopPath () {
      // This test should make one hop
      // In these terms, one hope is from v1 to v3 
      // e.g. [v1] -- [ v2] -- [v3]
      // TODO how to filter?? and rank the results
      def onehopVertices = graph.V('noun', 'Fox News')
                                .outE  // Get the incoming and out going edges
                                .inV
                                .outE
                                .inV
                                //.filter(noun == "pot")
                                //.noun
                                .path{(it.noun==null)?"${it.label}:${it.id}":"${it.noun}:${it.id}"}
      onehopVertices.each {
        System.err.println(">1> ${it}")
      }
      true
    }

    @Test
    void testTwoHopPath () {
      // Need to collect groups of facts.
      // How can we change the path so that we attach a 
      // v2 and some other v2' that isn't exactly same.
      //
      // Print out the actual path of a one hop query
      // e.g. [v1] -- [ v2] [v2'] -- [v3]
      // where v2 ~ v2'
      // Notice two edges may be connected if they are not approximatley equal
      // I think this is not directly doable in this framework. 
      def twohopVertices = graph.V('noun', 'Fox News')
                                .outE  // Get the incoming and out going edges
                                .inV
                                .outE
                                .inV
                                .random(0.2)
                                .outE
                                .random(0.2)
                                .inV
                                .filter{it.noun=='Obama'}
                                .path{(it.noun==null)?"${it.label}:${it.id}":"${it.noun}:${it.id}"}

      twohopVertices.each {
        System.err.println(">2> ${it}")
      }
      true
    }

    @Test
    void testWeirdPath () {
      def K = 3
      //def khopVertices = graph.V('noun', 'Fox News')
      //def khopVertices = graph.V('noun', 'N.C.').inE.outV
      def khopVertices = graph.V('noun', 'CNN, Fox News').inE.outV
                                .path{(it.noun==null)?"${it.label}:${it.id}":"${it.noun}:${it.id}"}

      khopVertices.each {
        System.err.println(">3> ${it}")
      }
      true
    }

    @Test
    void testKHopPath () {
      def K = 3
      //def khopVertices = graph.V('noun', 'Fox News')
      def khopVertices = graph.V('noun', 'Obama')
                                .outE  // Get the incoming and out going edges
                                .inV
                                .loop(K){it.loops < K}
                                //.filter(noun == "pot")
                                //.noun
                                .path{(it.noun==null)?"${it.label}:${it.id}":"${it.noun}:${it.id}"}

      khopVertices.each {
        System.err.println(">3> ${it}")
      }
      true
    }

    @Test
    void testSerialization () {
      
        props.load(GraphTest.class.getResourceAsStream('/test.properties'))

         // Test Edges
        def edgefile = props.getProperty('edgemap.file')
        def edgemap = GraphService.relationHistogram(graph)
        GraphService.serializeCompressedMap(edgemap,edgefile) 

        def edgemap2 = GraphService.deserializeCompressedMap(edgefile)

        // Thanks google guava
        assertTrue("Are edges counted and serialized correctly ", Maps.difference(edgemap, edgemap2).areEqual())
        assertEquals("Is the extracted edge count equal ", edgemap.get("Obama"), edgemap2.get("Obama"))
 
        // Test Vertices
        def vertexfile = props.getProperty('vertexmap.file')
        def vertexmap = GraphService.entityHistogram(graph)
        GraphService.serializeCompressedMap(vertexmap,vertexfile) 

        def vertexmap2 = GraphService.deserializeCompressedMap(vertexfile)

        // Thanks google guava
        assertTrue("Are Vertices counted and serialized correctly ", Maps.difference(vertexmap, vertexmap2).areEqual())
        assertEquals("Is the extracted vertex count equal ", vertexmap.get("Obama"), vertexmap2.get("Obama"))
    }


    @AfterClass
    static void afterClass() throws Exception {
        graph.shutdown()
    }
}
