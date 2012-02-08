/*
 * Created on 30.08.2004
 */
package org.netmelody.neoclassycle.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.junit.Test;
import org.netmelody.neoclassycle.util.StringPattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Franz-Josef Elmer
 */
public class DependencyDefinitionParserTest {

    private Hashtable<Object, Object> _defaultProps;

    @Test
    public void testShowStatements() {
        check("show a b c", new String[] { "[a, b, c]" });
        check("show a\n show b c", new String[] { "[a]", "[b, c]" });
    }

    @Test
    public void testCheckSetStatements() {
        check("check sets ${package}.lang.*", new String[] { "check set java.lang.*" });
        check("[lang] = java.lang.*\n" + "check sets [lang] java.util.*", new String[] { "check set [lang]", "check set java.util.*" });
    }

    @Test
    public void testCheckCycleStatements() {
        check("[base] = base.*\n" + "check   absenceOfClassCycles   >    10  in   java.lang.*\n"
                + "check absenceOfPackageCycles > 1  in [base]", new String[] { "check absenceOfClassCycles > 10 in java.lang.*",
                "check absenceOfPackageCycles > 1 in [base]" });
    }

    @Test
    public void testCheckMissSpelledKeywords() {
        try {
            createParser("[set] = b.* exluding *Test");
            fail("IllegalArgumentException expected because of miss-spelled keyword");
        }
        catch (final IllegalArgumentException e) {
            assertContains("exluding", e);
        }

        try {
            createParser("check b.* independentof [set]");
            fail("IllegalArgumentException expected because of miss-spelled keyword");
        }
        catch (final IllegalArgumentException e) {
            assertContains("independentof", e);
        }

        try {
            createParser("check b.* dependingOn [set]");
            fail("IllegalArgumentException expected because of miss-spelled keyword");
        }
        catch (final IllegalArgumentException e) {
            assertContains("dependingOn", e);
        }

        try {
            createParser("check layeringof [set]");
            fail("IllegalArgumentException expected because of miss-spelled keyword");
        }
        catch (final IllegalArgumentException e) {
            assertContains("layeringof", e);
        }

        try {
            createParser("layer l = layer");
            fail("IllegalArgumentException expected because of miss-spelled keyword");
        }
        catch (final IllegalArgumentException e) {
            assertContains("layer", e);
        }

        try {
            createParser("check sets set1");
            fail("IllegalArgumentException expected because of miss-spelled keyword");
        }
        catch (final IllegalArgumentException e) {
            assertContains("set1", e);
        }

    }

    private static void assertContains(final String expectedMessageFragment, final Throwable throwable) {

        final String message = throwable.getMessage();
        assertTrue("<" + message + "> does not contain <" + message + ">", message.indexOf(expectedMessageFragment) >= 0);
    }

    @Test
    public void testDependencyStatements() {
        check("check java.lang.Integer dependentOnlyOn java.lang.Number",
                new String[] { "check java.lang.Integer dependentOnlyOn java.lang.Number" });
        check("check java.lang.* independentOf ${awt}", new String[] { "check java.lang.* independentOf java.awt.*" });
        check("[lang] = java.lang.*\n" + "show shortestPathsOnly\n" + "check [lang] independentOf java.awt.*", new String[] {
                "[shortestPathsOnly]", "check [lang] independentOf java.awt.*" });
        check("[lang] = java.lang.*\n" + "  check   [lang]   ${package}.util.*   independentOf ${awt}",
                new String[] { "check [lang] java.util.* independentOf java.awt.*" });
        check("{jojo}   =   ${package}   \n" + "[lang] = java.lang.*\n" + "  check   [lang]   ${jojo}.util.*   independentOf\\\njava.awt.*",
                new String[] { "check [lang] java.util.* independentOf java.awt.*" });
        assertEquals(2, _defaultProps.size()); // We expect ${jojo} not in there
        check("######\n" + "[lang] = java.lang.*\n" + "# this is a comment\n" + "    # and this too\n\r\n"
                + "check [lang] java.util.* independentOf \\ \n  java.awt.*\n"
                + "check java.util.* independentOf \\\n   javax.swing.* java.awt.*\n", new String[] {
                "check [lang] java.util.* independentOf java.awt.*", "check java.util.* independentOf javax.swing.* java.awt.*" });
        check("[lang] = java.lang.*\n" + "check [lang] java.util.* independentOf\\  \n# hello\njava.awt.*",
                new String[] { "check [lang] java.util.* independentOf java.awt.*" });
        check("[lang] = java.lang.*\n" + "[a] = [lang] excluding java.lang.Class\n" + "check [a] independentOf java.awt.*",
                new String[] { "check [a] independentOf java.awt.*" });
    }

    @Test
    public void testLayeringStatements() {
        check("layer a = java.lang.*\n" + "layer b = java.util.*\n" + "check layeringOf a b", new String[] { "check layeringOf a b" });
        check("layer a = java.lang.*\n" + "layer b = java.util.*\n" + "check strictLayeringOf a b",
                new String[] { "check strictLayeringOf a b" });
    }

