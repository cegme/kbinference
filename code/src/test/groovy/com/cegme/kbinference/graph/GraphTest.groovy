package com.cegme.kbinference.graph

import com.thinkaurelius.titan.core.TitanFactory
import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.gremlin.groovy.Gremlin
import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals

public class GraphTest {
    private static Properties props = new Properties()
    static TransactionalGraph graph

    @BeforeClass
    static void initDb(){
        Gremlin.load()

        props.load(GraphTest.class.getResourceAsStream('/test.properties'))
        def path = props.getProperty('graphdb.path')

        def graphDir = new File(path)
        if(graphDir.exists()){
            FileUtils.cleanDirectory(graphDir)
        }

        graph = TitanFactory.open(path)
        GraphService.populateGraph(graph, '/test-reverb-triples.csv')
    }

    @Test
    void testGraphLoaded(){
        def vertexCnt = graph.vertices.size()
        assertEquals('All vertices were loaded', 20000, vertexCnt)
    }

    @Test
    void testQueries(){
        def chargerVertexCnt = graph.V('noun', 'the chargers').count()
        assertEquals('Able to query by vertex property', 48, chargerVertexCnt)

        def whoGotJoeMontana = graph.V('noun', 'joe montana').inE('traded for').outV.noun.next()
        assertEquals('Able to query & traverse the graph', 'the chiefs', whoGotJoeMontana)
    }

    @AfterClass
    static void afterClazz() throws Exception {
        graph.shutdown()
    }
}
