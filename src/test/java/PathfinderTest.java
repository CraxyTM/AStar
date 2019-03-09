/*
 * Developed by Felix on 09.03.19 12:37.
 *
 * Copyright (C) 2019. All rights reserved.
 */

import de.felix.astar.algorithm.Pathfinder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PathfinderTest {

    private Pathfinder pathfinder;

    @Before
    public void setup() {
        pathfinder = new Pathfinder(8, 8, true);
    }

    @Test
    public void testImpossibleDiagonal() {
        pathfinder.setStartNode(5, 5);
        pathfinder.setEndNode(7, 5);

        pathfinder.setBarrier(6, 0);
        pathfinder.setBarrier(6, 1);
        pathfinder.setBarrier(6, 2);
        pathfinder.setBarrier(6, 3);
        pathfinder.setBarrier(6, 4);
        pathfinder.setBarrier(6, 5);
        pathfinder.setBarrier(6, 6);
        pathfinder.setBarrier(6, 7);

        assertNull(pathfinder.findPath());
    }

    @Test
    public void testImpossibleNotDiagonal() {
        pathfinder.setDiagonal(false);
        pathfinder.setStartNode(0, 1);
        pathfinder.setEndNode(2, 1);

        pathfinder.setBarrier(2, 0);
        pathfinder.setBarrier(3, 1);
        pathfinder.setBarrier(1, 1);
        pathfinder.setBarrier(2, 2);

        assertNull(pathfinder.findPath());
    }

    @Test
    public void testPossible() {
        pathfinder.setStartNode(1, 2);
        pathfinder.setEndNode(6, 6);
        pathfinder.setBarrier(2, 1);
        pathfinder.setBarrier(2, 2);
        pathfinder.setBarrier(2, 3);
        pathfinder.setBarrier(2, 4);
        pathfinder.setBarrier(2, 5);
        pathfinder.setBarrier(2, 6);
        assertNotNull(pathfinder.findPath());
    }

    @Test
    public void testComplex() {
        pathfinder.setStartNode(0, 0);
        pathfinder.setEndNode(4, 1);

        pathfinder.setBarrier(2, 0);
        pathfinder.setBarrier(3, 0);
        pathfinder.setBarrier(4, 0);
        pathfinder.setBarrier(5, 0);
        pathfinder.setBarrier(6, 0);
        pathfinder.setBarrier(7, 0);
        pathfinder.setBarrier(2, 1);
        pathfinder.setBarrier(3, 1);
        pathfinder.setBarrier(6, 1);
        pathfinder.setBarrier(7, 1);
        pathfinder.setBarrier(2, 2);
        pathfinder.setBarrier(3, 2);
        pathfinder.setBarrier(4, 2);
        pathfinder.setBarrier(5, 2);
        pathfinder.setBarrier(7, 2);
        pathfinder.setBarrier(2, 3);
        pathfinder.setBarrier(3, 3);
        pathfinder.setBarrier(4, 3);
        pathfinder.setBarrier(6, 3);
        pathfinder.setBarrier(7, 3);
        pathfinder.setBarrier(2, 4);
        pathfinder.setBarrier(3, 4);
        pathfinder.setBarrier(4, 4);
        pathfinder.setBarrier(5, 4);
        pathfinder.setBarrier(7, 4);
        pathfinder.setBarrier(2, 5);
        pathfinder.setBarrier(3, 5);
        pathfinder.setBarrier(4, 5);
        pathfinder.setBarrier(5, 5);
        pathfinder.setBarrier(6, 5);

        assertNotNull(pathfinder.findPath());
    }

    @Test
    public void testComplexFail() {
        pathfinder.setStartNode(0, 0);
        pathfinder.setEndNode(4, 1);

        pathfinder.setBarrier(2, 0);
        pathfinder.setBarrier(3, 0);
        pathfinder.setBarrier(4, 0);
        pathfinder.setBarrier(5, 0);
        pathfinder.setBarrier(6, 0);
        pathfinder.setBarrier(7, 0);
        pathfinder.setBarrier(2, 1);
        pathfinder.setBarrier(3, 1);
        pathfinder.setBarrier(6, 1);
        pathfinder.setBarrier(7, 1);
        pathfinder.setBarrier(2, 2);
        pathfinder.setBarrier(3, 2);
        pathfinder.setBarrier(4, 2);
        pathfinder.setBarrier(5, 2);
        pathfinder.setBarrier(7, 2);
        pathfinder.setBarrier(2, 3);
        pathfinder.setBarrier(3, 3);
        pathfinder.setBarrier(4, 3);
        pathfinder.setBarrier(6, 3);
        pathfinder.setBarrier(7, 3);
        pathfinder.setBarrier(2, 4);
        pathfinder.setBarrier(3, 4);
        pathfinder.setBarrier(4, 4);
        pathfinder.setBarrier(5, 4);
        pathfinder.setBarrier(7, 4);
        pathfinder.setBarrier(2, 5);
        pathfinder.setBarrier(3, 5);
        pathfinder.setBarrier(4, 5);
        pathfinder.setBarrier(5, 5);
        pathfinder.setBarrier(6, 5);

        //This should make it fail
        pathfinder.setBarrier(5, 1);
        assertNull(pathfinder.findPath());
    }

}
