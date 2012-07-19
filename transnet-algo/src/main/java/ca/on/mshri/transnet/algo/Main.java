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

import ca.on.mshri.transnet.algo.operations.GeneJaccardAnalysis;
import ca.on.mshri.transnet.algo.operations.XRefFrequencyAnalysis;
import ca.on.mshri.transnet.algo.operations.XRefClusterAnalysis;
import ca.on.mshri.transnet.algo.operations.XRefRedundancyFinder;
import ca.on.mshri.transnet.algo.operations.XRefMerger;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This module performs algorithmic operations on the transnet triplestore.
 * 
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class Main {
    
    /**
     * Main method.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        try {
            
            if(args.length < 2) {
                usageAndDie();
            }
            
            setupLogging();
            
            new Main().run(args[0], args[1]);
            
        } catch (Throwable t) {
            processError(t);
        }
    }

    /**
     * compiles an error message and logs it.
     * @param t 
     */
    private static void processError(Throwable t) {
        
        StringBuilder b = new StringBuilder(256);

        b.append(t.getMessage());
        Throwable cause = t;
        while ((cause = cause.getCause()) != null) {
            b.append("\nReason: ").append(cause.getMessage());
        }

        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, b.toString(), t);
    }

    /**
     * Enables simple command line output for and extensive logging to file.
     * @throws IOException If the log file cannot be written.
     */
    private static void setupLogging() throws IOException {
        
        //setup CLI output
        for (Handler h : Logger.getLogger("").getHandlers()) {
            if (h instanceof ConsoleHandler) {
                ConsoleHandler ch = ((ConsoleHandler)h);
                ch.setLevel(Level.INFO);
                ch.setFormatter(new Formatter() {

                    @Override
                    public String format(LogRecord lr) {

                        StringBuilder b = new StringBuilder();

                        b.append(lr.getLevel().toString())
                                .append(": ");

                        b.append(lr.getMessage())
                                .append("\n");

                        return b.toString();
                    }

                });
                break;
            }
        }
        
        //setup log file writer
        File logFile = new File("transnet-algo.log");
        FileHandler fh = new FileHandler(logFile.getAbsolutePath());
        fh.setLevel(Level.ALL);
        fh.setFormatter(new SimpleFormatter());
        Logger.getLogger("").addHandler(fh);
    }

    /**
     * print usage and exit
     */
    private static void usageAndDie() {
        System.err.println("Usage: java -jar transnet-algo.jar <dbFile> <species>");
        System.exit(1);
    }

    /**
     * run the main program.
     */
    private void run(String dbPos, String species) {
        
        //check database
        File dbFile = new File(dbPos);
        if (!dbFile.exists() && dbFile.canRead()) {
            throw new RuntimeException("DB directory does not exist or cannot be read!");
        }
        
        IO io = IO.getInstance();
        
        String out;
        
//        out = new TDBAccess<String,String>(dbFile, new XRefFrequencyAnalysis())
//                .perform(species);
//        io.write("xref_freqs.csv", out);
//        
//        out = new TDBAccess<String,String>(dbFile, new XRefClusterAnalysis())
//                .perform(species);
//        io.write("xref_clusters.csv", out);
        
//        out = new TDBAccess<String,String>(dbFile, new XRefRedundancyFinder())
//                .perform(species);
//        io.write("xref_redundancies.csv", out);
//        
//        new TDBAccess<String,Void>(dbFile, new XRefMerger())
//                .perform(species);
//        
//        out = new TDBAccess<String,String>(dbFile, new XRefRedundancyFinder())
//                .perform(species);
//        io.write("xref_redundancies_after.csv", out);
        
        out = new TDBAccess<String,String>(dbFile, new GeneJaccardAnalysis())
                .perform(species);
        io.write("genepair_jaccard.csv", out);
        
    }
}
