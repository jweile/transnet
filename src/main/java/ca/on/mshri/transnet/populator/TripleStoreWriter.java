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
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import de.jweile.yogiutil.pipeline.EndNode;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author jweile
 */
final class TripleStoreWriter extends EndNode<Entry> {

    private File outFile;
    
    private OntModel model;
    private Dataset tdbSet;
    
    private OntClass objectClass;
    private OntClass xrefClass;
    private DatatypeProperty valueProp;
    private ObjectProperty nsProp;
    private ObjectProperty fromSpeciesProp;
    private ObjectProperty xrefProp;
    
    private Map<Integer,Individual> id2namespaces;
    private Map<Integer,Individual> id2species;
    
    public static final String SBNS = "http://llama.mshri.on.ca/sbns.owl#";
    public static final String TRN = "urn:transnet:";
    
    private long lastObject = 0;
    private long lastXref = 0;
    
    public TripleStoreWriter(File outFile) {
        super("TripleStore Writer");
        this.outFile = outFile;
    }

    @Override
    protected void before() {
        
        tdbSet = TDBFactory.createDataset(outFile.getAbsolutePath());
        
        InputStream in = Main.class.getClassLoader().getResourceAsStream("sbns.owl");
        
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, tdbSet.getDefaultModel());
        
        model.read(in,null);
                
        objectClass = model.getOntClass(SBNS+"Object");
        xrefClass = model.getOntClass(SBNS+"XRef");
        OntClass nsClass = model.getOntClass(SBNS+"Namespace");
        OntClass speciesClass = model.getOntClass(SBNS+"Species");
        
        valueProp = model.getDatatypeProperty(SBNS+"hasValue");
        
        nsProp = model.getObjectProperty(SBNS+"hasNamespace");
        fromSpeciesProp = model.getObjectProperty(SBNS+"fromSpecies");
        xrefProp = model.getObjectProperty(SBNS+"hasXRef");
        
        id2namespaces = new IndividualPopulator(model, nsClass).run("namespaces");
        id2species = new IndividualPopulator(model, speciesClass).run("species");
        
    }

    @Override
    public Void process(Entry in) {
        
        Individual object = model.createIndividual(TRN+"gene-" + ++lastObject, objectClass);
        
        Individual species = id2species.get(in.getSpeciesId());
        object.addProperty(fromSpeciesProp, species);
        
        for (Entry.Synonym syn : in.getSynonyms()) {
            
            Individual xref = model.createIndividual(TRN+"xref-"+ ++lastXref, xrefClass);
            object.addProperty(xrefProp, xref);
            xref.addLiteral(valueProp, syn.getSynonym());
            
            Individual namespace = id2namespaces.get(syn.getNsId());
            xref.addProperty(nsProp, namespace);
        }
        
        return null;
    }
    
    
    @Override
    protected void after() {
        
        model.commit();
        model.close();
        tdbSet.close();
        
//        Logger.getLogger(TripleStoreWriter.class.getName())
//                .info("Writing RDF file...");
//        
//        OutputStream out = null;
//        try {
//            out = new GZIPOutputStream(new FileOutputStream(outFile));
//            model.write(out, "RDF/XML", null);
//            model.close();
//            
//            Logger.getLogger(TripleStoreWriter.class.getName())
//                    .info("done.");
//        } catch (IOException ex) {
//            throw new RuntimeException("Unable to write to file "+outFile.getName(), ex);
//        } finally {
//            try {
//                out.close();
//            } catch (IOException ex) {
//                Logger.getLogger(TripleStoreWriter.class.getName())
//                        .log(Level.WARNING, "Failed to close output stream", ex);
//            }
//        }
    }
    
    

}
