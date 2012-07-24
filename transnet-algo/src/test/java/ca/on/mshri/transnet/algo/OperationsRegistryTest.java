/*
 * Copyright (C) 2012 Department of Molecular Genetics, University of Toronto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.mshri.transnet.algo;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class OperationsRegistryTest extends TestCase {
    
    private static Pattern CMD_PATTERN = Pattern.compile("(\\w+)\\(\"(\\w+)\"\\)");
    
    public void test() {
        
        String cmd = "XRefFrequencyAnalysis(\"Saccharomyces_cerevisiae\")";
        
        Matcher matcher = CMD_PATTERN.matcher(cmd);
        if (matcher.matches()) {
            System.out.println("Groups: "+matcher.groupCount());
            for (int i = 0; i <= matcher.groupCount(); i++) {
               System.out.println("g"+i+": "+matcher.group(i));
            }
        }
        
        System.out.println("end");
                
    }
    
    private void log(String s) {
        Logger.getLogger(OperationsRegistryTest.class.getName()).log(Level.INFO, s);
    }
}
