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
package org.modelio.juniper.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("unused") public class MethodInvocation implements Serializable {
	@Override
	public String toString() {
		return "MethodInvocation [methodName=" + methodName
				+ ", interfaceName=" + interfaceName + ", parameters="
				+ Arrays.toString(parameters) + "]";
	}

	private static final long serialVersionUID = 1L;
	private static ExclusionList exclusionList = new ExclusionList();;

	private String methodName;
	private String interfaceName;
	private Method method;
	private Serializable token;
	private Serializable[] parameters;
	
	public MethodInvocation(String interfaceName, String methodName, Serializable[] parameters, Serializable token) throws Exception {
		super();
		this.interfaceName = interfaceName;
		this.methodName = methodName;
		
		this.method = findMethod(interfaceName, methodName);
		this.parameters = parameters;
		this.token = token;
	}

	private Method findMethod(String interfaceName2, String methodName2) throws Exception {
		for(Method m: Class.forName(interfaceName2).getMethods()) {
			if (m.getName().equals(methodName2)) {
				return m;
			}
		}
		throw new NoSuchMethodException(interfaceName2 + "." + methodName2);
	}

	public Serializable getToken() {
		return token;
	}

	public void setToken(Serializable token) {
		this.token = token;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Serializable[] getParameters() {
		return parameters;
	}

	public void setParameters(Serializable[] parameters) {
		this.parameters = parameters;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	
	// TODO: use Externalizable instead of Serializable approach for that. 			
	// TODO: move all this complex stuff to another class!
	private void writeObject(ObjectOutputStream oos) throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException {		
		oos.writeObject(interfaceName);
		oos.writeObject(methodName);
		oos.writeObject(token);
		oos.writeInt(parameters.length);			

		for (Serializable parameter : parameters) {
			writeParameterObject(oos, parameter);
		}
	}
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		interfaceName = (String) ois.readObject();
		methodName = (String) ois.readObject();
		token = (Serializable) ois.readObject();

		int length = ois.readInt();	
		parameters = new Serializable[length];
		
		for(int i=0;i<length;++i) {
			parameters[i] = readParameterObject(ois); 
		}
	}

	@SuppressWarnings("rawtypes")
	private void writeParameterObject(ObjectOutputStream oos,
			Serializable parameter) throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException {
		Class pclass = parameter.getClass();
		oos.writeObject(pclass.getName());
		
		// before each parameter write a boolean, true if the whole object has been written, false otherwise
		if (exclusionList.noExclusion(this.method, pclass)) {
			oos.writeBoolean(true);
			oos.writeObject(parameter);
		} else {
			oos.writeBoolean(false);
			writeFieldsOf(oos, parameter);
		}
	}

	private static String getFieldName(String accessorName) {
		return new Character(accessorName.charAt(3)).toString().toLowerCase()+accessorName.substring(4);
	}
	
	
	private static boolean isGetter(Method method) {
		return method.getName().startsWith("get") && method.getParameterCount()==0 && method.getReturnType() != Void.TYPE; 
	}

	private static boolean isSetter(Method method) {
		return method.getName().startsWith("set") && method.getParameterCount()==1 && method.getReturnType() == Void.TYPE; 
	}

	private void writeFieldsOf(ObjectOutputStream oos, Serializable object) throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException {
		for(Method method : object.getClass().getDeclaredMethods()) {
			if (isGetter(method)) {
				Field field = object.getClass().getDeclaredField(getFieldName(method.getName()));
				if (exclusionList.isExcluded(this.method, field)) {
					oos.writeObject(null);
				} else {
					oos.writeObject(method.invoke(object, new Object[0]));					
				}				
			}
		}
	}

	private void readFieldsOf(ObjectInputStream ois, Serializable object) throws IOException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
		for(Method method : object.getClass().getDeclaredMethods()) {
			if (isGetter(method)) {
				Object value = ois.readObject();
				Method setter = getSetterForGetter(method);
				if (value != null) {
					setter.invoke(object, new Object[]{value});
				}
			}
		}
	}
	private Method getSetterForGetter(Method method) {
		String fieldName = getFieldName(method.getName());
		for(Method methodOther : method.getDeclaringClass().getDeclaredMethods()) {
			if (isSetter(methodOther) && getFieldName(methodOther.getName()).equals(fieldName)) return methodOther;
		}
		return null;
	}

	private Serializable readParameterObject(ObjectInputStream ois) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		String className = (String) ois.readObject();
		Class pclass = Class.forName(className);

		boolean fullObject = ois.readBoolean();
		
		if (fullObject) {
			return (Serializable)ois.readObject();
		} else {
			Serializable parameter = (Serializable) pclass.newInstance();
			readFieldsOf(ois, parameter);
			return parameter;
		}
	}
}