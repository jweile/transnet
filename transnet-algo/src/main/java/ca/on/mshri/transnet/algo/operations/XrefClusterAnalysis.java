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
package ca.on.mshri.transnet.algo.operations;

import ca.on.mshri.transnet.algo.Sparql;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.jweile.yogiutil.CliProgressBar;
import de.jweile.yogiutil.Counts;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class XRefClusterAnalysis extends JenaModelOperation<String,String> {
   
    
    /**
     * Computes the different clusters of xref namespaces and their frequencies.
     * 
     * @param species
     * The species to work with. Corresponds to the URI suffix for 
     * the species in the ontology (e.g. <code>Saccharomyces_cerevisiae</code>)
     * 
     * @return 
     * A string containing the statistics output, ready to be printed to the console
     * or written to a file.
     */
    @Override
    public String operation(Model tdbModel, String species) {
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbModel);
        
        ObjectProperty nsProp = model.getObjectProperty(SBNS+"hasNamespace");
        
        Logger.getLogger(XRefClusterAnalysis.class.getName())
                            .log(Level.INFO, "Calculating XRef clustering.");
        
        //init occurrence counter map
        Counts<String> freqs = new Counts<String>();
        
        //get a list of genes for this species using SPARQL
        List<Individual> genes = Sparql.getInstance()
                .queryIndividuals(model, "getGenesOfSpecies", "gene", SBNS+species);
        
        //init progressbar
        CliProgressBar pb = new CliProgressBar(genes.size());
        
        //for each gene object ...
        for (Individual gene : genes) {
            
            //init a treeset for sorting namespace labels alphabetically.
            TreeSet<String> nss = new TreeSet<String>();
            
            //get list of xrefs for current gene
            List<Individual> xrefs = Sparql.getInstance()
                    .queryIndividuals(model,"getXRefsOfGene", "xref", gene.getURI());
            
            //for each xref of current gene...
            for (Individual xref : xrefs) {
                
                //get namespace of xref
                Individual namespace = xref.getPropertyResourceValue(nsProp).as(Individual.class);
                //get its URI, crop out its suffix as a label and add it to the tree set.
                String nsURI = namespace.getURI();
                nss.add(nsURI.substring(34));
            }
            
            //create a signature string from the treeset, unique to this combination of namespaces
            //e.g: 2:[embl,uniprot]
            String signature = cons(nss.size(), ":", nss.toString());
            
            //count the signature
            freqs.count(signature);
            
            //update progressbar
            pb.next();
             
        }
        
        //## Output ##
        StringBuilder b = new StringBuilder();
        for (String signature : freqs.getKeys()) {
            int count = freqs.getCount(signature);
            b.append(signature).append("\t").append(count).append("\n");
        }
        
        return b.toString();
    }
    
    /**
     * Concatenate objects to a string, efficiently.
     * 
     * @param ss the input objects
     * @return a string s such that s = ss[0] + ss[1] + ...
     */
    private String cons(Object... ss) {
        StringBuilder b = new StringBuilder();
        for (Object s : ss) {
            b.append(s.toString());
        }
        return b.toString();
    }
    
    
}
