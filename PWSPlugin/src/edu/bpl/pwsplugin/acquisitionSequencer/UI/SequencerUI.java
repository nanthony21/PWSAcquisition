/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import com.google.gson.JsonIOException;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.RootStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.micromanager.internal.utils.FileDialogs;
import org.micromanager.internal.utils.ReportingUtils;
        

/**
 *
 * @author nick
 */
public class SequencerUI extends BuilderJPanel<RootStep> {
    SequenceTree seqTree = new SequenceTree();
    NewStepsTree newStepsTree = new NewStepsTree();
    SettingsPanel settingsPanel = new SettingsPanel(seqTree, newStepsTree);
    JButton runButton = new JButton("Run");
    JButton saveButton = new JButton("Save");
    JButton loadButton = new JButton("Load");
    private static final FileDialogs.FileType STEPFILETYPE = new FileDialogs.FileType("PWS Acquisition Sequence", "Sequence (.pwsseq)", "newAcqSequence.pwsseq", true, "pwsseq"); // The specification for how to save a Step to a file.
    
    public SequencerUI() {
        super(new MigLayout("fill"), RootStep.class);

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.runButton.addActionListener((evt) -> {  
            try {
                RootStep rootStep = this.build();
                List<String> errors = verifySequence(rootStep);
                if (!errors.isEmpty()) {
                    Globals.mm().logs().showError(String.join("\n", errors));
                    return;
                }
                
                //Validate the hardware state
                List<String> errs = Globals.getHardwareConfiguration().validate();
                if (!errs.isEmpty()) {
                    String msg = String.format("The following errors were detected. Do you want to proceeed with imaging?: %s", String.join("\n", errs));
                    int result = JOptionPane.showConfirmDialog(this, msg, "Errors!", 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
                    null);

                    if (result == JOptionPane.NO_OPTION) {
                        Globals.mm().logs().logMessage("Aborting due to errors.");
                        return;
                    }
                }
                
                boolean success = resolveFileConflicts(rootStep);
                if (!success) { return; }
                SequencerRunningDlg dlg = new SequencerRunningDlg(SwingUtilities.getWindowAncestor(this), "Acquisition Sequence Running", rootStep);
            } catch (BuilderPanelException | RuntimeException e) {
                ReportingUtils.showError(e, this); //This puts the error message over the plugin UI rather than the main Micro-Manager UI
            }
        }); //Run starting at cell 1.
        
        this.saveButton.addActionListener((evt) -> {
            try { 
                Step rootStep = this.build();
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                String path = FileDialogs.save(topFrame, "Save Sequence", STEPFILETYPE).getPath();
                if (path == null) {
                    return; // file dialog must have been cancelled.
                }
                rootStep.saveToJson(path); 
            } catch (IOException | BuilderPanelException e) {
                Globals.mm().logs().showError(e);
            }
        });
        
        this.loadButton.addActionListener((evt) -> {
            try {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                String path = FileDialogs.openFile(topFrame, "Load Sequence", STEPFILETYPE).getPath();
                if (path == null) {
                    return; // file dialog must have been cancelled.
                }
                RootStep rootStep;
                try (FileReader reader = new FileReader(path)) {
                    rootStep = GsonUtils.getGson().fromJson(reader, RootStep.class);
                } catch (IOException ioe) {
                    Globals.mm().logs().showError(ioe);
                    return;
                }
                this.populateFields(rootStep);
            } catch (NullPointerException | JsonIOException | ClassCastException e) {
                Globals.mm().logs().showError(e);
            }
        });
        
        JLabel l = new JLabel("Sequence");
        l.setFont(new Font("serif", Font.BOLD, 12));
        this.add(l, "wrap");
        this.add(seqTree, "growy, pushy, wrap");
        l = new JLabel("Available Step Types");
        l.setFont(new Font("serif", Font.BOLD, 12));
        this.add(l, "wrap");
        this.add(newStepsTree, "growy 20, wrap");
        this.add(settingsPanel, "cell 1 0 1 4");

        this.add(runButton, "cell 0 5 2 1, align center");
        this.add(saveButton, "cell 0 5");
        this.add(loadButton, "cell 0 5");
    }
    
