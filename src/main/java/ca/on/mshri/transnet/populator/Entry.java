/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.populator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jweile
 */
final class Entry {
    
    public static final int QUEUE_LENGTH = 10;

    private int geneId;
    
    private List<Synonym> synonyms;
    
    private int speciesId;
    
    private int sourceId;

    public Entry() {
    }

    public Entry(int geneId, int speciesId, int sourceId) {
        this.geneId = geneId;
        this.speciesId = speciesId;
        this.sourceId = sourceId;
    }
    
    public int getGeneId() {
        return geneId;
    }

    public void setGeneId(int geneId) {
        this.geneId = geneId;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }

    public List<Synonym> getSynonyms() {
        if (synonyms == null) {
            return Collections.EMPTY_LIST;
        }
        return synonyms;
    }

    public void addSynonym(Synonym s) {
        if (synonyms == null) {
            synonyms = new ArrayList<Synonym>();
        }
        synonyms.add(s);
    }
    
    static class Synonym {
        
        private String synonym;
        
        private int sourceId;
        
        private int nsId;

        public Synonym() {
        }

        public Synonym(String synonym, int sourceId, int nsId) {
            this.synonym = synonym;
            this.sourceId = sourceId;
            this.nsId = nsId;
        }
        
        public int getNsId() {
            return nsId;
        }

        public void setNsId(int nsId) {
            this.nsId = nsId;
        }

        public int getSourceId() {
            return sourceId;
        }

        public void setSourceId(int sourceId) {
            this.sourceId = sourceId;
        }

        public String getSynonym() {
            return synonym;
        }

        public void setSynonym(String synonym) {
            this.synonym = synonym;
        }
        
        
    }
}
