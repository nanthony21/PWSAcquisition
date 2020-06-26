/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.settings;

import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author nicke
 */
public class TranslationStage1dSettings extends JsonableParam {
    public TranslationStage1d.Types stageType = TranslationStage1d.Types.NikonTI;
    public int speed = 0;
}
