
package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.utils.UIBuildable;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class UIFactory {
    public static BuilderJPanel getUI(Class<? extends UIBuildable> clazz) {
        if (clazz.equals(StepSequence.class)) {
            return new StepSequenceUI.Editable();
        } else if (clazz.equals(HybridizationSequence.class)) {
            return new ListCardUI(clazz, "Select Hybridization:");
        } else {
            ReportingUtils.showError("Could not build UI for class: " + clazz);
            return null;
        }
    }
}