
package com.cegme.kbinference.graph;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
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

  class Edge {
    public int source;
    public int target;
    public double value;
    public Edge(int source, int target, double value) {
      this.source = source;
      this.target = target;
      this.value = value;
    }
  }

  public Sankey() {
    nodes = new ArrayList<Vertex>();
    links = new ArrayList<Edge>();
  }
  
  public void addLink(int source, int target, double value) {
    links.add(new Edge(source, target, value));
  }
  public void addNode(String name) {
    nodes.add(new Vertex(name));
  }

  public String toJson() {
    Gson gson = new Gson();
    //try {
      return gson.toJson(this);
    /*}
    catch  (IOException ioe) {
      log.error("Could not properly serialize to json", ioe);
    }
    finally {
      return "{}";
    }*/
  }

}
