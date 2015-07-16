
package com.cegme.kbinference.graph;

import com.cegme.kbinference.graph.PathNode;
import com.cegme.kbinference.graph.Sankey;

import java.lang.StringBuilder;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Path {

  public ArrayList<PathNode> path;
  private double conf;

  public Path() {
    this(new ArrayList<PathNode>(), 0.0);
  }

  public Path (List<PathNode> path, double conf) {
    if (path == null) {
      this.path = new ArrayList<PathNode>();
    }
    else {
      //Collections.copy(this.path, path);
      this.path = new ArrayList<PathNode>(path);
    }
    setConf(conf);
  }

  public void addPathNode(String term, String id, boolean edge) {
    path.add(new PathNode(term,id,edge));
  }

  public PathNode get(int node) {
    return path.get(node);
  }

  public int size () {
    return path.size();
  }

  public void setConf(double conf) {
    this.conf = Math.min(1.0,conf);
  }

  public String toString () {
    StringBuilder sb  = new StringBuilder();

    sb.append("[");
    if (path.size() > 0) {
      sb.append(path.get(0).toString());
      for (int i = 1; i < path.size(); ++i) {
        sb.append(",");
        sb.append(path.get(i).toString());
      }
    }
    sb.append("]");

    return sb.toString();
  }

  /**
    * Takes a path string and creates a path object.
    *
    * input: " [Fox News:170612, calls the election for:1ULNP-InO-mEnA, McCain:33452, may do well in:2FcbL-8Hy-Sa2, NH:1369296]"
    * output: path object
    * @deprecated use the {@link #buildPath} method.
    */
  @Deprecated
  public static Path oldBuildPath(String text, double conf) {
    Path path = new Path();
  
    //final String pattern = "[\\[,]\\s*(\\w+):(\\w+)[\\]?]";
    //final String pattern = "[\\s\\-]*([\\w\\-\\s]+):([\\w\\-\\s]+)[\\s\\-]*";
    //final String pattern = "[\\s\\-]*([\\w\\-\\s']+):([\\w\\-\\s']+)[\\s\\-]*";
    final String pattern =
      "([\\w\\-\\s'\\.]+):([\\w\\-\\s']+)";
    Matcher m = Pattern.compile(pattern).matcher(text);

    boolean isEdge = false;
    while(m.find()) {
      path.addPathNode(m.group(1), m.group(2), isEdge);
      isEdge ^= true;
    }
    path.setConf(conf);

    return path;
  }

  public static Path buildPath(String text, double conf) {
    Path path = new Path();
    StringBuilder sb = new StringBuilder();

    String term = "", id = "";
    boolean isEdge = false;
    boolean lookingForId = false;

    for (int i = 1; i < text.length()-1; ++i) {
      char token = text.charAt(i);
      if (!lookingForId && token != ':') {
        sb.append(token);
      }
      else if (!lookingForId && token == ':') {
        lookingForId ^= true;
        term = sb.toString();
        sb.setLength(0); // Reset buffer
      }
      else if (lookingForId && token != ',') {
        sb.append(token);
      }
      else if (lookingForId && token == ',') {
        lookingForId ^= true;
        id = sb.toString();
        sb.setLength(0); // Reset buffer
        path.addPathNode(term, id, isEdge);
        isEdge ^= true;
      }
    }

    // Add the leffover if
    path.addPathNode(term, sb.toString(), isEdge);
    path.setConf(conf);

    return path; 
  }

  public static Sankey toSankey(ArrayList<Path> paths) {
    Sankey sankey = new Sankey();
   
    // Pass one, build nodes 
    HashMap<String,Integer> nodeMap = new HashMap<String, Integer>();
    int nodeCounter = 0;
    for (Path p : paths) {
      for (PathNode pn : p.path) {
        if (!pn.edge && !nodeMap.containsKey(pn.term)) {
          nodeMap.put(pn.term, nodeCounter++); 
          sankey.addNode(pn.term);
        }
      }
    }

    // Pass two, build edges
    for (Path p : paths) {
      //for (PathNode pn : p.path) {
      for (int i = 0; i < p.path.size(); ++i) {
        if (p.path.get(i).edge) {
          // NOTE: an edge is ALWAYS between two vertices
          int src = nodeMap.get(p.path.get(i-1).term);
          int dst = nodeMap.get(p.path.get(i+1).term);
          sankey.addLink(src, dst, p.conf); 
        }
      }
    }

    return sankey;
  }


  public static void main (String[] args) {}

}
