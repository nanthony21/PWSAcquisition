/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.ImSaverRaw;
import edu.bpl.pwsplugin.PWSAlbum;
import java.nio.file.FileAlreadyExistsException;
import org.json.JSONObject;

/**
 *
 * @author backman05
 */
public interface AcquisitionManager {
    public void acquireImages(PWSAlbum album, ImSaverRaw imSaver, JSONObject metadata);
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException;
    public int getExpectedFrames();
}
