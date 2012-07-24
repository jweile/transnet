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

import ca.on.mshri.transnet.algo.operations.JenaModelOperation;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class TDBAccessTest extends TestCase {
    
    public static final String PRE = "urn:test:";
    
    private File dbDir = new File("target/tdb_test");
 
    public void testWrite() {
        
        JenaModelOperation<Void,Void> jmo = new JenaModelOperation<Void,Void>() {

            @Override
            public Void operation(Model model, Void in) {
                
                Resource r1 = model.getResource(PRE+1);
                Resource r2 = model.getResource(PRE+2);
                Property p = model.createProperty(PRE+"p");
                
                r1.addProperty(p, r2);
                
                return null;
            }
        };
        
        TDBAccess<Void,Void> tdba = new TDBAccess<Void, Void>(dbDir, jmo);
        tdba.perform(null);
        
    }
    
    public void testRead() {
        
        testWrite();
        
        JenaModelOperation<Void,String> jmo = new JenaModelOperation<Void,String>() {

            @Override
            public String operation(Model model, Void in) {
                
                StmtIterator it = model.listStatements();
                
                StringBuilder b = new StringBuilder();
                while (it.hasNext()) {
                    b.append(it.next().toString()+"\n");
                }
                
                return b.toString();
            }
        };
        
        TDBAccess<Void,String> tdba = new TDBAccess<Void, String>(dbDir, jmo);
        String out = tdba.perform(null);
        
        System.out.println(out);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (dbDir.exists()) {
            IO.getInstance().deleteRecursively(dbDir);
        }
    }
    
    
    
}
