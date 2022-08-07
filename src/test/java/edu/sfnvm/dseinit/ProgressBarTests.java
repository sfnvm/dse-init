package edu.sfnvm.dseinit;

import me.tongfei.progressbar.ProgressBar;
import org.junit.jupiter.api.Test;

public class ProgressBarTests {
  @Test
  void progressBarTest() {
    // try-with-resource block
    try (ProgressBar pb = new ProgressBar("Test", 100)) { // name, initial max
      // Use ProgressBar("Test", 100, ProgressBarStyle.ASCII) if you want ASCII output style
      for (int n = 0; n <= 100; n++) {
        pb.step(); // step by 1
        Thread.sleep(1000);
        // pb.stepBy(n); // step by n
        // pb.stepTo(n); // step directly to n
        // pb.maxHint(n);
        // reset the max of this progress bar as n. This may be useful when the program
        // gets new information about the current progress.
        // Can set n to be less than zero: this means that this progress bar would become
        // indefinite: the max would be unknown.
        pb.setExtraMessage("Reading..."); // Set extra message to display at the end of the bar
      }
    } // progress bar stops automatically after completion of try-with-resource block
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
