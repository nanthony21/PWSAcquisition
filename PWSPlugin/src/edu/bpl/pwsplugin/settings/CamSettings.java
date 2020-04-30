/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.List;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class CamSettings extends JsonableParam {
    public String name;
    public Camera.Types camType;
    public List<Double> linearityPolynomial;
    public int darkCounts;
    public double[] affineTransform; //A 2x3 affine transformation matrix specifying how coordinates in one camera translate to coordinates in another camera. For simplicity we store this array as a 1d array of length 6
}
