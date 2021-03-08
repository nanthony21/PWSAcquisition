///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsAF;
//import org.micromanager.autofocus.internal.oughtafocus.ImgSharpnessAnalysis;
import org.micromanager.imageprocessing.ImgSharpnessAnalysis
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

/**
 *
 * @author nick
 */
public class ImgCornerAutofocus {
    private final org.micromanager.autofocus.internal.oughtafocus.ImgSharpnessAnalysis evaluator_ = new ImgSharpnessAnalysis();
    private final Map<Corners, Rectangle> rois_ = new HashMap<>();
    
    
    public ImgCornerAutofocus(int imgWidth, int imgHeight, double radius) {
        RoiManager manager = new RoiManager(imgWidth, imgHeight, radius);
        evaluator_.setComputationMethod(ImgSharpnessAnalysis.Method.Redondo);
        for (Corners corner : Corners.values()) {
            rois_.put(corner, manager.getCornerRoi(corner));
        }
    }
    
    /*
    public Map<Corners, Double> evaluateGradient(Image img) {

        for (Map.Entry<Corners, Rectangle> entry : rois_.entrySet()) {
            proc.setRoi(entry.getValue());
            ImageProcessor subProc = proc.crop();
        }        
        Map<Corners, Double> sharpMap = new HashMap<>();
        for (Map.Entry<Corners, Rectangle> entry : rois_.entrySet()) {
            double sharpness = evaluator_.evaluate(img, entry.getValue());
            sharpMap.put(entry.getKey(), sharpness);
        }
        return sharpMap;
    } 
    */
    
    public double fullFocus() {
        return 0;
    }
    
    public void setOverlayVisible(boolean visible) {
        
    }
}


/**
 * Provides ROIs at the 4 corners of the image based on a specified distance from
 * the image corners that the ROIS should extend.
 * 
 * @author Nick Anthony
 */
class RoiManager {
    private double radius_ = 95;  // The percentage radius (from center of image to corner of image to draw the corner of the ROI rectangles
    private final int w_;
    private final int h_;
    private final double sqrt2 = 1.41421356237;

    public RoiManager(int imgWidth, int imgHeight, double radius) {
        w_ = imgWidth;
        h_ = imgHeight;
        radius_ = radius;
    }

    public void setRadiusPercentage(double percentage) {
        radius_ = percentage;
    }

    public Rectangle getCornerRoi(Corners corner) { //TODO test, test with rectangle cameras.
        Rectangle rect = new Rectangle();
        int sideLength = Math.max(w_/2, h_/2); //The whole `radius` idea relies on having the width and height of our image be equal. Since that may not be true, we just use the largest of the two dimensions.
        double radiusPixels = sqrt2 * sideLength;
        double offsetPixels = (1.0 - (radius_ / 100)) * radiusPixels;
        double offsetPxSide = offsetPixels / sqrt2;
        
        int xOffset = (int) Math.round(Math.max(w_/2 - sideLength + offsetPxSide, 0));
        int yOffset = (int) Math.round(Math.max(h_/2 - sideLength + offsetPxSide, 0));

        switch (corner) {
            case TL:
                rect.add(0, h_);
                rect.add(xOffset, h_ - yOffset);
                break;
            case TR:
                rect.add(w_, h_);
                rect.add(w_ - xOffset, h_ - yOffset);
                break;
            case BR:
                rect.add(w_, 0);
                rect.add(w_ - xOffset, yOffset);
                break;
            case BL:
                rect.add(0, 0);
                rect.add(xOffset, yOffset);
                break;
        }
        return rect;
    }
}


class CornerCombiner {
    private final Map<Corners, Rectangle> rois_ = new HashMap<>();
    private final int sumWidth_;
    private final int sumHeight_;
        
    public CornerCombiner(RoiManager manager) {
        for (Corners corner : Corners.values()) {
            rois_.put(corner, manager.getCornerRoi(corner));
        } 
        sumWidth_ = rois_.get(Corners.TL).width + rois_.get(Corners.TR).width;
        sumHeight_ = rois_.get(Corners.TL).height + rois_.get(Corners.BL).height;
    }
    
    public ImageProcessor process(Image img) {
        //Combine the 4 corners into a single image
        ImageProcessor proc = MMStudio.getInstance().data().getImageJConverter().createProcessor(img);
        ImageProcessor ipComposite = new ShortProcessor(sumWidth_, sumHeight_);

        for (Map.Entry<Corners, Rectangle> entry : rois_.entrySet()) {
            proc.setRoi(entry.getValue());
            ImageProcessor subIm = proc.crop();
            
            int xOffset;
            int yOffset;
            switch (entry.getKey()) {
                case TL:
                    xOffset = 0;
                    yOffset = 0;
                    break;
                case TR:
                    xOffset = (int) Math.round(rois_.get(Corners.TL).getWidth());
                    yOffset = 0;
                    break;
                case BL:
                    xOffset = 0;
                    yOffset = (int) Math.round(rois_.get(Corners.TL).getHeight());
                    break;
                case BR:
                    xOffset = (int) Math.round(rois_.get(Corners.TL).getWidth());
                    yOffset = (int) Math.round(rois_.get(Corners.TL).getHeight());
                    break;
                default:
                    throw new RuntimeException("Programming Error!!!!");
            }
            copyTo(subIm, ipComposite, xOffset, yOffset);
        }
        return ipComposite;
    }
    
    private static void copyTo(ImageProcessor from, ImageProcessor to, int xOffset, int yOffset) {
        for (int i=0; i<from.getWidth(); i++) {
            for (int j=0; j<from.getHeight(); j++) {
                to.putPixelValue(i+xOffset, j+yOffset, from.getPixelValue(i, j));
            }
        }
    }
}

enum Corners {
    TL,
    TR,
    BL,
    BR;
}