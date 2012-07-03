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
package ca.on.mshri.transnet.populator;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.InputStream;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class IndividualPopulatorTest extends TestCase {
    
    public void test() {
        InputStream in = Main.class.getClassLoader().getResourceAsStream("sbns.owl");
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(in,null);
                
        OntClass nsClass = model.getOntClass(TripleStoreWriter.SBNS+"Namespace");
        
        Map<Integer, Individual> map = new IndividualPopulator(model, nsClass).run("namespaces");
        
        Individual i = map.get(1);
        assertNotNull("Individual 1 not found!",i);
                
        Individual i2 = model.getIndividual(i.getURI());
        assertNotNull("Individual not found in model!",i2);
        
        assertTrue("Individual does not equal itself!",i.equals(i2));
    }
    
}
