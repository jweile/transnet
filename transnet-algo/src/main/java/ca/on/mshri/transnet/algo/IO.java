/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.algo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jweile
 */
public class IO {
    
    private static IO instance;
    
    private IO() {
        
    }

    public static IO getInstance() {
        if (instance == null) {
            instance = new IO();
        }
        return instance;
    }
    
    
    
    public void write(String filename, String contents) {
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter(filename));
            w.write(contents);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write output file!", e);
        } finally {
            try {
                w.close();
            } catch (IOException ex) {
                Logger.getLogger(Algo.class.getName())
                        .log(Level.SEVERE, "Unable to close stream", ex);
            }
        }
    }
    
    public String read(String path, InputStream inputStream) {
        
        StringBuilder b = new StringBuilder();
        
        InputStreamReader r = null;
        try {
            
            r = new InputStreamReader(inputStream);
            char[] buf = new char[1024];
            int len = 0;
            while ((len = r.read(buf)) > -1) {
                b.append(buf, 0, len);
            }
            
            return b.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("Error reading file "+path, e);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Sparql.class.getName())
                        .log(Level.SEVERE, "Unable to close stream", ex);
            }
        }
    }
    
}
