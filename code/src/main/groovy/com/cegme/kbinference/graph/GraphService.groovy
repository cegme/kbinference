package com.cegme.kbinference.graph

import com.opencsv.CSVReader
import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph
import com.tinkerpop.blueprints.util.wrappers.batch.VertexIDType
import com.tinkerpop.gremlin.groovy.Gremlin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.configuration.BaseConfiguration
import org.apache.commons.lang3.time.StopWatch

import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.util.ArrayList;
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

@Slf4j
//@CompileStatic
class GraphService {
    static Properties props = new Properties()
    static TitanGraph graph

    static {
        Gremlin.load()
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

    static TransactionalGraph loadDb () {
        TransactionalGraph graph;
        Gremlin.load()

        props.load(GraphService.class.getResourceAsStream('/test.properties'))
        def path = props.getProperty('graphdb.path')

        def graphDir = new File(path)

        BaseConfiguration conf = new BaseConfiguration()
        //conf.setProperty("storage.batch-loading", true);
        //conf.setProperty("autotype", "none");
        conf.setProperty("storage.directory", path);
        conf.setProperty("storage.backend", "persistit");
        conf.setProperty("storage.transactions","false")
        //conf.setProperty("storage.buffer-size", "1073741824") // 1G
        //conf.setProperty("buffer.count.16384", "50000");
        graph = TitanFactory.open(conf)
        return graph;
    }

    static Map<String,Integer> relationHistogram (TransactionalGraph g) {
      // Get all the edges and create a map of the countes

      def map = new TreeMap<String,Integer>();

      g.getEdges().each {
        Integer val = map.get(it.label);
        String label = (String) it.label;
        if (val == null) {
          map.put(label, 1);
        }
        else {
          map.put(label, val+1);
        }
      }

      return map;
    }

    static Map<String,Integer> entityHistogram (TransactionalGraph g) {
      // Get all the vertices

      def map = new TreeMap<String,Integer>();

      g.getVertices().each {
        Integer val = map.get(it.getProperty('noun'));
        String noun = it.getProperty('noun')
        if (val == null) {
          map.put(noun, 1);
        }
        else {
          map.put(noun, val+1);
        }
      }

      return map;
    }


    static void serializeCompressedMap(Map map, String location) {

      try {
        FileOutputStream fos =
          new FileOutputStream(location);
        GZIPOutputStream gzos = new GZIPOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(gzos);
        oos.writeObject(map);
        oos.close();
        gzos.close();
        fos.close();
      }
      catch (IOException ioe) {
        log.error("Error serialzing the map. ", ioe);
      }

    }

    static java.util.Map<String,Integer> deserializeCompressedMap(String location) {
      java.util.TreeMap<String, Integer> map = null;
      try
      {
        FileInputStream fis = new FileInputStream(location);
        GZIPInputStream gzis = new GZIPInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(gzis);
        map = (TreeMap) ois.readObject();
        ois.close();
        gzis.close();
        fis.close();
      }
      catch(IOException ioe) {
        log.error("Error deserialzing the map.", ioe);
      }
      catch(ClassNotFoundException c) {
        log.error("Could not find the class.", c);
      }
      finally {
        return map;
      }
    }

    /**
      * The map
      */
    static ArrayList<String> buildPath(TransactionalGraph g, String src, String dst, int max_path) {

      ArrayList<String> paths = new ArrayList<String>();

      // Maybe convert it to the propertypes in the code
      def khopVertices =
        g.V('noun', 'Fox News')
        .outE
        .inV
        .loop(max_path){it.loops < max_path}
        .path{(it.noun==null)?"${it.label}:${it.id}":"${it.noun}:${it.id}"} 

      khopVertices.each {
        paths.add("" + "${it}")
      }

      return paths;
    } 
}
