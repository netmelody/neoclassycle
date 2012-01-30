/*
 * Created on 30.06.2004
 */
package classycle.util;

import junit.framework.TestCase;

/**
 * @author  Franz-Josef Elmer
 */
public class TextTest extends TestCase
{

  public void testExcapeForXML()
  {
    assertEquals("&lt;hel&amp;lo&gt;", Text.excapeForXML("<hel&lo>"));
  }

}
