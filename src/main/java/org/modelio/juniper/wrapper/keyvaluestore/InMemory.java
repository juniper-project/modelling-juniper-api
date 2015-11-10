/**
 * Copyright 2014 Modeliosoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modelio.juniper.wrapper.keyvaluestore;

import java.util.HashMap;

import mpi.MPI;

import org.modelio.juniper.platform.JuniperProgram;
import org.modelio.juniper.wrapper.WrapperHelper;

public class InMemory extends JuniperProgram {
	
    public IKeyValueStore<Object, Object> iKeyValueStoreImpl = new IKeyValueStore<Object, Object>() {
		public void put(Object key, Object value) {
			store.put(key, value);
		}
		public Object find(Object key) {
			Object value = store.get(key);
			return value;
		}
	};

    public final HashMap<Object, Object> store = new HashMap<Object, Object>();

    public static void main(final String[] args) throws java.lang.Exception {
    	InMemory inMemory = new InMemory();
    	
    	WrapperHelper.initialize(inMemory.communicationToolkit, inMemory.iKeyValueStoreImpl, args);

    	MPI.Init(args);
    	inMemory.communicationToolkit.initProgramCommunication();
    	inMemory.initProvidedInterfaces();
        while (true) {
            Thread.yield();        
            inMemory.communicationToolkit.processReceivedMessages();
        }
    }

    public void initProvidedInterfaces() throws Exception {
    	if (juniperPlatform !=  null) {
    		WrapperHelper.initialize(communicationToolkit,iKeyValueStoreImpl, juniperPlatform);
    	}
    }
    
}
