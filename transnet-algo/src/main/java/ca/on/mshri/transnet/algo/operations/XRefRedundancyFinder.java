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
import de.jweile.yogiutil.LazyInitMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class XRefRedundancyFinder implements JenaModelOperation<String,String> {
    
    
    /**
     * Computes a list of redundant XRefs and their respective abundances.
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
        
        //get metadata
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbModel);
        
        ObjectProperty nsProp = model.getObjectProperty(SBNS+"hasNamespace");
        DatatypeProperty valueProp = model.getDatatypeProperty(SBNS+"hasValue");
        
        Logger.getLogger(XRefRedundancyFinder.class.getName())
                            .log(Level.INFO, "Calculating XRef ambiguity.");
        
        //set up index datastructure
        LazyInitMap<String,Set<Individual>> xrefIndex = new LazyInitMap<String, Set<Individual>>(HashSet.class);
        
        //get list of xrefs via sparql
        List<Individual> xrefs = Sparql.getInstance()
                .queryIndividuals(model, "getXRefsOfSpecies", "xref", SBNS+species);
        
        //set up progress bar
        CliProgressBar pb = new CliProgressBar(xrefs.size());
        
        //for each xref...
        for (Individual xref : xrefs) {
            
            //get namespace of xref
            Individual ns = xref.getPropertyResourceValue(nsProp).as(Individual.class);
            
            //get synonym value of xref
            String syn = xref.getPropertyValue(valueProp).asLiteral().getString();
            
            //assemble signature from namespace and synoym
            String key = cons(ns.getURI().substring(34) , "-" , syn);
            //index xref by signature
            xrefIndex.getOrCreate(key).add(xref);
            
            //update progressbar
            pb.next();
        }
        
        //###Output###
        StringBuilder b = new StringBuilder();
        for (Entry<String,Set<Individual>> entry : xrefIndex.entrySet()) {
            if (entry.getValue().size() > 1) {
                b.append(entry.getKey()).append("\t")
                .append(entry.getValue().size()).append("\n");
            }
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
