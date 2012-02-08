/*
 * Created on 04.09.2004
 */
package org.netmelody.neoclassycle.dependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.graph.GraphTestCase.MockAttributes;

import static org.junit.Assert.assertEquals;

/**
 * @author Franz-Josef Elmer
 */
public class DependencyProcessorTest {
    private static final String DIDOF = ' ' + DependencyDefinitionParser.DIRECTLY_INDEPENDENT_OF_KEY_WORD + ' ';
    private static final String CHECK = DependencyDefinitionParser.CHECK_KEY_WORD + ' ';
    private static final String OK = DependencyResult.OK + '\n';
    private final String DF = DependencyResult.DEPENDENCIES_FOUND;
    private static final String SHOW_ALLRESULTS = "show allResults\n";
    private static final String SHOW_ALL = "show allPaths allResults\n";
    private static final String SHOW_ONLY_ALL = "show onlyShortestPaths allResults\n";

    @Test
    public void testCheckSets() {
        final String s = "[a] = a.*\n" + "check sets [a] b.* bla.bla";
        check(SHOW_ALLRESULTS + s, new String[] { SHOW_ONLY_ALL, "Set [a] has 2 classes.\n", "Set b.* has one class.\n",
                "Set bla.bla is empty.\n" });
        check(s, new String[] { "", "", "Set bla.bla is empty.\n" });
    }

    @Test
    public void testClassCycleCheck() {
        final String s = "[a] = a.*\n" + "check absenceOfClassCycles > 1 in b.*\n" + "check absenceOfClassCycles > 1 in [a]";
        final String result = "check absenceOfClassCycles > 1 in [a]\n" + "  a.A et al. contains 2 classes:\n" + "    a.A\n" + "    a.B\n";
        check(SHOW_ALLRESULTS + s, new String[] { SHOW_ONLY_ALL, "check absenceOfClassCycles > 1 in b.*\tOK\n", result });
        check(s, new String[] { "", result });
    }

    @Test
    public void testPackageCycleCheck() {
        final String s = "[A] = *.A\n" + "check absenceOfPackageCycles > 1 in *.B\n" + "check absenceOfPackageCycles > 1 in [A]";
        final String result1 = "check absenceOfPackageCycles > 1 in [A]\n" + "  i et al. contains 2 packages:\n" + "    i\n" + "    b\n";
        final String result2 = "check absenceOfPackageCycles > 1 in [A]\n" + "  b et al. contains 2 packages:\n" + "    b\n" + "    i\n";
        check(SHOW_ALLRESULTS + s, new String[] { SHOW_ONLY_ALL, "check absenceOfPackageCycles > 1 in *.B\tOK\n", result2 }, new String[] {
                SHOW_ONLY_ALL, "check absenceOfPackageCycles > 1 in *.B\tOK\n", result2 });
        check(s, new String[] { "", result1 }, new String[] { "", result2 });
    }

    @Test
    public void testSimpleDependencyCheck() {
        String s = "check b.* independentOf c.*";
        check(SHOW_ALLRESULTS + s + "\nshow allPaths", new String[] { SHOW_ONLY_ALL, s + OK, SHOW_ALL });
        s = "check f.* independentOf b.*";
        check(SHOW_ALLRESULTS + s, new String[] { SHOW_ONLY_ALL, s + DF + "\n  f.A\n    -> b.A\n" });
    }

    @Test
    public void testPreferences() {
        final String s1 = "check b.* independentOf c.*";
        final String s2 = "check f.* independentOf b.*";
        final String s = s1 + '\n' + s2 + '\n';
        check(s + SHOW_ALLRESULTS + s + "show onlyFailures\n" + s, new String[] { "", s2 + DF + "\n  f.A\n    -> b.A\n", SHOW_ONLY_ALL,
                s1 + OK, s2 + DF + "\n  f.A\n    -> b.A\n", "", "", s2 + DF + "\n  f.A\n    -> b.A\n" });
    }

    @Test
    public void testSwitchAllPathsAndBack() {
        final String s = "check h.* independentOf b.*";
        check(SHOW_ALLRESULTS + s + "\nshow allPaths\n" + s + "\nshow onlyShortestPaths\n" + s, new String[] { SHOW_ONLY_ALL,
                s + DF + "\n  h.A\n    -> f.A\n      -> b.A\n", SHOW_ALL,
                s + DF + "\n  h.A\n    -> e.A\n      -> e.B\n" + "        -> b.A\n    -> f.A\n      -> b.A\n", SHOW_ONLY_ALL,
                s + DF + "\n  h.A\n    -> f.A\n      -> b.A\n" });
    }

