/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;

/**
 *
 * @author N2-LiveCell
 */
public abstract class ImagingConfiguration {
    public abstract boolean hasTunableFilter();
    public abstract Camera camera();
    public abstract TunableFilter tunableFilter();
}
