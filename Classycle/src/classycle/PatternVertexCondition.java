/*
 * Created on 19.06.2004
 */
package classycle;

import classycle.graph.Attributes;
import classycle.graph.NameAttributes;
import classycle.graph.Vertex;
import classycle.graph.VertexCondition;
import classycle.util.StringPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class PatternVertexCondition implements VertexCondition
{
  private final StringPattern _pattern;
  
  public PatternVertexCondition(StringPattern pattern)
  {
    _pattern = pattern;
  }

  public boolean isFulfilled(Vertex vertex)
  {
    boolean result = false;
    if (vertex != null)
    {
      Attributes attributes = vertex.getAttributes();
      if (attributes instanceof NameAttributes)
      {
        result = _pattern.matches(((NameAttributes) attributes).getName());
      }
    }
    return result;
  }
  
  public String toString()
  {
    return _pattern.toString();
  }
}
