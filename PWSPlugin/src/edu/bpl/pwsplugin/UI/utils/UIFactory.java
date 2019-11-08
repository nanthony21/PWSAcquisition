
package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.utils.JsonableParam;
import edu.bpl.pwsplugin.utils.UIBuildable;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class UIFactory {
    public static BuilderJPanel getUI(Class<? extends UIBuildable> clazz) {
        if (clazz.equals(PWSSettings.class)) {
            return new PWSPanel();
        } else if (clazz.equals(DynSettings.class)) {
            return new DynPanel();
        } else if (clazz.equals(SysConfig.class)) {
            return new HWConfPanel();
        } else if (clazz.equals(CamSettings.class)) {
            return new CamPanel();
        } else {
            ReportingUtils.showError("Could not build UI for class: " + clazz);
            return null;
        }
    }
}