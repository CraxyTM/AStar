/*
 * Developed by Felix on 09.03.19 12:37.
 *
 * Copyright (C) 2019. All rights reserved.
 */

package de.felix.astar.algorithm;

/**
 * The Node class represents a node inside the grid of the {@link Pathfinder} class.
 *
 * @author Felix
 */
public class Node {

    /**
     * The listener for node updates, if applied.
     */
    private INodeUpdateListener listener;

    /**
     * The current type of the node.
     */
    private NodeType nodeType;

    /**
     * The node who led the algorithm to this node. Used in order to reconstruct the path.
     */
    private Node parent;

    /**
     * The Sum of the g- and h-costs.
     */
    private int fCost;

    /**
     * Basically the distance to the start node.
     */
    private int gCost;

    /**
     * Basically the distance to the end node.
     */
    private int hCost;

    /**
     * The x-coordinate of this node.
     */
    private int x;

    /**
     * The y-coordinate of this node.
     */
    private int y;

    /**
     * Creates a new node using only coordinates. The default node type is {@link NodeType#UNEVALUATED}
     *
     * @param x the x-coordinate of the node.
     * @param y the y-coordinate of the node.
     */
    public Node(int x, int y) {
        this(NodeType.UNEVALUATED, x, y);
    }

    /**
     * Creates a new node using the coordinates and a {@link NodeType}
     *
     * @param nodeType the initial {@link NodeType}
     * @param x        the x-coordinate of the node.
     * @param y        the y-coordinate of the node.
     */
    public Node(NodeType nodeType, int x, int y) {
        this.x = x;
        this.y = y;
        this.nodeType = nodeType;
    }

    /**
     * Calculates the f costs by adding up the g and h cost.
     * Called whenever one of them is updated.
     */
    private void calculatefCost() {
        this.fCost = this.gCost + this.hCost;
        if (listener != null) {
            listener.onUpdate(this);
        }
    }

    /**
     * Sets the g-costs of this node and recalculates the f costs if the new g-costs aren't equal to the old.
     *
     * @param gCost the new g-costs.
     */
    protected void setgCost(int gCost) {
        if (gCost == this.gCost) return;
        this.gCost = gCost;
        calculatefCost();
    }

    /**
     * Sets the h-costs of this node and recalculates the f costs if the new h-costs aren't equal to the old.
     *
     * @param hCost the new g-costs.
     */
    protected void sethCost(int hCost) {
        if (hCost == this.hCost) return;
        this.hCost = hCost;
        calculatefCost();
    }

    /**
     * Sets the type of this node and calls the {@link INodeUpdateListener} if it was set.
     * This method is preventing the start and end node from getting the NodeType {@link NodeType#OPEN} or {@link NodeType#CLOSED}
     *
     * @param nodeType the new type of this node.
     */
    protected void setNodeType(NodeType nodeType) {
        if ((this.nodeType == NodeType.START || this.nodeType == NodeType.END)
                && (nodeType == NodeType.OPEN || nodeType == NodeType.CLOSED)) {
            return;
        }

        this.nodeType = nodeType;
        if (listener != null) {
            listener.onUpdate(this);
        }
    }

    /**
     * Sets the {@link INodeUpdateListener} for this Node. There can only be one listener at a time, so old listeners are overwritten.
     * See the {@link INodeUpdateListener} documentation for further explanation.
     *
     * @param listener the new listener for this node.
     */
    public void setListener(INodeUpdateListener listener) {
        this.listener = listener;
    }

    protected void setParent(Node parent) {
        this.parent = parent;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public int getfCost() {
        return fCost;
    }

    public int getgCost() {
        return gCost;
    }

    public int gethCost() {
        return hCost;
    }

    public Node getParent() {
        return parent;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
