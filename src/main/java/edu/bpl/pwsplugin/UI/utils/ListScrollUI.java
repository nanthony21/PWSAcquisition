package edu.bpl.pwsplugin.UI.utils;


import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.ReportingUtils;

;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ListScrollUI<T extends List<S>, S extends JsonableParam> extends
      ListBuilderJPanel<T> implements MouseListener {

   //A UI component that allows the user to flip through multiple UI componenent representing UIBuildable classes.
   JPanel scrollContents = new JPanel(new MigLayout("insets 0 0 0 0"));
   JPopupMenu popupMenu = new JPopupMenu("Popup Menu");
   JMenuItem addItem = new JMenuItem("Add");
   JMenuItem clearItem = new JMenuItem("Clear All");
   private List<BuilderJPanel<S>> components = new ArrayList<>();

   private S[] defaultStepTypes = null;


   public ListScrollUI(Class<T> clazz, S... defaultStepTypes) {
      //super(new MigLayout("insets 0 0 0 0, fill"), clazz);
      super(new BorderLayout(0, 0), clazz);
      this.defaultStepTypes = defaultStepTypes;

      double maxWidth = 0;
      double maxHeight = 0;
      for (S step : defaultStepTypes) {
         BuilderJPanel<S> panel = UIFactory.getUI((Class<? extends JsonableParam>) step.getClass());
         Dimension dim = panel.getPreferredSize();
         if (dim.getHeight() > maxHeight) {
            maxHeight = dim.getHeight();
         }
         if (dim.getWidth() > maxWidth) {
            maxWidth = dim.getWidth();
         }
      }
      this.setPreferredSize(new Dimension((int) maxWidth + 60, (int) (maxHeight * 1.5)));
      //this.setMaximumSize(new Dimension((int) maxWidth+60, (int)(maxHeight*100)));
      this.scrollContents.addMouseListener(this);

      JScrollPane scroll = new JScrollPane(this.scrollContents,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      //super.add(scroll, "span, wrap, grow");
      this.add(scroll);

      popupMenu.add(addItem);
      popupMenu.add(clearItem);

      addItem.addActionListener((evt) -> {
         try {
            this.addStepAction(this.components.size());
         } catch (BuilderPanelException exc) {
            ReportingUtils.showError(exc);
            ReportingUtils.logError(exc);
         }
      });

      clearItem.addActionListener((e) -> {
         try {
            this.clearAllAction();
         } catch (Exception exc) {
            ReportingUtils.showError(exc);
            ReportingUtils.logError(exc);
         }
      });
   }

   @Override
   public void populateFields(T t) throws BuilderPanelException {
      this.scrollContents.removeAll(); //Make sure to remove the old stuff if this is a refresh.
      components = new ArrayList<BuilderJPanel<S>>();
      Integer i = 0;
      for (S s : t) {
         BuilderJPanel<S> p = UIFactory.getUI((Class<? extends JsonableParam>) s.getClass());
         p.populateFields(s);
         ListScrollItem item = new ListScrollItem(p, i, this);
         this.scrollContents.add(item,
               "wrap"); //Must add to layout before populating or we get a null pointer error.
         components.add(p);
         i++;
      }
      if (t.isEmpty()) {
         JLabel l = new JLabel("Empty");
         Font font = new Font("Serif", Font.BOLD, 18);
         l.setFont(font);
         this.scrollContents.add(l, "gapleft push, align center");
      }
      validate();
      repaint();
   }

   @Override
   public T build() throws BuilderPanelException {
      T t;
      try {
         t = this.typeParamClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
         throw new BuilderPanelException(e);
      }
      for (BuilderJPanel<S> p : components) {
         S s = p.build();
         t.add(s);
      }
      return t;
   }


   public void addStepAction(int position) throws BuilderPanelException {
      if (this.defaultStepTypes.length > 1) { //We have multiple subtypes of S that could be added.
         JPopupMenu menu = new JPopupMenu("Menu");
         for (S step : this.defaultStepTypes) {
            JMenuItem item = new JMenuItem(step.getClass().getSimpleName());
            item.addActionListener((evtt) -> {
               try {
                  this.addStep(step, position);
               } catch (BuilderPanelException e) {
                  ReportingUtils.logError(e);
               }
            });
            menu.add(item);
         }
         Point pos = MouseInfo.getPointerInfo().getLocation();
         Point p = this.scrollContents.getLocationOnScreen();
         Point pp = new Point((int) (pos.getX() - p.getX()), (int) (pos.getY() - p.getY()));
         menu.show(this.scrollContents, (int) pp.getX() - 10,
               (int) pp.getY() - 10); //show the menu 0,0 from the add button
      } else {
         this.addStep(this.defaultStepTypes[0]);
      }

   }

   private void addStep(S step) throws BuilderPanelException {
      this.addStep(step, this.components.size());
   }

   public void addStep(S s, int position) throws BuilderPanelException {
      T t = this.build();
      t.add(position, s);
      this.populateFields(t);
   }

   public void removeStep(int position) throws BuilderPanelException {
      T t = this.build();
      t.remove(position);
      this.populateFields(t);
   }

   public void moveStep(int startPos, int endPos) throws BuilderPanelException {
      T t = this.build();
      S step = t.remove(startPos);
      t.add(endPos, step);
      this.populateFields(t);
   }

   public void clearAllAction() throws BuilderPanelException {
      T t = this.build();
      t.clear();
      this.populateFields(t);
   }


   //Mouse listener methods
   @Override
   public void mousePressed(MouseEvent evt) {
      if (evt.getButton() == MouseEvent.BUTTON3) {
         this.popupMenu.show(this.scrollContents, evt.getX(), evt.getY());
      }
   }

   public void mouseClicked(MouseEvent evt) {
   }

   public void mouseExited(MouseEvent evt) {
   }

   public void mouseEntered(MouseEvent evt) {
   }

   public void mouseReleased(MouseEvent evt) {
   }
}

class ListScrollItem<S extends JsonableParam> extends JPanel implements MouseListener {

   BuilderJPanel<S> panel;
   ListScrollUI<?, S> parent;
   int position;
   JPopupMenu menu = new JPopupMenu();
   JMenuItem removeItem = new JMenuItem("Remove");
   JMenuItem insertAfterItem = new JMenuItem("Insert After");
   JMenuItem duplicateItem = new JMenuItem("Duplicate");
   JMenuItem moveUpItem = new JMenuItem("Move Up");
   JMenuItem moveDownItem = new JMenuItem("Move Down");
   JMenuItem clearItem = new JMenuItem("Clear All");

   public ListScrollItem(BuilderJPanel<S> panel, int position, ListScrollUI parent) {
      super(new MigLayout("insets 0 0 0 0"));
      this.addMouseListener(this);
      this.panel = panel;
      this.parent = parent;
      this.position = position;
      JLabel index = new JLabel(String.valueOf(position + 1));
      index.setFont(index.getFont().deriveFont(Font.BOLD, 24));
      index.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.black));
      this.add(index, "growy");
      this.add(panel);
      this.setBorder(BorderFactory.createRaisedSoftBevelBorder());

      this.menu.add(insertAfterItem);
      this.menu.add(removeItem);
      this.menu.add(duplicateItem);
      this.menu.addSeparator();
      if (this.position != 0) {
         this.menu.add(moveUpItem);
      }
      this.menu.add(moveDownItem);
      this.menu.addSeparator();
      this.menu.add(clearItem);

      removeItem.addActionListener((evt) -> {
         try {
            parent.removeStep(this.position);
         } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.showError(evt);
         }
      });

      insertAfterItem.addActionListener((evt) -> {
         try {
            parent.addStepAction(this.position + 1);
         } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.showError(evt);
         }
      });

      duplicateItem.addActionListener((evt) -> {
         try {
            S s = this.panel.build();
            parent.addStep(s, this.position + 1);
         } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.showError(evt);
         }
      });

      moveDownItem.addActionListener((evt) -> {
         try {
            parent.moveStep(this.position, this.position + 1);
         } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.showError(evt);
         }
      });

      moveUpItem.addActionListener((evt) -> {
         try {
            parent.moveStep(this.position, this.position - 1);
         } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.showError(evt);
         }
      });

      clearItem.addActionListener((evt) -> {
         try {
            this.parent.clearAllAction();
         } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.showError(e);
         }
      });
   }

   //Mouse listener methods
   @Override
   public void mousePressed(MouseEvent evt) {
      if (evt.getButton() == MouseEvent.BUTTON3) {
         this.menu.show(this, evt.getX(), evt.getY());
      }
   }

   public void mouseClicked(MouseEvent evt) {
   }

   public void mouseExited(MouseEvent evt) {
   }

   public void mouseEntered(MouseEvent evt) {
   }

   public void mouseReleased(MouseEvent evt) {
   }

}