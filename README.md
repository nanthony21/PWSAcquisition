# MicromanagerMacros
This repository contains the Micro-Manager plugins that are used for PWS acquisition.

## PWS Plugin

#### Acquisition Sequencer
A tree based acquisition engine allows extremely flexible automated imaging.

#### Reliable hardware autofocus.
The reliability of Hardware Autofocus ZStage devices during automated imaging are enhanced with Software Autofocus in the event of a hardware fault.

#### Utilities
Utilities provide automatic exposure time reccomendation, autofocus. More to come.

#### Hardware Abstraction
Enhanced handling of specral cameras, Nikon PFS ZStages, Illumination sources.

## Image Sharpness Plugin
This plugin appears as a panel in the "Image Inspector".  It provides a graph of image sharpness vs Z position.
This helps with the vital task of focusing images before PWS acquisition.

# Dependencies
The plugins have a few dependencies that are not included with Micro-Manager:
1: Twelve-Monkeys TIFF plugin for ImageIO. These can be downloaded from https://github.com/haraldk/TwelveMonkeys. The required files
are: 
  - common-image
  - common-io
  - common-lang
  - imageio-core
  - imageio-metadata
  - imageio-tiff

