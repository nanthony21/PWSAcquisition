///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2019
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//

package edu.bpl.pwsplugin;

import java.io.IOException;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.DatastoreRewriteException;
import org.micromanager.data.Image;
import org.micromanager.display.DisplayWindow;
import org.micromanager.internal.utils.ReportingUtils;
import javax.swing.SwingUtilities;
import org.micromanager.data.RewritableDatastore;

public class PWSAlbum { //TODO default to a smaller size
   private RewritableDatastore store_;
   private int idx = 0;
   String displayName_;
   private DisplayWindow display = null;
   
   PWSAlbum(String displayName) {
       store_ = Globals.mm().data().createRewritableRAMDatastore();
       displayName_ = displayName;
   }
   
   public Datastore getDatastore() {
      return store_;
   }
   
   public void clear() throws IOException{
       idx = 0;
       store_.deleteAllImages();
   }

   public void addImage(Image image){   
        if ((display==null) || (display.isClosed())) {
            display = Globals.mm().displays().createDisplay(store_);
            //display.setZoom(0.25); Should be implemented im micromanager soon.
            display.setCustomTitle(displayName_);
        }
        Coords newCoords = image.getCoords().copyBuilder().t(idx).build();
        idx++;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    store_.putImage(image.copyAtCoords(newCoords));
                }
                catch (DatastoreFrozenException e) {
                   ReportingUtils.showError(e, "Album datastore is locked.");
                }
                catch (DatastoreRewriteException e) {
                   // This should never happen.
                   ReportingUtils.showError(e, "Unable to add image at " + newCoords + 
                           " to album as another image with those coords already exists.");
                }
                catch (IOException e) {
                    ReportingUtils.showError(e, "PWSAlbum IOException");
                }
            }
        });  
    }
}
