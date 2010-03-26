package com.sd_editions.collatex.Collate;

public class Tuple implements Comparable<Tuple> {
  public int baseIndex;
  public int witnessIndex;
  private boolean isTransposTupel;

  public Tuple(int baseIndex1, int witnessIndex1) {
    this.baseIndex = baseIndex1;
    this.witnessIndex = witnessIndex1;
    this.isTransposTupel = false;
  }

  public boolean isTransposTupel() {
    return isTransposTupel;
  }

  public void markAsTransposTupel() {
    this.isTransposTupel = true;
  }

  public int getBaseIndex() {
    return baseIndex;
  }

  public int getWitnessIndex() {
    return witnessIndex;
  }

  public int compareTo(Tuple tmp) {
    int newer = Math.abs(tmp.baseIndex - tmp.witnessIndex);
    int me = Math.abs(this.baseIndex - this.witnessIndex);
    if (me < newer) {
      return -1;
    } else if (me > newer) {
      return 1;
    } else if (me == newer) {
      int newer2 = tmp.witnessIndex;
      int me2 = this.witnessIndex;
      if (me2 == newer2) {
        return 0;
      } else if (me2 < newer2) {
        return -1;
      } else if (me2 > newer2) {
        return 1;

      }
    }
    return 0;
  }

  @Override
  public String toString() {
    return "[" + baseIndex + "," + witnessIndex + "]";
  }
}
