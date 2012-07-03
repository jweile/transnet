/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.algo;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
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
 *
 * @author jweile
 */
public class Main {
    
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

    private static void processError(Throwable t) {
        
        StringBuilder b = new StringBuilder(256);

        b.append(t.getMessage());
        Throwable cause = t;
        while ((cause = cause.getCause()) != null) {
            b.append("\nReason: ").append(cause.getMessage());
        }

        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, b.toString(), t);
    }

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

    private static void usageAndDie() {
        System.err.println("Usage: java -jar transnet-algo.jar <dbFile> <species>");
        System.exit(1);
    }

    /**
     * 
     */
    private void run(String dbPos, String species) {
        
        File dbFile = new File(dbPos);
        if (!dbFile.exists() && dbFile.canRead()) {
            throw new RuntimeException("DB directory does not exist or cannot be read!");
        }
        
        IO io = IO.getInstance();
        
        Dataset tdbSet = null;
        try {
            
            tdbSet = TDBFactory.createDataset(dbFile.getAbsolutePath());
            Algo algo = new Algo(tdbSet.getDefaultModel());
            
            String out = algo.xrefStats(species);
            io.write("xref_freqs.csv", out);
            
            out = algo.xrefClusters(species);
            io.write("xref_clusters.csv",out);
            
            out = algo.xrefAmbiguity(species);
            io.write("xref_ambiguous.txt", out);
            
        } finally {
            if (tdbSet == null) {
                tdbSet.close();
            }
        }
        
    }
}