    @Test
    public void testLayering() {
        check(SHOW_ALLRESULTS + "layer bc = b.* c.*\n" + "layer f = f.*\n" + "check layeringOf bc f", new String[] {
                SHOW_ONLY_ALL,
                CHECK + "b.*" + DIDOF + "c.*" + OK + CHECK + "c.*" + DIDOF + "b.*" + OK + CHECK + "b.*" + DIDOF + "f.*" + OK + CHECK
                        + "c.*" + DIDOF + "f.*" + OK });
        final String result = CHECK + "[a]" + DIDOF + "b.*" + OK + CHECK + "[a]" + DIDOF + "c.*" + OK + CHECK + "b.*" + DIDOF + "[a]" + OK
                + CHECK + "b.*" + DIDOF + "c.*" + OK + CHECK + "c.*" + DIDOF + "[a]" + OK + CHECK + "c.*" + DIDOF + "b.*" + OK + CHECK
                + "[a]" + DIDOF + "e.*" + OK + CHECK + "[a]" + DIDOF + "f.*" + OK + CHECK + "b.*" + DIDOF + "e.*" + OK + CHECK + "b.*"
                + DIDOF + "f.*" + OK + CHECK + "c.*" + DIDOF + "e.*" + OK + CHECK + "c.*" + DIDOF + "f.*" + OK + CHECK + "[a]" + DIDOF
                + "h.*" + OK + CHECK + "b.*" + DIDOF + "h.*" + OK + CHECK + "c.*" + DIDOF + "h.*" + OK + CHECK + "e.*" + DIDOF + "f.*" + OK
                + CHECK + "f.*" + DIDOF + "e.*" + OK + CHECK + "e.*" + DIDOF + "h.*" + OK + CHECK + "f.*" + DIDOF + "h.*" + OK;
        check(SHOW_ALLRESULTS + "[a] = a.* excluding a.B\n" + "layer abc = [a] b.* c.* \n" + "layer ef = e.* f.*\n" + "layer h = h.*\n"
                + "check layeringOf abc ef h\n" + "check strictLayeringOf abc ef h", new String[] {
                SHOW_ONLY_ALL,
                result,
                result + CHECK + "h.*" + DIDOF + "[a]" + DF + "\n  h.A\n    -> a.A\n" + CHECK + "h.*" + DIDOF + "b.*" + OK + CHECK + "h.*"
                        + DIDOF + "c.*" + OK });
        check("[a] = a.* excluding a.B\n" + "layer abc = [a] b.* c.* \n" + "layer ef = e.* f.*\n" + "layer h = h.*\n"
                + "check layeringOf abc ef h\n" + "check strictLayeringOf abc ef h", new String[] { "",
                CHECK + "h.*" + DIDOF + "[a]" + DF + "\n  h.A\n    -> a.A\n" });
    }

    private static void check(final String description, final String[] expectedResults) {
        check(description, expectedResults, expectedResults);
    }

    private static void check(final String description, final String[] expectedResults1, final String[] expectedResults2) {
        final AtomicVertex[] graph = createGraph();
        final ResultRenderer renderer = new DefaultResultRenderer();
        final DependencyProcessor processor = new DependencyProcessor(description, null, renderer);
        final List<String> results = new ArrayList<String>();
        while (processor.hasMoreStatements()) {
            results.add(renderer.render(processor.executeNextStatement(graph)));
        }
        try {
            assertEquals(Arrays.asList(expectedResults1), results);
        }
        catch (final AssertionError e) {
            assertEquals(Arrays.asList(expectedResults2), results);
        }
    }

    private static AtomicVertex[] createGraph() {
        return createGraph(new String[] { "a.A", "a.B", "b.A", "c.A", "e.A", "e.B", "f.A", "h.A", "i.A", }, new int[][] { { 1 }, // 0:
                                                                                                                                 // a.A
                { 0 }, // 1: a.B
                { 8 }, // 2: b.A
                {}, // 3: c.A
                { 0, 5 }, // 4: e.A
                { 2 }, // 5: e.B
                { 2 }, // 6: f.A
                { 0, 4, 6 },// 7: h.A
                { 2 }, // 8: i.A
        });

    }

    private static AtomicVertex[] createGraph(final String[] nodes, final int[][] nodeLinks) {
        final AtomicVertex[] result = new AtomicVertex[nodes.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new AtomicVertex(new MockAttributes(nodes[i]));
        }
        for (int i = 0; i < result.length; i++) {
            final int[] links = nodeLinks[i];
            for (final int link : links) {
                result[i].addOutgoingArcTo(result[link]);
            }
        }
        return result;
    }
}
