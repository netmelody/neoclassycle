/*
 * Created on 30.08.2004
 */
package classycle.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import junit.framework.TestCase;
import classycle.util.StringPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyDefinitionParserTest extends TestCase
{
  
  private Hashtable _defaultProps;

  public void testShowStatements() 
  {
    check("show a b c", new String[] {"[a, b, c]"});
    check("show a\n show b c", new String[] {"[a]", "[b, c]"});
  }
  
  public void testCheckSetStatements() 
  {
    check("check sets ${package}.lang.*", new String[] {"check set java.lang.*"});
    check("[lang] = java.lang.*\n"
          + "check sets [lang] java.util.*", 
          new String[] {"check set [lang]", 
                        "check set java.util.*"});
  }
  
  public void testCheckCycleStatements() 
  {
    check("[base] = base.*\n"
          + "check   absenceOfClassCycles   >    10  in   java.lang.*\n"
          + "check absenceOfPackageCycles > 1  in [base]", 
            new String[] {"check absenceOfClassCycles > 10 in java.lang.*",
                          "check absenceOfPackageCycles > 1 in [base]"});
  }
  
  public void testCheckMissSpelledKeywords()
  {
    try
    {
      createParser("[set] = b.* exluding *Test");
      fail("IllegalArgumentException expected because of miss-spelled keyword");
    } catch (IllegalArgumentException e)
    {
      assertContains("exluding", e);
    }

    try
    {
      createParser("check b.* independentof [set]");
      fail("IllegalArgumentException expected because of miss-spelled keyword");
    } catch (IllegalArgumentException e)
    {
      assertContains("independentof", e);
    }
    
    try
    {
      createParser("check b.* dependingOn [set]");
      fail("IllegalArgumentException expected because of miss-spelled keyword");
    } catch (IllegalArgumentException e)
    {
      assertContains("dependingOn", e);
    }
    
    try
    {
      createParser("check layeringof [set]");
      fail("IllegalArgumentException expected because of miss-spelled keyword");
    } catch (IllegalArgumentException e)
    {
      assertContains("layeringof", e);
    }
    
    try
    {
      createParser("layer l = layer");
      fail("IllegalArgumentException expected because of miss-spelled keyword");
    } catch (IllegalArgumentException e)
    {
      assertContains("layer", e);
    }
    
    try
    {
      createParser("check sets set1");
      fail("IllegalArgumentException expected because of miss-spelled keyword");
    } catch (IllegalArgumentException e)
    {
      assertContains("set1", e);
    }
    
  }

  private void assertContains(String expectedMessageFragment, Throwable throwable)
  {
    
    String message = throwable.getMessage();
    assertTrue("<" + message + "> does not contain <" + message + ">", 
               message.indexOf(expectedMessageFragment) >= 0);
  }
  
  public void testDependencyStatements() 
  {
    check("check java.lang.Integer dependentOnlyOn java.lang.Number", 
          new String[] {"check java.lang.Integer dependentOnlyOn java.lang.Number"});
    check("check java.lang.* independentOf ${awt}", 
            new String[] {"check java.lang.* independentOf java.awt.*"});
    check("[lang] = java.lang.*\n"
          + "show shortestPathsOnly\n"
          + "check [lang] independentOf java.awt.*",  
          new String[] {"[shortestPathsOnly]",
                        "check [lang] independentOf java.awt.*"});
    check("[lang] = java.lang.*\n"
          + "  check   [lang]   ${package}.util.*   independentOf ${awt}",  
          new String[] {"check [lang] java.util.* independentOf java.awt.*"});
    check("{jojo}   =   ${package}   \n"
          + "[lang] = java.lang.*\n"
          + "  check   [lang]   ${jojo}.util.*   independentOf\\\njava.awt.*",  
          new String[] {"check [lang] java.util.* independentOf java.awt.*"});
    assertEquals(2, _defaultProps.size()); // We expect ${jojo} not in there
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
    check("[a] = j.* k.*", new String[][] {{"[a]", "j.* k.*"}});
    check("[a] = j.* k.* excluding a.b", 
          new String[][] {{"[a]", "(j.* k.* & !a.b)"}});
    check("[a] = j.* k.* excluding a.b c.d", 
          new String[][] {{"[a]", "(j.* k.* & !(a.b c.d))"}});
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
                          {"[c]", "a.* j.*"},
                          {"[d]", "(j.* b.* & !(a.* j.* k.*))"}
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
    DependencyDefinitionParser parser = createParser(definition);
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

  private DependencyDefinitionParser createParser(String definition)
  {
    _defaultProps = new Hashtable();
    _defaultProps.put("package", "java");
    _defaultProps.put("awt", "java.awt.*");
    DependencyProperties properties = new DependencyProperties(_defaultProps);
    DependencyDefinitionParser parser 
        = new DependencyDefinitionParser(definition, properties, 
                                         new MockResultRenderer());
    return parser;
  }
  
  private void check(String definition, String[][] expectedSets) 
  {
    DependencyDefinitionParser parser = createParser(definition);
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
    DependencyDefinitionParser parser = createParser(definition);
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
  
  private static class MockResultRenderer extends ResultRenderer
  {
    ArrayList list = new ArrayList();
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
    public boolean isOk()
    {
      return true;
    }
  }
}
