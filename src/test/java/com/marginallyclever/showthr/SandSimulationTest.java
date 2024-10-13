package com.marginallyclever.showthr;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.*;

public class SandSimulationTest {
    @Test
    public void testSandSimulation() throws IOException {
        SandSimulation sandSimulation = new SandSimulation(300,300, 5,2);
        var image = sandSimulation.renderSandImage();
        // save the image to disk
        var file = new File("sand_simulation.png");
        ImageIO.write(image, "png", file);
        System.out.println("Image saved to " + file.getAbsolutePath());
    }

    @Test
    public void testSandSimulationSpiral() throws IOException {
        int w = 300;
        int h = 300;
        SandSimulation sandSimulation = new SandSimulation(w, h, 5,2);
        sandSimulation.setTarget(100, 100);
        double d = (w-40)/2.0;
        double a = 0.0;
        for(int i=0;i<10000;i++) {
            sandSimulation.update(0.5);
            if(sandSimulation.ballAtTarget()) {
                var r = Math.toRadians(a);
                sandSimulation.setTarget(
                        w/2.0 + Math.cos(r)*d,
                        h/2.0 + Math.sin(r)*d);
                d=((w-40)/2.0)-(a/360.0)*10;
                a+=5.0;
            }
            if(i%100==0) {
                System.out.println(i);
            }
        }

        var image = sandSimulation.renderSandImage();
        // save the image to disk
        var file = new File("sand_simulation.png");
        ImageIO.write(image, "png", file);
        System.out.println("Image saved to " + file.getAbsolutePath());
    }


    /**
     * Read a THR file and simulate the sand being pushed by the ball.
     * @throws IOException if the file cannot be read
     */
    @Test
    public void testSandSimulationFromFile() throws IOException {
        // table size
        int w = 1000;
        int h = 1000;
        SandSimulation sandSimulation = new SandSimulation(w, h, 5,2);

        sandSimulation.processFile("src/test/resources/Vaporeon with Waves.thr");

        var image = sandSimulation.renderSandImage();
        // save the image to disk
        var file = new File("sand_simulation.png");
        ImageIO.write(image, "png", file);
        System.out.println("Image saved to " + file.getAbsolutePath());
    }
}
