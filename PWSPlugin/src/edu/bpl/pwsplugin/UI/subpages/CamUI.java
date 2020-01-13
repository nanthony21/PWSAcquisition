/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.subpages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class CamUI extends SingleBuilderJPanel<PWSPluginSettings.HWConfiguration.CamSettings>{
    private JComboBox camCombo = new JComboBox();
    private JSpinner darkCountsSpinner;
    private JTextField linEdit = new JTextField();
    private JCheckBox hasTFCheckbox = new JCheckBox("Uses Tunable Filter:");
    private JComboBox tunableFilterCombo = new JComboBox();
    
    public CamUI() {
        super(new MigLayout(), PWSPluginSettings.HWConfiguration.CamSettings.class);
        

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100000, 1);
        this.darkCountsSpinner = new JSpinner(model);
        ((JSpinner.DefaultEditor)this.darkCountsSpinner.getEditor()).getTextField().setColumns(4);
        
        darkCountsSpinner.setToolTipText("# of counts per pixel when the camera is not exposed to any light. E.g if measuring dark counts with 2x2 binning the number here should be 1/4 of your measurement 2x2 binning pools 4 pixels.");
        linEdit.setToolTipText("Comma separated values representing the polynomial to linearize the counts from the camera. In the form \"A,B,C\" = Ax + Bx^2 + Cx^3. Type \"None\" or \"null\" if correction is not needed.");

        
        this.hasTFCheckbox.setHorizontalTextPosition(SwingConstants.LEFT); //move labelto the left of the button.
        this.linEdit.setColumns(10);
        
        this.hasTFCheckbox.addActionListener((evt) -> {
            this.tunableFilterCombo.setEnabled(this.hasTFCheckbox.isSelected());
        });
        this.hasTFCheckbox.setSelected(true); //This triggers the action listener. TODO this doesn't work.
        
        super.add(new JLabel("Camera:"), "gapleft push");
        super.add(camCombo, "wrap");
        super.add(new JLabel("Dark Counts:"), "gapleft push");
        super.add(darkCountsSpinner, "wrap");
        super.add(new JLabel("Linearity Polynomial:"), "gapleft push");
        super.add(linEdit, "wrap");
        super.add(hasTFCheckbox);
        super.add(tunableFilterCombo);
        
        this.updateComboBoxes();
    }
    
    private void updateComboBoxes() {
        this.camCombo.setModel(new DefaultComboBoxModel<String>(new Vector<String>(Globals.getMMConfigAdapter().getConnectedCameras())));
        this.tunableFilterCombo.setModel(new DefaultComboBoxModel(TunableFilter.Types.values()));
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", camCombo);
        map.put("linearityPolynomial", linEdit);
        map.put("darkCounts", darkCountsSpinner);
        map.put("hasTunableFilter", hasTFCheckbox);
        map.put("tunableFilterName", tunableFilterCombo);
        return map;
    }
}
