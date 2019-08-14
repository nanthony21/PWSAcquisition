///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL.  2019
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
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.ImSaverRaw;
import edu.bpl.pwsplugin.PWSAlbum;
import java.nio.file.FileAlreadyExistsException;
import org.json.JSONObject;


public interface AcquisitionManager {
    public void acquireImages(PWSAlbum album, ImSaverRaw imSaver, JSONObject metadata); //Begin the acquisition process.
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException; // given a parent directory and a cell number, return the full path to save to.
    public int getExpectedFrames(); //The number of images that are expected from running `acquireImages`
    public String getFilePrefix(); //Return the prefix that the saved files should have.
}
