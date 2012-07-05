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

import ca.on.mshri.transnet.algo.operations.XRefMerger;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class MergerTest extends TestCase {

    private OntTestData otd;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        otd = new OntTestData();
        otd.setUpTestModel();
    }
    
    
    
    public void testXRefMerger() throws Exception {
        
        new XRefMerger().operation(otd.getModel(), OntTestData.YEAST);
        
        Individual xref2 = otd.getIndividual(OntTestData.TRN+"xref2");
        Individual xref5 = otd.getIndividual(OntTestData.TRN+"xref5");
        boolean has2 = otd.getModel().containsResource(xref2);
        boolean has5 = otd.getModel().containsResource(xref5);
        
        assertTrue(has2 || has5);
        assertFalse(has2 && has5);
        
        Individual kept = has2 ? xref2 : xref5;
        
        
        //check the genes
        QueryExecution q = QueryExecutionFactory
                .create("SELECT ?gene {?gene <"+OntTestData.SBNS+"hasXRef> <"+kept.getURI()+">}",
                otd.getModel());
        ResultSet r = q.execSelect();
        Set<String> found = new HashSet<String>();
        while (r.hasNext()) {
            Resource gene = r.next().getResource("gene");
            found.add(gene.getURI());
        }
        q.close();
        
        Set<String> expected = new HashSet<String>(){{
            add(OntTestData.TRN+"gene2");
            add(OntTestData.TRN+"gene3");
        }};
        
        assertEquals(expected, found);
        
        
        //check the values
        q = QueryExecutionFactory
                .create("SELECT ?syn {<"+kept.getURI()+"> <"+OntTestData.SBNS+"hasValue> ?syn}",
                otd.getModel());
        r = q.execSelect();
        found = new HashSet<String>();
        while (r.hasNext()) {
            Literal val = r.next().getLiteral("syn");
            found.add(val.getString());
        }
        q.close();
        
        expected = new HashSet<String>() {{
            add("YML123C");
        }};
        
        assertEquals(expected, found);
        
        //print all outgoing relations
        StmtIterator it = kept.listProperties();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        otd.getModel().close();
    }
    
    
    
}
