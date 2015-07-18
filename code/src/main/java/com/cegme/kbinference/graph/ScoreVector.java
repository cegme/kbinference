
package com.cegme.kbinference.graph;

import java.util.concurrent.ThreadLocalRandom;

public class ScoreVector {
  public int TOTAL = 8;

  public ScoreVector () {

    globalEdgeIDF = 0.0;
    resultEdgeIDF = 0.0;
    globalNodeIDF = 0.0;
    resultNodeIDF = 0.0;
    pathCapitalLetters = 0.0;
    pathLength = 0.0;
    timeliness = 1.0;
    words = Double.MIN_VALUE;
  }

  // Global is the full data set
  // Result is the set of answers
  // Path is an indivudual path

  public double globalEdgeIDF;
  public double resultEdgeIDF;
  public double globalNodeIDF;
  public double resultNodeIDF;
  public double pathCapitalLetters;
  public double pathLength;
  public double words;
  public double timeliness = 1.0;
  public double noise () {
    return 0.98 + 0.02*ThreadLocalRandom.current().nextDouble();
  }

  public double score() {
    double score = (globalEdgeIDF + resultEdgeIDF + globalNodeIDF + resultNodeIDF + pathCapitalLetters + pathLength +  words + timeliness + noise()) / TOTAL;
    //return Math.min(1.0, Math.max(0.0, score));
    //return Math.log(score);
    return score;
  }

  public String toString () {
    return globalEdgeIDF + " " + resultEdgeIDF + " " + globalNodeIDF + " " + resultNodeIDF + " " + pathCapitalLetters + " " + pathLength + " " + words + " " + timeliness + " " + noise();
  }
}

