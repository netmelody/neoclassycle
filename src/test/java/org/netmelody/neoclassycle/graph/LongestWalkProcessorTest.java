package org.netmelody.neoclassycle.graph;

import org.junit.Test;

/**
 *
 *
 * @author Franz-Josef Elmer
 */
public final class LongestWalkProcessorTest extends GraphTestCase {

    @Override
    protected void process(final StrongComponentProcessor processor, final AtomicVertex[] graph) {
        processor.deepSearchFirst(graph);
        new LongestWalkProcessor().deepSearchFirst(processor.getStrongComponents());
    }

    @Test
    public void testOneVertexGraphs() {
        check(new String[] { "0: 0" }, new int[][] { {} });
    }

    @Test
    public void testTwoVerticesGraphs() {
        check(new String[] { "0: 0 1" }, new int[][] { { 1 }, { 0 } });
        check(new String[] { "0: 0", "0: 1" }, new int[][] { {}, {} });
        check(new String[] { "0: 1", "0: 0" }, new int[][] { { 0 }, { 1, -1 } });
        check(new String[] { "1: 1", "0: 0" }, new int[][] { { 0 }, { 0 } });
    }

    @Test
    public void testThreeVerticesGraphs() {
        check(new String[] { "0: 0", "0: 1", "0: 2" }, new int[][] { {}, {}, { -1 } });
        check(new String[] { "0: 0", "2: 1", "1: 2" }, new int[][] { { 0 }, { 2 }, { 0 } });
        check(new String[] { "0: 0 1", "0: 2" }, new int[][] { { 1 }, { 0 }, { -1, 2 } });
        check(new String[] { "0: 0 1", "1: 2" }, new int[][] { { 1 }, { 0, 1 }, { 0 } });
        check(new String[] { "1: 0 1", "0: 2" }, new int[][] { { 1, 2 }, { 0, 1 }, {} });
        check(new String[] { "1: 2", "0: 0 1" }, new int[][] { { 1 }, { 0 }, { 0, 1 } });
        check(new String[] { "0: 0 2", "1: 1" }, new int[][] { { -1, 2 }, { 0 }, { -1, 0 } });
        check(new String[] { "0: 0 1 2" }, new int[][] { { 2 }, { 0 }, { 1 } });
    }

    @Test
    public void testComplexGraphs() {
        check(new String[] { "1: 0 1 2 3", "0: 4 5" }, new int[][] { { 1, 2 }, // 0
                { 0 }, // 1
                { 3, -1, -1 }, // 2
                { 1, -1, 4 }, // 3
                { 5 }, // 4
                { 4 }, // 5
        });
        check(new String[] { "1: 0 1 2 3", "0: 4 5" }, new int[][] { { 1, 2 }, // 0
                { 0 }, // 1
                { 3, -1, -1 }, // 2
                { 1, -1, 4 }, // 3
                { 5 }, // 4
                { 4 }, // 5
        });
        check(new String[] { "1: 0 1 2 3", "0: 4 5" }, new int[][] { { 1, 2 }, // 0
                { 0 }, // 1
                { 3, -1, -1 }, // 2
                { 4, -1, 1 }, // 3
                { 5 }, // 4
                { 4 }, // 5
        });
        check(new String[] { "1: 0 1 2 3 6", "0: 4 5", "0: 7" }, new int[][] { { 1 }, // 0
                { 2 }, // 1
                { 2, 3, -1, 4 }, // 2
                { 1, -1, 0, 6 }, // 3
                { 5 }, // 4
                { 4 }, // 5
                { 3, 7 }, // 6
                { -1 } // 7
                });
        check(new String[] { "2: 0", "0: 1", "1: 2 6", "2: 4", "3: 3 7 8", "1: 5" }, new int[][] { { 2 }, // 0
                { -1 }, // 1
                { 1, 6, -1 }, // 2
                { 7, -1, 4 }, // 3
                { 6 }, // 4
                { 1 }, // 5
                { 2, -1 }, // 6
                { 8, 5, -1 }, // 7
                { 3, -1 } // 8
                });
    }
}
