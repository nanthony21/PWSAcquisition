# PWS Acquisition

A set of Micro-Manager plugins that are used for the acquisition PWS and other interference-based imaging..

## PWS Plugin

### Acquisition Sequencer  
A graph-based acquisition engine allows extremely flexible automated imaging to describe nearly any conceivable imaging session. A simple user interface provides
users an intuitive drag-and-drop method of defining the automation sequence they want.

### Reliable hardware autofocus  
The reliability of hardware autofocus z stage devices during automated imaging is enhanced with software autofocus in the event of a hardware autofocus failure. This allows the usage of hardware autofocus over the course of multi-day experiments when under normal circumstances focus lock would have broken after only an hour.

### Utilities  
Additional utilities provide automatic exposure time recommendation, and software-autofocus based escape/refocus of the microscope objective.

### Hardware Abstraction  
Additional hardware abstraction extends Micro-Manager's built-in device interfaces to provide uniform handling of hyperspectral cameras, wavelength-tunable filters, and hardware-autofocus equipped ZStages such as Nikon PFS z stages.

## Image Sharpness Plugin  
This plugin appears as a panel in the "Image Inspector". It provides a graph of image sharpness vs Z
position. This helps with the vital task of achieving precisely focused images before PWS acquisition.

## Dependencies  
The plugins included in this repository have a few dependencies that are not included with Micro-Manager:
1: Twelve-Monkeys TIFF plugin for ImageIO. These can be downloaded
from https://github.com/haraldk/TwelveMonkeys or sourced from a maven repository. The require files are:

- common-image
- common-io
- common-lang
- imageio-core
- imageio-metadata
- imageio-tiff