    @Test
    public void testSetDefinition() {
        check("[a] = j.*", new String[][] { { "[a]", "j.*" } });
        check("[a] = j.* k.*", new String[][] { { "[a]", "j.* k.*" } });
        check("[a] = j.* k.* excluding a.b", new String[][] { { "[a]", "(j.* k.* & !a.b)" } });
        check("[a] = j.* k.* excluding a.b c.d", new String[][] { { "[a]", "(j.* k.* & !(a.b c.d))" } });
        check("[a] = k$t.*_$ excluding a.b c.d", new String[][] { { "[a]", "(k$t.*_$ & !(a.b c.d))" } });
        check("[a] = excluding a.b c.d", new String[][] { { "[a]", "!(a.b c.d)" } });
    }

    @Test
    public void testSetDefinitions() {
        check("[a] = a.*\n" + "[b] = b.*\n" + "[c] = [a] excluding j.*", new String[][] { { "[a]", "a.*" }, { "[b]", "b.*" },
                { "[c]", "(a.* & !j.*)" } });
        check("[a] = a.*\n" + "[b] = b.*\n" + "[c] = [a] j.*\n" + "[d] = j.* [b] excluding [c] k.*\n", new String[][] { { "[a]", "a.*" },
                { "[b]", "b.*" }, { "[c]", "a.* j.*" }, { "[d]", "(j.* b.* & !(a.* j.* k.*))" } });
    }

    @Test
    public void testLayerDefinitions() {
        check("[a] = a.*\n" + "[b] = b.*\n" + "[c] = [a] excluding j.*\n" + "layer a = z.*\n" + "layer b = [c]\n" + "layer bla = [a] [c]\n"
                + "layer c = [a] s.*\n", new String[] { "a", "b", "bla", "c" }, new String[][] { { "z.*" }, { "(a.* & !j.*)" },
                { "a.*", "(a.* & !j.*)" }, { "a.*", "s.*" } });
    }

    private void check(final String definition, final String[] expectedStatements) {
        final DependencyDefinitionParser parser = createParser(definition);
        final Statement[] statements = parser.getStatements();
        final int len = Math.min(statements.length, expectedStatements.length);
        for (int i = 0; i < len; i++) {
            assertEquals("Statement " + (i + 1), expectedStatements[i], statements[i].toString());
        }
        if (len < statements.length) {
            fail(statements.length - len + " additional statements");
        }
        if (len < expectedStatements.length) {
            fail(expectedStatements.length - len + " missing statements");
        }
    }

    private DependencyDefinitionParser createParser(final String definition) {
        _defaultProps = new Hashtable<Object, Object>();
        _defaultProps.put("package", "java");
        _defaultProps.put("awt", "java.awt.*");
        final DependencyProperties properties = new DependencyProperties(_defaultProps);
        final DependencyDefinitionParser parser = new DependencyDefinitionParser(definition, properties, new MockResultRenderer());
        return parser;
    }

    private void check(final String definition, final String[][] expectedSets) {
        final DependencyDefinitionParser parser = createParser(definition);
        final SetDefinitionRepository definitions = parser._setDefinitions;
        for (final String[] expectedSet2 : expectedSets) {
            final String setName = expectedSet2[0];
            final String expectedSet = expectedSet2[1];
            assertEquals("Set " + setName, expectedSet, definitions.getPattern(setName) + "");
        }
    }

    private void check(final String definition, final String[] layerNames, final String[][] expectedLayers) {
        final DependencyDefinitionParser parser = createParser(definition);
        final LayerDefinitionRepository definitions = parser._layerDefinitions;
        for (int i = 0; i < expectedLayers.length; i++) {
            final StringPattern[] layer = definitions.getLayer(layerNames[i]);
            final int n = Math.min(layer.length, expectedLayers[i].length);
            for (int j = 0; j < n; j++) {
                assertEquals("Layer " + layerNames[i] + " " + j, expectedLayers[i][j], layer[j] + "");
            }
            final int d = expectedLayers[i].length - layer.length;
            if (d > 0) {
                fail(d + " terms missed");
            }
            else if (d < 0) {
                fail(-d + " unexpected terms");
            }
        }
    }

    private static class MockPreference implements Preference {
        String _preference;

        public MockPreference(final String preference) {
            _preference = preference;
        }

        @Override
        public String getKey() {
            return _preference;
        }

        @Override
        public String toString() {
            return _preference;
        }
    }

    private static class MockPreferenceFactory implements PreferenceFactory {
        private final HashMap<String, Preference> _keyToPreferenceMap = new HashMap<String, Preference>();

        @Override
        public Preference get(final String key) {
            Preference preference = _keyToPreferenceMap.get(key);
            if (preference == null) {
                preference = new MockPreference(key);
                _keyToPreferenceMap.put(key, preference);
            }
            return preference;
        }
    }

    private static class MockResultRenderer extends ResultRenderer {
        List<Preference> list = new ArrayList<Preference>();
        private final PreferenceFactory _factory = new MockPreferenceFactory();

        @Override
        public void considerPreference(final Preference preference) {
            list.add(preference);
        }

        @Override
        public Result getDescriptionOfCurrentPreferences() {
            return new MockResult();
        }

        @Override
        public PreferenceFactory getPreferenceFactory() {
            return _factory;
        }

        @Override
        public String render(final Result result) {
            return null;
        }
    }

    private static class MockResult implements Result {
        @Override
        public boolean isOk() {
            return true;
        }
    }
}
