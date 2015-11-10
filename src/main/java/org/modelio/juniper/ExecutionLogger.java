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
package org.modelio.juniper;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.modelio.juniper.platform.JuniperProgram;

@SuppressWarnings("rawtypes")
public class ExecutionLogger {

	private JuniperProgram program;
	private PrintStream stream;
	
	public ExecutionLogger(JuniperProgram thisProgram) {
		super();
		this.program = thisProgram;
		String streamName = System.getProperty("org.modelio.juniper.ExecutionLogger", "err");
		if ("none".equals(streamName)) {
			stream = null;
		} else if ("err".equals(streamName)) {
			stream = System.err;			
		} else if ("out".equals(streamName)) {
			stream = System.out;			
		} else {
			try {
				stream = new PrintStream(streamName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				stream = null;
			}			
		}
	}

	private Serializable log(String source, String target, Method operation, String direction, String order, boolean createToken, Serializable originalToken) {
		long time = System.currentTimeMillis();

		Serializable token = originalToken;
		if (createToken) {
			token = new Object().hashCode();
		}

		if (stream != null)
			stream.println(program.getJuniperPlatform().getHostIp()+", "+order+","+direction+","+source+","+operation.getName()+","+target+(token!=null?",#"+token.hashCode():""));

		return token;	
	}

	public void logSendAfter(Class target, Method operation, Serializable token) {
		log(program.getClass().getSimpleName(), target.getSimpleName(), operation, "send", "after", false, token);
	}

	public Serializable logSendBefore(Class target, Method operation) {
		return log(program.getClass().getSimpleName(), target.getSimpleName(), operation, "send", "before", true, null);		
	}

	public void logReceive(Class source, Method operation, Serializable token) {
		log(source.getSimpleName(), program.getClass().getSimpleName(), operation, "receive", "after", false, token);
	}

}
