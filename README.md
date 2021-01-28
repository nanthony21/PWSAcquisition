# MicromanagerMacros
This repository contains the Micro-Manager plugins that are used for PWS acquisition.

## PWS Plugin
This plugin appears as an option in the `acquisition` menu. It handles acquisition of PWS data.

## Image Sharpness Plugin
This plugin appears as a panel in the "Image Inspector".  It provides a graph of image sharpness vs Z position.
This helps with the vital task of focusing images before PWS acquisition.

# Dependencies
The plugins have a few dependencies that are not included with Micro-Manager:
1: Twelve-Monkeys TIFF plugin for ImageIO. These can be downloaded from https://github.com/haraldk/TwelveMonkeys. The require files
are: 
  - common-image
  - common-io
  - common-lang
  - imageio-core
  - imageio-metadata
  - imageio-tiff

