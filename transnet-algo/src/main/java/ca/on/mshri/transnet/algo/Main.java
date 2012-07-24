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

import java.io.Console;
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
            
            if(args.length == 0) {
                usageAndDie();
            }
            
            setupLogging();
            
            new Main().run(args);
            
        } catch (Throwable t) {
            processError(t);
            System.exit(1);
        }
    }

    /**
     * compiles an error message and logs it.
     * @param t 
     */
    private static void processError(Throwable t) {
        
        StringBuilder b = new StringBuilder(256);

        b.append(t.getMessage() != null ? t.getMessage() : "An error ocurred!");
        Throwable cause = t;
        while ((cause = cause.getCause()) != null) {
            b.append("\nReason: ").append(cause.getMessage() != null ? cause.getMessage() : "An error ocurred!");
        }

        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, b.toString(), t);
        System.err.println("See log file for details.");
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
        System.err.println("Usage: java -jar transnet-algo.jar <dbFile> {<command>}");
        System.exit(1);
    }

    /**
     * run the main program.
     */
    private void run(String args[]) {
        
        String dbPos = args[0];
        
        //check database
        File dbFile = new File(dbPos);
        if (!dbFile.exists() && dbFile.canRead()) {
            throw new RuntimeException("DB directory does not exist or cannot be read!");
        }
        
        greet();
        
        IO io = IO.getInstance();
        
        if (args.length > 1) {
            
            //execute command directly
            String cmd = args[1];
            try {
                String out = OperationsRegistry.getInstance().executeCommand(dbFile, cmd);
                io.write("out.csv", out);
                Logger.getLogger(Main.class.getName())
                        .log(Level.INFO, "Results written to out.csv");
            } catch (Exception e) {
                throw new RuntimeException("An error occurred executing your command", e);
            }
            
        } else {
            
            //interactive mode
            Console cons = System.console();
            if (cons == null) {
                throw new RuntimeException("Unable to access console!");
            }

            String outFile = "out.csv";
            boolean exit = false;
            while(!exit) {
                String cmd = cons.readLine("> ");
                if (cmd.equalsIgnoreCase("exit")) {
                    exit = true;
                } else if (cmd.equalsIgnoreCase("quit")) {
                    exit = true;
                } else if (cmd.startsWith("out=")) {
                    outFile = cmd.split("=")[1];
                } else if (cmd.equalsIgnoreCase("help")) {
                    System.out.println("Available commands:");
                    System.out.println(OperationsRegistry.getInstance().list());
                } else {
                    try {
                        String out = OperationsRegistry.getInstance().executeCommand(dbFile, cmd);
                        io.write(outFile, out);
                        Logger.getLogger(Main.class.getName())
                                .log(Level.INFO, "Results written to "+outFile);
                    } catch (Exception ex) {
                        processError(ex);
                    }
                }
            }
        
        }
        
    }

    private void greet() {
        System.out.println("\n\nTransnet Algorithms v0.1 -- Copyright Roth Lab 2012 LGPL");
        System.out.println("Send bug reports to Jochen Weile <jochenweile@gmail.com>");
    }
}
