/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.derivative.DerivativeType;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.PixelMath;
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayF32;
import java.awt.Rectangle;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.micromanager.data.Image;

/**
 *
 * @author nicke
 */
public class SharpnessEvaluator {
    public int denoiseRadius = 3;    
    
    public double evaluateGradient(Image img, Rectangle r) {
        GrayF32 im = new GrayF32(r.width, r.height);
        for (int i=0; i<r.width; i++) {
            for (int j=0; j<r.height; j++) {
                long intensity = img.getIntensityAt(r.x + i, r.y + j);
                im.set(i, j, (int) intensity);
            }
        }
        GrayF32 blurred = BlurImageOps.gaussian(im, null, -1, this.denoiseRadius, null);
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
