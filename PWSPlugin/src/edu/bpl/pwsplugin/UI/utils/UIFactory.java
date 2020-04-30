
package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.UI.settings.CamUI;
import edu.bpl.pwsplugin.UI.settings.ImagingConfigUI;
import edu.bpl.pwsplugin.UI.settings.TunableFilterUI;
import edu.bpl.pwsplugin.UI.subpages.DynPanel;
import edu.bpl.pwsplugin.UI.subpages.FluorPanel;
import edu.bpl.pwsplugin.UI.subpages.HWConfPanel;
import edu.bpl.pwsplugin.UI.subpages.PWSPanel;
import edu.bpl.pwsplugin.settings.CamSettings;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import edu.bpl.pwsplugin.settings.TunableFilterSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class UIFactory {
    public static BuilderJPanel getUI(Class<?> clazz) {
        if (clazz.equals(PWSSettings.class)) {
            return new PWSPanel();
        } else if (clazz.equals(DynSettings.class)) {
            return new DynPanel();
        } else if (clazz.equals(HWConfigurationSettings.class)) {
            return new HWConfPanel();
        } else if (clazz.equals(CamSettings.class)) {
            return new CamUI();
        } else if (clazz.equals(TunableFilterSettings.class)) {
            return new TunableFilterUI();
        } else if (clazz.equals(ImagingConfigurationSettings.class)) {
            return new ImagingConfigUI();
        } else if (clazz.equals(FluorSettings.class)) {
            return new FluorPanel();
        } else {
            ReportingUtils.showError("Could not build UI for class: " + clazz);
            return null;
        }
    }
}