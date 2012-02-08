/*
 * Created on 30.06.2004
 */
package org.netmelody.neoclassycle.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Franz-Josef Elmer
 */
public class TextTest {

    @Test
    public void testExcapeForXML() {
        assertEquals("&lt;hel&amp;lo&gt;", Text.excapeForXML("<hel&lo>"));
    }

}
