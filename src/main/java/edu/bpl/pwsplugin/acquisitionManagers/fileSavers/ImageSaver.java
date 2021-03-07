///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
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
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.concurrent.ExecutionException;
import org.micromanager.data.Image;

/**
 * Basic interface that must be implemented for a class to act as an image saver.
 * @author nick
 */
public interface ImageSaver {
    public void configure(String savePath, String fileNamePrefix, Integer expectedFrames); //This must be run before `beginSavingThread`
    public void beginSavingThread() throws InterruptedException, ExecutionException;
    public void setMetadata(MetadataBase md);
    public void addImage(Image img);
    //public void closeThread();
    //public void joinThread()
}
