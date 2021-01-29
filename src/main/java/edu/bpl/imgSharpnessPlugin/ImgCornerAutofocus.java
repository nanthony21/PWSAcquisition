/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import java.awt.Rectangle;

/**
 *
 * @author nick
 */
public class ImgCornerAutofocus {
   
}


/**
 * Provides ROIs at the 4 corners of the image based on a specified distance from
 * the image corners that the ROIS should extend.
 * 
 * @author nick
 */
class RoiManager {
   private double radius_ = 95;
   private final int w_;
   private final int h_;
   
   public enum Corners {
      TL,
      TR,
      BL,
      BR;
   }
   
   public RoiManager(int imgWidth, int imgHeight) {
      w_ = imgWidth;
      h_ = imgHeight;
   }
   
   public void setRadiusPercentage(double percentage) {
      radius_ = percentage;
   }
   
   public Rectangle getCornerRoi(Corners corner) {
      
   }
}