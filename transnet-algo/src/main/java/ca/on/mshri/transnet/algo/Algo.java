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

import cern.colt.list.IntArrayList;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import de.jweile.yogiutil.CliProgressBar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * At the moment this is more or less a multifunctional class that contains the
 * algorithmic code for different functions.
 * 
 * @author Jochen Weile <jochenweile@gmail.com>
 */
class Algo {

    /**
     * The Jena OntModel
     */
    private OntModel model;
    
    /**
     * The dataset in the Jena TDB store
     */
    private Dataset tdbSet;
    
//    private OntClass objectClass;
//    private OntClass xrefClass;
    
    /**
     * OWL property hasValue
     */
    private DatatypeProperty valueProp;
    
    /**
     * OWL property hasNamespace
     */
    private ObjectProperty nsProp;
    
//    private ObjectProperty fromSpeciesProp;
//    private ObjectProperty xrefProp;
        
    /**
     * SBNS namespace prefix fpr OWL
     */
    public static final String SBNS = "http://llama.mshri.on.ca/sbns.owl#";
    
    
    /**
     * Constructor. Inits the Ontology model and property references.
     * @param base 
     */
    public Algo(Model base) {
        
        this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, base);
                        
//        objectClass = model.getOntClass(PRE+"Object");
//        xrefClass = model.getOntClass(PRE+"XRef");
//        
        valueProp = this.model.getDatatypeProperty(SBNS+"hasValue");
        
        nsProp = this.model.getObjectProperty(SBNS+"hasNamespace");
//        fromSpeciesProp = model.getObjectProperty(PRE+"fromSpecies");
//        xrefProp = model.getObjectProperty(PRE+"hasXRef");
        
    }

