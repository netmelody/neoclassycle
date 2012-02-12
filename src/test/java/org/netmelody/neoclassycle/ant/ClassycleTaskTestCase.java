/*
 * Created on 01.07.2004
 */
package org.netmelody.neoclassycle.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.tools.ant.BuildFileTest;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

/**
 * @author Franz-Josef Elmer
 */
public abstract class ClassycleTaskTestCase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public final BuildFileTest antTestcase = new BuildFileTest() {
    };

    protected void checkNumberOfLines(final int expectedNumberOfLines, final String fileName) throws Exception {
        final File file = new File(folder.getRoot(), fileName);
        final FileReader reader = new FileReader(file);
        checkNumberOfLines(reader, expectedNumberOfLines);
    }

    protected void checkNumberOfLines(final Reader reader, final int expectedNumberOfLines) throws IOException {
        final BufferedReader br = new BufferedReader(reader);
        int numberOfLines = 0;
        while (br.readLine() != null) {
            numberOfLines++;
        }
        assertEquals("Number of lines", expectedNumberOfLines, numberOfLines);
    }

    protected void checkLine(final String expectedLine, final int lineNumber, final String fileName) throws Exception {
        final File file = new File(folder.getRoot(), fileName);
        final FileReader reader = new FileReader(file);
        checkLine(reader, expectedLine, lineNumber);
    }

    protected void checkLine(final Reader reader, final String expectedLine, int lineNumber) throws IOException {
        final BufferedReader br = new BufferedReader(reader);

        int lineNo = lineNumber;
        String line = null;
        while ((line = br.readLine()) != null && --lineNo > 0) { }
        
        assertEquals(expectedLine, line);
    }

    protected String readFile(final String fileName) throws Exception {
        final File file = new File(folder.getRoot(), fileName);
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        return builder.toString();

    }

    protected void configureProject(final String antBuildFilename) {
        final File antBuildFile = copyResource(antBuildFilename, folder.getRoot());

        folder.newFolder("classes/example/p").mkdirs();
        final File classesDir = new File(folder.getRoot(), "classes");
        
        copyResource("example/A.class", classesDir);
        copyResource("example/AA.class", classesDir);
        copyResource("example/B.class", classesDir);
        copyResource("example/B$M.class", classesDir);
        copyResource("example/BofA.class", classesDir);
        copyResource("example/p/A.class", classesDir);

        try {
            System.setProperty("classes.dir", classesDir.getAbsolutePath());
            antTestcase.configureProject(antBuildFile.getAbsolutePath());
        }
        catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private File copyResource(final String resourceName, File dest) {
        final File file = new File(dest, resourceName);
        final InputStream in = ClassycleTaskTestCase.class.getResourceAsStream("/" + resourceName);
        try {
            final FileOutputStream out = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
            out.close();
            in.close();
        }
        catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        return file;
    }

    protected void executeTarget(final String targetName) {
        antTestcase.executeTarget(targetName);
    }

    protected String getOutput() {
        return antTestcase.getOutput();
    }
}
