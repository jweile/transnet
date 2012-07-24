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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles Input/Output of this program.
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class IO {
    
    /**
     * singleton instance
     */
    private static IO instance;
    
    /**
     * output directory
     */
    private File outputDir;
    
    /**
     * singleton constructor
     */
    private IO() {
        
    }

    /**
     * singleton getter
     * @return the singleton instance
     */
    public static IO getInstance() {
        if (instance == null) {
            instance = new IO();
        }
        return instance;
    }
    
    
    /**
     * writes a string to a file.
     * @param file the file
     * @param contents the contents to be written.
     */
    public void write(File file, String contents) {
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter(file));
            w.write(contents);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write output file!", e);
        } finally {
            try {
                w.close();
            } catch (IOException ex) {
                Logger.getLogger(IO.class.getName())
                        .log(Level.SEVERE, "Unable to close stream", ex);
            }
        }
    }
    
    /**
     * writes a string to a file in the predefined output directory.
     * @param filename the file
     * @param contents the contents to be written.
     */
    public void write(String filename, String contents) {
        write(new File(getOutputDir(),filename),contents);
    }
    
    /**
     * reads from an input stream into a string
     * @param path path that identifies the inputStream (e.g. file path)
     * @param inputStream an input stream to read from.
     * @return a string containing whatever was read from the stream.
     */
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
    
    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    
    public File getOutputDir() {
        if (outputDir == null) {
            outputDir = new File(new File("output"),format.format(new Date()));
            outputDir.mkdirs();
        }
        return outputDir;
    }
    
    public void deleteRecursively(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteRecursively(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }
    
    
    
}
