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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class OperationsRegistry {
        
    private static OperationsRegistry instance;
    
    private Map<String,JenaModelOperation<?,?>> registry = new HashMap<String, JenaModelOperation<?,?>>();

    private OperationsRegistry() {
        register();
    }

    public static OperationsRegistry getInstance() {
        if (instance == null) {
            instance = new OperationsRegistry();
        }
        return instance;
    }
    
    private void register() {
        for (JenaModelOperation<?,?> op : ServiceLoader.load(JenaModelOperation.class)) {
            registry.put(op.getClass().getSimpleName(), op);
        }
    }
    
    public List<String> list() {
        return new ArrayList<String>(registry.keySet());
    }
    
    public <I,O> JenaModelOperation<I,O> get(String name) {
        return (JenaModelOperation<I,O>) registry.get(name);
    }
    
    //matches method("arg")
    private static Pattern CMD_PATTERN = Pattern.compile("(\\w+)\\(\"(\\w+)\"\\)");
    
    public <O> O executeCommand(File dbFile, String cmd) throws Exception {
        
        Matcher matcher = CMD_PATTERN.matcher(cmd);
        if (!matcher.matches()) {
            throw new Exception("Invalid command! Usage: method(\"arg\")\n"
                    + "Available method names: "+list());
        }
        
        String methodName = matcher.group(1);
        String arg = matcher.group(2);
        
        JenaModelOperation<String, O> op = get(methodName);
        
        return new TDBAccess<String,O>(dbFile, op)
                .perform(arg);
    }
    
    
}
