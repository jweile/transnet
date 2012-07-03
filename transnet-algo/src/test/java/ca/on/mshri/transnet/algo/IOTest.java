/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.algo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import junit.framework.TestCase;

/**
 *
 * @author jweile
 */
public class IOTest extends TestCase {
    
    public void test() throws FileNotFoundException {
        File f = new File("target/iotest.txt");
        String contents = "These are some\n test contents.";
        
        IO.getInstance().write(f.getAbsolutePath(), contents);
        
        assertTrue(f.exists());
        
        String read = IO.getInstance().read(f.getAbsolutePath(), new FileInputStream(f));
        
        assertEquals(contents, read);
        
        f.delete();
    }
    
}
