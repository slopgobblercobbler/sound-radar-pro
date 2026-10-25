package com.slopgobbler.soundradar.math;

import java.util.Objects;

/**
 * High-performance immutable 3D double vector for acoustic trajectory and radar math.
 */
public final class Vector3d {
    public static final Vector3d ZERO = new Vector3d(0.0, 0.0, 0.0);

    public final double x;
    public final double y;
    public final double z;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3d of(double x, double y, double z) {
        return new Vector3d(x, y, z);
    }

    public Vector3d add(double dx, double dy, double dz) {
        return new Vector3d(this.x + dx, this.y + dy, this.z + dz);
    }

    public Vector3d add(Vector3d other) {
        return new Vector3d(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3d subtract(double dx, double dy, double dz) {
        return new Vector3d(this.x - dx, this.y - dy, this.z - dz);
    }

    public Vector3d subtract(Vector3d other) {
        return new Vector3d(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3d multiply(double factor) {
        return new Vector3d(this.x * factor, this.y * factor, this.z * factor);
    }

    public double distanceTo(Vector3d other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double distanceTo(double ox, double oy, double oz) {
        double dx = this.x - ox;
        double dy = this.y - oy;
        double dz = this.z - oz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double horizontalDistanceTo(double ox, double oz) {
        double dx = this.x - ox;
        double dz = this.z - oz;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3d normalize() {
        double len = length();
        if (len < 1.0E-6D) {
            return ZERO;
        }
        return new Vector3d(this.x / len, this.y / len, this.z / len);
    }

    public double dot(Vector3d other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector3d other)) return false;
        return Double.compare(other.x, x) == 0 &&
               Double.compare(other.y, y) == 0 &&
               Double.compare(other.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vector3d(%.2f, %.2f, %.2f)", x, y, z);
    }
}