//    /**
//     * Mostly test code. not used anymore
//     * @param gene 
//     */
//    void run(String gene) {
//        
//        Sparql sparql = Sparql.getInstance();
//        
//        QueryExecution qexec = QueryExecutionFactory
//                .create(sparql.get("synonyms",gene), model);
//        
//        try {
//            ResultSet r = qexec.execSelect();
//            while (r.hasNext()) {
//                QuerySolution sol = r.next();
//                
//                String xref = sol.getLiteral("synonym").getString();
//                Individual namespace = sol.getResource("namespace")
//                        .as(Individual.class);
//                String nsName = namespace.getURI().replaceFirst(SBNS, "sbns:");
//                
//                System.out.println(nsName+"\t"+xref);
//            }
//        } finally {
//            qexec.close();
//        }
//        
//    }
    
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
    public String analyzeXRefFrequencies(String species) {
        
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Calculating XRef NS frequencies.");
        
        //init global and local freqency maps
        Map<String,IntArrayList> freq = new HashMap<String, IntArrayList>();
        Map<String,Counter> freq_loc = new HashMap<String, Counter>();
                
        //get list of genes through SPARQL query
        List<Individual> genes = queryIndividuals("getGenesOfSpecies", "gene", SBNS+species);
        
        //init progressbar
        CliProgressBar pb = new CliProgressBar(genes.size());
        
        //for each gene...
        for (Individual gene : genes) {
            
            //reset local counters
            for (String key : freq_loc.keySet()) freq_loc.get(key).reset();
            
            //get all XRefs for the current gene via SPARQL
            List<Individual> xrefs = queryIndividuals("getXRefsOfGene", "xref", gene.getURI());
            
            //for each xref of the current gene ...
            for (Individual xref : xrefs) {
                
                //get the xref's namespace
                Individual namespace = xref.getPropertyResourceValue(nsProp).as(Individual.class);
                
                //and increase the counter for it in the local map (with lazy init)
                String nsURI = namespace.getURI();
                Counter counter = freq_loc.get(nsURI);
                if (counter == null) {
                    counter = new Counter();
                    freq_loc.put(nsURI, counter);
                }
                counter.inc();
            }
            
            //read out the local counter and append its result to the global counter
            //(also with lazy init)
            for (String key : freq_loc.keySet()) {
                
                IntArrayList fList = freq.get(key);
                if (fList == null) {
                    fList = new IntArrayList();
                    freq.put(key, fList);
                }
                fList.add(freq_loc.get(key).getVal());
            }
            
            //update progressbar
            pb.next();
            
        }
        
        //## normalize lengths of global list entries ##
        //FIXME: Normalization doesn't work with long lists for some reason.
        int maxlen = Integer.MIN_VALUE;
        for (String key : freq.keySet()) {
            int len = freq.get(key).size();
            maxlen = len > maxlen ? len : maxlen;
        }
        for (String key : freq.keySet()) {
            IntArrayList list = freq.get(key);
            for (int i = 0; i < maxlen - list.size(); i++) {
                list.add(0);
            }
        }
        
        //## output ##
        StringBuilder b = new StringBuilder();
        for (String key : freq.keySet()) {
            b.append(key).append("\t");
        }
        b.deleteCharAt(b.length()-1).append("\n");
        for (int i = 0; i < maxlen; i++) {
            for (String key : freq.keySet()) {
                try {
                    int count = freq.get(key).get(i);
                    b.append(count).append("\t");
                } catch (IndexOutOfBoundsException e) {
                    //if normalization didn't work
                    Logger.getLogger(Algo.class.getName())
                            .log(Level.WARNING, "Error during result iteration.",e);
                    b.append("0\t");
                }
            }
            b.deleteCharAt(b.length()-1).append("\n");
        }
        
        return b.toString();
        
    }
    
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
    public String analyzeXRefClusters(String species) {
        
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Calculating XRef clustering.");
        
        //init occurrence counter map
        Map<String,Counter> freqs = new HashMap<String, Counter>();
        
        //get a list of genes for this species using SPARQL
        List<Individual> genes = queryIndividuals("getGenesOfSpecies", "gene", SBNS+species);
        
        //init progressbar
        CliProgressBar pb = new CliProgressBar(genes.size());
        
        //for each gene object ...
        for (Individual gene : genes) {
            
            //init a treeset for sorting namespace labels alphabetically.
            TreeSet<String> nss = new TreeSet<String>();
            
            //get list of xrefs for current gene
            List<Individual> xrefs = queryIndividuals("getXRefsOfGene", "xref", gene.getURI());
            
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
            
            //increase occurrence counter for that signature (with lazy init)
            Counter freq = freqs.get(signature);
            if (freq == null) {
                freq = new Counter();
                freqs.put(signature,freq);
            }
            freq.inc();
            
            //update progressbar
            pb.next();
             
        }
        
        //## Output ##
        StringBuilder b = new StringBuilder();
        for (String signature : freqs.keySet()) {
            int count = freqs.get(signature).getVal();
            b.append(signature).append("\t").append(count).append("\n");
        }
        
        return b.toString();
    }

    /**
     * Merges ambiguous xrefs for all species
     */
    public void mergeAmbiguousXRefs() {

        //get list of all species via SPARQL
        List<Individual> speciesList = queryIndividuals("getAllSpecies", "species");
        //for each species...
        for (Individual species : speciesList) {
            //perform the merging of xrefs
            mergeAmbiguousXRefsOfSpecies(species);
        }

    }

    /**
     * Merges ambiguous xrefs for given species
     * @param species 
     * The species to work with. Corresponds to the URI suffix for 
     * the species in the ontology (e.g. <code>Saccharomyces_cerevisiae</code>)
     */
    public void mergeAmbiguousXRefsOfSpecies(Individual species) {

        //##Indexing##
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Indexing XRefs for "+species.getURI());

        //an index map for xrefs by namespace and xref value (e.g: entrez-12345)
        Map<String,Set<Individual>> xrefIndex = new HashMap<String, Set<Individual>>();
        
        //get list of xrefs for species via SPARQL
        List<Individual> xrefs = queryIndividuals("getXRefsOfSpecies", "xref", species.getURI());
        
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
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Merging ..."+species.getURI());
        
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
            
            
            //create a set of Neighbour objects
            Set<Connection> neighbours = new HashSet<Connection>();
            
            //for each individual in the set of redundancies
            for (Individual ind : mergeSet) {
                
                //find neighbouring objects in the ontology graph and store them
                neighbours.addAll(findNeighbours(ind));
                
                //save the first individual as the one we'll keep, the rest, we'll delete
                if (toKeep == null) {
                    toKeep = ind;
                } else {
                    toDelete.add(ind);
                }
            }
            
            //reconnect the neighbours to the keeper
            for (Connection neighbour : neighbours) {
                if (neighbour.isOutgoing()) {
                    toKeep.addProperty(neighbour.getPredicate(), neighbour.getNeighbour());
                } else {
                    OntResource nRes = neighbour.getNeighbour().as(OntResource.class);
                    nRes.addProperty(neighbour.getPredicate(), toKeep);
                }
            }
            
            //delete the rest
            for (Individual ind : toDelete) {
                ind.remove();
            }
        }

    }
    
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
    public String findXRefRedundancies(String species) {
        
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Calculating XRef ambiguity.");
        
        Map<String,Set<Individual>> xrefIndex = new HashMap<String, Set<Individual>>();
        
        List<Individual> xrefs = queryIndividuals("getXRefsOfSpecies", "xref", SBNS+species);
        CliProgressBar pb = new CliProgressBar(xrefs.size());
        for (Individual xref : xrefs) {
            
            Individual ns = xref.getPropertyResourceValue(nsProp).as(Individual.class);
            
            String syn = xref.getPropertyValue(valueProp).asLiteral().getString();
            
            String key = cons(ns.getURI().substring(34) , "-" , syn);
            Set<Individual> set = xrefIndex.get(key);
            if (set == null) {
                set = new HashSet<Individual>();
                xrefIndex.put(key,set);
            }
            set.add(xref);
            pb.next();
        }
        
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
     * Performs a SPARQL query that returns a list of individuals.
     * 
     * @param query
     * The name of the query. Corresponds to the name of the SPARQL file.
     * 
     * @param key 
     * The name of the key in the query, that represents the individuals.
     * 
     * @param args
     * additional <code>String</code> arguments that substitute 
     * '<code>%s</code>' tags in the query.
     * 
     * @return The list of individuals.
     */
    private List<Individual> queryIndividuals(String query, String key, String... args) {
        List<Individual> list = new ArrayList<Individual>();
        
        Sparql sparql = Sparql.getInstance();
        
        QueryExecution qexec = QueryExecutionFactory
                .create(sparql.get(query, args), model);
        
        try {
            ResultSet r = qexec.execSelect();
            while (r.hasNext()) {
                QuerySolution sol = r.next();
                
                Individual i = sol.getResource(key)
                        .as(Individual.class);
                
                list.add(i);
            }
        } finally {
            qexec.close();
        }
        
        return list;
    }
    
    /**
     * Finds all neighbours of a resource in the RDF graph.
     * 
     * @param r 
     * the resource
     * 
     * @return 
     * A set of <code>r</code>'s neighbours.
     */
    private Set<Connection> findNeighbours(Resource r) {
        
        //init set
        Set<Connection> neighbours = new HashSet<Connection>();
        
        //find outgoing neighbours via API call
        StmtIterator it = r.listProperties();
        while (it.hasNext()) {
            Statement s = it.nextStatement();
            neighbours.add(new Connection(s.getPredicate(),s.getObject(), true));
        }
        
        //find incoming neighbours via SPARQL call. (unfortunately, there is no
        //API function available for this)
        QueryExecution qexec = QueryExecutionFactory
                .create("SELECT ?s ?p WHERE {?s ?p <"+r.getURI()+">}",model);
        try {
            ResultSet result = qexec.execSelect();
            while (result.hasNext()) {
                QuerySolution sol = result.next();
                RDFNode subject = sol.get("s");
                Property pred = sol.get("p").as(Property.class);
                neighbours.add(new Connection(pred, subject, false));
            }
        } finally {
            qexec.close();
        }
        
        return neighbours;
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
    
    /**
     * a class representing a connection to an adjacent node in an RDF graph.
     */
    private static class Connection {
        
        /**
         * The predicate
         */
        private Property predicate;
        
        /**
         * The actual neighbouring node
         */
        private RDFNode neighbour;
        
        /**
         * Whether the connection is outgoing or incoming.
         */
        private boolean outgoing;

        /**
         * Convenience constructor.
         */
        public Connection(Property predicate, RDFNode neighbour, boolean outgoing) {
            this.predicate = predicate;
            this.neighbour = neighbour;
            this.outgoing = outgoing;
        }

        /**
         * @return the neighbouring node.
         */
        public RDFNode getNeighbour() {
            return neighbour;
        }

        /**
         * @return whether the connection is outgoing or incoming
         */
        public boolean isOutgoing() {
            return outgoing;
        }

        /**
         * @return the predicate of the connection.
         */
        public Property getPredicate() {
            return predicate;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Connection other = (Connection) obj;
            if (this.predicate != other.predicate && (this.predicate == null || !this.predicate.equals(other.predicate))) {
                return false;
            }
            if (this.neighbour != other.neighbour && (this.neighbour == null || !this.neighbour.equals(other.neighbour))) {
                return false;
            }
            if (this.outgoing != other.outgoing) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + (this.predicate != null ? this.predicate.hashCode() : 0);
            hash = 29 * hash + (this.neighbour != null ? this.neighbour.hashCode() : 0);
            hash = 29 * hash + (this.outgoing ? 1 : 0);
            return hash;
        }
        
        
    }
    
    /**
     * A simple class for counting occurrences.
     */
    private static class Counter {
        int val = 0;
        
        /**
         * increase counter value.
         */
        public void inc() {
            val++;
        }
        
        /**
         * Reset counter to 0.
         */
        public void reset() {
            val = 0;
        }

        /**
         * @return current counter value.
         */
        public int getVal() {
            return val;
        }
        
        
    }
    
}
