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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExclusionList {

	private Map<String, Set<String>> exclusionList = new HashMap<String, Set<String>>();
	
	public ExclusionList() {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("excludeList"); 
		if (stream != null) {
			try {
				loadExclusionList(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ExclusionList(String filename) throws IOException {
		loadExclusionList(filename);
	}

	private void loadExclusionList(InputStream stream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		
		while((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			String excluder = parts[0];
			String excluded = parts[1];
			
			Set<String> excludedSet = exclusionList.get(excluder);
			if (excludedSet == null) {
				excludedSet = new HashSet<String>();
				exclusionList.put(excluder, excludedSet);
			}
			excludedSet.add(excluded);			
		}
		br.close();		
	}

	private void loadExclusionList(String exclusionListFileName) throws IOException {
		loadExclusionList(new FileInputStream(exclusionListFileName));
	}

	public boolean noExclusion(Method method, Class pclass) {
		String interfaceName = method.getDeclaringClass().getName();
		String methodName = method.getName();
		String thisMethodNormalized = interfaceName + "#" + methodName;
		if (exclusionList == null) {
			return true;
		}
		if (exclusionList.containsKey(interfaceName)) {
			for(String elem : exclusionList.get(interfaceName)) {
				if (elem.startsWith(pclass.getName() + "#")) {
					return false;
				}
			}
		}
		if (exclusionList.containsKey(thisMethodNormalized)) {
			for(String elem : exclusionList.get(thisMethodNormalized)) {
				if (elem.startsWith(pclass.getName() + "#")) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Should I exclude field when I call method?
	 * 
	 * @param method
	 * @param field
	 * @return
	 */
	public boolean isExcluded(Method method, Field field) {
		String interfaceName = method.getDeclaringClass().getName();
		String methodName = method.getName();
		String normalizedField = field.getDeclaringClass().getName() + "#" + field.getName();
		String thisMethodNormalized = interfaceName + "#" + methodName;
		
		if (exclusionList.containsKey(interfaceName)) {
			if (exclusionList.get(interfaceName).contains(normalizedField)) {
				return true;
			}
		}
		
		if (exclusionList.containsKey(thisMethodNormalized)) {
			if (exclusionList.get(thisMethodNormalized).contains(normalizedField)) {
				return true;
			}
		}
		return false;
	}
	
}
