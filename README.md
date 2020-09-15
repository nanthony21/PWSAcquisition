# MicromanagerMacros
This repository contains the Micro-Manager plugins that are used for PWS acquisition.

### Dependencies
The plugins have a few dependencies that are not included with Micro-Manager:
1: Twelve-Monkeys TIFF plugin for ImageIO. These can be downloaded from https://github.com/haraldk/TwelveMonkeys. The require files
are: 
  - common-image
  - common-io
  - common-lang
  - imageio-core
  - imageio-metadata
  - imageio-tiff

2: JFreeChart plotting library. the jfreechart.jar file can be downloaded from jfree.org.

These extra dependencies should be placed in `Micro-Manager-2.0gamma\plugins\micro-manager`
