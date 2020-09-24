/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EnterSubfolderStep;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class EnterSubfolderFactory extends StepFactory {
     @Override
    public Class<? extends BuilderJPanel> getUI() {
        return EnterSubfolderUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.EnterSubfolderSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return EnterSubfolderStep.class;
    }
    
    @Override
    public String getDescription() {
        return "Change the folder that enclosed acquisitions are saved to.";
    }
    
    @Override
    public String getName() {
        return "Enter Subfolder";
    }
    
    @Override
    public SequencerConsts.Category getCategory() {
        return SequencerConsts.Category.UTIL;
    }

    @Override
    public SequencerConsts.Type getType() {
        return SequencerConsts.Type.SUBFOLDER;
    }
}



class EnterSubfolderUI extends BuilderJPanel<SequencerSettings.EnterSubfolderSettings> {
    private final JTextField relPath = new JTextField();
    
    public EnterSubfolderUI() {
        super(new MigLayout("insets 0 0 0 0"), SequencerSettings.EnterSubfolderSettings.class);
        
        ((AbstractDocument) relPath.getDocument()).setDocumentFilter(new MyDocumentFilter()); //Disallow certain characters.
        
        this.add(new JLabel("Subfolder:"));
        this.add(relPath, "wrap, pushx, growx");
    }
    
    public SequencerSettings.EnterSubfolderSettings build() {
        String p = this.relPath.getText();
        File f = new File(p);
        SequencerSettings.EnterSubfolderSettings settings = new SequencerSettings.EnterSubfolderSettings();
        settings.relativePath = p;
        return settings;
    }
    
    @Override
    public void populateFields(SequencerSettings.EnterSubfolderSettings settings) {
        this.relPath.setText(settings.relativePath.toString());
    }
}

class MyDocumentFilter extends DocumentFilter {
    private static final String disallowedChars = ".,\\';`";
    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset,
            String text, AttributeSet attr) throws BadLocationException {
        StringBuilder buffer = new StringBuilder(text.length());
        for (int i=0; i<text.length(); i++) {
            char ch = text.charAt(i);
            if (disallowedChars.indexOf(ch) == -1) { //Don't allow disallowed characters.
                buffer.append(ch);
            }
        }
        super.insertString(fb, offset, buffer.toString(), attr);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb,
            int offset, int length, String string, AttributeSet attr) throws BadLocationException {
        if (length > 0) {
            fb.remove(offset, length);
        }
        insertString(fb, offset, string, attr);
    }
}