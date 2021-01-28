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
package edu.bpl.pwsplugin.hardware;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class MMDeviceException extends Exception { //Micromanager core stupidly throw generic Exceptions. When possible we wrap them in this specific exception for overall cleaner code.
    public MMDeviceException() { super(); }
    public MMDeviceException(Throwable cause) { super(cause); }
    public MMDeviceException(String msg) { super(msg); }
}
