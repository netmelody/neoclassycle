/*
 * Created on 12.09.2004
 */
package classycle.ant;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tools.ant.BuildException;

import classycle.dependency.DefaultResultRenderer;
import classycle.dependency.DependencyChecker;
import classycle.dependency.ResultRenderer;
import classycle.util.Text;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyCheckingTask extends ClassycleTask
{
  private String _definitionFile;
  private String _dependencyDefinition;
  private String _resultRenderer;
  
  public void setDefinitionFile(String definitionFile)
  {
    _definitionFile = definitionFile;
  }

  public void setResultRenderer(String resultRenderer)
  {
    _resultRenderer = resultRenderer;
  }
  
  public void addText(String text) 
  {
    _dependencyDefinition = text.trim();
  }
  
  public void execute() throws BuildException
  {
    super.execute();
    
    boolean ok = false;
    try
    {
      DependencyChecker dependencyChecker 
          = new DependencyChecker(getClassFileNames(), getPattern(), 
                                  getReflectionPattern(),
                                  getDependencyDefinitions(), getRenderer());
      PrintWriter printWriter = new PrintWriter(System.out);
      ok = dependencyChecker.check(printWriter);
      printWriter.flush();
    } catch (Exception e)
    {
      throw new BuildException(e);
    }
    if (ok == false) {
      throw new BuildException(
              "Invalid dependencies found. See output for details.");
    }
  }

  private ResultRenderer getRenderer() throws InstantiationException, IllegalAccessException, ClassNotFoundException
  {
    ResultRenderer renderer = new DefaultResultRenderer();
    if (_resultRenderer != null) 
    {
      renderer = (ResultRenderer) Class.forName(_resultRenderer).newInstance();
    }
    return renderer;
  }

  private String getDependencyDefinitions() throws IOException
  {
    String result = null;
    if (_definitionFile != null) 
    {
      File baseDir = getOwningTarget().getProject().getBaseDir();
      result = Text.readTextFile(new File(baseDir, _definitionFile));
    } else if (_dependencyDefinition.length() > 0)
    {
      result = _dependencyDefinition;
    }
    return result;
  }
}
