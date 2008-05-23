package classycle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;
import classycle.graph.AtomicVertex;
import classycle.graph.NameAttributes;
import classycle.graph.Vertex;
import classycle.util.StringPattern;
import classycle.util.TrueStringPattern;
import classycle.util.WildCardPattern;

import com.sun.tools.javac.Main;

/**
 * 
 * 
 * @author Franz-Josef Elmer
 */
public class ParserTest extends TestCase {
  private static final String INNER_CLASS_EXAMPLE = "class Test {" 
              + "interface A { String b();}"
              + "Integer i;}";
  private static final String REFLECTION_EXAMPLE 
      = "class Test { "
      + "  String[] a = {\"java.util.Vector\", \"hello\", \"www.w3c.org\"};"
      + "  Class c = Thread.class;"
      + "}";
  private static final Main JAVAC = new Main();
  private static final String TMP = "temporaryDirectory" + File.separator;
  private static final String CLASS_NAME = "Test";
  private static final String JAVA_FILE = TMP + CLASS_NAME + ".java";
  private static final String CLASS_FILE = TMP + CLASS_NAME + ".class";
  
  private static int compile(String file)
  {
    return JAVAC.compile(new String[] {file, "-target", "1.1"});
  }

  private static AtomicVertex createVertex(String code, 
                                           StringPattern reflectionPattern, 
                                           boolean mergeInnerClasses) 
                 throws IOException 
  {
    Writer writer = new FileWriter(JAVA_FILE);
    writer.write(code);
    writer.close();
    assertEquals("Exit code", 0, compile(JAVA_FILE));
    AtomicVertex[] vertices = Parser.readClassFiles(new String[] {TMP}, 
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

  public ParserTest(String name) {
    super(name);
  }
  
  protected void setUp() throws Exception {
    new File(TMP).mkdir();
  }

  protected void tearDown() throws Exception {
    File dir = new File(TMP);
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++)
    {
      files[i].delete();
    }
    dir.delete();
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
    assertEquals(TMP, ((ClassAttributes) vertex.getAttributes()).getSource());
    HashSet classSet = new HashSet();
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

  public void testParseFieldDescriptor() throws IOException {
    check(new String[] {"java.lang.Object", "java.awt.color.ICC_ColorSpace", 
                        "java.lang.String", "java.awt.LayoutManager2",
                        "java.lang.StringBuffer"},
          "class Test { String[][] a; java.awt.LayoutManager2 b; int i;"
          + "StringBuffer[] sb;"
          + "double[] d; boolean[][] z; java.awt.color.ICC_ColorSpace cs;}");
  }
  
  public void testNoReflection() throws IOException
  {
    check(new String[] {"java.lang.Object", "java.lang.String", 
                        "java.lang.Class", "java.lang.NoClassDefFoundError",
                        "java.lang.ClassNotFoundException", 
                        "java.lang.Throwable"},
          REFLECTION_EXAMPLE);
  }

  public void testReflection() throws IOException
  {
    check(new String[] {"java.lang.Object", "java.lang.String", 
            "java.lang.Class", "java.lang.NoClassDefFoundError",
            "java.lang.ClassNotFoundException", 
            "java.lang.Throwable", 
            "java.util.Vector", "java.lang.Thread", 
            "hello", "www.w3c.org"},
            REFLECTION_EXAMPLE,
            new TrueStringPattern(), false);
    check(new String[] {"java.lang.Object", "java.lang.String", 
            "java.lang.Class", "java.lang.NoClassDefFoundError",
            "java.lang.ClassNotFoundException", 
            "java.lang.Throwable", 
            "java.util.Vector", "java.lang.Thread"},
            REFLECTION_EXAMPLE,
            new WildCardPattern("java.*"), false);
  }

  public void testInvalidFieldDescriptors() throws IOException {
    check(new String[] {"java.lang.Object", "java.lang.String"},
          "class Test { String[] a = {\"La;a\", \"[Lb;?\", \"L;\", \"L ;\","
          + "\"La.;\", \"L.a;\", \"La..b;\", \"L1;\", };}");
  }
  
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
  
  public void testInvalidMethodDescriptors() throws IOException {
    check(new String[] {"java.lang.Object", "java.lang.String"},
          "class Test { String[] a = {\"(La;\", \"(La1;??\", \"(Lb;)\","
          + "\"(Lc;)?\", \"(Ld;)v\", \"(Le;)V?\", \"(Lf;)D?\", \"(Lg;)L1;\","
          + "\"(Lh;)Li;?\", \"([)Lj;\", \"(L2;)Lk;\", \"( )Ll;\", \"(d)Lm;\","
          + "\"(Ln;[)Lo;\", \"(Lp;L)V\"};}");
  }
  
  public void testIndirectReference() throws IOException {
    check(new String[] {"java.lang.Object", "java.util.Enumeration",
                        "java.util.Hashtable", "java.lang.System",
                        "java.util.Properties"},
          "class Test { Object e = System.getProperties().keys();}");
  }
  
  public void testConstantsReference() throws IOException {
    check(new String[] {"java.lang.Object"},
            "class Test { int e = java.awt.Label.LEFT;}");
  }
  
  public void testSuperClass() throws IOException {
    check(new String[] {"java.lang.Runnable", "java.awt.Canvas"},
            "class Test extends java.awt.Canvas implements Runnable {"
            + "public void run() {}}");
  }
  
  public void testCastingElementaryDataTypeArray() throws IOException {
    check(new String[] {"java.lang.Object"},
            "class Test { Object a() { return null; } "
            + "void b() { byte[][] n = (byte[][]) a();}}");
  }
  
  public void testInnerClasses() throws IOException {
    check(new String[] {"java.lang.Object", "java.lang.Integer", "Test$A"},
            INNER_CLASS_EXAMPLE);
  }
  
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
