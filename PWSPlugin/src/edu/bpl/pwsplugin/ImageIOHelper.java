/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import com.twelvemonkeys.imageio.metadata.tiff.TIFF;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.imageio.metadata.IIOMetadataNode;

/**
 *
 * @author Nick Anthony
 */
public class ImageIOHelper {
    static BufferedImage arrtoim(int width, int height, short[] arr) {
        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster r = (WritableRaster) im.getData();
        int[] newarr = new int[arr.length];
        for (int i=0; i<newarr.length; i++) {
            newarr[i] = (int) arr[i];
        }
        r.setPixels(0,0,width,height,newarr);
        im.setData(r);
        return im;
    }
    
    static void createTIFFFieldNode(final IIOMetadataNode parentIFDNode, int tag, short type, Object value) {
        IIOMetadataNode fieldNode = new IIOMetadataNode("TIFFField");

        fieldNode.setAttribute("number", String.valueOf(tag));
        parentIFDNode.appendChild(fieldNode);

        switch (type) {
            case TIFF.TYPE_ASCII:
                createTIFFFieldContainerNode(fieldNode, "Ascii", value);
                break;
            case TIFF.TYPE_BYTE:
                createTIFFFieldContainerNode(fieldNode, "Byte", value);
                break;
            case TIFF.TYPE_SHORT:
                createTIFFFieldContainerNode(fieldNode, "Short", value);
                break;
            case TIFF.TYPE_RATIONAL:
                createTIFFFieldContainerNode(fieldNode, "Rational", value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    static void createTIFFFieldContainerNode(final IIOMetadataNode fieldNode, final String type, final Object value) {
        IIOMetadataNode containerNode = new IIOMetadataNode("TIFF" + type + "s");
        fieldNode.appendChild(containerNode);
        IIOMetadataNode valueNode = new IIOMetadataNode("TIFF" + type);
        valueNode.setAttribute("value", String.valueOf(value));
        containerNode.appendChild(valueNode);
    }
}
