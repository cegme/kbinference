
import com.tinkerpop.blueprints.TransactionalGraph;

import java.util.List;
import java.util.ArrayList;



public class KbInference {

  class PathResult {
    public String path;
    public double conf;
  }

  public static List <PathResult> buildPath (TransactionalGraph graph, String src, Integer max_length ) {
   
   // TODO  

 
    return new ArrayList<PathResult>();
  }

  /**
    * Coun the number of capical letters in the path and return the count.
    * We want to know the number of capical letters because that correlates with the number of entities mentioned.
    */
  public static double capitalLetters (String path) {
    return 0.0;
  }
   

  public static void main(String [] args) {
    
    

  }

}
