/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.populator;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import de.jweile.yogiutil.pipeline.Pipeline;
import de.jweile.yogiutil.pipeline.StartNode;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import junit.framework.TestCase;

/**
 *
 * @author jweile
 */
public class TripleStoreWriterTest extends TestCase {
    
    public void test() {
        File outFile = new File(new File("target"),"tdb.out");
        
        
        Pipeline p = new Pipeline();
        p.addNode(new StartNode<Entry>("Mock start") {

            private Queue<Entry> q = new LinkedList<Entry>() {{
                Entry e = new Entry(1, 1, 1);
                e.addSynonym(new Entry.Synonym("foo", 1, 1));
                add(e);
            }};
            
            @Override
            public Entry process(Void in) {
                return q.poll();
            }
        });
                
        p.addNode(new TripleStoreWriter(outFile));
        
        p.start();
        
        assertTrue(outFile.exists());
        
        
        Dataset tdbSet = TDBFactory.createDataset(outFile.getAbsolutePath());
                
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbSet.getDefaultModel());
        
        assertNotNull("Triplestore contents sample not found!",model.getIndividual(TripleStoreWriter.PRE+"entrezgene"));
        
        model.close();
        tdbSet.close();
    }
    
//    public File toFile(String[] path) {
//        if (path == null || path.length == 0) {
//            return null;
//        } else if (path.length == 1) {
//            return new File(path[0]);
//        } else {
//            String[] ps = Arrays.copyOfRange(path, 0, path.length-1);
//            return new File(toFile(ps),path[path.length-1]);
//        }
//    }
    
}
