package com.marginallyclever.showthr;

import javax.vecmath.Vector2d;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * A simulation of loose sand on a table and being displaced by a ball.
 */
public class SandSimulation {
    private final double MAX_SLOPE = 1;  // Threshold for sand redistribution
    private final double REDISTRIBUTION_RATE = 0.5;  // Amount of sand transferred per step
    private final double RELAX_MARGIN = 4.0;  // must be at greater than 1.

    private final int tableWidth, tableHeight;
    private final double[][] sandGrid;  // 2D array for sand density
    private final Ball ball;
    private Vector2d startPosition;

    public SandSimulation(int width, int height, double ballRadius,double initialSandDepth) {
        this.tableWidth = width;
        this.tableHeight = height;
        this.sandGrid = new double[width][height];
        this.ball = new Ball(ballRadius);
        this.ball.setPosition(new Vector2d(width / 2.0, height / 2.0));

        // Initialize sand grid to uniform density
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                sandGrid[i][j] = initialSandDepth;  // some sand in every square
            }
        }
    }

    public void setTarget(double x, double y) {
        ball.setTarget(x, y);
        startPosition = ball.getPosition();
    }

    public boolean ballAtTarget() {
        return ball.atTarget;
    }

    public void update(double dt) {
        ball.updatePosition(dt);
        makeBallPushSand();
        relaxSand();  // redistribute the sand
    }

    private void makeBallPushSand() {
        // Iterate over the area affected by the ball's radius
        int ballX = (int)ball.getPosition().x;
        int ballY = (int)ball.getPosition().y;
        int radius = (int)ball.getRadius();

        for (int i = ballX - radius; i <= ballX + radius; i++) {
            for (int j = ballY - radius; j <= ballY + radius; j++) {
                if (i >= 0 && i < tableWidth && j >= 0 && j < tableHeight) {
                    int dx = i - ballX;
                    int dy = j - ballY;
                    if(insideTable(i+dx,j+dy)) {
                        // Distance from the ball's center
                        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                        if (distance <= radius) {
                            // Displace sand based on distance
                            // if the amount of sand here (A) is greater than the height of the ball at this point (B)
                            // displace A-B away from the center of the ball.
                            var A = sandGrid[i][j];
                            var B = Math.max(0,1-Math.cos(Math.max(0,1-distance/radius)));
                            if(A>=B) {
                                var toMove = A - B;
                                A -= toMove;
                                if(A<0) {
                                    toMove-=A;
                                    A=0;
                                }
                                sandGrid[i + dx][j + dy] += toMove;
                                sandGrid[i][j] = A;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean insideTable(int x, int y) {
        return x >= 0 && x < tableWidth && y >= 0 && y < tableHeight;
    }

    /**
     * Make the sand naturally collapse into a more stable shape.
     */
    public void relaxSand() {
        Vector2d pos = ball.getPosition();
        double radius = ball.getRadius();

        int startX = (int)startPosition.x;
        int endX   = (int)pos.x;
        if(startX>endX) {
            int temp = startX;
            startX = endX;
            endX = temp;
        }
        startX -= (int)(radius* RELAX_MARGIN);
        endX += (int)(radius* RELAX_MARGIN);

        int startY = (int)startPosition.y;
        int endY   = (int)pos.y;
        if(startY>endY) {
            int temp = startY;
            startY = endY;
            endY = temp;
        }
        startY -= (int)(radius* RELAX_MARGIN);
        endY += (int)(radius* RELAX_MARGIN);

        if(startX<0) startX=0;
        if(startY<0) startY=0;
        if(endX>=tableWidth) endX=tableWidth-1;
        if(endY>=tableHeight) endY=tableHeight-1;

        boolean settled;
        do {
            settled = true;
            int[] lowerNeighbors =new int[]{0,0,0,0,0,0,0,0};


            for (int y = startY; y < endY - 1; y++) {
                for (int x = startX; x < endX - 1; x++) {
                    double here = sandGrid[x][y];
                    int c = 0;

                    // Check up, down, left, right neighbors
                    if (insideTable(x - 1, y) && sandGrid[x - 1][y] < here- MAX_SLOPE) {
                        lowerNeighbors[c++] = x - 1;
                        lowerNeighbors[c++] = y;
                    }
                    if (insideTable(x + 1, y) && sandGrid[x + 1][y] < here- MAX_SLOPE) {
                        lowerNeighbors[c++] = x + 1;
                        lowerNeighbors[c++] = y;
                    }
                    if (insideTable(x, y - 1) && sandGrid[x][y - 1] < here- MAX_SLOPE) {
                        lowerNeighbors[c++] = x;
                        lowerNeighbors[c++] = y - 1;
                    }
                    if (insideTable(x, y + 1) && sandGrid[x][y + 1] < here- MAX_SLOPE) {
                        lowerNeighbors[c++] = x;
                        lowerNeighbors[c++] = y + 1;
                    }

                    if(c!=0) {
                        settled = false;
                        var d = REDISTRIBUTION_RATE * 2.0/c;

                        for(int i=0;i<c;i+=2) {
                            int x2 = lowerNeighbors[i];
                            int y2 = lowerNeighbors[i+1];
                            double heightDiff = sandGrid[x][y] - sandGrid[x2][y2];
                            double transferAmount = heightDiff * d;
                            sandGrid[x2][y2] += transferAmount;
                            sandGrid[x][y] -= transferAmount;
                        }
                    }
                }
            }
        } while (!settled);
    }

    /**
     * Render the sand density as a grayscale image.  The darkest pixels have the least sand.
     * @return the image
     */
    public BufferedImage renderSandImage() {
        BufferedImage image = new BufferedImage(tableWidth, tableHeight, BufferedImage.TYPE_INT_ARGB);

        double max = 0;
        for (int i = 0; i < tableWidth; i++) {
            for (int j = 0; j < tableHeight; j++) {
                max = Math.max(max, sandGrid[i][j]);
            }
        }
        System.out.println("max = " + max);

        for (int i = 0; i < tableWidth; i++) {
            for (int j = 0; j < tableHeight; j++) {
                int gray = (int)(sandGrid[i][j] * 255.0/max);  // Convert density to grayscale
                image.setRGB(i, j, encode32bit(gray));
            }
        }

        return image;
    }


    /**
     * @param greyscale 0-255
     * @return RGB fully opaque
     */
    public int encode32bit(int greyscale) {
        greyscale &= 0xff;
        return (0xff << 24) | (greyscale << 16) | (greyscale << 8) | greyscale;
    }

    /**
     * Read a THR file and simulate the sand displacement.
     * @param filename THR file to read
     * @throws IOException if the file cannot be read
     */
    public void processFile(String filename) throws IOException {
        File f = new File(filename);

        // count lines in file f for progress report
        int lines = 0;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            while(reader.readLine() != null) {
                lines++;
            }
        }

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            int cx = tableWidth/2;
            int cy = tableHeight/2;

            var maxRadius = tableWidth/2 - 20;

            int j=0;
            String line;
            while((line = reader.readLine()) != null) {
                j++;
                line = line.trim();
                if(line.isEmpty()) continue;
                if(line.startsWith("#")) continue;

                StringBuilder b = new StringBuilder();
                var percent = String.format("%.2f%% ", 100.0 * j / lines);
                b.append(percent).append(line);
                // read a line from the THR file
                String[] parts = line.split(" ");
                double theta = Double.parseDouble(parts[0]);
                double rho = Double.parseDouble(parts[1]) * maxRadius;
                // convert polar to cartesian
                double y = cy + -Math.cos(theta) * rho;
                double x = cx + Math.sin(theta) * rho;
                // set the target
                setTarget(x, y);
                // wait for the ball to reach the target
                while(!ballAtTarget()) {
                    update(0.2);
                    b.append('.');
                }
                var dots = b.toString();
                if(!dots.isEmpty()) System.out.println(dots);
            }
        }
    }

}
