/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.subpages;

import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.settings.Settings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class CamUI extends SingleBuilderJPanel<Settings.CamSettings>{
    private JComboBox camCombo = new JComboBox();
    private JSpinner darkCountsSpinner = new JSpinner();
    private JTextField linEdit = new JTextField();
    private JCheckBox hasTFCheckbox = new JCheckBox("Uses Tunable Filter:");
    private JComboBox tunableFilterCombo = new JComboBox();
    
    public CamUI() {
        super(new MigLayout(), Settings.CamSettings.class);
        
        darkCountsSpinner.setToolTipText("# of counts per pixel when the camera is not exposed to any light. E.g if measuring dark counts with 2x2 binning the number here should be 1/4 of your measurement 2x2 binning pools 4 pixels.");
        linEdit.setToolTipText("Comma separated values representing the polynomial to linearize the counts from the camera. In the form \"A,B,C\" = Ax + Bx^2 + Cx^3. Type \"None\" or \"null\" if correction is not needed.");

        
        super.add(new JLabel("Camera:"));
        super.add(camCombo, "wrap");
        super.add(new JLabel("Dark Counts:"));
        super.add(darkCountsSpinner, "wrap");
        super.add(new JLabel("Linearity Polynomial:"));
        super.add(linEdit, "wrap");
        super.add(hasTFCheckbox);
        super.add(tunableFilterCombo);
    }
    
    @Override
    public Map<String, JComponent> getPropertyFieldMap() {
        Map<String, JComponent> map = new HashMap<String, JComponent>();
        map.put("name", camCombo);
        map.put("linearityPolynomial", linEdit);
        map.put("darkCounts", darkCountsSpinner);
        map.put("hasTunableFilter", hasTFCheckbox);
        map.put("tunableFilterName", tunableFilterCombo);
        return map;
    }
}
