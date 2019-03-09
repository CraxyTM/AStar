/*
 * Developed by Felix on 09.03.19 12:37.
 *
 * Copyright (C) 2019. All rights reserved.
 */

package de.felix.astar.algorithm;

/**
 * The NodeListener represents a event listener whose events are called when a specific action of a node is happening.
 *
 * @author Felix
 */
public interface INodeUpdateListener {

    /**
     * Called when the NodeType of a node is changed or the f-, g- or h-costs of a node are changed.
     *
     * @param node a reference to the node whose event is called.
     */
    void onUpdate(Node node);
}
