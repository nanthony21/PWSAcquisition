/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import java.util.ArrayList;
import java.util.List;
import org.micromanager.AutofocusPlugin;

/**
 *
 * @author nick
 */
public class SoftwareAutofocus extends EndpointStep<SequencerSettings.SoftwareAutoFocusSettings> {
    
    public SoftwareAutofocus() {
        super(new SequencerSettings.SoftwareAutoFocusSettings(), SequencerConsts.Type.AF);
    }

    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
        SequencerSettings.SoftwareAutoFocusSettings settings = this.settings;
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                AutofocusPlugin af = initializeAFPlugin(settings.exposureMs);
                double z = af.fullFocus();
                double score = af.getCurrentFocusScore();
                status.newStatusMessage(String.format("Autofocus terminated at Z=%.2f with score=%.2f", z, score));
                return status;
            }
        };
    }

    @Override
    protected SimFn getSimulatedFunction() {
        return (Step.SimulatedStatus status) -> {
            return status;
        };
    }

        
    @Override
    public List<String> validate() {
        return new ArrayList<>(); // I can't think of anything to validate here.
    }
    
    private AutofocusPlugin initializeAFPlugin(double exposureMs) throws Exception {
        Globals.mm().getAutofocusManager().setAutofocusMethodByName("OughtaFocus");
        AutofocusPlugin of = Globals.mm().getAutofocusManager().getAutofocusMethod();
        //TODO these are the default values, we need to find better ones.
        //https://micro-manager.org/wiki/Autofocus_manual
        of.setPropertyValue("SearchRange_um", "10"); //Will search half of this range on either side of the current position.
        of.setPropertyValue("Tolerance_um", "1");
        of.setPropertyValue("CropFactor", "1"); //Between 0 and 1. A value of 1 here indicates that the entirety of the current ROI will be used. By default the current ROI is the whole FOV.
        of.setPropertyValue("Exposure", String.valueOf(exposureMs)); //Use the current exposure.
        of.setPropertyValue("ShowImages", "Yes");  //{"Yes", "No"};
        of.setPropertyValue("Maximize", "Volath5");    //Values I think we should try: {"Volath5", "Redondo", "StdDev"}, Possible values: {"Edges", "StdDev", "Mean",  "NormalizedVariance", "SharpEdges", "Redondo", "Volath", "Volath5",  "MedianEdges", "Tenengrad", "FFTBandpass"};
        /*of.setPropertyValue("FFTUpperCutoff(%)", "14"); //These FFT properties are only used of the "Maximize" property is set to "FFTBandpass"
        of.setPropertyValue("FFTLowerCutoff(%)", "2.5");*/
        of.setPropertyValue("Channel", ""); //Do not switch to a specific channel for this.
        return of;
    }    
}

