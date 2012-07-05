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

import ca.on.mshri.transnet.algo.operations.JenaModelOperation;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.io.File;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class TDBAccess<I,O> {
    
    private File dbFile;
    
    private JenaModelOperation<I,O> jma;
    
    public TDBAccess(File dbFile, JenaModelOperation<I,O> jma) {
        this.dbFile = dbFile;
    }
    
    public O perform(I in) {
                
        //try to connect to TDB
        Dataset tdbSet = null;
        try {
            
            tdbSet = TDBFactory.createDataset(dbFile.getAbsolutePath());
            
            Model model = tdbSet.getDefaultModel();
            
            O out = jma.operation(model, in);
            
            model.commit();
            
            return out;
            
        } finally {
            if (tdbSet == null) {
                tdbSet.close();
                TDB.closedown();
            }
        }
    }
    
}
