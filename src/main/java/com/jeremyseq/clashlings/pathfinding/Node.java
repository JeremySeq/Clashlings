package com.jeremyseq.clashlings.pathfinding;

import java.util.Objects;

public class Node {
    private final int x;
    private final int y;
    private final int z;
    private boolean isObstacle;
    private boolean isStair;
    private boolean isGround;
    private int targetLayer;
    private Node parent;
    private double gCost;
    private double hCost;

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isGround = false; // By default, nodes are not ground nodes
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public boolean isObstacle() { return isObstacle; }
    public void setObstacle(boolean isObstacle) { this.isObstacle = isObstacle; }
    public boolean isStair() { return isStair; }
    public void setStair(boolean isStair, int targetLayer) {
        this.isStair = isStair;
        this.targetLayer = targetLayer;
    }
    public boolean isGround() { return isGround; }
    public void setGround(boolean isGround) { this.isGround = isGround; }
    public int getTargetLayer() { return targetLayer; }
    public Node getParent() { return parent; }
    public void setParent(Node parent) { this.parent = parent; }
    public double getGCost() { return gCost; }
    public void setGCost(double gCost) { this.gCost = gCost; }
    public double getHCost() { return hCost; }
    public void setHCost(double hCost) { this.hCost = hCost; }
    public double getFCost() { return gCost + hCost; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return x == node.x && y == node.y && z == node.z;
    }

    @Override
    public String toString() {
        return "Node{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
