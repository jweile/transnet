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

import ca.on.mshri.transnet.algo.operations.XRefMerger;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.jweile.yogiutil.Pair;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    
    public void testGetAllConnectedGenes() throws Exception {
        
        Query q = Sparql.getInstance().get("getAllConnectedGenes", 
                OntTestData.SBNS+OntTestData.YEAST);
        assertNotNull("Query did not load!", q);
        
        OntTestData testData = new OntTestData();
        OntModel model = testData.setUpTestModel();
        new XRefMerger().operation(model, OntTestData.YEAST);
        
        
        Set<Pair<Individual>> pairs = new HashSet<Pair<Individual>>();
        
        ResultSet r = QueryExecutionFactory.create(q,model).execSelect();
        while (r.hasNext()) {
            QuerySolution s = r.next();
            pairs.add(new Pair<Individual>(
                    s.getResource("gene1").as(Individual.class), 
                    s.getResource("gene2").as(Individual.class)));
        }
        
        assertFalse("No results returned!",pairs.isEmpty());
        
        System.out.println(pairs.toString());
        
    }
    
    public void testGetCommonNs() throws Exception {
        
        OntTestData testData = new OntTestData();
        OntModel model = testData.setUpTestModel();
        new XRefMerger().operation(model, OntTestData.YEAST);
        
        
        Query q = Sparql.getInstance().get("getCommonNsOfPair", 
                OntTestData.TRN+"gene2", OntTestData.TRN+"gene3");
        assertNotNull("Query did not load!", q);
        
        ResultSet r = QueryExecutionFactory.create(q, model).execSelect();
        
        int count = 0;
        while (r.hasNext()) {
            QuerySolution s = r.next();
            Individual ns = s.getResource("ns").as(Individual.class);
            int numref = s.getLiteral("numref").getInt();
            System.out.println(ns+"\t"+numref);
//            System.out.println(numref);
            count++;
        }
        
        assertTrue("No results",count > 0);
        
    }
    
}
