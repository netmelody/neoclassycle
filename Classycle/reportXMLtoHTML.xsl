<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
   Copyright (c) 2003, Franz-Josef Elmer, All rights reserved.
 
   Redistribution and use in source and binary forms, with or without 
   modification, are permitted provided that the following conditions are met:
   
   - Redistributions of source code must retain the above copyright notice, 
     this list of conditions and the following disclaimer.
   - Redistributions in binary form must reproduce the above copyright notice, 
     this list of conditions and the following disclaimer in the documentation 
     and/or other materials provided with the distribution.
 
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
   EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
  <xsl:strip-space elements="*"/>
  
  <!-- ====================================================================
       Matches root element <classycle>. Creates HTML page with headers, 
       style sheets, JavaScript, and title.
       ==================================================================== -->
  <xsl:template match="classycle">
    <html>
      <head>
        <title>Classycle Analysis of <xsl:value-of select="/classycle/@title"/>
        </title>
        <style type="text/css">
          body { font-family:Helvetica,Arial,sans-serif; }
          th { background-color:#aaaaaa; } 
        </style>
        <script type="text/javascript"><![CDATA[
        <!--
          function show(title, classes) {
            list = window.open("", "list", 
                "dependent=yes,location=no,menubar=no,toolbar=no,width=400,height=500");
            list.document.close();
            list.document.open();
            list.document.write("<html><head><style type=\"text/css\">");
            list.document.write("body { font-family:Helvetica,Arial,sans-serif; } </style></head><body>");
            list.document.write("<h3>" + title + "</h3><p>");
            list.document.write(classes.replace(/ /g,"<br/>"));
            list.document.write("</body></html>");
            list.document.close();
            list.focus();
          }
        //-->
        ]]>
        </script>
      </head>
      <body>
        <h1><a href="http://classycle.sourceforge.net">
              <img src="images/logo.png" alt="Classcyle" width="200" 
                   height="135" border="0" align="middle" hspace="4"/>
            </a>
            Analysis of <xsl:value-of select="/classycle/@title"/></h1>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <!-- ====================================================================
       Matches element <cycles>. Creates cycles section of HTML page.
       Creates summary and table headers.
       ==================================================================== -->
  <xsl:template match="cycles">
    <h2>Cycles</h2>
    Summary: <xsl:value-of select="count(cycle)"/> cycles detected.
    <table border="1" cellpadding="5" cellspacing="0" width="770">
      <tr>
        <th>Name</th>
        <th>Number of classes</th>
        <th>Girth</th>
        <th>Radius</th>
        <th>Diameter</th>
        <th>Layer</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <!-- ====================================================================
       Matches element <cycle>. Creates a row in the cycles table with
       JavaScript popups.
       ==================================================================== -->
  <xsl:template match="cycle">
    <tr>
      <td>
        <xsl:choose>
          <xsl:when test="contains(@name,'et al.')">
            <img src="images/mix.png" alt="inner class" width="20"
                 height="20" align="middle" hspace="4"/>
          </xsl:when>
          <xsl:when test="contains(@name,'inner classes')">
            <img src="images/inner.png" alt="class" width="20" height="20"
                 align="middle" hspace="4"/>
          </xsl:when>
        </xsl:choose>
        <xsl:value-of select="@name"/>
      </td>
      <td align="right">
        <div style="cursor:pointer;">
          <xsl:element name="a">
            <xsl:attribute name="onClick">
              <xsl:call-template name="classRefWithEccentricityPopup">
                <xsl:with-param name="set" select="classes/classRef"/>
                <xsl:with-param name="text">Classes of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
            <xsl:value-of select="@size"/>
          </xsl:element>
        </div>
      </td>
      <td align="right"><xsl:value-of select="@girth"/></td>
      <td align="right">
        <div style="cursor:pointer;">
          <xsl:element name="a">
            <xsl:attribute name="onClick">
              <xsl:call-template name="classRefPopup">
                <xsl:with-param name="set" select="centerClasses/classRef"/>
                <xsl:with-param name="text">Center classes of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
            <xsl:value-of select="@radius"/>
          </xsl:element>
        </div>
      </td>
      <td align="right"><xsl:value-of select="@diameter"/></td>
      <td align="right"><xsl:value-of select="@longestWalk"/></td>
    </tr>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates JavaScript popup with sorted class 
       references.
       
       parameters:
       
       set Set of elements with attribute "name". The set will be sorted in
           accordance with this attribute.
       text Explaining header of the popup
       
       ==================================================================== -->
  <xsl:template name="classRefPopupSorted">
    <xsl:param name="set"/>
    <xsl:param name="text"/>
    <xsl:for-each select="$set">
      <xsl:sort select="@name"/>
    </xsl:for-each>
    <xsl:call-template name="classRefPopup">
      <xsl:with-param name="set" select="$set"/>
      <xsl:with-param name="text" select="$text"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates JavaScript popup class references.
       
       parameters:
       
       set Set of elements with attribute "name". 
       text Explaining header of the popup
       
       ==================================================================== -->
  <xsl:template name="classRefPopup">
    <xsl:param name="set"/>
    <xsl:param name="text"/>
    <xsl:text>javascript:show(&quot;</xsl:text>
    <xsl:value-of select="$text"/><xsl:text>&quot;,&quot;</xsl:text>
    <xsl:for-each select="$set">
      <xsl:value-of select="@name"/><xsl:text> </xsl:text>
    </xsl:for-each>
    <xsl:text>&quot;)</xsl:text>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates JavaScript popup class references with
       eccentricities.
       
       parameters:
       
       set Set of elements with attribute "eccentricity" and "name". 
       text Explaining header of the popup
       
       ==================================================================== -->
  <xsl:template name="classRefWithEccentricityPopup">
    <xsl:param name="set"/>
    <xsl:param name="text"/>
    <xsl:text>javascript:show(&quot;</xsl:text>
    <xsl:value-of select="$text"/><xsl:text>&quot;,&quot;</xsl:text>
    <xsl:for-each select="$set">
      <xsl:value-of select="@eccentricity"/><xsl:text>&amp;nbsp;</xsl:text>
      <xsl:value-of select="@name"/><xsl:text> </xsl:text>
    </xsl:for-each>
    <xsl:text>&quot;)</xsl:text>
  </xsl:template>

  <!-- ====================================================================
       Matches element <classes>. Creates classes section of HTML page.
       Creates summary and table headers.
       ==================================================================== -->
  <xsl:template match="classes">
    <h2>Classes</h2>
    Summary: <xsl:value-of select="count(class)"/> classes using
    <xsl:value-of select="@numberOfExternalClasses"/> external classes.
    <table border="1" cellpadding="5" cellspacing="0" width="770">
      <tr>
        <th>Type</th>
        <th>Number of classes</th>
        <th>Averaged (maximum) size in bytes</th>
        <th>Averaged (maximum) used by</th>
        <th>Averaged (maximum) uses internal</th>
        <th>Averaged (maximum) uses external</th>
      </tr>
      <xsl:call-template name="summary">
        <xsl:with-param name="type">Interfaces</xsl:with-param>
        <xsl:with-param name="set" select="class[@type='interface']"/>
        <xsl:with-param name="totalNumber" select="count(class)"/>
      </xsl:call-template>
      <xsl:call-template name="summary">
        <xsl:with-param name="type">Abstract classes</xsl:with-param>
        <xsl:with-param name="set" select="class[@type='abstract class']"/>
        <xsl:with-param name="totalNumber" select="count(class)"/>
      </xsl:call-template>
      <xsl:call-template name="summary">
        <xsl:with-param name="type">Concrete classes</xsl:with-param>
        <xsl:with-param name="set" select="class[@type='class']"/>
        <xsl:with-param name="totalNumber" select="count(class)"/>
      </xsl:call-template>
    </table>
    <p/>
    <table cellpadding="3" cellspacing="0" border="1" width="770">
      <tr>
        <th>Class</th>
        <th>Size</th>
        <th>Used by</th>
        <th>Uses internal</th>
        <th>Uses external</th>
        <th>Layer</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <!-- ====================================================================
       Matches element <class>. Creates a row in the classes table with
       JavaScript popups.
       ==================================================================== -->
  <xsl:template match="class">
     <tr>
       <td>
         <xsl:choose>
           <xsl:when test="@type='class' and @innerClass='true'">
             <img src="images/innerclass.png" alt="inner class" width="20"
                  height="20" align="middle" hspace="4"/>
           </xsl:when>
           <xsl:when test="@type='class' and @innerClass='false'">
             <img src="images/class.png" alt="class" width="20" height="20"
                  align="middle" hspace="4"/>
           </xsl:when>
           <xsl:when test="@type='abstract class' and @innerClass='true'">
             <img src="images/innerabstract.png" alt="inner class" width="20"
                  height="20" align="middle" hspace="4"/>
           </xsl:when>
           <xsl:when test="@type='abstract class' and @innerClass='false'">
             <img src="images/abstract.png" alt="class" width="20" height="20"
                  align="middle" hspace="4"/>
           </xsl:when>
           <xsl:when test="@type='interface' and @innerClass='true'">
             <img src="images/innerinterface.png" alt="inner class" width="20"
                  height="20" align="middle" hspace="4"/>
           </xsl:when>
           <xsl:when test="@type='interface' and @innerClass='false'">
             <img src="images/interface.png" alt="class" width="20" height="20"
                  align="middle" hspace="4"/>
           </xsl:when>
         </xsl:choose>
         <xsl:element name="a">
           <xsl:attribute name="name">
             <xsl:value-of select="@name"/>
           </xsl:attribute>
           <xsl:value-of select="@name"/>
         </xsl:element>
       </td>
       <td align="right"><xsl:value-of select="@size"/></td>
       <td align="right">
         <div style="cursor:pointer;">
           <xsl:element name="a">
             <xsl:attribute name="onClick">
               <xsl:call-template name="classRefPopupSorted">
                 <xsl:with-param name="set" select="classRef[@type='usedBy']"/>
                 <xsl:with-param name="text">Classes using <xsl:value-of select="@name"/>:</xsl:with-param>
               </xsl:call-template>
             </xsl:attribute>
             <xsl:value-of select="@usedBy"/>
           </xsl:element>
         </div>
       </td>
       <td align="right">
         <div style="cursor:pointer;">
           <xsl:element name="a">
             <xsl:attribute name="onClick">
               <xsl:call-template name="classRefPopupSorted">
                 <xsl:with-param name="set" select="classRef[@type='usesInternal']"/>
                 <xsl:with-param name="text"><xsl:value-of select="@name"/> uses:</xsl:with-param>
               </xsl:call-template>
             </xsl:attribute>
             <xsl:value-of select="@usesInternal"/>
           </xsl:element>
         </div>
       </td>
       <td align="right">
         <div style="cursor:pointer;">
           <xsl:element name="a">
             <xsl:attribute name="onClick">
               <xsl:call-template name="classRefPopupSorted">
                 <xsl:with-param name="set" select="classRef[@type='usesExternal']"/>
                 <xsl:with-param name="text"><xsl:value-of select="@name"/> uses:</xsl:with-param>
               </xsl:call-template>
             </xsl:attribute>
             <xsl:value-of select="@usesExternal"/>
           </xsl:element>
         </div>
       </td>
      <td align="right"><xsl:value-of select="@layer"/></td>
     </tr>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which calculates the summary for the specified subset of
       classes and adds a row to the class summary table.
       
       parameters:
       
       type Text which will appear in the column "Type"
       set Subset of <class> elements
       totalNumber Total number of <class> elements
       
       ==================================================================== -->
  <xsl:template name="summary">
    <xsl:param name="type"/>
    <xsl:param name="set"/>
    <xsl:param name="totalNumber"/>
    <tr>
      <td>
        <nobr>
          <xsl:value-of select="round(100 * count($set) div $totalNumber)"/>% <xsl:value-of select="$type"/>
        </nobr>
      </td>
      <td align="right"><xsl:value-of select="count($set)"/></td>
      <td align="right">
        <xsl:value-of select="round(sum($set/@size) div count($set))"/>
        <xsl:variable name="max">
          <xsl:for-each select="$set">
            <xsl:sort select="@size" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@size"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$max"/>)
      </td>
      <td align="right">
        <xsl:value-of select="round(10 * sum($set/@usedBy) div count($set)) div 10"/>
        <xsl:variable name="max">
          <xsl:for-each select="$set">
            <xsl:sort select="@usedBy" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@usedBy"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$max"/>)
      </td>
      <td align="right">
        <xsl:value-of select="round(10 * sum($set/@usesInternal) div count($set)) div 10"/>
        <xsl:variable name="max">
          <xsl:for-each select="$set">
            <xsl:sort select="@usesInternal" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@usesInternal"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$max"/>)
      </td>
      <td align="right">
        <xsl:value-of select="round(10 * sum($set/@usesExternal) div count($set)) div 10"/>
        <xsl:variable name="max">
          <xsl:for-each select="$set">
            <xsl:sort select="@usesExternal" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@usesExternal"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$max"/>)
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>




