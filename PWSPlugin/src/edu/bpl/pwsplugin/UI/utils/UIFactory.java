
package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.UI.subpages.CamUI;
import edu.bpl.pwsplugin.UI.subpages.DynPanel;
import edu.bpl.pwsplugin.UI.subpages.FluorPanel;
import edu.bpl.pwsplugin.UI.subpages.HWConfPanel;
import edu.bpl.pwsplugin.UI.subpages.PWSPanel;
import edu.bpl.pwsplugin.settings.Settings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import edu.bpl.pwsplugin.utils.UIBuildable;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class UIFactory {
    public static BuilderJPanel getUI(Class<? extends UIBuildable> clazz) {
        if (clazz.equals(Settings.PWSSettings.class)) {
            return new PWSPanel();
        } else if (clazz.equals(Settings.DynSettings.class)) {
            return new DynPanel();
        } else if (clazz.equals(Settings.HWConfiguration.class)) {
            return new HWConfPanel();
        } else if (clazz.equals(Settings.CamSettings.class)) {
            return new CamUI();
        } else if (clazz.equals(Settings.FluorSettings.class)) {
            return new FluorPanel();
        } else {
            ReportingUtils.showError("Could not build UI for class: " + clazz);
            return null;
        }
    }
}