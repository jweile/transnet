/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author jweile
 */
public class IndividualPopulatorTest extends TestCase {
    
    public void test() {
        InputStream in = Main.class.getClassLoader().getResourceAsStream("sbns.owl");
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(in,null);
                
        OntClass nsClass = model.getOntClass(TripleStoreWriter.PRE+"Namespace");
        
        Map<Integer, Individual> map = new IndividualPopulator(model, nsClass).run("namespaces");
        
        Individual i = map.get(1);
        assertNotNull("Individual 1 not found!",i);
                
        Individual i2 = model.getIndividual(i.getURI());
        assertNotNull("Individual not found in model!",i2);
        
        assertTrue("Individual does not equal itself!",i.equals(i2));
    }
    
}
