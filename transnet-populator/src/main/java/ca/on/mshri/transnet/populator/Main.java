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
package ca.on.mshri.transnet.populator;

import de.jweile.yogiutil.pipeline.Pipeline;
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
 * Reads from the synergizer database and writes to a Jena TDB triplestore.
 * Uses a parallelized pipeline configuration.
 * 
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public final class Main {
    
    /**
     * Main method.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            
            if(args.length < 2) {
                usageAndDie();
            }
            
            setupLogging();
            
            new Main().run(args[0],args[1]);
            
        } catch (Throwable t) {
            processError(t);
        }
    }

    /**
     * compile error message and log it.
     * @param t the error
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
     * simple CLI output + detailed log file.
     * @throws IOException if log file cannot be written.
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
        File logFile = new File("transnet-populator.log");
        FileHandler fh = new FileHandler(logFile.getAbsolutePath());
        fh.setLevel(Level.ALL);
        fh.setFormatter(new SimpleFormatter());
        Logger.getLogger("").addHandler(fh);
    }

    /**
     * print usage and exit
     */
    private static void usageAndDie() {
        System.err.println("Usage: java -jar transnet-populator.jar <username> <password>");
        System.exit(1);
    }

    /**
     * sets up the pipeline and runs it.
     */
    private void run(String user, String pwd) {
        
        File outFile = new File("tdb_out");
        
        Pipeline pipeline = new Pipeline();
        pipeline.addNode(new DBReader(user,pwd));
        pipeline.addNode(new TripleStoreWriter(outFile));
        pipeline.start();
        
    }
    
}
