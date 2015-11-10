package org.modelio.juniper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mpi.MPI;
import mpi.MPIException;

public class ExecutionHelper {

	private static Method findMethod(Object obj, String methodName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for(Method m : obj.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				return m;
			}
		}
		return null;
	}
	
	private static Object getAttribute(Object obj, String attributeName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for(Field f : obj.getClass().getFields()) {
			if (f.getName().equals(attributeName)) {
				return f.get(obj);
			}
		}
		throw new RuntimeException("Attribute " + attributeName + " does not exist in class " + obj.getClass().getName());
	}

	public static void runJuniperProgram(Class programClass)
			throws MPIException, Exception {
		Object obj = programClass.newInstance();
		CommunicationToolkit communicationToolkit = (CommunicationToolkit) getAttribute(
					obj, "communicationToolkit");

		MPI.Init(new String[0]);
		communicationToolkit.initProgramCommunication();
		
		Method m = findMethod(obj, "initProvidedInterfaces");
		if (m != null) {
			m.invoke(obj, new Object[0]);
		}

		while (true) {
			Thread.yield();

			communicationToolkit.processReceivedMessages();
			m = findMethod(obj, "execute");
			if (m != null) {
				m.invoke(obj, new Object[0]);
			}
		}
		// MPI.Finalize();
	}
	
	public static void main(String[] args) {
		
	}

}
