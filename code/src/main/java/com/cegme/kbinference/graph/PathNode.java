
package com.cegme.kbinference.graph;

public class PathNode {

  public String term;
  public String id;
  public boolean edge; // This is probably not needed

  public PathNode () {
    this("<null>", "N/A", false);
  }

  public PathNode (String term, String id, boolean edge) {
    this.term = term;
    this.id = id;
    this.edge = edge;
  }

  public String toString () {
    return term + ":" + id;
  }

}
