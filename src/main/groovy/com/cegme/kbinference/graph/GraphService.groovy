package com.cegme.kbinference.graph

import com.opencsv.CSVReader
import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Graph
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.time.StopWatch

import java.util.concurrent.TimeUnit

@Slf4j
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
                def lineTxt = Arrays.toString(arr)
                try {
                    long id = arr[0].toLong()
                    String nounA = arr[1].toLowerCase()
                    String verb = arr[2].toLowerCase()
                    String nounB = arr[3].toLowerCase()

                    def containsInvalidData = [].any {
                        it.matches(".*\\d+.*")  || reservedWords.contains(it)
                    }

                    if (!containsInvalidData){
                        def v1 = graph.addVertex(null)
                        v1.setProperty('noun', nounA)

                        def v2 = graph.addVertex(null)
                        v2.setProperty('noun', nounB)

                        //graph.addEdge(null, v1, v2, verb) // Save space, no id
                        graph.addEdge(id, v1, v2, verb)
                        counter++

                        if (counter % 100_000 == 0L) {
                            def durationMins = TimeUnit.MINUTES.convert(watch.time, TimeUnit.MILLISECONDS)
                            println String.format("Added %,d nodes in %s mins", counter, durationMins)
                        }
                    }
                    else {
                        log.warn(">> " + "$lineTxt")
                    }
                } catch (Exception e) {
                    log.error("Error adding triple: $lineTxt", e)
                }
            }
        }
    }
}
