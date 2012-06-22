/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.populator;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.jweile.yogiutil.pipeline.EndNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author jweile
 */
final class TripleStoreWriter extends EndNode<Entry> {

    private File outFile;
    
    private OntModel model;
    
    private OntClass objectClass;
    private OntClass xrefClass;
    private DatatypeProperty valueProp;
    private ObjectProperty nsProp;
    private ObjectProperty fromSpeciesProp;
    private ObjectProperty xrefProp;
    
    private Map<Integer,Individual> id2namespaces;
    private Map<Integer,Individual> id2species;
    
    public static final String PRE = "http://llama.mshri.on.ca/sbns.owl#";
    
    public TripleStoreWriter(File outFile) {
        super("TripleStore Writer");
        this.outFile = outFile;
    }

    @Override
    protected void before() {
        
        InputStream in = Main.class.getClassLoader().getResourceAsStream("sbns.owl");
        
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(in,null);
                
        objectClass = model.getOntClass(PRE+"Object");
        xrefClass = model.getOntClass(PRE+"XRef");
        OntClass nsClass = model.getOntClass(PRE+"Namespace");
        OntClass speciesClass = model.getOntClass(PRE+"Species");
        
        valueProp = model.getDatatypeProperty(PRE+"hasValue");
        
        nsProp = model.getObjectProperty(PRE+"hasNamespace");
        fromSpeciesProp = model.getObjectProperty(PRE+"fromSpecies");
        xrefProp = model.getObjectProperty(PRE+"hasXRef");
        
        id2namespaces = new IndividualPopulator(model, nsClass).run("namespaces");
        id2species = new IndividualPopulator(model, speciesClass).run("species");
        
    }

    @Override
    public Void process(Entry in) {
        
        Individual object = model.createIndividual(objectClass);
        
        Individual species = id2species.get(in.getSpeciesId());
        object.addProperty(fromSpeciesProp, species);
        
        for (Entry.Synonym syn : in.getSynonyms()) {
            
            Individual xref = model.createIndividual(xrefClass);
            object.addProperty(xrefProp, xref);
            xref.addLiteral(valueProp, syn.getSynonym());
            
            Individual namespace = id2namespaces.get(syn.getNsId());
            xref.addProperty(nsProp, namespace);
        }
        
        return null;
    }


    
    @Override
    protected void after() {
        
        Logger.getLogger(TripleStoreWriter.class.getName())
                .info("Writing RDF file...");
        
        OutputStream out = null;
        try {
            out = new GZIPOutputStream(new FileOutputStream(outFile));
            model.write(out, "RDF/XML", null);
            model.close();
            
            Logger.getLogger(TripleStoreWriter.class.getName())
                    .info("done.");
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write to file "+outFile.getName(), ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(TripleStoreWriter.class.getName())
                        .log(Level.WARNING, "Failed to close output stream", ex);
            }
        }
    }
    
    

}
