package com.cegme.kbinference.graph

import com.thinkaurelius.titan.core.TitanFactory
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

@Slf4j
public class LargeGraphTest {
    private static Properties props = new Properties()
    static TransactionalGraph graph

    @BeforeClass
    static void initDb() {
        Gremlin.load()

        props.load(GraphTest.class.getResourceAsStream('/test.properties'))
        boolean createGraph = props.getProperty('populate.graph', 'false').toBoolean()
        def path = props.getProperty('graphdb.path')

        def graphDir = new File(path)
        BaseConfiguration conf = new BaseConfiguration()

        conf.setProperty("storage.directory", path);
        conf.setProperty("storage.backend", "persistit");

        if(createGraph){
            if(graphDir.exists()){
                FileUtils.cleanDirectory(graphDir)
            }

            //conf.setProperty("storage.batch-loading", true);
            //conf.setProperty("autotype", "none");
            conf.setProperty("storage.transactions", "false")
            conf.setProperty("storage.buffer-size", "1073741824") // 1G
            conf.setProperty("buffer.count.16384", "50000");
        }
        graph = TitanFactory.open(conf)


        if(createGraph){
            graph.makeKey("noun").dataType(String).indexed(Vertex).make()
            graph.commit()
            log.debug("Populating the graph.")
            GraphService.populateGraph(graph, '/reverb_clueweb_tuples-1.1.triples.clean.csv')
        }
    }

    @Test
    void testGraphLoaded() {
        def vertexCnt = graph.vertices.size()
        assertEquals('All vertices were loaded', 2220582, vertexCnt)
    }

    @Test
    void testQueries() {
        def whoGotJoeMontana = graph.V('noun', 'Joe Montana').inE('traded for').outV.noun.next()
        assertEquals('Able to query & traverse the graph', 'the Chiefs', whoGotJoeMontana)

        def chargerVertexCnt = graph.V('noun', 'the Chargers').count()
        assertEquals('Able to query by vertex property', 1, chargerVertexCnt)

    }

    @AfterClass
    static void afterClazz() throws Exception {
        graph.shutdown()
    }
}
