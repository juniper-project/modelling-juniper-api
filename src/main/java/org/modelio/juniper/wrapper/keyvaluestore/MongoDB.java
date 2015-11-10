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

import java.net.UnknownHostException;
import java.util.logging.Logger;

import mpi.MPI;

import org.modelio.juniper.CommunicationToolkit;
import org.modelio.juniper.platform.JuniperProgram;
import org.modelio.juniper.wrapper.WrapperHelper;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoDB extends JuniperProgram {
    private MongoClient mongoClient;
    private DB db;
    private DBCollection dbc;    
    
    public MongoDB() {
    	try {
			mongoClient = new MongoClient("localhost", 27017);
			db = mongoClient.getDB("db");
			dbc = db.getCollection("col");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}
    }
    
    public IKeyValueStore<Object,Object> iKeyValueStoreImpl = new IKeyValueStore<Object,Object>() {
		public void put(Object key, Object value) {			
			DBObject kdoc = BasicDBObjectBuilder.start().add("_id", key).get();
			if (dbc.find(kdoc).hasNext()) {
				DBObject vdoc = BasicDBObjectBuilder
						.start()
						.add("$set",
								BasicDBObjectBuilder.start()
										.add("value", value).get()).get();
				dbc.update(kdoc, vdoc);
			} else {
				DBObject doc = BasicDBObjectBuilder.start().add("_id", key)
						.add("value", value).get();
				dbc.insert(doc);
			}
		}
		public Object find(Object key) {
			DBObject doc = BasicDBObjectBuilder.start().add( "_id" , key ).get();
			DBCursor cursor = dbc.find(doc);
			Object value = null;
			if (cursor.hasNext()) {
				value = cursor.next().get("value");
			}
			return value;
		}
	};

    public static final Logger log = Logger.getLogger(MongoDB.class.getName());

    public static void main(final String[] args) throws java.lang.Exception {
    	MongoDB mongodb = new MongoDB();
    	WrapperHelper.initialize(mongodb.communicationToolkit, mongodb.iKeyValueStoreImpl, args);

    	MPI.Init(new String[0]);
    	mongodb.communicationToolkit.initProgramCommunication();                            	
        mongodb.initProvidedInterfaces();
        while (true) {
            Thread.yield();        
            mongodb.communicationToolkit.processReceivedMessages();
        }
    }

    public void initProvidedInterfaces() throws Exception {
    	if (juniperPlatform !=  null) {
    		WrapperHelper.initialize(communicationToolkit,iKeyValueStoreImpl, juniperPlatform);
    	}
    }
    
}
