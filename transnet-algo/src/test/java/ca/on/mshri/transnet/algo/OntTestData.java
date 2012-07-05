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

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class OntTestData {
    
    public static final String SBNS = "http://llama.mshri.on.ca/sbns.owl#";
    public static final String TRN = "urn:transnet:";
    public static final String YEAST = "Saccharomyces_cerevisiae";
    
    private Map<String,Individual> inds = new HashMap<String,Individual>();
    
    private OntModel model;
    
    public OntModel setUpTestModel() throws Exception {
        InputStream in = new FileInputStream("src/test/resources/sbns.owl");
        
        model = ModelFactory.createOntologyModel();
        model.read(in,null);

        addTestData(model);
        return model;
    }
    
    private void addTestData(OntModel model) {
        
        Individual yeast = model.createIndividual(SBNS+YEAST, 
                model.getOntClass(SBNS+"Species"));
        Individual sgd = model.createIndividual(SBNS+"SGD", 
                model.getOntClass(SBNS+"Namespace"));
        Individual entrez = model.createIndividual(SBNS+"Entrez", 
                model.getOntClass(SBNS+"Namespace"));
        
        Individual gene = addGene(model, TRN+"gene1", yeast);
        addXRef(model, TRN+"xref1", gene, sgd, "YDL220C");
        
        gene = addGene(model, TRN+"gene2", yeast);
        addXRef(model, TRN+"xref2", gene, sgd, "YML123C");
        addXRef(model, TRN+"xref3", gene, sgd, "YUB024W");
        addXRef(model, TRN+"xref4", gene, entrez, "12345");
        
        //add gene with redundant xref
        gene = addGene(model, TRN+"gene3", yeast);
        addXRef(model, TRN+"xref5", gene, sgd, "YML123C");
        
    }
    
    private Individual addGene(OntModel model, String uri, Individual species) {
        Individual gene = model.createIndividual(uri,model.getOntClass(SBNS+"Object"));
        gene.addProperty(model.getObjectProperty(SBNS+"fromSpecies"), species);
        inds.put(uri,gene);
        return gene;
    }
    
    private Individual addXRef(OntModel model, String uri, Individual gene, Individual ns, String value) {
        Individual xref = model.createIndividual(uri,model.getOntClass(SBNS+"XRef"));
        gene.addProperty(model.getObjectProperty(SBNS+"hasXRef"), xref);
        xref.addProperty(model.getObjectProperty(SBNS+"hasNamespace"), ns);
        xref.addProperty(model.getDatatypeProperty(SBNS+"hasValue"), value);
        inds.put(uri, xref);
        return xref;
    }
    
    public Individual getIndividual(String uri) {
        return inds.get(uri);
    }

    public OntModel getModel() {
        return model;
    }
    
    
    
}
