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
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.jweile.yogiutil.CliProgressBar;
import de.jweile.yogiutil.IntArrayList;
import de.jweile.yogiutil.LazyInitMap;
import de.jweile.yogiutil.SetOfTwo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Infer a coherence score for each namespace based on how many xref-links agree
 * for this each namespace.
 * 
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class NamespaceCoherenceAnalysis implements JenaModelOperation<String, String> {
    
    @Override
    public String operation(Model tdbModel, String species) {
        
        OntModel model = ModelFactory
                .createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbModel);
        
        Sparql sparql = Sparql.getInstance();
        
        Logger.getLogger(XRefClusterAnalysis.class.getName())
                            .log(Level.INFO, "Searching for connected genes...");
                
        Set<SetOfTwo<Individual>> pairs = getAllConnectedPairs(model, species);
        
        Logger.getLogger(XRefClusterAnalysis.class.getName())
                            .log(Level.INFO, "Found "+pairs.size()+
                            " connected gene pairs.");
        
        CliProgressBar pb = new CliProgressBar(pairs.size());
        
        LazyInitMap<Individual,IntArrayList> index = 
                new LazyInitMap<Individual, IntArrayList>(IntArrayList.class);
        
        for (SetOfTwo<Individual> pair : pairs) {
            
            Map<Individual, Integer> scores = getNamespaceScores(pair.getA(), pair.getB(), model);
            
            int sum = 0;
            for (int score : scores.values()) {
                sum += score;
            }
            for (Individual ns : scores.keySet()) {
                index.getOrCreate(ns).add(sum-scores.get(ns));
            }
                        
            pb.next();
        }
        
        //OUTPUT
        
        //deterimine longest namespace list
        int maxlen = Integer.MIN_VALUE;
        for (List<Integer> l : index.values()) {
            maxlen = l.size() > maxlen ? l.size() : maxlen;
        }
        
        StringBuilder b = new StringBuilder();
        
        //header
        List<Individual> keys = new ArrayList<Individual>(index.keySet());
        for (Individual ns : keys) {
            b.append(ns).append("\t");
        }
        if (b.length() > 0) {
            b.deleteCharAt(b.length() -1);
            b.append("\n");
        }
        
        for (int i = 0; i < maxlen; i++) {
            
            for (Individual key : keys) {
                List<Integer> list = index.get(key);
                if (list.size() > i) {
                    b.append(list.get(i));
                } 
                b.append("\t");
            }
            b.deleteCharAt(b.length() -1);
            b.append("\n");
            
        }
        
        return b.toString();
    }
    
    private static Comparator<Individual> COMP = new Comparator<Individual>() {

        @Override
        public int compare(Individual t, Individual t1) {
            return t.getURI().compareTo(t1.getURI());
        }
        
    };
    
    /**
     * Get all pairs of genes that share at least one xref.
     * 
     * @param model
     * @param species
     * @return 
     */
    private Set<SetOfTwo<Individual>> getAllConnectedPairs(Model model, String species) {
        
        Sparql sparql = Sparql.getInstance();
        
        Set<SetOfTwo<Individual>> list = new HashSet<SetOfTwo<Individual>>();
        
        QueryExecution q = QueryExecutionFactory.create(
                sparql.get("getAllConnectedGenes", SBNS+species), 
                model
        );
        
        try {
            ResultSet r = q.execSelect();

            while (r.hasNext()) {

                QuerySolution sol = r.next();

                Individual gene1 = sol.getResource("gene1")
                        .as(Individual.class);
                Individual gene2 = sol.getResource("gene2")
                        .as(Individual.class);

                list.add(new SetOfTwo<Individual>(gene1, gene2, COMP));
                    
            }
            
        } finally {
            if (q != null) {
                q.close();
            }
        }
        
        
        return list;
    }

    private Map<Individual,Integer> getNamespaceScores(Individual a, Individual b, OntModel model) {
        
        Map<Individual,Integer> counts = new HashMap<Individual, Integer>();
        
        Sparql sparql = Sparql.getInstance();
        
        ResultSet r = QueryExecutionFactory.create(
            sparql.get("getCommonNsOfPair",
                a.getURI(), 
                b.getURI()
            ),
            model
        ).execSelect();

        while (r.hasNext()) {

            QuerySolution s = r.next();

            int numref = s.getLiteral("numref").getInt();
            Individual ns = s.getResource("ns").as(Individual.class);

            counts.put(ns,numref);

        }
        
        return counts;
    }
    
}
