package com.cegme.kbinference.graph

import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.gremlin.groovy.Gremlin
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals

public class GraphTest {
    TinkerGraph graph

    @BeforeClass
    static void beforeClass(){
        Gremlin.load()
    }

    @Before
    void setUp() throws Exception {
        graph = ReverbGraph.loadGraph('/test-reverb-triples.csv')
    }

    @Test
    void testGraphLoaded(){
        def vertexCnt = graph.vertices.size()
        assertEquals('All vertices were loaded', 20000, vertexCnt)
    }

    @Test
    void testQueries(){
        //def chargerVertexCnt = graph.V('noun', 'the Chargers').count()
        def chargerVertexCnt = graph.V('noun', 'the chargers').count()
        assertEquals('Able to query by vertex property', 48, chargerVertexCnt)

        //def whoGotJoeMontana = graph.V('noun', 'Joe Montana').inE('traded for').outV.noun.next()
        def whoGotJoeMontana = graph.V('noun', 'joe montana').inE('traded for').outV.noun.next()
        //assertEquals('Able to query & traverse the graph', 'the Chiefs', whoGotJoeMontana)
        assertEquals('Able to query & traverse the graph', 'the chiefs', whoGotJoeMontana)
    }


    @After
    void tearDown() throws Exception {
        graph.shutdown()
    }
}
