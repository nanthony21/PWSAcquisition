package edu.bpl.pwsplugin.acquisitionsequencer.utility;

import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step.SimulatedStatus;
import java.nio.file.Paths;

public class SubfolderHelper {
   public static class Runtime implements AutoCloseable {
      private final String origPath_;
      private final int origCellNum_;
      private final AcquisitionStatus status_;

      public Runtime(AcquisitionStatus status, String folderName, int startingCellNum) {
         status_ = status;
         origPath_ = status.getSavePath();
         origCellNum_ = status.getAcquisitionNum();
         status.setSavePath(Paths.get(origPath_).resolve(folderName).toString());
         status.setAcquisitionNum(startingCellNum);
      }

      /**
       * Exit back out of the subfolder.
       */
      @Override
      public void close() {
         status_.setSavePath(origPath_);
         status_.setAcquisitionNum(origCellNum_);
      }

      public int getCurrentCellNumber() {
         return status_.getAcquisitionNum();
      }
   }

   public static class Simulated implements AutoCloseable {
      private final int origCellNum_;
      private final String origPath_;
      private final SimulatedStatus status_;

      public Simulated(SimulatedStatus status, String folderName, int startingCellNum) {
         origCellNum_ = status.cellNum;
         origPath_ = status.workingDirectory;
         status.cellNum =
               startingCellNum; // Even if we exit and enter this subfolder multiple times we should still remember which cell num we're on.
         status.workingDirectory = Paths.get(status.workingDirectory, folderName).toString();
         status_ = status;
      }

      public int getCurrentCellNum() {
         return status_.cellNum;
      }

      @Override
      public void close() {
         status_.workingDirectory = origPath_;
         status_.cellNum = origCellNum_;
      }
   }
}
