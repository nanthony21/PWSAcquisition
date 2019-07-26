/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.PWSAlbum;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author backman05
 */
public interface AcquisitionManager {
    public void acquireImages(PWSAlbum album, LinkedBlockingQueue imageQueue);
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException;
    public JSONObject modifyMetadata(JSONObject metadata) throws JSONException;
    public int getExpectedFrames();
}
