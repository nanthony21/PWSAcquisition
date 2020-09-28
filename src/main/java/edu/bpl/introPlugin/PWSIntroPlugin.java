/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.introPlugin;

import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.micromanager.IntroPlugin;
import org.micromanager.Studio;
import org.scijava.plugin.Plugin;

/**
 *
 * @author nick
 */
@Plugin(type = IntroPlugin.class)
public class PWSIntroPlugin implements IntroPlugin {
    public List<String> getConfigFilePaths() {
        return null;
    }
    
    public Icon getSplashImage() {
        return new ImageIcon(getClass().getResource("/edu/bpl/icons/splash.png"));
    }
    
    public String getCopyright() {
        return "Nick Anthony 2020";
    }
    
    public void setContext(Studio studio) {}

    public String getName() {
        return "PWS Intro Plugin";
    }


    public String getHelpText() {
        return "This just changees the Image displayed at startup.";
    }


    public String getVersion() {
        return "0.0.1";
    }

}
