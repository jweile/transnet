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

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class AlgoTest extends TestCase {
    
    public static final String PRE = "http://llama.mshri.on.ca/sbns.owl#";
    public static final String TRN = "urn:transnet:";
    
    public void testStats() throws Exception {
        
        InputStream in = new FileInputStream("src/test/resources/sbns.owl");
        OntModel model = null;
        try {
            model = ModelFactory.createOntologyModel();
            model.read(in,null);
            
            addTestData(model);

            Algo algo = new Algo(model);
            String out = algo.analyzeXRefFrequencies("Saccharomyces_cerevisiae");
            
            System.out.println(out);
            
        } finally {
            model.close();
        }
        
    }
    
    public void testClusters() throws Exception {
        
        InputStream in = new FileInputStream("src/test/resources/sbns.owl");
        OntModel model = null;
        try {
            model = ModelFactory.createOntologyModel();
            model.read(in,null);
            
            addTestData(model);

            Algo algo = new Algo(model);
            String out = algo.analyzeXRefClusters("Saccharomyces_cerevisiae");
            
            System.out.println(out);
            
        } finally {
            model.close();
        }
        
    }

    private void addTestData(OntModel model) {
        
        Individual yeast = model.createIndividual(PRE+"Saccharomyces_cerevisiae", 
                model.getOntClass(PRE+"Species"));
        Individual sgd = model.createIndividual(PRE+"SGD", 
                model.getOntClass(PRE+"Namespace"));
        Individual entrez = model.createIndividual(PRE+"Entrez", 
                model.getOntClass(PRE+"Namespace"));
        
        Individual gene = addGene(model, TRN+"gene1", yeast);
        addXRef(model, TRN+"xref1", gene, sgd, "YDL220C");
        
        gene = addGene(model, TRN+"gene2", yeast);
        addXRef(model, TRN+"xref2", gene, sgd, "YML123C");
        addXRef(model, TRN+"xref3", gene, sgd, "YUB024W");
        addXRef(model, TRN+"xref4", gene, entrez, "12345");
        
    }
    
    private Individual addGene(OntModel model, String uri, Individual species) {
        Individual gene = model.createIndividual(uri,model.getOntClass(PRE+"Object"));
        gene.addProperty(model.getObjectProperty(PRE+"fromSpecies"), species);
        return gene;
    }
    
    private Individual addXRef(OntModel model, String uri, Individual gene, Individual ns, String value) {
        Individual xref = model.createIndividual(uri,model.getOntClass(PRE+"XRef"));
        gene.addProperty(model.getObjectProperty(PRE+"hasXRef"), xref);
        xref.addProperty(model.getObjectProperty(PRE+"hasNamespace"), ns);
        xref.addProperty(model.getDatatypeProperty(PRE+"hasValue"), value);
        return xref;
    }
    
}
