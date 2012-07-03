/*
 *  Copyright (C) 2011 The Roth Lab
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.mshri.transnet.algo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
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
