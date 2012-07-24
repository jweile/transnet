/*
 * Copyright (C) 2012 Department of Molecular Genetics, University of Toronto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.mshri.transnet.algo;

import ca.on.mshri.transnet.algo.operations.JenaModelOperation;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class IntegrationIT extends TestCase{

    
    private File targetDir = new File("target");
    
    private File unzippedDir;
    
    private File tdbDir;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        tdbDir = new File(targetDir,"tdb_test");
        
        JenaModelOperation<Void,Void> jmo = new JenaModelOperation<Void,Void>() {
            @Override
            public Void operation(Model model, Void in) {
                try {
                    model.add(new OntTestData().setUpTestModel());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                return null;
            }
        };
        
        TDBAccess<Void,Void> tdba = new TDBAccess<Void, Void>(tdbDir, jmo);
        tdba.perform(null);
        
        File zipFile = new File(targetDir, "transnet-algo-0.1-SNAPSHOT-distro.zip");
        
        unzip(zipFile, targetDir);
        unzippedDir = new File(targetDir,"transnet-algo-0.1-SNAPSHOT");
        
        assertTrue(unzippedDir.exists());
    }
    
    
    
    public void test() throws Exception {
        
        File jarFile = new File(new File(unzippedDir,"bin"),"transnet-algo-0.1-SNAPSHOT.jar");
                
        String operation = "XRefFrequencyAnalysis(\"Saccharomyces_cerevisiae\")";
        
        String[] cmdargs = {"java", "-jar", jarFile.getAbsolutePath(), tdbDir.getAbsolutePath(), operation};
        
        Logger.getLogger(IntegrationIT.class.getName())
                .log(Level.INFO, "Executing: "+cons(cmdargs));
        
        Process process = Runtime.getRuntime().exec(cmdargs, new String[0], unzippedDir);
        follow(process);
        process.waitFor();
        
        assertTrue(process.exitValue() == 0);
        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        if (unzippedDir.exists()) {
            IO.getInstance().deleteRecursively(unzippedDir);
        }
        
        if (tdbDir.exists()) {
            IO.getInstance().deleteRecursively(tdbDir);
        }
    }
    
    

    private void unzip(File zipfile, File directory) throws IOException {
        ZipFile zfile = new ZipFile(zipfile);
        Enumeration<? extends ZipEntry> entries = zfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(directory, entry.getName());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                InputStream in = zfile.getInputStream(entry);
                try {
                  copy(in, file);
                } finally {
                  in.close();
                }
            }
        }
    }
    
    private static void copy(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            copy(in, out);
        } finally {
            out.close();
        }
    }
    
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    private void follow(Process process) {
        InputStream err = process.getErrorStream(),
                out = process.getInputStream();
        
        byte[] errbuff = new byte[1024], 
                outbuff = new byte[1024];
        
        int errlen = 0,
                outlen = 0;
        
        while (errlen >= 0 && outlen >= 0) {
            if (errlen >= 0) {
                try {
                    errlen = err.read(errbuff);
                    System.err.write(errbuff, 0, errlen);
                } catch (IOException e) {
                    errlen = -1;
                    System.err.println("Error reading process output!");
                }
            }
            if (outlen >= 0) {
                try {
                    outlen = out.read(outbuff);
                    System.out.write(outbuff, 0, outlen);
                } catch (IOException e) {
                    outlen = -1;
                    System.err.println("Error reading process output!");
                }
            }
        }
    }

    private String cons(String[] strings) {
        StringBuilder b = new StringBuilder();
        
        for (String string : strings) {
            b.append(string);
            b.append(" ");
        }
        if (b.length() > 0) {
            b.deleteCharAt(b.length()-1);
        }
        
        return b.toString();
    }
    
}
