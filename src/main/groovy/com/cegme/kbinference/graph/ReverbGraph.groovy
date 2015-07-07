package com.cegme.kbinference.graph

import com.tinkerpop.blueprints.impls.tg.TinkerGraph

class ReverbGraph {
    static void main(def args) {
        def graph = loadGraph('/data.csv')
        //some code here
    }

    static TinkerGraph loadGraph(String resourcePath) {
        TinkerGraph graph = new TinkerGraph()

        def dataStr = ReverbGraph.class.getResourceAsStream(resourcePath).text
        dataStr.eachLine {
            def arr = it.split(',').collect { val ->
                //strip leading/trailing quotes
                val.take(val.length() - 1).drop(1)
            }
            long id = arr[0].toLong()
            String nounA = arr[1]
            String verb = arr[2]
            String nounB = arr[3]

            def v1 = graph.addVertex(null)
            v1.setProperty('noun', nounA)

            def v2 = graph.addVertex(null)
            v2.setProperty('noun', nounB)

            graph.addEdge(id, v1, v2, verb)
        }
        return graph
    }
}
