package edu.bpl.pwsplugin.acquisitionsequencer.UI.components;

import java.awt.Window;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

public class FileConflictDlg extends JDialog {

   //A dialog that displays all the conflicting files detected and asks for permission to overwrite.
   //Use the getresult method to get a boolean for if it is ok to overwrite the files.
   private boolean result;

   public FileConflictDlg(Window owner, String dir, List<Path> conflicts) {
      super(owner);
      this.setTitle("File Conflict!");
      this.setModal(true);
      this.setLocationRelativeTo(this.getOwner());

      JPanel cont = new JPanel(new MigLayout("fill"));
      JTextArea textTop = new JTextArea(
            String.format("The following files already exist at %s:", dir));
      textTop.setWrapStyleWord(true);
      textTop.setLineWrap(true);
      textTop.setOpaque(false);
      textTop.setEditable(false);
      textTop.setFocusable(false);
      cont.add(textTop, "wrap, growx, span");
      JTextArea text = new JTextArea();
      text.setText(String.join("\n",
            conflicts.stream().map(Object::toString).collect(Collectors.toList())));
      text.setWrapStyleWord(true);
      text.setLineWrap(true);
      text.setOpaque(false);
      text.setEditable(false);
      JScrollPane scroll = new JScrollPane(text);
      cont.add(scroll, "wrap, grow, span, width 15sp, height 15sp");
      JButton okButton = new JButton("Overwrite");
      okButton.addActionListener((evt) -> {
         result = true;
         this.setVisible(false);
      });
      cont.add(okButton);
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener((evt) -> {
         result = false;
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
