package classycle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;

import junit.framework.TestCase;

import classycle.classfile.ConstantPoolPrinter;
import classycle.graph.AtomicVertex;
import com.sun.tools.javac.Main;

/**
 * 
 * 
 * @author Franz-Josef Elmer
 */
public class ParserTest extends TestCase {
  private static final Main JAVAC = new Main();
  private static final String TMP = "temporaryDirectory" + File.separator;
  private static final String CLASS_NAME = "Test";
  private static final String JAVA_FILE = TMP + CLASS_NAME + ".java";
  private static final String CLASS_FILE = TMP + CLASS_NAME + ".class";
  
  public static void main(String[] args) throws IOException {
    Writer writer = new FileWriter("Test.java");
    writer.write(args[0]);
    writer.close();
    if (JAVAC.compile(new String[] {"Test.java"}) == 0) {
      ConstantPoolPrinter.main(new String[] {"Test.class"});
    }
  }
  
  private static AtomicVertex createVertex(String code) throws IOException {
    Writer writer = new FileWriter(JAVA_FILE);
    writer.write(code);
    writer.close();
    assertEquals("Exit code", 0, JAVAC.compile(new String[] {JAVA_FILE}));
    return Parser.readClassFiles(new String[] {CLASS_FILE})[0];
  }

  public ParserTest(String name) {
    super(name);
  }
  
  protected void setUp() throws Exception {
    new File(TMP).mkdir();
  }

  protected void tearDown() throws Exception {
    new File(JAVA_FILE).delete();
    new File(CLASS_FILE).delete();
    new File(TMP).delete();
  }

  private void check(String[] expectedClasses, String javaCode) 
                                                          throws IOException {
    AtomicVertex vertex = createVertex(javaCode);
    HashSet classSet = new HashSet();
    for (int i = 0; i < expectedClasses.length; i++) {
      classSet.add(expectedClasses[i]);
    }
    for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i < n; i++) {
      String name = ((ClassAttributes) vertex.getHeadVertex(i).getAttributes())
                                                                    .getName();
      assertTrue(name + " not expected", classSet.contains(name)); 
    }
    assertEquals("number of classes", expectedClasses.length, 
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
            "class Test { Object e = System.getProperties().elements();}");
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
  
}
