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
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class TripleStoreWriterTest extends TestCase {
    
    public static final File OUT_FILE = new File(new File("target"),"tdb.out");
    
    public void test() {
                
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
                
        p.addNode(new TripleStoreWriter(OUT_FILE));
        
        p.start();
        
        assertTrue(OUT_FILE.exists());
        
        
        Dataset tdbSet = null;
        OntModel model = null;
        try {
            tdbSet = TDBFactory.createDataset(OUT_FILE.getAbsolutePath());

            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbSet.getDefaultModel());

            assertNotNull("Triplestore contents sample not found!",model.getIndividual(TripleStoreWriter.SBNS+"entrezgene"));
        
        } finally {
            if (model != null) {
                model.close();
            }
            if (tdbSet != null) {
                tdbSet.close();
            }
        }
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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        if (OUT_FILE.exists()) {
            OUT_FILE.delete();
        }
    }
    
}
