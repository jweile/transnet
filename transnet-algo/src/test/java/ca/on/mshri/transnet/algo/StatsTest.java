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

import ca.on.mshri.transnet.algo.operations.XRefRedundancyFinder;
import ca.on.mshri.transnet.algo.operations.XRefMerger;
import ca.on.mshri.transnet.algo.operations.XRefFrequencyAnalysis;
import ca.on.mshri.transnet.algo.operations.XRefClusterAnalysis;
import com.hp.hpl.jena.ontology.OntModel;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class StatsTest extends TestCase {
    
    public static final String PRE = "http://llama.mshri.on.ca/sbns.owl#";
    public static final String TRN = "urn:transnet:";
    public static final String YEAST = "Saccharomyces_cerevisiae";
    
    private OntModel model;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        model = new OntTestData().setUpTestModel();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        model.close();
    }
    
    
    
    public void testXRefFrequencyAnalysis() throws Exception {
        
        String out = new XRefFrequencyAnalysis().operation(model, YEAST);
        
        System.out.println(out);
        
    }
    
    public void testXRefClusterAnalysis() throws Exception {
        
        String out = new XRefClusterAnalysis().operation(model, YEAST);
        
        System.out.println(out);
        
    }
    
    public void testXRefRedundancyFinder() throws Exception {
        
        String out = new XRefRedundancyFinder().operation(model, YEAST);
        
        System.out.println(out);
        
    }
    

}
