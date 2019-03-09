/*
 * Developed by Felix on 09.03.19 12:37.
 *
 * Copyright (C) 2019. All rights reserved.
 */

package de.felix.astar.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The Pathfinder class represents a Grid in which the A* algorithm can be executed after setting a start and an end node.
 *
 * @author Felix
 */
public class Pathfinder {

    //Constants

    /**
     * The costs for a horizontal or vertical move inside the grid.
     */
    private static final int HORIZONTAL_COST = 10;

    /**
     * The costs for a diagonal move inside the grid.
     */
    private static final int DIAGONAL_COST = 14;

    //Attributes

    /**
     * Stores all nodes as a grid.
     */
    private Node[][] grid;

    /**
     * Whether or not diagonal movement is allowed.
     */
    private boolean diagonal;

    /**
     * Stores all open nodes that have to be evaluated.
     */
    private PriorityQueue<Node> openCollection;

    /**
     * Stores all closed nodes that are evaluated.
     */
    private Set<Node> closedCollection;

    /**
     * The starting node inside the grid.
     */
    private Node startNode;

    /**
     * The end node inside the grid.
     */
    private Node endNode;

    //Methods

    /**
     * Creates a new A* Pathfinder.
     * Be advised that the rows and columns in the grid start at zero.
     * So a grid that is 10x10 cannot have a point P(10/10).
     * The point in the bottom right corner of a 10x10 grid would be P(9/9).
     *
     * @param rows     the amount of rows the grid should have.
     * @param columns  the amount of columns the grid should have.
     * @param diagonal whether or not the algorithm is allowed to do diagonal steps.
     */
    public Pathfinder(int rows, int columns, boolean diagonal) {
        this.diagonal = diagonal;
        this.openCollection = new PriorityQueue<>(Comparator.comparingInt(Node::getfCost));
        this.closedCollection = new HashSet<>();
        this.grid = new Node[rows][columns];

        //Fill grid
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                this.grid[x][y] = new Node(x, y);
            }
        }

    }

    /**
     * Sets the type of the node at the given coordinates, if they are inside the grid.
     *
     * @param x        the x-coordinate.
     * @param y        the y-coordinate.
     * @param nodeType the new node type.
     */
    public void setNodeType(int x, int y, NodeType nodeType) {
        if (!isInsideGrid(x, y)) {
            return;
        }
        setNodeType(this.grid[x][y], nodeType);
    }

    /**
     * Sets the type of the given node.
     *
     * @param node     the node to set its type.
     * @param nodeType the new type.
     */
    public void setNodeType(Node node, NodeType nodeType) {
        if (nodeType == NodeType.START) {
            if (startNode != null) {
                setNodeType(startNode, NodeType.UNEVALUATED);
            }
            startNode = node;
        } else if (nodeType == NodeType.END) {
            if (endNode != null) {
                setNodeType(endNode, NodeType.UNEVALUATED);
            }
            endNode = node;
        } else if (this.startNode == node) {
            this.startNode = null;
        } else if (this.endNode == node) {
            this.endNode = null;
        }

        node.setNodeType(nodeType);
    }

    /**
     * Gives the costs of the distance between two nodes using the DIAGONAL_COST and HORIZONTAL_COST constants.
     *
     * @param nodeA the first node.
     * @param nodeB the second node.
     * @return the absolute distance.
     */
    private int distance(Node nodeA, Node nodeB) {
        int distanceX = Math.abs(nodeA.getX() - nodeB.getX());
        int distanceY = Math.abs(nodeA.getY() - nodeB.getY());

        if (distanceX > distanceY) {
            return DIAGONAL_COST * distanceY + HORIZONTAL_COST * (distanceX - distanceY);
        }

        return DIAGONAL_COST * distanceX + HORIZONTAL_COST * (distanceY - distanceX);
    }

    /**
     * Checks whether or not the given coordinates are inside of the defined grid.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @return true, if the point is in the gird, otherwise false.
     */
    public boolean isInsideGrid(int x, int y) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[x].length;
    }

    /**
     * Retraces the path from the end to the starting node.
     *
     * @return a list with all nodes of the path.
     */
    private List<Node> retracePath() {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;

        //Walk through the whole path until reaching the start node
        while (currentNode != startNode) {
            path.add(currentNode);
            currentNode = currentNode.getParent();
        }
        path.add(startNode);

        //Reverse the path so the first node is the start node
        Collections.reverse(path);

        //Change type of all nodes to PATH except for start and end so they can still be identified
        for (Node node : path) {
            if (node == startNode || node == endNode) {
                continue;
            }
            node.setNodeType(NodeType.PATH);
        }

        return path;
    }

    /**
     * Tries to find the shortest path between the start and the endpoint.
     * <p>
     * The returned list contains all nodes that are on the optimal path.
     * The first node is the end node and the last is the starting node.
     *
     * @return If a path was found, a list of all nodes which lead to the target node,
     * otherwise null.
     */
    public List<Node> findPath() {
        if (startNode == null || endNode == null) {
            throw new NullPointerException("Start and end node have to be set before starting the algorithm!");
        }

        //Add the start node to the open set.
        openCollection.add(startNode);

        //Break when the open set is empty.
        while (openCollection.size() > 0) {

            //Find lowest f cost node and close it
            Node currentNode = openCollection.poll();
            closedCollection.add(currentNode);

            //Check if we've reached the end.
            if (currentNode == endNode) {
                //Retrace path
                return retracePath();
            }

            //Mark nodes as closed
            currentNode.setNodeType(NodeType.CLOSED);

            //Go through all neighbors from the top left to the bottom right neighbour.
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0) continue;
                    if (!diagonal && (x == -1 && y == -1 || x == 1 && y == 1 || x == 1 && y == -1 || x == -1 && y == 1))
                        continue;
                    int xCoordinate = currentNode.getX() + x;
                    int yCoordinate = currentNode.getY() + y;

                    //Skip if node is outside of the grid.
                    if (!isInsideGrid(xCoordinate, yCoordinate)) {
                        continue;
                    }

                    //Get the neighbour node object from the grid.
                    Node neighbour = grid[xCoordinate][yCoordinate];

                    //Skip if the node is a barrier or already closed.
                    if (neighbour.getNodeType() == NodeType.BARRIER || closedCollection.contains(neighbour)) {
                        continue;
                    }

                    //Calculate the new g cost for the neighbour node;
                    int newGCost = currentNode.getgCost() + distance(currentNode, neighbour);

                    //Go on if the new path to the neighbour is cheaper or the neighbour isn't open
                    if (newGCost < neighbour.getgCost() || !openCollection.contains(neighbour)) {

                        //Set G, H and F cost for neighbour
                        neighbour.setgCost(newGCost);
                        neighbour.sethCost(distance(neighbour, endNode));

                        //Set the parent to be able to retrace the final path
                        neighbour.setParent(currentNode);

                        //Add the parent to the open set if it isn't already
                        if (!openCollection.contains(neighbour)) {
                            openCollection.add(neighbour);

                            //Mark node as open
                            neighbour.setNodeType(NodeType.OPEN);
                        }
                    }
                }
            }
        }

        //No path found
        return null;
    }

    /**
     * Marks the node at the given coordinates as a barrier, if the node is inside the grid.
     *
     * @param x the x-coordinate of the node.
     * @param y the y-coordinate of the node.
     */
    public void setBarrier(int x, int y) {
        setNodeType(x, y, NodeType.BARRIER);
    }

    /**
     * Marks the node at the given coordinates as the starting node, if the node is inside the grid.
     * If called multiple times the the old start node is being marked as a normal node normal again.
     *
     * @param x the x-coordinate of the node.
     * @param y the y-coordinate of the node.
     */
    public void setStartNode(int x, int y) {
        setNodeType(x, y, NodeType.START);
    }

    /**
     * Marks the node at the given coordinates as the end node, if the node is inside the grid.
     * If called multiple times the the old end node is being marked as a normal node normal again.
     *
     * @param x the x-coordinate of the node.
     * @param y the y-coordinate of the node.
     */
    public void setEndNode(int x, int y) {
        setNodeType(x, y, NodeType.END);
    }

    /**
     * Sets whether or not the pathfinder is allowed to go in diagonal direction.
     *
     * @param diagonal whether diagonal is allowed or not.
     */
    public void setDiagonal(boolean diagonal) {
        this.diagonal = diagonal;
    }

    /**
     * Getter for the startNode attribute.
     *
     * @return the start node.
     */
    public Node getStartNode() {
        return startNode;
    }

    /**
     * Getter for the endNode attribute.
     *
     * @return the end node.
     */
    public Node getEndNode() {
        return endNode;
    }

    /**
     * Gives the currently used grid.
     *
     * @return the grid.
     */
    public Node[][] getGrid() {
        return grid;
    }

    /**
     * Whether or not the pathfinder allows diagonal movement
     *
     * @return true, if diagonal movement is allowed, otherwise false.
     */
    public boolean isDiagonal() {
        return diagonal;
    }
}
