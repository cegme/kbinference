
package com.cegme.kbinference.graph;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sankey {
  public ArrayList<Vertex> nodes;
  public ArrayList<Edge> links;
  
  class Vertex {
    public String name;
    public Vertex (String name) {
      this.name = name;
    }
  }

  class Edge implements Comparable<Edge>{
    public int source;
    public int target;
    public double value;
    public String edge;

    public Edge(int source, int target, double value, String edge) {
      this.source = source;
      this.target = target;
      this.value = value;
      this.edge = edge;
    }
    public int compareTo(Edge o){
      int first = source - o.source;
      int second = target - o.target;
      //int third = Double.compare(value, o.value);
      //int forth = edge.compareTo(o.edge);
      //return (first != 0)?first : (second != 0)? second : (third != 0)?third : forth;
      return (first != 0)?first : second;
    }
  }

  public Sankey() {
    nodes = new ArrayList<Vertex>();
    links = new ArrayList<Edge>();
  }
  
  public void addLink(int source, int target, double value, String edge) {
    links.add(new Edge(source, target, value, edge));
  }
  public void addNode(String name) {
    nodes.add(new Vertex(name));
  }

  public String toJson() {
    compressDuplicates();

    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public void compressDuplicates () {
    log.info("Compressing Duplicates");
    // Assume the edges are sorted and adjacent
    Collections.sort(links);

    ArrayList<Edge> newlinks = new ArrayList<Edge>();
    
    if (links.size() > 0) {
      newlinks.add(links.get(0));
    }
    int dups = 0;
    int newcounter = 0;
    for (int i = 1; i < links.size(); ++i) {
      if(links.get(i-1).compareTo(links.get(i)) == 0) {
        newlinks.get(newcounter).value += links.get(i).value;
        newlinks.get(newcounter).edge = newlinks.get(newcounter).edge + "\n" + links.get(i).edge;
      }
      else {
        newlinks.add(links.get(i));
        ++newcounter;
      }
    }
    
    links = newlinks;

  }

}
