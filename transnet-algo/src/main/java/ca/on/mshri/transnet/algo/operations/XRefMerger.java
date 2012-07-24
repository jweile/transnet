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

import ca.on.mshri.transnet.algo.Connection;
import ca.on.mshri.transnet.algo.Sparql;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.jweile.yogiutil.CliProgressBar;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Merges ambiguous xrefs for a given species
 * 
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class XRefMerger implements JenaModelOperation<String, Void> {


    
    /**
     * performs the merging operation.
     * 
     * @param model 
     * the Ontology model
     * 
     * @param species 
     * the species to work with.
     * 
     * @return 
     * null.
     */
    @Override
    public Void operation(Model tdbmodel, String species) {
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbmodel);
                        
        DatatypeProperty valueProp = model.getDatatypeProperty(SBNS+"hasValue");
        
        ObjectProperty nsProp = model.getObjectProperty(SBNS+"hasNamespace");
        
        
        //##Indexing##
        Logger.getLogger(XRefMerger.class.getName())
                            .log(Level.INFO, "Indexing XRefs for "+species);

        //an index map for xrefs by namespace and xref value (e.g: entrez-12345)
        Map<String,Set<Individual>> xrefIndex = new HashMap<String, Set<Individual>>();
        
        //get list of xrefs for species via SPARQL
        List<Individual> xrefs = Sparql.getInstance()
                .queryIndividuals(model, "getXRefsOfSpecies", "xref", SBNS+species);
        
        //init progressbar
        CliProgressBar pb = new CliProgressBar(xrefs.size());
        
        //for each xref...
        for (Individual xref : xrefs) {
            
            //get xref namespace
            Individual ns = xref.getPropertyResourceValue(nsProp).as(Individual.class);
            
            //get xref value
            String syn = xref.getPropertyValue(valueProp).asLiteral().getString();
            
            //construct key as <namespace>-<value>
            String key = cons(ns.getURI().substring(34) , "-" , syn);
            
            //append current xref object to list in index (with lazy init)
            Set<Individual> set = xrefIndex.get(key);
            if (set == null) {
                set = new HashSet<Individual>();
                xrefIndex.put(key,set);
            }
            set.add(xref);
            
            //update progressbar
            pb.next();
        }
        
        
        //##Do the actual merging##
        Logger.getLogger(XRefMerger.class.getName())
                            .log(Level.INFO, "Merging ..."+species);
        
        //for each set of xref objects indexed under the same keys...
        for (Set<Individual> mergeSet : xrefIndex.values()) {
            
            //if there's only one object in the set, we don't have to do anything
            if (mergeSet.size() <= 1) {
                continue;
            }
            
            //otherwise...
            
            //a variable for the xref instance we will keep in the end
            Individual toKeep = null;
            //a bag for all the instances we will delete in the end
            List<Individual> toDelete = new ArrayList<Individual>();
            
            Set<Connection> connections = new HashSet<Connection>();
            
            //for each individual in the set of redundancies
            for (Individual ind : mergeSet) {
                
                //find connections in the ontology graph and store them
                connections.addAll(Connection.findConnections(ind));
                
                //save the first individual as the one we'll keep, the rest, we'll delete
                if (toKeep == null) {
                    toKeep = ind;
                } else {
                    toDelete.add(ind);
                }
            }
            
            //reconnect the neighbours to the keeper
            for (Connection connection : connections) {
                if (connection.isOutgoing()) {
                    toKeep.addProperty(connection.getPredicate(), connection.getNeighbour());
                } else {
                    OntResource nRes = connection.getNeighbour().as(OntResource.class);
                    nRes.addProperty(connection.getPredicate(), toKeep);
                }
            }
            
            //delete the rest
            for (Individual ind : toDelete) {
                ind.remove();
            }
        }

        return null;
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
