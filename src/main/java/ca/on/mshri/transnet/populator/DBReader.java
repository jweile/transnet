/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.mshri.transnet.populator;

import de.jweile.yogiutil.CliProgressBar;
import de.jweile.yogiutil.pipeline.StartNode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jweile
 */
final class DBReader extends StartNode<Entry> {
    
    private Connection db;
    
    private String user, pwd;
    
    private int[] geneIds;
    
    private int geneIdPointer = 0;
    
    private CliProgressBar pb;

    public DBReader(String user, String pwd) {
        super("DB Reader");
        this.user = user;
        this.pwd = pwd;
    }

    
    
    @Override
    public void before() {
        try {
            
            //load driver
            Class.forName("org.postgresql.Driver");
            
            //open connection
            db = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/synergizer", 
                    user, pwd);
            
            
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT COUNT(id) FROM gene;");
            r.next();
            geneIds = new int[r.getInt(1)];
            s.close();
            
            s = db.createStatement();
            r = s.executeQuery("SELECT id FROM gene;");
            int i=0;
            while (r.next()) {
                geneIds[i++] = r.getInt(1);
            }
            s.close();
            
            pb = new CliProgressBar(geneIds.length);
            
        } catch (SQLException e) {
            throw new RuntimeException("Initiation of DB connection failed!", e);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Failed to invoke JDBC driver. Report this as a bug!");
        }
            
    }
    
    @Override
    public void after() {
        try {
            db.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBReader.class.getName())
                    .log(Level.SEVERE, "Failed to close DB connection!", ex);
        }
    }
    

    @Override
    public Entry process(Void in) {
        
        if (geneIdPointer >= geneIds.length) {
            Logger.getLogger(DBReader.class.getName()).info("Finished reading DB!");
            return null;
        }
        
        int currGeneId = geneIds[geneIdPointer++];
        
        try {
            
                        
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery(
                    "SELECT org_id, source_id FROM gene WHERE id='"+
                    currGeneId+"';");
            r.next();
            Entry e = new Entry(currGeneId, 
                    r.getInt("org_id"), 
                    r.getInt("source_id")
                    );
            s.close();
            
            s = db.createStatement();
            r = s.executeQuery(
                    "SELECT syn_id, source_id, ns_id FROM synonym WHERE gene_id='"+
                    currGeneId+"';"
                    );
            
            while (r.next()) {
                e.addSynonym(new Entry.Synonym(r.getString("syn_id"), 
                        r.getInt("source_id"), 
                        r.getInt("ns_id")
                        ));
            }
            s.close();
            
            pb.next();
            
            return e;
            
        } catch (SQLException ex) {
            throw new RuntimeException("Query failed!",ex);
        }
    }
    
    
}
