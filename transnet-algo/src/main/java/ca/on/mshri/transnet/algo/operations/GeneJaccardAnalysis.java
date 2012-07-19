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
import com.hp.hpl.jena.ontology.ConversionException;
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
import de.jweile.yogiutil.SetOfTwo;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculate Jaccard coefficient for all gene pairs.
 * 
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class GeneJaccardAnalysis extends JenaModelOperation<String, String> {

    @Override
    public String operation(Model tdbModel, String species) {
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbModel);
        
        Sparql sparql = Sparql.getInstance();
        
        Logger.getLogger(XRefClusterAnalysis.class.getName())
                            .log(Level.INFO, "Searching for connected genes...");
        
        StringBuilder b = new StringBuilder("Jaccard\tIntersection\n");
        
        Set<SetOfTwo<Individual>> allConnectedPairs = getAllConnectedPairs(model, species);
        
        Logger.getLogger(XRefClusterAnalysis.class.getName())
                            .log(Level.INFO, "Found "+allConnectedPairs.size()+" connected gene pairs.");
        
        CliProgressBar pb = new CliProgressBar(allConnectedPairs.size());
        
        for (SetOfTwo<Individual> pair : allConnectedPairs) {
            
            List<Individual> xrefs1 = sparql.queryIndividuals(model, "getXRefsOfGene", "xref", pair.getA().getURI());
            List<Individual> xrefs2 = sparql.queryIndividuals(model, "getXRefsOfGene", "xref", pair.getB().getURI());
            
            Set<Individual> union = new HashSet<Individual>();
            union.addAll(xrefs1);
            union.addAll(xrefs2);
            
            Set<Individual> intersection = new HashSet<Individual>();
            intersection.addAll(xrefs1);
            intersection.retainAll(xrefs2);
            
            double jaccard = (double)intersection.size() / (double)union.size();
            
            b.append(jaccard)
                .append("\t")
                .append(intersection.size())
                .append("\n");
            
            pb.next();
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
    
}
