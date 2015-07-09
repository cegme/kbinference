package com.cegme.kbinference.graph

import com.opencsv.CSVReader
import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Graph
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
    static void populateGraph(Graph graph, String resourcePath) {
        StopWatch watch = new StopWatch()
        watch.start()

        def stream = GraphService.class.getResourceAsStream(resourcePath)
        def isr = new InputStreamReader(stream)
        CSVReader reader = new CSVReader(isr)
        reader.withCloseable {
            String[] arr
            long counter = 0
            final Set<String> reservedWords = ['id', 'label']
            while ((arr = reader.readNext()) != null) {
                try {

                    def containsInvalidData = [arr[1], arr[2], arr[3]].any {
                        it.matches(".*\\d+.*")  || reservedWords.contains(it)
                    }

                    if (!containsInvalidData){
                        def v1 = graph.addVertex(null)
                        v1.setProperty('noun', arr[1])

                        def v2 = graph.addVertex(null)
                        v2.setProperty('noun', arr[3])

                        graph.addEdge(arr[0].toLong(), v1, v2, arr[2])
                        
                        counter++

                        if (counter % 100_000 == 0L) {
                            def durationMins = TimeUnit.MINUTES.convert(watch.time, TimeUnit.MILLISECONDS)
                            println String.format("Added %,d nodes in %s mins", counter, durationMins)
                        }
                    }
                    else {
                        log.warn("Invalid Data: ${Arrays.toString(arr)}")
                    }
                } catch (Exception e) {
                    //log.error("Error adding triple: $lineTxt", e)
                    log.error("Error adding triple: ${Arrays.toString(arr)}", e)
                }
            }
        }
    }
}
