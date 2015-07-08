package com.cegme.kbinference.graph

import com.tinkerpop.blueprints.impls.tg.TinkerGraph

class GraphService {
    static void main(def args) {
        def graph = loadGraph('/data.csv')
        //some code here
    }

    static TinkerGraph loadGraph(String resourcePath) {
        TinkerGraph graph = new TinkerGraph()

        def dataStr = GraphService.class.getResourceAsStream(resourcePath).text
        dataStr.eachLine {

            long counter = 0
            def arr = it.split(',').collect { val ->
              //strip leading/trailing quotes
              val.take(val.length() - 1).drop(1)
            }
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
            }
            else {
              System.err.println(">> " + it)
            }
            counter += 1
            if (counter % 100000 == 0) 
              System.err.println(">line> " + counter)
        }
        return graph
    }
}
