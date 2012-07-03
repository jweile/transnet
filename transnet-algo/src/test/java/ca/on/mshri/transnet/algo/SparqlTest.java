/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.algo;

import com.hp.hpl.jena.query.Query;
import junit.framework.TestCase;

/**
 *
 * @author jweile
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
