/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import org.micromanager.Studio;
import org.micromanager.display.DataViewer;
import org.micromanager.display.inspector.InspectorPanelController;
import org.micromanager.display.inspector.InspectorPanelPlugin;
import org.micromanager.display.inspector.internal.panels.intensity.ImageStatsPublisher;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 *
 * @author N2-LiveCell
 */
@Plugin(type = InspectorPanelPlugin.class,
    priority = Priority.VERY_HIGH,
    name = "Focus Sharpness",
    description = "View quantitative image sharpness.")
public class SharpnessInspectorPlugin implements InspectorPanelPlugin {
    @Override
    public boolean isApplicableToDataViewer(DataViewer viewer) {
        return viewer.getDataProvider() != null && viewer instanceof ImageStatsPublisher;
    }

    @Override
    public InspectorPanelController createPanelController(Studio studio) {
        return SharpnessInspectorController.create(studio);
    }
}
