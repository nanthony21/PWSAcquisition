
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.awt.Color;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author nick
 */
public class CamUI extends BuilderJPanel<CamSettings>{
    private final JComboBox<String> camCombo = new JComboBox<>();
    private final JSpinner darkCountsSpinner;
    private final DoubleListTextField linEdit = new DoubleListTextField();
    private final JComboBox<Camera.Types> camType = new JComboBox<>();
    
    public CamUI() {
        super(new MigLayout(), CamSettings.class);
        

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100000, 1);
        this.darkCountsSpinner = new JSpinner(model);
        ((JSpinner.DefaultEditor)this.darkCountsSpinner.getEditor()).getTextField().setColumns(4);
        
        darkCountsSpinner.setToolTipText("# of counts per pixel when the camera is not exposed to any light. E.g if measuring dark counts with 2x2 binning the number here should be 1/4 of your measurement 2x2 binning pools 4 pixels.");
        linEdit.setToolTipText("Comma separated values representing the polynomial to linearize the counts from the camera. In the form \"A,B,C\" = Ax + Bx^2 + Cx^3. Type \"None\" or \"null\" if correction is not needed.");

        
        this.linEdit.textField.setColumns(10);
        
        this.camType.setModel(new DefaultComboBoxModel<>(Camera.Types.values()));

        super.add(new JLabel("Device Name:"), "gapleft push");
        super.add(camCombo, "wrap");
        super.add(new JLabel("Camera Type:"), "gapleft push");
        super.add(camType, "wrap");
        super.add(new JLabel("Dark Counts:"), "gapleft push");
        super.add(darkCountsSpinner, "wrap");
        super.add(new JLabel("Linearity Polynomial:"), "gapleft push");
        super.add(linEdit, "wrap");

        this.updateComboBoxes();
    }
    
    private void updateComboBoxes() {
        this.camCombo.setModel(new DefaultComboBoxModel<>(new Vector<String>(Globals.getMMConfigAdapter().getConnectedCameras())));
    }
    
    @Override
    public void populateFields(CamSettings settings) {
        camCombo.setSelectedItem(settings.name);
        camType.setSelectedItem(settings.camType);
        darkCountsSpinner.setValue(settings.darkCounts);
        linEdit.populateFields(settings.linearityPolynomial);
    }
    
    @Override
    public CamSettings build() throws BuilderPanelException {
        CamSettings settings = new CamSettings();
        settings.name = (String) camCombo.getSelectedItem();
        settings.camType = (Camera.Types) camType.getSelectedItem();
        settings.darkCounts = (Integer) darkCountsSpinner.getValue();
        settings.linearityPolynomial = linEdit.build(); //This can throw an exception.
        return settings;
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
    public List<Double> build() throws BuilderPanelException {
        if (!this.textField.getInputVerifier().verify(this.textField)) {
            String msg = String.format("Linearity polynomial input of \"%s\" is not valid.", this.textField.getText());
            throw new BuilderPanelException(msg);
        }
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
        boolean status = verifyInnerFunc(input);
        if (status) {
            DoubleListTextField.this.textField.setBackground(DoubleListTextField.this.getBackground());
        } else {
            DoubleListTextField.this.textField.setBackground(Color.red);
        }
        return status;
    }
    
    private boolean verifyInnerFunc(JComponent input) {
        String text = ((JTextField) input).getText().trim();
        if ((text.equals("None")) || (text.equals("null")) || text.equals("")) {
            return true;
        } else {
            try {
                Arrays.asList(text.split(","))
                                    .stream()
                                    .map(String::trim)
                                    .mapToDouble(Double::parseDouble).boxed()
                                    .collect(Collectors.toList()); 
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
    }
}
}