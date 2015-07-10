package com.cegme.kbinference.graph

import com.opencsv.CSVReader
import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph
import com.tinkerpop.blueprints.util.wrappers.batch.VertexIDType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.time.StopWatch

import java.util.concurrent.TimeUnit

@Slf4j
@CompileStatic
class GraphService {
    static Properties props = new Properties()
    static TitanGraph graph

    static {
        props.load(GraphService.class.getResourceAsStream('/app.properties'))
        def path = props.getProperty('graphdb.path')
        graph = TitanFactory.open(path)
    }

    static void main(def args) {
        //some code here
    }

    /**
     * Populate a graph from the given csv file
     * @param graph graph to populate
     * @param resourcePath relative path to a csv file
     */
    static void populateGraph(TransactionalGraph graph, String resourcePath) {
        StopWatch watch = new StopWatch()
        watch.start()

        BatchGraph bg = new BatchGraph(graph, VertexIDType.STRING, 300_000)
        TitanGraph tg = bg.baseGraph as TitanGraph
        bg.setVertexIdKey('noun')
        def reservedWords = ['id', 'label'] as Set<String>

        def isr = new InputStreamReader(GraphService.class.getResourceAsStream(resourcePath))
        new CSVReader(isr).withCloseable {
            long counter = 0
            String[] arr
            def id
            def noun1
            def verb
            def noun2
            while ((arr = it.readNext()) != null) {
                try {
                    id = arr[0].toLong()
                    noun1 = arr[1]
                    verb = arr[2]
                    noun2 = arr[3]

                    def containsInvalidData = [noun1, verb, noun2].any { String val ->
                        val.matches(".*\\d+.*")  || reservedWords.contains(val.toLowerCase())
                    }

                    if (!containsInvalidData){
/*
                        //with batch loading
                        def type = tg.getType(verb)
                        if(!type){
                            tg.makeLabel(verb).make()
                        }
*/

                        def v1 = bg.getVertex(noun1) ?: bg.addVertex(noun1)
                        def v2 = bg.getVertex(noun2) ?: bg.addVertex(noun2)
                        bg.addEdge(id, v1, v2, verb)

                        counter++
                        if (counter % 100_000 == 0L) {
                            def durationMins = TimeUnit.MINUTES.convert(watch.time, TimeUnit.MILLISECONDS)
                            println String.format("Added %,d nodes in %s mins", counter, durationMins)
                        }
                    }
                    else {
                        //log.warn("Invalid Data: ${Arrays.toString(arr)}")
                    }
                } catch (Exception e) {
                    log.error("Error adding triple: ${Arrays.toString(arr)}", e)
                }
            }
        }
    }
}
