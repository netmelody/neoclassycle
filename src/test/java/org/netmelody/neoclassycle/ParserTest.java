package org.netmelody.neoclassycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.graph.NameAttributes;
import org.netmelody.neoclassycle.graph.Vertex;
import org.netmelody.neoclassycle.util.StringPattern;
import org.netmelody.neoclassycle.util.TrueStringPattern;
import org.netmelody.neoclassycle.util.WildCardPattern;

/**
 * @author Franz-Josef Elmer
 */
public final class ParserTest {
  private static final String INNER_CLASS_EXAMPLE = "class Test {" 
              + "interface A { String b();}"
              + "Integer i;}";
  private static final String REFLECTION_EXAMPLE 
      = "class Test { "
      + "  String[] a = {\"java.util.Date\", \"hello\", \"www.w3c.org\"};"
      + "  Class c = Integer.class;"
      + "}";
  private static final String CLASS_NAME = "Test";
  
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  private static int compile(String file)
  {
    try {
        final Class<?> compilerClass = Class.forName("com.sun.tools.javac.Main", true, compilerClassLoader());
        final Object compiler = compilerClass.newInstance();
        final Method compileMethod = compilerClass.getMethod("compile", String[].class);
        return (Integer)compileMethod.invoke(compiler, ((Object)new String[] {file, "-target", "1.5"}));
    } catch (Exception e) {
        throw new IllegalStateException(e);
    }
  }

  private static ClassLoader compilerClassLoader() {
      try {
          final List<URL> urls = new ArrayList<URL>();
          urls.add(new File(System.getenv("JAVA_HOME") + File.separator + "lib" + File.separator + "tools.jar").toURI().toURL());
          return new URLClassLoader(urls.toArray(new URL[urls.size()]));
      } catch (Exception e) {
          throw new IllegalStateException(e);
      }
  }
  
  private AtomicVertex createVertex(String code, 
                                           StringPattern reflectionPattern, 
                                           boolean mergeInnerClasses) 
                 throws IOException 
  {
    final File sourceFile = new File(folder.getRoot(), CLASS_NAME + ".java");
    Writer writer = new FileWriter(sourceFile);
    writer.write(code);
    writer.close();
    assertEquals("Exit code", 0, compile(sourceFile.getAbsolutePath()));
    AtomicVertex[] vertices = Parser.readClassFiles(new String[] {folder.getRoot().getAbsolutePath()}, 
                                                    new TrueStringPattern(), 
                                                    reflectionPattern, 
                                                    mergeInnerClasses);
    for (int i = 0; i < vertices.length; i++)
    {
      NameAttributes attributes = (NameAttributes) vertices[i].getAttributes();
      if (attributes.getName().equals(CLASS_NAME))
      {
        AtomicVertex vertex = vertices[i];
        return vertex;
      }
    }
    throw new IOException("Test class not found: " + Arrays.asList(vertices));
  }

  private void check(String[] expectedClasses, String javaCode) 
               throws IOException 
  {
     check(expectedClasses, javaCode, null, false); 
  }

