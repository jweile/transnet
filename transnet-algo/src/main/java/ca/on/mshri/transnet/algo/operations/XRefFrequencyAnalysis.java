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
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.jweile.yogiutil.CliProgressBar;
import de.jweile.yogiutil.Counts;
import de.jweile.yogiutil.IntArrayList;
import de.jweile.yogiutil.LazyInitMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculates occurrences of references per gene in each namespace.
 * 
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class XRefFrequencyAnalysis extends JenaModelOperation<String,String> {
  
    
    /**
     * Calculates occurrences of references per gene in each namespace.
     * 
     * @param species 
     * The species to work with. Corresponds to the URI suffix for 
     * the species in the ontology (e.g. <code>Saccharomyces_cerevisiae</code>)
     * 
     * @return 
     * A string containing the tab-delim output ready to be printed to the 
     * console or written to a file.
     */
    @Override
    public String operation(Model tdbmodel, String species) {
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbmodel);
                        
        DatatypeProperty valueProp = model.getDatatypeProperty(SBNS+"hasValue");
        
        ObjectProperty nsProp = model.getObjectProperty(SBNS+"hasNamespace");
        
        
        Logger.getLogger(XRefFrequencyAnalysis.class.getName())
                            .log(Level.INFO, "Calculating XRef NS frequencies.");
        
        //init global and local freqency maps
        LazyInitMap<String,IntArrayList> countLists = new LazyInitMap<String, IntArrayList>(IntArrayList.class);
        Counts<String> localCounts = new Counts<String>();
                
        //get list of genes through SPARQL query
        List<Individual> genes = Sparql.getInstance()
                .queryIndividuals(model, "getGenesOfSpecies", "gene", SBNS+species);
        
        //init progressbar
        CliProgressBar pb = new CliProgressBar(genes.size());
        
        //for each gene...
        for (Individual gene : genes) {
            
            //reset local counters
            localCounts.resetAll();
            
            //get all XRefs for the current gene via SPARQL
            List<Individual> xrefs = Sparql.getInstance()
                    .queryIndividuals(model,"getXRefsOfGene", "xref", gene.getURI());
            
            //for each xref of the current gene ...
            for (Individual xref : xrefs) {
                
                //get the xref's namespace
                Individual namespace = xref.getPropertyResourceValue(nsProp).as(Individual.class);
                
                //count namespace
                String nsURI = namespace.getURI();
                localCounts.count(nsURI);
            }
            
            //read out the local counter and append its result to the global counter
            for (String key : localCounts.getKeys()) {
                countLists.getOrCreate(key).add(localCounts.getCount(key));
            }
            
            //update progressbar
            pb.next();
            
        }
        
        //## normalize lengths of global list entries ##
        //FIXME: Normalization doesn't work with long lists for some reason.
        int maxlen = Integer.MIN_VALUE;
        for (String key : countLists.keySet()) {
            int len = countLists.get(key).size();
            maxlen = len > maxlen ? len : maxlen;
        }
        for (String key : countLists.keySet()) {
            IntArrayList list = countLists.get(key);
            for (int i = 0; i < maxlen - list.size(); i++) {
                list.add(0);
            }
        }
        
        //## output ##
        StringBuilder b = new StringBuilder();
        for (String key : countLists.keySet()) {
            b.append(key).append("\t");
        }
        b.deleteCharAt(b.length()-1).append("\n");
        for (int i = 0; i < maxlen; i++) {
            for (String key : countLists.keySet()) {
                try {
                    int count = countLists.get(key).get(i);
                    b.append(count).append("\t");
                } catch (IndexOutOfBoundsException e) {
                    //if normalization didn't work
                    Logger.getLogger(XRefFrequencyAnalysis.class.getName())
                            .log(Level.WARNING, "Error during result iteration.",e);
                    b.append("0\t");
                }
            }
            b.deleteCharAt(b.length()-1).append("\n");
        }
        
        return b.toString();
        
    }
    
}
