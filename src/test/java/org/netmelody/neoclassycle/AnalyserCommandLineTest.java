/*
 * Created on 07.07.2004
 */
package org.netmelody.neoclassycle;

import org.junit.Test;
import org.netmelody.neoclassycle.util.StringPattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz-Josef Elmer
 */
public final class AnalyserCommandLineTest {
    @Test
    public void testNoOptionsNoClasses() {
        final AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[0]);
        assertFalse(commandLine.isValid());
        assertEquals(0, commandLine.getClassFiles().length);
    }

    @Test
    public void testNoOptions() {
        AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[] { "bla" });
        checkNoOptions(commandLine);
        assertEquals(1, commandLine.getClassFiles().length);
        assertEquals("bla", commandLine.getClassFiles()[0]);
        assertEquals("bla", commandLine.getTitle());

        commandLine = new AnalyserCommandLine(new String[] { "bla", "foo.jar" });
        checkNoOptions(commandLine);
        assertEquals(2, commandLine.getClassFiles().length);
        assertEquals("bla", commandLine.getClassFiles()[0]);
        assertEquals("foo.jar", commandLine.getClassFiles()[1]);
        assertEquals("bla", commandLine.getTitle());
    }

    private static void checkNoOptions(final AnalyserCommandLine commandLine) {
        assertFalse(commandLine.isRaw());
        assertFalse(commandLine.isStrong());
        assertFalse(commandLine.isPackagesOnly());
        assertFalse(commandLine.isCycles());
        assertNull(commandLine.getCsvFile());
        assertNull(commandLine.getXmlFile());
        final StringPattern pattern = commandLine.getPattern();
        assertTrue(pattern.matches("blabla"));
        assertTrue(commandLine.isValid());
    }

    @Test
    public void testOptionsNoClasses() {
        AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[] { "-raw" });
        assertFalse(commandLine.isValid());
        assertEquals(0, commandLine.getClassFiles().length);

        commandLine = new AnalyserCommandLine(new String[] { "-raw", "-title=42" });
        assertFalse(commandLine.isValid());
        assertEquals(0, commandLine.getClassFiles().length);
    }

    @Test
    public void testOptionTitle() {
        final AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[] { "-title=42", "foo.jar" });
        assertTrue(commandLine.isValid());
        assertEquals(1, commandLine.getClassFiles().length);
        assertEquals("foo.jar", commandLine.getClassFiles()[0]);
        assertEquals("42", commandLine.getTitle());
    }

    @Test
    public void testOptionIncludingClasses() {
        final AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[] { "-includingClasses=foo*,*la", "foo.jar" });
        assertTrue(commandLine.isValid());
        assertEquals(1, commandLine.getClassFiles().length);
        assertEquals("foo.jar", commandLine.getClassFiles()[0]);
        assertEquals("foo.jar", commandLine.getTitle());
        final StringPattern pattern = commandLine.getPattern();
        checkPattern(pattern, true, new String[] { "foo", "foo.jar", "foola", "bla" });
        checkPattern(pattern, false, new String[] { "bla.jar", "fo" });
    }

    @Test
    public void testOptionExcludingClasses() {
        final AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[] { "-excludingClasses=foo*,*la", "foo.jar" });
        assertTrue(commandLine.isValid());
        final StringPattern pattern = commandLine.getPattern();
        checkPattern(pattern, false, new String[] { "foo", "foo.jar", "foola", "bla" });
        checkPattern(pattern, true, new String[] { "bla.jar", "fo" });
    }

    @Test
    public void testOptionIncludingExcludingClasses() {
        final AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[] { "-includingClasses=foo*", "-excludingClasses=*la" });
        final StringPattern pattern = commandLine.getPattern();
        checkPattern(pattern, true, new String[] { "foo", "foo.jar" });
        checkPattern(pattern, false, new String[] { "foola", "bla" });
    }

    private static void checkPattern(final StringPattern pattern, final boolean expectedMatchResult, final String[] examples) {
        for (final String example : examples) {
            assertTrue(example + " match " + expectedMatchResult, pattern.matches(example) == expectedMatchResult);
        }
    }
}
