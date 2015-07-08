package com.cegme.kbinference.graph

import com.opencsv.CSVReader
import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Graph
import groovy.util.logging.Slf4j

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
        def stream = GraphService.class.getResourceAsStream(resourcePath)
        def isr = new InputStreamReader(stream)
        CSVReader reader = new CSVReader(isr)
        reader.withCloseable {
            String[] arr
            long counter = 0
            while ((arr = reader.readNext()) != null) {
                try {
                    long id = arr[0].toLong()
                    String nounA = arr[1].toLowerCase()
                    String verb = arr[2].toLowerCase()
                    String nounB = arr[3].toLowerCase()

                    if (!arr[1].matches(".*\\d+.*") && !arr[2].matches(".*\\d+.*") && !arr[3].matches(".*\\d+.*")) {
                        def v1 = graph.addVertex(null)
                        v1.setProperty('noun', nounA)

                        def v2 = graph.addVertex(null)
                        v2.setProperty('noun', nounB)

                        //graph.addEdge(null, v1, v2, verb) // Save space, no id
                        graph.addEdge(id, v1, v2, verb)
                        counter++

                        if (counter % 100_000 == 0L) {
                            println String.format("Added %,d nodes", counter)
                        }
                    }
                    else {
                        log.error(">> ${Arrays.toString(arr)}")
                    }
                } catch (Exception e) {
                    log.error("Error adding triple: ${Arrays.toString(arr)}", e)
                }
            }
        }
    }
}