  private void check(String[] expectedClasses, String javaCode, 
                     StringPattern reflectionPattern, 
                     boolean mergeInnerClasses) 
               throws IOException {
    AtomicVertex vertex = createVertex(javaCode, reflectionPattern, 
                                       mergeInnerClasses);
    assertEquals(folder.getRoot().getAbsolutePath(), ((ClassAttributes) vertex.getAttributes()).getSources());
    HashSet<String> classSet = new HashSet<String>();
    for (int i = 0; i < expectedClasses.length; i++) {
      classSet.add(expectedClasses[i]);
    }
    for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i < n; i++) {
      Vertex v = vertex.getHeadVertex(i);
      String name = ((ClassAttributes) v.getAttributes()).getName();
      assertTrue(name + " not expected", classSet.contains(name));
      classSet.remove(name); 
    }
    assertEquals("number of classes (missing: " + classSet + ")", 
                 expectedClasses.length, 
                 vertex.getNumberOfOutgoingArcs());
  }

  @Test
  public void testParseFieldDescriptor() throws IOException {
    check(new String[] {"java.lang.Object", "java.awt.color.ICC_ColorSpace", 
                        "java.lang.String", "java.awt.LayoutManager2",
                        "java.lang.StringBuffer"},
          "class Test { String[][] a; java.awt.LayoutManager2 b; int i;"
          + "StringBuffer[] sb;"
          + "double[] d; boolean[][] z; java.awt.color.ICC_ColorSpace cs;}");
  }

  @Test
  public void testNoReflection() throws IOException
  {
    check(new String[] {"java.lang.Object", "java.lang.String", 
                        "java.lang.Class",  "java.lang.Integer"},
          REFLECTION_EXAMPLE);
  }

  @Test
  public void testReflection() throws IOException
  {
    check(new String[] {"java.lang.Object", "java.lang.String", 
            "java.lang.Class", "java.lang.Integer",
            "java.util.Date", 
            "hello", "www.w3c.org"},
            REFLECTION_EXAMPLE,
            new TrueStringPattern(), false);
    check(new String[] {"java.lang.Object", "java.lang.String", 
            "java.lang.Class", "java.lang.Integer", 
            "java.util.Date"},
            REFLECTION_EXAMPLE,
            new WildCardPattern("java.*"), false);
  }

  @Test
  public void testInvalidFieldDescriptors() throws IOException {
    check(new String[] {"java.lang.Object", "java.lang.String"},
          "class Test { String[] a = {\"La;a\", \"[Lb;?\", \"L;\", \"L ;\","
          + "\"La.;\", \"L.a;\", \"La..b;\", \"L1;\", };}");
  }

  @Test
  public void testParseMethodDescriptor() throws IOException {
    check(new String[] {"java.lang.String", "java.lang.Integer",
                        "java.lang.Double", "java.lang.Boolean",
                        "java.lang.Byte", "java.lang.Object", 
                        "java.lang.Long", "java.lang.Short",
                        "java.lang.Exception", "java.lang.Class"},
          "interface Test { void a(String a); int b(); Integer c();"
          + "Exception d(double d, Double d2); int e(Boolean[][] z);"
          + "short f(Byte b, int[][] i); Short g(Long l, Class c);}");
  }

  @Test
  public void testInvalidMethodDescriptors() throws IOException {
    check(new String[] {"java.lang.Object", "java.lang.String"},
          "class Test { String[] a = {\"(La;\", \"(La1;??\", \"(Lb;)\","
          + "\"(Lc;)?\", \"(Ld;)v\", \"(Le;)V?\", \"(Lf;)D?\", \"(Lg;)L1;\","
          + "\"(Lh;)Li;?\", \"([)Lj;\", \"(L2;)Lk;\", \"( )Ll;\", \"(d)Lm;\","
          + "\"(Ln;[)Lo;\", \"(Lp;L)V\"};}");
  }

  @Test
  public void testIndirectReference() throws IOException {
    check(new String[] {"java.lang.Object", "java.util.Enumeration", "java.lang.System",
                        "java.util.Properties"},
          "class Test { Object e = System.getProperties().keys();}");
  }

  @Test
  public void testConstantsReference() throws IOException {
    check(new String[] {"java.lang.Object"},
            "class Test { int e = java.awt.Label.LEFT;}");
  }

  @Test
  public void testSuperClass() throws IOException {
    check(new String[] {"java.lang.Runnable", "java.awt.Canvas"},
            "class Test extends java.awt.Canvas implements Runnable {"
            + "public void run() {}}");
  }

  @Test
  public void testCastingElementaryDataTypeArray() throws IOException {
    check(new String[] {"java.lang.Object"},
            "class Test { Object a() { return null; } "
            + "void b() { byte[][] n = (byte[][]) a();}}");
  }

  @Test
  public void testInnerClasses() throws IOException {
    check(new String[] {"java.lang.Object", "java.lang.Integer", "Test$A"},
            INNER_CLASS_EXAMPLE);
  }

  @Test
  public void testMergeInnerClasses() throws IOException {
    check(new String[] {"java.lang.Object", "java.lang.Integer", 
                        "java.lang.String"},
            INNER_CLASS_EXAMPLE, new TrueStringPattern(), true);

    // check that size of merged vertices is the sum of vertices sizes
    AtomicVertex vertex = createVertex(INNER_CLASS_EXAMPLE, 
                                       new TrueStringPattern(), false);
    int size1 = ((ClassAttributes) vertex.getAttributes()).getSize();
    Vertex innerClass = null;
    for (int i = 0; i < vertex.getNumberOfOutgoingArcs(); i++)
    {
      innerClass = vertex.getHeadVertex(i);
      String name = ((ClassAttributes) innerClass.getAttributes()).getName();
      if (name.startsWith(CLASS_NAME))
      {
        break;
      }
    }
    int size2 = ((ClassAttributes) innerClass.getAttributes()).getSize();
    vertex = createVertex(INNER_CLASS_EXAMPLE, new TrueStringPattern(), true);
    int size = ((ClassAttributes) vertex.getAttributes()).getSize();
    assertEquals(size1 + size2, size);
  }
  
}
