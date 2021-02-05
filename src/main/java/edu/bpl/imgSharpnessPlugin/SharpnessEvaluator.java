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
package edu.bpl.imgSharpnessPlugin;

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.derivative.DerivativeType;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.misc.PixelMath;
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayF32;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.micromanager.autofocus.internal.oughtafocus.ImgSharpnessAnalysis;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

/**
 *
 * @author Nick Anthony
 */
public class SharpnessEvaluator {
    public int denoiseRadius = 3;   
    private final ImgSharpnessAnalysis anl = new ImgSharpnessAnalysis();
    public static enum Method {
      Gradient,
      
      Edges, StdDev, Mean, //Values from the ImgSharpnessAnalysis.Method enum
      NormalizedVariance, SharpEdges, Redondo, Volath, Volath5, 
      MedianEdges, Tenengrad, FFTBandpass;
    }
    private Method method_ = Method.Gradient;
    
    public void setMethod(Method method) {
        if (method == Method.Gradient) {
            method_ = method;
        } else {
            method_ = method;
            anl.setComputationMethod(ImgSharpnessAnalysis.Method.valueOf(method.name()));
        }
    }
    
    public Method getMethod() {
       return method_;
    }
    
    public double evaluate(Image img, Rectangle r) {
       if (method_ == Method.Gradient) {
          return evaluateGradient(img, r);
       }
       else {
          ImageProcessor proc = MMStudio.getInstance().data().getImageJConverter().createProcessor(img);
          proc.setRoi(r);
          proc = proc.crop();
          return anl.compute(proc);
       }
    }
    
    private double evaluateGradient(Image img, Rectangle r) {
        GrayF32 im = new GrayF32(r.width, r.height);
        for (int i=0; i<r.width; i++) {
            for (int j=0; j<r.height; j++) {
                long intensity = img.getIntensityAt(r.x + i, r.y + j);
                im.set(i, j, (int) intensity);
            }
        }
        GrayF32 blurred = BlurImageOps.gaussian(im, null, -1, this.denoiseRadius, null);
        PixelMath.divide(blurred, ImageStatistics.mean(blurred), blurred); //Normalize?
        GrayF32 dx = new GrayF32(im.width, im.height);
        GrayF32 dy = new GrayF32(im.width, im.height);
        GImageDerivativeOps.gradient(DerivativeType.THREE, blurred, dx, dy, BorderType.EXTENDED);
        //Calculate magnitude of the gradient
        PixelMath.pow2(dx, dx);
        PixelMath.pow2(dy, dy);
        GrayF32 mag = new GrayF32(dx.width, dx.height);
        PixelMath.add(dx, dy, mag);
        PixelMath.sqrt(mag, mag);
        float[] arr = mag.getData();
        double[] dubArr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) { // must convert from float[] to double[]
            dubArr[i] = arr[i];
        }
        return new Percentile().evaluate(dubArr, 95);
    }
}
