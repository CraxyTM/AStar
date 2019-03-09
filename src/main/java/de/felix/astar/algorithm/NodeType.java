/*
 * Developed by Felix on 09.03.19 12:37.
 *
 * Copyright (C) 2019. All rights reserved.
 */

package de.felix.astar.algorithm;

/**
 * The NodeType Enum represents the current type of a node. E.g. When a node is in the closed-collection it has the NodeType {@link NodeType#CLOSED}
 *
 * @author Felix
 */
public enum NodeType {
    UNEVALUATED, OPEN, CLOSED, START, END, BARRIER, PATH
}
