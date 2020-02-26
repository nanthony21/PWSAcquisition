
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author nick
 */
public class CamUI extends SingleBuilderJPanel<PWSPluginSettings.HWConfiguration.CamSettings>{
    private JComboBox<String> camCombo = new JComboBox<>();
    private JSpinner darkCountsSpinner;
    private DoubleListTextField linEdit = new DoubleListTextField();
    private JComboBox<Camera.Types> camType = new JComboBox<>();
    
    public CamUI() {
        super(new MigLayout(), PWSPluginSettings.HWConfiguration.CamSettings.class);
        

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100000, 1);
        this.darkCountsSpinner = new JSpinner(model);
        ((JSpinner.DefaultEditor)this.darkCountsSpinner.getEditor()).getTextField().setColumns(4);
        
        darkCountsSpinner.setToolTipText("# of counts per pixel when the camera is not exposed to any light. E.g if measuring dark counts with 2x2 binning the number here should be 1/4 of your measurement 2x2 binning pools 4 pixels.");
        linEdit.setToolTipText("Comma separated values representing the polynomial to linearize the counts from the camera. In the form \"A,B,C\" = Ax + Bx^2 + Cx^3. Type \"None\" or \"null\" if correction is not needed.");

        
        this.linEdit.textField.setColumns(10);
        
        this.camType.setModel(new DefaultComboBoxModel<>(Camera.Types.values()));

        super.add(new JLabel("Camera:"), "gapleft push");
        super.add(camCombo, "wrap");
        super.add(new JLabel("Type:"), "gapleft push");
        super.add(camType, "wrap");
        super.add(new JLabel("Dark Counts:"), "gapleft push");
        super.add(darkCountsSpinner, "wrap");
        super.add(new JLabel("Linearity Polynomial:"), "gapleft push");
        super.add(linEdit, "wrap");

        this.updateComboBoxes();
    }
    
    private void updateComboBoxes() {
        this.camCombo.setModel(new DefaultComboBoxModel<String>(new Vector<String>(Globals.getMMConfigAdapter().getConnectedCameras())));
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", camCombo);
        map.put("camType", camType);
        map.put("linearityPolynomial", linEdit);
        map.put("darkCounts", darkCountsSpinner);
        return map;
    }
}

class DoubleListTextField extends BuilderJPanel<List<Double>> {
    public JTextField textField = new JTextField();
    
    public DoubleListTextField() {
        super(new MigLayout("insets 0 0 0 0"), (Class<List<Double>>)(Object) ArrayList.class);
        this.textField.setInputVerifier(new CSVInputVerifier());
        this.add(this.textField);
    }
    
    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        this.textField.setToolTipText(text);
    }
    
    @Override
    public void populateFields(List<Double> linList) {
        if (linList==null) {
            this.textField.setText("null");
            return;
        }
        if (linList.size() > 0) {
             this.textField.setText(StringUtils.join(linList.toArray(), ","));
        } else {
             this.textField.setText("null");
        }
    }
    
    @Override
    public List<Double> build() {
        String text = this.textField.getText().trim();
        List<Double> linearityPolynomial;
        if ((text.equals("None")) || (text.equals("null")) || text.equals("")) {
            linearityPolynomial = new ArrayList<>();
        } else {
            linearityPolynomial = Arrays.asList(text.split(","))
                .stream()
                .map(String::trim)
                .mapToDouble(Double::parseDouble).boxed()
                .collect(Collectors.toList());
        }                           
        return linearityPolynomial;
    }
    
    class CSVInputVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
        String text = ((JTextField) input).getText().trim();
        try {
            Arrays.asList(text.split(","))
                                .stream()
                                .map(String::trim)
                                .mapToDouble(Double::parseDouble); 
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
}