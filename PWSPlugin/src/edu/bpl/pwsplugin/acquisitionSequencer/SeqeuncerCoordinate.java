/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author nick
 */
public class SeqeuncerCoordinate {
    //Uniquely identifies the location in the tree structure of steps used by the sequencer.
    //In addition to a `TreePath` containing the IDs for each step back up to the root
    //some steps also have multiple iterations which must be accounted for.
    @SerializedName("treeIdPath")
    private final List<Step> treePath = new ArrayList<>();
    @SerializedName("treeIterations")
    private final List<Integer> iterations = new ArrayList<>();
    
    public SeqeuncerCoordinate() {}
    
    public void moveDownTree(Step newStep) {
        //Add a new node to the end of the current path
        this.treePath.add(newStep);
        this.iterations.add(0); // We start on the zeroth iteration.
    }
    
    public void moveUpTree() {
        //Remove the last node once we exit that step.
        this.treePath.remove(this.treePath.size()-1);
        this.iterations.remove(this.iterations.size()-1);
    }
    
    public void incrementIteration() {
        int idx = this.iterations.size()-1; // Last item in the path
        this.iterations.set(idx, this.iterations.get(idx)+1);
    }
    
    public JsonObject toJson() {
        //Conver the tree path to be just the ID numbers of each step.
        JsonObject obj1 = new JsonObject();
        JsonArray arr = new JsonArray();
        for (Step s : treePath) {
            arr.add(new JsonPrimitive(s.getID()));
        }
        obj1.add("treeIdPath", arr);    
        obj1.add("stepIterations", (JsonArray) GsonUtils.getGson().toJsonTree(iterations));
        return obj1;
    }
    
    public List<Step> getTreePath() {
        return Collections.unmodifiableList(treePath);
    }
    /*public void setTreePath(Step[] treePath) {
        idPath.clear();
        for (Step step : treePath) {
            idPath.add(step.getID());
        }
        //Clear the iteration since we are 
    }*/
    
    
    
    
    
}
