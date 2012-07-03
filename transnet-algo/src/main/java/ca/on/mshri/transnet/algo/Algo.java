/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author jweile
 */
class Algo {

    private OntModel model;
    private Dataset tdbSet;
    
//    private OntClass objectClass;
//    private OntClass xrefClass;
    private DatatypeProperty valueProp;
    private ObjectProperty nsProp;
//    private ObjectProperty fromSpeciesProp;
//    private ObjectProperty xrefProp;
        
    public static final String PRE = "http://llama.mshri.on.ca/sbns.owl#";
    
    
    public Algo(Model base) {
        
        this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, base);
                        
//        objectClass = model.getOntClass(PRE+"Object");
//        xrefClass = model.getOntClass(PRE+"XRef");
//        
        valueProp = this.model.getDatatypeProperty(PRE+"hasValue");
        
        nsProp = this.model.getObjectProperty(PRE+"hasNamespace");
//        fromSpeciesProp = model.getObjectProperty(PRE+"fromSpecies");
//        xrefProp = model.getObjectProperty(PRE+"hasXRef");
        
    }

    void run(String gene) {
        
        Sparql sparql = Sparql.getInstance();
        
        QueryExecution qexec = QueryExecutionFactory
                .create(sparql.get("synonyms",gene), model);
        
        try {
            ResultSet r = qexec.execSelect();
            while (r.hasNext()) {
                QuerySolution sol = r.next();
                
                String xref = sol.getLiteral("synonym").getString();
                Individual namespace = sol.getResource("namespace")
                        .as(Individual.class);
                String nsName = namespace.getURI().replaceFirst(PRE, "sbns:");
                
                System.out.println(nsName+"\t"+xref);
            }
        } finally {
            qexec.close();
        }
        
    }
    
    public String xrefStats(String species) {
        
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Calculating XRef NS frequencies.");
        
        Map<String,IntArrayList> freq = new HashMap<String, IntArrayList>();
        Map<String,Counter> freq_loc = new HashMap<String, Counter>();
                
        List<Individual> genes = queryIndividuals("getGenesOfSpecies", "gene", PRE+species);
        CliProgressBar pb = new CliProgressBar(genes.size());
        
        for (Individual gene : genes) {
            
            //reset local counters
            for (String key : freq_loc.keySet()) freq_loc.get(key).reset();
            
            List<Individual> xrefs = queryIndividuals("getXRefsOfGene", "xref", gene.getURI());
            
            for (Individual xref : xrefs) {
                
                Individual namespace = xref.getPropertyResourceValue(nsProp).as(Individual.class);
                
                String nsURI = namespace.getURI();
                Counter counter = freq_loc.get(nsURI);
                if (counter == null) {
                    counter = new Counter();
                    freq_loc.put(nsURI, counter);
                }
                counter.inc();
            }
            
            for (String key : freq_loc.keySet()) {
                
                IntArrayList fList = freq.get(key);
                if (fList == null) {
                    fList = new IntArrayList();
                    freq.put(key, fList);
                }
                fList.add(freq_loc.get(key).getVal());
            }
            
            pb.next();
            
        }
        
        //normalize lengths
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
        
        //output
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
                    Logger.getLogger(Algo.class.getName())
                            .log(Level.WARNING, "Error during result iteration.",e);
                    b.append("0\t");
                }
            }
            b.deleteCharAt(b.length()-1).append("\n");
        }
        
        return b.toString();
        
    }
    
    public String xrefClusters(String species) {
        
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Calculating XRef clustering.");
        
        Map<String,Counter> freqs = new HashMap<String, Counter>();
        
        List<Individual> genes = queryIndividuals("getGenesOfSpecies", "gene", PRE+species);
        CliProgressBar pb = new CliProgressBar(genes.size());
        for (Individual gene : genes) {
            
            TreeSet<String> nss = new TreeSet<String>();
            
            List<Individual> xrefs = queryIndividuals("getXRefsOfGene", "xref", gene.getURI());
            for (Individual xref : xrefs) {
                Individual namespace = xref.getPropertyResourceValue(nsProp).as(Individual.class);
                String nsURI = namespace.getURI();
                nss.add(nsURI.substring(34));
            }
            
            String signature = cons(nss.size(), ":", nss.toString());
            Counter freq = freqs.get(signature);
            if (freq == null) {
                freq = new Counter();
                freqs.put(signature,freq);
            }
            freq.inc();
            pb.next();
             
        }
        
        //output
        StringBuilder b = new StringBuilder();
        for (String signature : freqs.keySet()) {
            int count = freqs.get(signature).getVal();
            b.append(signature).append("\t").append(count).append("\n");
        }
        
        return b.toString();
    }

    public void mergeAmbiguousXRefs() {

        List<Individual> speciesList = queryIndividuals("getAllSpecies", "species");
        for (Individual species : speciesList) {
            mergeAmbiguousXRefsOfSpecies(species);
        }

    }

    public void mergeAmbiguousXRefsOfSpecies(Individual species) {

        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Indexing XRefs for "+species.getURI());

        Map<String,Set<Individual>> xrefIndex = new HashMap<String, Set<Individual>>();
        
        List<Individual> xrefs = queryIndividuals("getXRefsOfSpecies", "xref", species.getURI());
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
        
        
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Merging ..."+species.getURI());
        
        for (Set<Individual> mergeSet : xrefIndex.values()) {
            
            Set<Neighbour> neighbours = new HashSet<Neighbour>();
            Individual toKeep = null;
            List<Individual> toDelete = new ArrayList<Individual>();
            
            for (Individual ind : mergeSet) {
                neighbours.addAll(findNeighbours(ind));
                if (toKeep == null) {
                    toKeep = ind;
                } else {
                    toDelete.add(ind);
                }
            }
            
            //reconnect neighbours to the keeper
            for (Neighbour neighbour : neighbours) {
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
    
    public String xrefAmbiguity(String species) {
        
        Logger.getLogger(Algo.class.getName())
                            .log(Level.INFO, "Calculating XRef ambiguity.");
        
        Map<String,Set<Individual>> xrefIndex = new HashMap<String, Set<Individual>>();
        
        List<Individual> xrefs = queryIndividuals("getXRefsOfSpecies", "xref", PRE+species);
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
    
    private Set<Neighbour> findNeighbours(Resource r) {
        
        Set<Neighbour> neighbours = new HashSet<Neighbour>();
        
        StmtIterator it = r.listProperties();
        while (it.hasNext()) {
            Statement s = it.nextStatement();
            neighbours.add(new Neighbour(s.getPredicate(),s.getObject(), true));
        }
        
        QueryExecution qexec = QueryExecutionFactory
                .create("SELECT ?s ?p WHERE {?s ?p <"+r.getURI()+">}",model);
        try {
            ResultSet result = qexec.execSelect();
            while (result.hasNext()) {
                QuerySolution sol = result.next();
                RDFNode subject = sol.get("s");
                Property pred = sol.get("p").as(Property.class);
                neighbours.add(new Neighbour(pred, subject, false));
            }
        } finally {
            qexec.close();
        }
        
        return neighbours;
    }
    
    private String cons(Object... ss) {
        StringBuilder b = new StringBuilder();
        for (Object s : ss) {
            b.append(s.toString());
        }
        return b.toString();
    }
    
    private static class Neighbour {
        private Property predicate;
        private RDFNode neighbour;
        private boolean outgoing;

        public Neighbour(Property predicate, RDFNode neighbour, boolean outgoing) {
            this.predicate = predicate;
            this.neighbour = neighbour;
            this.outgoing = outgoing;
        }

        public RDFNode getNeighbour() {
            return neighbour;
        }

        public boolean isOutgoing() {
            return outgoing;
        }

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
            final Neighbour other = (Neighbour) obj;
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
    
    
    private static class Counter {
        int val = 0;
        
        public void inc() {
            val++;
        }
        
        public void reset() {
            val = 0;
        }

        public int getVal() {
            return val;
        }
        
        
    }
    
}
