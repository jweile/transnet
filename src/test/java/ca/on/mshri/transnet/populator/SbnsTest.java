/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.populator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author jweile
 */
public class SbnsTest extends TestCase {
    
    
    public void test() {
        
        InputStream in = Main.class.getClassLoader().getResourceAsStream("sbns.owl");
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(in,null);
        
        ExtendedIterator<OntClass> it = model.listClasses();
        while (it.hasNext()) {
            System.out.println(it.next().getURI());
        }
    }
}
