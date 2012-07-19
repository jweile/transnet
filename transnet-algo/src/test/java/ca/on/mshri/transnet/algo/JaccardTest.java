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

import ca.on.mshri.transnet.algo.operations.GeneJaccardAnalysis;
import ca.on.mshri.transnet.algo.operations.XRefMerger;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class JaccardTest extends TestCase {
    
    private OntTestData otd;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        otd = new OntTestData();
        otd.setUpTestModel();
    }
    
    
    
    public void test() {
        
        new XRefMerger().operation(otd.getModel(), OntTestData.YEAST);
        
        String out = new GeneJaccardAnalysis().operation(otd.getModel(), OntTestData.YEAST);
        
        assertNotNull(out);
        assertFalse(out.length() == 0);
        
        String[] vals = out.split("\n");
        assert(vals.length > 0);
        
        System.out.println(out);
        
        
        
    }
}
