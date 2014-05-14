package org.gephi.simulation;

import java.util.Arrays;

public class NodeScaler {

    private final Float[] xs;
    private final Float[] ys;
    private int size;
    private float maxX;
    private float minX;
    private float maxY;
    private float minY;

    public NodeScaler(int capacity) {
        xs = new Float[capacity];
        ys = new Float[capacity];
        reset();
    }

    public void reset() {
        size = 0;
        minX = minY = Float.MAX_VALUE;
        maxX = maxY = Float.MIN_VALUE;
    }

    public void update(float x, float y) {
        xs[size] = x;
        ys[size] = y;
        maxX = Math.max(x, maxX);
        minX = Math.min(x, minX);
        maxY = Math.max(y, maxY);
        minY = Math.min(y, minY);
        size++;
    }

    public Coords getMedian() {
        float medX, medY;
        Arrays.sort(xs, 0, size);
        Arrays.sort(ys, 0, size);

        if (size % 2 == 0) {
            medX = (xs[size / 2] + xs[size / 2 - 1]) / 2;
            medY = (ys[size / 2] + ys[size / 2 - 1]) / 2;
        } else {
            medX = xs[size / 2];
            medY = ys[size / 2];
        }

        return new Coords(medX, medY);
    }

    public Coords getMean() {
        float sumX = 0;
        float sumY = 0;
        for (int i = 0; i < size; i++) {
            sumX += xs[i];
            sumY += ys[i];
        }

        return new Coords(sumX / size, sumY / size);
    }

    public Coords getScaled(float x, float y, int scaleFactor) {
        Float factor = Math.max(maxX - minX, maxY - minY);
        x = (scaleFactor * x) / factor;
        y = (scaleFactor * y) / factor;
        return new Coords(x, y);
    }

    public void getScaled(Coords c, int scaleFactor) {
        Float factor = Math.max(maxX - minX, maxY - minY);
        c.x = (scaleFactor * c.x) / factor;
        c.y = (scaleFactor * c.y) / factor;
    }

    public Coords getNormlized(float x, float y, int scaleFactor) {
        double mag = Math.sqrt(x * x + y * y);
        if (mag != 0) {
            x /= mag;
            y /= mag;
            x *= scaleFactor;
            y *= scaleFactor;
        }
        return new Coords(x, y);
    }

    public void getNormlized(Coords c, int scaleFactor) {
        double mag = Math.sqrt(c.x * c.x + c.y * c.y);
        if (mag != 0) {
            c.x /= mag;
            c.y /= mag;
            c.x *= scaleFactor;
            c.y *= scaleFactor;
        }
    }
}
