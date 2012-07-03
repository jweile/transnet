/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.populator;

import java.io.File;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static ca.on.mshri.transnet.populator.TripleStoreWriter.SBNS;

/**
 *
 * @author jweile
 */
class IndividualPopulator {

    private OntClass clazz;
    private OntModel model;
    
    public IndividualPopulator(OntModel model, OntClass clazz) {
        this.clazz = clazz;
        this.model = model;
    }

    Map<Integer, Individual> run(String file) {
        
        Map<Integer,Individual> map = new HashMap<Integer, Individual>();
        
        InputStream in = IndividualPopulator.class.getClassLoader().getResourceAsStream(file+".tsv");
        if (in == null) {
            try {
               in = new FileInputStream(new File(new File(new File(new File("src"),"main"),"resources"),file+".tsv"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to find resource file",e);
            }
        }
        
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        try {
            
            String line; int lnum = 0;
            while ((line = r.readLine()) != null) {
                lnum++;
                if (line.length() == 0) {
                    continue;
                }
                
                String[] cols = line.split("\t");
                
                int id;
                
                try {
                    id = Integer.parseInt(cols[0]);
                } catch (NumberFormatException e) {
                    Logger.getLogger(IndividualPopulator.class.getName())
                        .log(Level.WARNING, "Formatting error in file "+
                            file
                            +".tsv in line "+lnum, e);
                    continue;
                }
                
                Individual ind = model.createIndividual(SBNS+cols[1],clazz);
                
                map.put(id, ind);
                
            }
            
            return map;
            
        } catch (IOException ex) {
            throw new RuntimeException("Error trying to read file "+file+".tsv");
        } finally {
            try {
                r.close();
            } catch (IOException ex) {
                Logger.getLogger(IndividualPopulator.class.getName())
                        .log(Level.WARNING, "Unable to close input stream!", ex);
            }
        }
    }
    
}
