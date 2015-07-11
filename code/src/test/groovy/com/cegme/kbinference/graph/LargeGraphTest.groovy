package com.cegme.kbinference.graph

import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.groovy.Gremlin
import org.apache.commons.configuration.BaseConfiguration
import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals

public class LargeGraphTest {
    private static Properties props = new Properties()
    static TransactionalGraph graph

    @BeforeClass
    static void initDb(){
       

        Scanner userInput = new Scanner(System.in)
        System.out.print("Do you want to load a new graph (y/n)?\n>> ")
        char c = userInput.nextChar().toLowerCase()

        Gremlin.load()

        props.load(GraphTest.class.getResourceAsStream('/test.properties'))
        def path = props.getProperty('graphdb.path')

        def graphDir = new File(path)
        if(graphDir.exists() && c == 'y'){
            //log.debug("Cleaning the graph directory ${path}")
            FileUtils.cleanDirectory(graphDir)
        }

        BaseConfiguration conf = new BaseConfiguration()
        //conf.setProperty("storage.batch-loading", true);
        //conf.setProperty("autotype", "none");
        conf.setProperty("storage.directory", path);
        conf.setProperty("storage.backend", "persistit");
        conf.setProperty("storage.transactions","false")
        //conf.setProperty("storage.buffer-size", "1073741824") // 1G
        //conf.setProperty("buffer.count.16384", "50000");
        graph = TitanFactory.open(conf)

        if (c == 'y') {
            //log.info("Populating the graph.")
            graph.makeKey("noun").dataType(String).indexed(Vertex).make()
            graph.commit()
            GraphService.populateGraph(graph, '/reverb_clueweb_tuples-1.1.triples.clean.csv')
        }
    }

    @Test
    void testGraphLoaded(){
        def vertexCnt = graph.vertices.size()
        assertEquals('All vertices were loaded', 2220582, vertexCnt)
    }

    @Test
    void testQueries(){
        def chargerVertexCnt = graph.V('noun', 'the chargers').count()
        assertEquals('Able to query by vertex property', 137, chargerVertexCnt)

        def whoGotJoeMontana = graph.V('noun', 'joe montana').inE('traded for').outV.noun.next()
        assertEquals('Able to query & traverse the graph', 'the chiefs', whoGotJoeMontana)
    }

    @AfterClass
    static void afterClazz() throws Exception {
        graph.shutdown()
    }
}