/*
 * Created on 30.08.2004
 */
package classycle.dependency;

import java.util.ArrayList;
import java.util.HashMap;

import classycle.util.StringPattern;

import junit.framework.TestCase;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyDefinitionParserTest extends TestCase
{
  
  public void testShowStatements() 
  {
    check("show a b c", new String[] {"[a, b, c]"});
    check("show a\n show b c", new String[] {"[a]", "[b, c]"});
  }
  
  public void testDependencyStatements() 
  {
    check("check java.lang.* independentOf java.awt.*", 
          new String[] {"check java.lang.* independentOf java.awt.*"});
    check("[lang] = java.lang.*\n"
          + "show shortestPathsOnly\n"
          + "check [lang] independentOf java.awt.*",  
          new String[] {"[shortestPathsOnly]",
                        "check [lang] independentOf java.awt.*"});
    check("[lang] = java.lang.*\n"
          + "  check   [lang]   java.util.*   independentOf java.awt.*",  
          new String[] {"check [lang] java.util.* independentOf java.awt.*"});
    check("[lang] = java.lang.*\n"
          + "  check   [lang]   java.util.*   independentOf\\\njava.awt.*",  
          new String[] {"check [lang] java.util.* independentOf java.awt.*"});
    check("######\n"
          + "[lang] = java.lang.*\n"
          + "# this is a comment\n"
          + "    # and this too\n\r\n"
          + "check [lang] java.util.* independentOf \\ \n  java.awt.*\n"  
          + "check java.util.* independentOf \\\n   javax.swing.* java.awt.*\n",  
          new String[] {"check [lang] java.util.* independentOf java.awt.*", 
                 "check java.util.* independentOf javax.swing.* java.awt.*"});
    check("[lang] = java.lang.*\n"
          + "check [lang] java.util.* independentOf\\  \n# hello\njava.awt.*",  
          new String[] {"check [lang] java.util.* independentOf java.awt.*"});
    check("[lang] = java.lang.*\n"
          + "[a] = [lang] excluding java.lang.Class\n"
          + "check [a] independentOf java.awt.*",  
          new String[] {"check [a] independentOf java.awt.*"});
  }
  
  public void testLayeringStatements() 
  {
    check("layer a = java.lang.*\n"
          + "layer b = java.util.*\n"
          + "check layeringOf a b", new String[] {"check layeringOf a b"});
    check("layer a = java.lang.*\n"
          + "layer b = java.util.*\n"
          + "check strictLayeringOf a b", 
          new String[] {"check strictLayeringOf a b"});
  }
  
  public void testSetDefinition() 
  {
    check("[a] = j.*", new String[][] {{"[a]", "j.*"}});
    check("[a] = j.* k.*", new String[][] {{"[a]", "(j.* k.*)"}});
    check("[a] = j.* k.* excluding a.b", 
          new String[][] {{"[a]", "((j.* k.*) & !a.b)"}});
    check("[a] = j.* k.* excluding a.b c.d", 
          new String[][] {{"[a]", "((j.* k.*) & !(a.b c.d))"}});
    check("[a] = k$t.*_$ excluding a.b c.d", 
          new String[][] {{"[a]", "(k$t.*_$ & !(a.b c.d))"}});
    check("[a] = excluding a.b c.d", 
          new String[][] {{"[a]", "!(a.b c.d)"}});
  }
  
  public void testSetDefinitions() 
  {
    check("[a] = a.*\n"
          + "[b] = b.*\n"
          + "[c] = [a] excluding j.*", 
          new String[][] {{"[a]", "a.*"},
                          {"[b]", "b.*"},
                          {"[c]", "(a.* & !j.*)"}
                         });
    check("[a] = a.*\n"
          + "[b] = b.*\n"
          + "[c] = [a] j.*\n"
          + "[d] = j.* [b] excluding [c] k.*\n", 
          new String[][] {{"[a]", "a.*"},
                          {"[b]", "b.*"},
                          {"[c]", "(a.* j.*)"},
                          {"[d]", "((j.* b.*) & !((a.* j.*) k.*))"}
                         });
  }
  
  public void testLayerDefinitions()
  {
    check("[a] = a.*\n"
          + "[b] = b.*\n"
          + "[c] = [a] excluding j.*\n"
          + "layer a = z.*\n"
          + "layer b = [c]\n"
          + "layer bla = [a] [c]\n"
          + "layer c = [a] s.*\n",
          new String[] {"a", "b", "bla", "c"},
          new String[][] {{"z.*"},
                          {"(a.* & !j.*)"},
                          {"a.*", "(a.* & !j.*)"},
                          {"a.*", "s.*"}
                         });    
  }
  
  private void check(String definition, String[] expectedStatements) 
  {
    DependencyDefinitionParser parser 
        = new DependencyDefinitionParser(definition, new MockResultRenderer());
    Statement[] statements = parser.getStatements();
    int len = Math.min(statements.length, expectedStatements.length);
    for (int i = 0; i < len; i++)
    {
      assertEquals("Statement " + (i + 1), expectedStatements[i], 
                   statements[i].toString());
    }
    if (len < statements.length)
    {
      fail(statements.length - len + " additional statements");
    }
    if (len < expectedStatements.length)
    {
      fail(expectedStatements.length - len + " missing statements");
    }
  }
  
  private void check(String definition, String[][] expectedSets) 
  {
    DependencyDefinitionParser parser 
        = new DependencyDefinitionParser(definition, new MockResultRenderer());
    SetDefinitionRepository definitions = parser._setDefinitions;
    for (int i = 0; i < expectedSets.length; i++)
    {
      String setName = expectedSets[i][0];
      String expectedSet = expectedSets[i][1];
      assertEquals("Set " + setName, expectedSet, 
                   definitions.getPattern(setName) + "");
    }
  }
  
  private void check(String definition, String[] layerNames, 
                     String[][] expectedLayers)
  {
    DependencyDefinitionParser parser 
        = new DependencyDefinitionParser(definition, new MockResultRenderer());
    LayerDefinitionRepository definitions = parser._layerDefinitions;
    for (int i = 0; i < expectedLayers.length; i++)
    {
      StringPattern[] layer = definitions.getLayer(layerNames[i]);
      int n = Math.min(layer.length, expectedLayers[i].length);
      for (int j = 0; j < n; j++)
      {
        assertEquals("Layer " + layerNames[i] + " " + j, 
                     expectedLayers[i][j], layer[j] + "");
      }
      int d = expectedLayers[i].length - layer.length;
      if (d > 0)
      {
        fail(d + " terms missed");
      } else if (d < 0) 
      {
        fail(-d + " unexpected terms");
      }
    }
  }
  
  private static class MockPreference implements Preference
  {
    String _preference;
    public MockPreference(String preference)
    {
      _preference = preference;
    }
    public String getKey()
    {
      return _preference;
    }
    public String toString()
    {
      return _preference;
    }
  }
  
  private static class MockPreferenceFactory implements PreferenceFactory
  {
    private final HashMap _keyToPreferenceMap = new HashMap();
    public Preference get(String key)
    {
      Preference preference = (Preference) _keyToPreferenceMap.get(key);
      if (preference == null)
      {
        preference = new MockPreference(key);
        _keyToPreferenceMap.put(key, preference);
      }
      return preference;
    }
  }
  
  private static class MockResultRenderer implements ResultRenderer
  {
    ArrayList list = new ArrayList();
    boolean shortestPathsOnly;
    private PreferenceFactory _factory = new MockPreferenceFactory();
    public void considerPreference(Preference preference)
    {
      list.add(preference);
    }
    public Result getDescriptionOfCurrentPreferences()
    {
      return new MockResult(list.toString());
    }
    public PreferenceFactory getPreferenceFactory()
    {
      return _factory;
    }
    public boolean onlyShortestPaths()
    {
      return shortestPathsOnly;
    }
    public String render(Result result)
    {
      return null;
    }
  }
  
  private static class MockResult implements Result
  {
    String value;
    public MockResult(String value)
    {
      this.value = value;
    }
  }
}
