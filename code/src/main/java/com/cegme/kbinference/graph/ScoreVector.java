
package com.cegme.kbinference.graph;

import java.util.concurrent.ThreadLocalRandom;

public class ScoreVector {
  public int TOTAL = 6;

  public ScoreVector () {

    globalIDF = 0.0;
    resultIDF = 0.0;
    pathCapitalLetters = 0.0;
    pathLength = 0.0;
    timeliness = 1.0;
  }

  // Global is the full data set
  // Result is the set of answers
  // Path is an indivudual path

  public double globalIDF;
  public double resultIDF;
  public double pathCapitalLetters;
  public double pathLength;
  public double timeliness = 1.0;
  public double noise () {
    return 0.9 + 0.10*ThreadLocalRandom.current().nextDouble();
  }

  public double score() {
    return (globalIDF + resultIDF + pathCapitalLetters + pathLength +  timeliness + noise()) / TOTAL;
  }
}

