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
package ca.on.mshri.transnet.algo.operations;

import com.hp.hpl.jena.rdf.model.Model;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public interface JenaModelOperation<I,O> {
    
    
    /**
     * SBNS namespace prefix fpr OWL
     */
    public static final String SBNS = "http://llama.mshri.on.ca/sbns.owl#";
    
    public abstract O operation(Model model, I in);
    
}
