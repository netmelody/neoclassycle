/*
 * Created on 23.11.2004
 */
package org.netmelody.neoclassycle.ant;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;

import org.apache.tools.ant.BuildException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Franz-Josef Elmer
 */
public class DependencyCheckingTaskTest extends ClassycleTaskTestCase
{
    @Before
    public void setUp() throws Exception
    {
        configureProject("dependencyCheckingTaskTestBuild.xml");
    }

    private void checkNumberOfOutputLines(int number) throws IOException
    {
        checkNumberOfLines(new StringReader(getOutput()), number);
    }

    private void checkLine(String expectedLine, int lineNumber) throws IOException
    {
        checkLine(new StringReader(getOutput()), expectedLine, lineNumber);
    }

    @Test
    public void testEmbeddedDefinitions() throws Exception
    {
        executeTarget("testEmbeddedDefinitions");
        checkNumberOfOutputLines(14);
        checkLine("  <unexpected-dependencies statement='check [A] independentOf [non-A]'/>", 3);
        checkLine("  <unexpected-dependencies statement='check [non-A] independentOf [A]'>", 4);
    }

    @Test
    public void testEmbeddedDefinitionsFailureOn() throws Exception
    {
        try
        {
            executeTarget("testEmbeddedDefinitionsFailureOn");
            fail("BuildException expected");
        }
        catch (BuildException e)
        {
            checkNumberOfOutputLines(10);
            checkLine("check [A] independentOf [non-A]\tOK", 2);
            checkLine("check [non-A] independentOf [A]", 3);
            checkLine("  Unexpected dependencies found:", 4);
        }
    }

    @Test
    public void testCheckCyclesMergedInnerClassesFailureOn() throws Exception
    {
        antTestcase.executeTarget("testCheckCyclesMergedInnerClassesFailureOn");
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n"
                + "<dependency-checking-results>\n"
                + "  <cycles statement='check absenceOfClassCycles &gt; 1 "
                + "in example.*' vertex-type='class'/>\n"
                + "</dependency-checking-results>\n",
                readFile("dependency-checking-result.xml"));
    }

    @Test
    public void testCheckCyclesFailureOn() throws Exception
    {
        try
        {
            antTestcase.executeTarget("testCheckCyclesFailureOn");
            fail("BuildException expected");
        }
        catch (BuildException e)
        {
            checkNumberOfOutputLines(4);
            assertThat(getOutput(),
                    Matchers.<String> either(equalTo("check absenceOfClassCycles > 1 in example.*\n" +
                            "  example.B and inner classes contains 2 classes:\n" +
                            "    example.B\n" +
                            "    example.B$M\n"))
                            .or(equalTo("check absenceOfClassCycles > 1 in example.*\n" +
                                    "  example.B and inner classes contains 2 classes:\n" +
                                    "    example.B$M\n" +
                                    "    example.B\n")));
        }
    }

    @Test
    public void testExcluding() throws Exception
    {
        executeTarget("testExcluding");
        checkNumberOfOutputLines(8);
    }

    @Test
    public void testResetGraphAfterCheck() throws Exception
    {
        executeTarget("testResetGraphAfterCheck");
        checkNumberOfOutputLines(8);
        checkLine("check [A-not-p] independentOf *h*", 1);
        checkLine("check [A-not-p] independentOf *S*", 5);
        checkLine("    -> java.lang.String", 8);
    }

    @Test
    public void testDependentOnlyOn() throws Exception
    {
        executeTarget("testDependentOnlyOn");

        final String[] outputLines = getOutput().split("\\n");
        Arrays.sort(outputLines);

        assertThat(outputLines, Matchers.arrayContaining(
                "    -> example.A",
                "    -> example.A",
                "    -> example.A",
                "    -> example.p.A",
                "    -> example.p.A",
                "    -> java.lang.Class",
                "    -> java.lang.Object",
                "    -> java.lang.Object",
                "    -> java.lang.Thread",
                "  Unexpected dependencies found:",
                "  Unexpected dependencies found:",
                "  Unexpected dependencies found:",
                "  example.B",
                "  example.B$M",
                "  example.B$M",
                "  example.B$M",
                "  example.BofA",
                "  example.BofA",
                "  example.p.A",
                "check [set] dependentOnlyOn java.lang.*",
                "check example.B* dependentOnlyOn *A",
                "check example.B* dependentOnlyOn java.lang.* example.A*"));
    }

    @Test
    public void testReflection() throws Exception
    {
        executeTarget("testReflection");
        checkNumberOfOutputLines(6);
        checkLine("check [A-not-p] independentOf *h*", 1);
        assertTrue(getOutput().indexOf("-> hello") > 0);
    }

    @Test
    public void testReflectionWithRestriction() throws Exception
    {
        executeTarget("testReflectionWithRestriction");
        checkNumberOfOutputLines(4);
        checkLine("check [A-not-p] independentOf *h*", 1);
        assertTrue(getOutput().indexOf("-> java.lang.Thread") > 0);
    }

    @Test
    public void testFile() throws Exception
    {
        Writer writer = new FileWriter(folder.getRoot() + "/test.ddf");
        writer.write("show allResults\n"
                + "[A] = *A*\n"
                + "[non-A] = example.* excluding [A]\n"
                + "check [A] independentOf [non-A]");
        writer.close();
        executeTarget("testFile");
        checkNumberOfOutputLines(2);
        checkLine("check [A] independentOf [non-A]\tOK", 2);
    }

    @Test
    public void testNoClasses() throws Exception
    {
        try
        {
            executeTarget("testNoClasses");
            fail("BuildException expected");
        }
        catch (BuildException e)
        {
            checkNumberOfOutputLines(0);
        }
    }

    @Test
    public void testEmpty() throws Exception
    {
        try
        {
            executeTarget("testEmpty");
            fail("BuildException expected");
        }
        catch (BuildException e)
        {
            checkNumberOfOutputLines(0);
            assertEquals("Empty dependency definition.", e.getMessage());
        }
    }
}
