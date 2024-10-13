package com.marginallyclever.showthr;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * <p>Simulate a sand table from a THR file and save the result as an image.</p>
 * <p><code>ShowTHR inputfile.thr outputfile [-w width] [-h height] [-b ball size] [-d sand depth]</code></p>
 * <p>The THR file is a text file that describes the motion of a ball across a table of sand.  The output file is an
 * image of the sand table after the ball has moved.</p>
 * <p>THR format is a text file with one command per line.  Each command is "theta rho", where theta is an angle in
 * radians and rho is a value from 0...1.  lines that are blank or begin with # can be safely ignored.</p>
 * <p>The simulation attempts to push some sand away from the ball and then </p>
 */
public class ShowTHR {
    private static String inputFilename;
    private static String outputFilename;
    private static int w = 300;
    private static int h = 300;
    private static int ballSize = 5;
    private static int initialDepth = 2;
    private static String ext;

    public static void main(String[] args) {
        System.out.println("ShowTHR");

        if(readInputs(args) || outputFileNotSupported()) {
            showHelp();
            return;
        }

        printSettings();

        // get start time
        long start = System.currentTimeMillis();

        SandSimulation sandSimulation = new SandSimulation(w,h,ballSize,initialDepth);
        try {
            sandSimulation.processFile(inputFilename);
        } catch(IOException e) {
            System.out.println("Error reading file "+inputFilename+": "+e.getMessage());
        }

        var image = sandSimulation.renderSandImage();
        try {
            // save the image to disk
            var file = new File(outputFilename);
            ImageIO.write(image, ext, file);
            System.out.println("Image saved to " + file.getAbsolutePath());
        } catch(IOException e) {
            System.out.println("Error saving file "+outputFilename+": "+e.getMessage());
        }
        // get end time
        long end = System.currentTimeMillis();
        System.out.println("Done!  Time taken: "+((end-start)*0.001)+"s");
    }

    // verify the file extension is supported by ImageIO
    private static boolean outputFileNotSupported() {
        if(!ImageIO.getImageWritersByFormatName(ext).hasNext()) {
            System.out.println("Unsupported file format "+ext);
            return true;
        }
        return false;
    }

    // print the settings
    private static void printSettings() {
        System.out.println("inputFilename="+inputFilename);
        System.out.println("outputFilename="+outputFilename);
        System.out.println("w="+w);
        System.out.println("h="+h);
        System.out.println("ballSize="+ballSize);
        System.out.println("initialDepth="+initialDepth);
    }

    /**
     * Read the command line arguments and set the inputFilename, outputFilename, w, h, ballSize, and initialDepth.
     * @param args the command line arguments
     * @return true if the arguments are invalid
     */
    private static boolean readInputs(String[] args) {
        if(args.length<2) {
            return true;
        }
        inputFilename = args[0];
        outputFilename = args[1];
        ext = outputFilename.substring(outputFilename.lastIndexOf('.')+1);

        for(int i=2;i<args.length;i++) {
            if(args[i].equals("-w")) {
                i++;
                if(i==args.length) {
                    System.out.println("Missing value for -w");
                    return true;
                }
                w = Integer.parseInt(args[i].trim());
            } else if(args[i].equals("-h")) {
                i++;
                if(i==args.length) {
                    System.out.println("Missing value for -h");
                    return true;
                }
                h = Integer.parseInt(args[i].trim());
            } else if(args[i].equals("-b")) {
                i++;
                if(i==args.length) {
                    System.out.println("Missing value for -b");
                    return true;
                }
                ballSize = Integer.parseInt(args[i].trim());
            } else if(args[i].equals("-d")) {
                i++;
                if(i==args.length) {
                    System.out.println("Missing value for -d");
                    return true;
                }
                initialDepth = Integer.parseInt(args[i].trim());
            } else {
                System.out.println("Unknown option "+args[i]);
                return true;
            }
        }

        return false;
    }

    private static void showHelp() {
        System.out.println("ShowTHR inputfile.thr outputfile.png [-w width] [-h height] [-b ball size] [-d initial depth]");
        System.out.println("default width=300, height=300, ball size=5, initial depth=2");
        System.out.println("output formats supported: "+ Arrays.toString(ImageIO.getWriterFormatNames()));
    }
}
