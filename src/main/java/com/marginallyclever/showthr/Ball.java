package com.marginallyclever.showthr;

import javax.vecmath.Vector2d;

// Ball class for handling ball movement and position
class Ball {
    private final Vector2d position;
    private final Vector2d target;
    private final double radius;
    private double speed = 1.0;  // Arbitrary speed value
    public boolean atTarget = false;

    public Ball(double radius) {
        this.radius = radius;
        this.position = new Vector2d(0, 0);
        this.target = new Vector2d(0, 0);
    }

    public void setTarget(double x, double y) {
        this.target.set(x, y);
        Vector2d diff = new Vector2d(target);
        diff.sub(position);
        atTarget = diff.lengthSquared() < 0.1;
    }

    public void updatePosition(double dt) {
        Vector2d direction = new Vector2d(target);
        direction.sub(position);
        var len = direction.lengthSquared();
        if (len < speed * dt) {
            position.set(target);
            atTarget = true;
        } else {
            direction.normalize();
            direction.scale(speed * dt);
            position.add(direction);
            atTarget = false;
        }
    }

    public Vector2d getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }

    public void setPosition(Vector2d p) {
        position.set(p);
    }
}
