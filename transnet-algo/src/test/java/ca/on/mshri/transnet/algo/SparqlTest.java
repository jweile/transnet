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

import com.hp.hpl.jena.query.Query;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class SparqlTest extends TestCase {
    
    public void testSynonyms() {
        
        Sparql s = Sparql.getInstance();
        
        Query q = s.get("synonyms","YDL220C");
        assertNotNull("Query did not load!", q);
        
        System.out.println(q.toString());
                
    }
    
    public void testGenesOfSpecies() {
        
        Sparql s = Sparql.getInstance();
        
        Query q = s.get("getGenesOfSpecies",
                "http://llama.mshri.on.ca/sbns.owl#Saccharomyces_cerevisiae");
        assertNotNull("Query did not load!", q);
        
        System.out.println(q.toString());
                
    }
    
    public void testXRefsOfGene() {
        
        Sparql s = Sparql.getInstance();
        
        Query q = s.get("getXRefsOfGene",
                "http://llama.mshri.on.ca/sbns.owl#_b0");
        assertNotNull("Query did not load!", q);
        
        System.out.println(q.toString());
                
    }
    
}
