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
package org.modelio.juniper.wrapper;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

import mpi.MPI;

import org.modelio.juniper.CommunicationToolkit;

import eu.juniper.platform.Core;
import eu.juniper.platform.models.ApplicationModel;
import eu.juniper.platform.models.auxiliary.DataConnection;
import eu.juniper.platform.models.auxiliary.Program;

public class WrapperHelper {

	/**
	 * Initializes the wrapper using its arguments
	 * 
	 * @param communicationToolkit
	 * @param interfaceImplementation
	 * @param args 0: RANK, 1-n: the class names of the components that require its provided interfaces
	 * @throws Exception if something goes wrong ;)
	 */
	public static void initialize(CommunicationToolkit communicationToolkit,
			Object interfaceImplementation, String[] args) throws Exception {
		
    	if (args.length == 0) throw new Error("Classloader not specified!!");
    	
    	ClassLoader cl = null;
    	if (!"default".equals(args[0])) {
    		cl = new URLClassLoader(new URL[]{new URL(args[0])});
    	}    	    	
    	
		for (int i=1;i<args.length;++i) {
			Class c = null;
			if (cl == null) {
				c = Class.forName(args[i]);
			} else {
				c = Class.forName(args[i], true, cl); 
			}
			communicationToolkit.initProvidedInterface(c, interfaceImplementation);
		}		
	}

	public static void initialize(CommunicationToolkit communicationToolkit,
			Object interfaceImplementation,
			Core juniperPlatform) throws Exception {
		Collection<Class> connections = getConnectedClasses(MPI.COMM_WORLD.getRank(), juniperPlatform.getApplicationModel());
		for(Class connected : connections) {
			communicationToolkit.initProvidedInterface(connected, interfaceImplementation);
		}
	}

	private static Collection<Class> getConnectedClasses(int myRank,
			ApplicationModel model) throws Exception {
		String myGroup = model.getGroupModel().getGroupNameOfMpiRank(myRank);
		ArrayList<Class> classes = new ArrayList<Class>(5);
		for(DataConnection dc : model.getCommunicationModel().getDataConnections()) {
			if (dc.getReceiverGroup().equals(myGroup) && !dc.getName().endsWith("_return")) {
				Class senderClass = getClassForGroup(dc.getSenderGroup(), model);
				classes.add(senderClass);
			}
		}
		return classes;
	}
	
	private static Class getClassForGroup(String senderMpiGroupi, ApplicationModel model) throws Exception {
		int mpiGlobalRank = Integer.parseInt(model.getGroupModel().getMpiglobalranksByGroupName(senderMpiGroupi).get(0)); 
		String programName = model.getGroupModel().getProgramNameForGlobalRank(""+mpiGlobalRank);
		Program program = model.getProgramModel().getProgramByName(programName);				
		return Class.forName(program.getJavaClass());
	}
	
}
