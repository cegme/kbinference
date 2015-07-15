
package com.cegme.kbinference.graph;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ranking {
  
   /**
    * Count the number of capical letters in the path and return the count.
    * We want to know the number of capical letters because that correlates with the number of entities mentioned.
    */
  public static double capitalLetters (final String path) {
    final String pattern = "([A-Z]\\w+[ \\s:$])";
    Matcher m = Pattern.compile(pattern).matcher(path);
    
    short words = 0;
    while(m.find()) {
      //System.err.println(">2> " + m.group());
      ++words;
    }

    short items = 0;
    for (int i = 0; i < path.length(); ++i) {
      if (path.charAt(i) == ':') {
        ++items;
      }
    }

    return (double) words / items;
  }
 

  public static void main(String[] args) {

  }

}
