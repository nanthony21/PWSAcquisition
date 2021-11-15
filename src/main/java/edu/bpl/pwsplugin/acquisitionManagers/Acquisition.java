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

/**
 * The basic requirements for an acquisition managed by the `AcquisitionManager class.
 * @param <S> The settings class that configures this acquisition.
 */
interface Acquisition<S> {

   /**
    * Begin acquiring images with the save location determined by the args
    * @param savePath The path to save to
    * @param cellNum  The number of acquisition (determines folder naming).
    * @throws Exception
    */
   void acquireImages(String savePath, int cellNum) throws Exception;

   /**
    *
    * @param settings The settings to configure the acquisition with.
    */
   void setSettings(S settings);
}