    private boolean resolveFileConflicts(RootStep step) {
        //returns true if it is ok to proceed, false if cancel.
        String dir = step.getSettings().directory;
        List<String> relativePaths = step.getRequiredPaths(); //step.requiredRelativePaths(1);
        List<Path> conflict = new ArrayList<>();
        for (String relPath : relativePaths) {
            File cellFolder = Paths.get(dir).resolve(relPath).toFile();
            if (cellFolder.exists()) {
                conflict.add(Paths.get(dir).relativize(cellFolder.toPath()));
            }
        }
        if (conflict.isEmpty()) {
            return true;
        }
        boolean overwrite = new FileConflictDlg(SwingUtilities.getWindowAncestor(this), dir, conflict).getResult();  //Open a dialog asking to overwrite, if true then go ahead.
        
        boolean success = overwrite;
        if (overwrite) {
            boolean err = false;
            for (Path p : conflict) {
                try {
                    File f = Paths.get(dir).resolve(p).toFile();
                    if (f.isDirectory()) {
                        FileUtils.deleteDirectory(f);
                    } else {
                        err = !f.delete(); // This line returns false if the deletion fails.
                    }
                } catch (IOException e) {
                    Globals.mm().logs().logError(e);
                    err = true;
                }
            }
            if (err) {
                Globals.mm().logs().showMessage("A file exception was detected. Please check the CoreLog for more information.");
                success = false;
            }
        }
        return success;
    }
    
    private List<String> verifySequence(Step parent, List<String> errs) {
        if (parent instanceof ContainerStep) {
            errs.addAll(parent.validate());
            for (Step substep : ((ContainerStep<?>) parent).getSubSteps()) {
                errs.addAll(verifySequence(substep, new ArrayList<>()));
            }
        } 
        return errs;
    }
    
    private List<String> verifySequence(RootStep parent) {
        List<String> errs = new ArrayList<>();
        return verifySequence(parent, errs);
    }
    
    @Override
    public RootStep build() throws BuilderPanelException {
        return (RootStep) seqTree.tree().getModel().getRoot();
    }
    

    @Override
    public void populateFields(RootStep rootStep) {
        ((DefaultTreeModel) seqTree.tree().getModel()).setRoot(rootStep);
    }
    
}

class FileConflictDlg extends JDialog {
    //A dialog that displays all the conflicting files detected and asks for permission to overwrite.
    //Use the getresult method to get a boolean for if it is ok to overwrite the files.
    private boolean result;
    
    public FileConflictDlg(Window owner, String dir, List<Path> conflicts) {
        super(owner);
        this.setTitle("File Conflict!");
        this.setModal(true);
        this.setLocationRelativeTo(this.getOwner());

        JPanel cont = new JPanel(new MigLayout("fill"));
        JTextArea textTop = new JTextArea(String.format("The following files already exist at %s:", dir));
        textTop.setWrapStyleWord(true);
        textTop.setLineWrap(true);
        textTop.setOpaque(false);
        textTop.setEditable(false);
        textTop.setFocusable(false);
        cont.add(textTop, "wrap, growx, span");
        JTextArea text = new JTextArea();
        text.setText(String.join("\n", conflicts.stream().map(Object::toString).collect(Collectors.toList())));
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        text.setOpaque(false);
        text.setEditable(false);
        JScrollPane scroll = new JScrollPane(text);
        cont.add(scroll, "wrap, grow, span, width 15sp, height 15sp");
        JButton okButton = new JButton("Overwrite");
        okButton.addActionListener((evt)->{
            result=true;
            this.setVisible(false);
        });
        cont.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((evt)->{
            result=false;
            this.setVisible(false);
        });
        cont.add(cancelButton);
        this.setContentPane(cont);
        this.pack();
        this.setMinimumSize(this.getSize());
    }
    
    public boolean getResult() {
        //Make visible which will block until the user performs an action that hides the dialog, then return the result.
        this.setVisible(true);
        return this.result;
    }
}