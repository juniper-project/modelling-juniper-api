package org.modelio.juniper.platform;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Logger;

import mpi.MPIException;

import org.modelio.juniper.CommunicationToolkit;
import org.modelio.juniper.wrapper.WrapperHelper;

import eu.juniper.MonitoringLib;
import eu.juniper.platform.Core;
import eu.juniper.platform.models.ApplicationModel;
import eu.juniper.sa.monitoring.agent.MonitoringAgentFactory;
import eu.juniper.sa.monitoring.agent.MonitoringAgentInterface;
import eu.juniper.sa.monitoring.sensor.DataConnectionSensorInterface;
import eu.juniper.sa.monitoring.sensor.ProgramInstanceSensorInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class JuniperProgram extends eu.juniper.program.JuniperProgram {
	public final CommunicationToolkit communicationToolkit = new org.modelio.juniper.CommunicationToolkit(
			this);
	protected Core juniperPlatform;
	protected MonitoringLib monitoringLib;
	protected MonitoringAgentInterface monitoringAgent;
	protected ProgramInstanceSensorInterface programInstanceSensor;
	protected Map<String, DataConnectionSensorInterface> dataConnectionSensors = new HashMap<>();
	private boolean lastExecuteMethodInvocationWasEmpty = false;

	protected ArrayList<String> inboundConnections = new ArrayList();
	protected ArrayList<String> outboundConnections = new ArrayList();
	
	public Core getJuniperPlatform() {
		return juniperPlatform;
	}

	public final Logger logger = Logger.getLogger(this.getClass().getName());

	public ApplicationModel getApplicationModel() {
		return juniperPlatform.getApplicationModel();
	}
	
	public ArrayList<String> getInboundConnections() {
		return this.inboundConnections;
	}

	public ArrayList<String> getOutboundConnections() {
		return this.outboundConnections;
	}

	public MonitoringLib getMonitoringLib() {
		return monitoringLib;
	}

	public MonitoringAgentInterface getMonitoringAgent() {
		return monitoringAgent;
	}

	public ProgramInstanceSensorInterface getProgramInstanceSensor() {
		return programInstanceSensor;
	}

	public DataConnectionSensorInterface getDataConnectionSensor(
			String dataConnectionName) {
		DataConnectionSensorInterface dataConnectionSensor = this.dataConnectionSensors
				.get(dataConnectionName);
		// support lazy initialization of new (yet uninitialized) data
		// connection sensors
		if (dataConnectionSensor == null && this.monitoringAgent != null) {			
			dataConnectionSensor = this.monitoringAgent
					.createDataConnectionSensor(
							this.juniperPlatform.getMpiRank(),
							dataConnectionName);
			this.dataConnectionSensors.put(dataConnectionName,
					dataConnectionSensor);
		}
		return dataConnectionSensor;
	}

	protected boolean isExecuteMethodOverriden() {
		try {
			return !this.getClass().getMethod("execute").getDeclaringClass()
					.equals(JuniperProgram.class);
		} catch (NoSuchMethodException | SecurityException ex) {
			return false;
		}
	}

	public void setExecuteMethodInvocationAsEmpty() {
		this.lastExecuteMethodInvocationWasEmpty = true;
	}

	public void execute() throws Exception {

	}

	public void initProvidedInterfaces() throws Exception {
		if (juniperPlatform != null) {
			for (Field f : this.getClass().getFields()) {
				if (f.getAnnotation(Provided.class) != null) {
					Object value = f.get(this);
					try {
						WrapperHelper.initialize(communicationToolkit, value,
								juniperPlatform);
					} catch (Exception e) {
						System.err.println("Failed to initialize field "
								+ f.getName() + " of " + this.getClass().getName());
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void run(Core juniperPlatform) throws MPIException {
		super.run(juniperPlatform);

        this.juniperPlatform = juniperPlatform;

        try {
        	// start a monitoring agent
            this.monitoringAgent = MonitoringAgentFactory.createMonitoringAgentSingletonBySystemProperty(
            		this.juniperPlatform.getApplicationModel().getName()
            );
		} catch(Throwable e) {
			System.err.println("Monitoring agent failed to start!");
//			e.printStackTrace();
		}
		
		ArrayList<String> myGroups = juniperPlatform.getApplicationModel().getGroupModel().getGroupsOfMpiRank(juniperPlatform.getMpiRank());
		
		for (int i = 0; i < juniperPlatform.getApplicationModel().getCommunicationModel().getDataConnections().size(); i++) {
			for (int j = 0; j < myGroups.size(); j++) {
				if (juniperPlatform.getApplicationModel().getCommunicationModel().getDataConnections().get(i).getSenderGroup().equals(myGroups.get(j)))
					outboundConnections.add(juniperPlatform.getApplicationModel().getCommunicationModel().getDataConnections().get(i).getName());
				else if (juniperPlatform.getApplicationModel().getCommunicationModel().getDataConnections().get(i).getReceiverGroup().equals(myGroups.get(j))) {
                    final String dataConnectionName = juniperPlatform.getApplicationModel().getCommunicationModel().getDataConnections().get(i).getName();
					inboundConnections.add(dataConnectionName);
                    // early initialization of data connection sensors
                    // (the monitoring agent and this.juniperPlatform have to be already initialized;
                    // comment out to enable lazy initialization)
                    this.getDataConnectionSensor(dataConnectionName);
                }
			}
		}
		
		try {
			this.monitoringLib = new MonitoringLib("http://172.18.2.117:3000/executions/", juniperPlatform.getApplicationModel().getName());
		} catch(Throwable e) {
			e.printStackTrace();
		}
		
		try {
            // create a sensor for the program
            this.programInstanceSensor = this.monitoringAgent.createProgramInstanceSensor(
            		this.juniperPlatform.getMpiRank()
            );
		} catch(Throwable e) {
			System.err.println("Monitoring library failed to start!");
//			e.printStackTrace();
		}
		
		communicationToolkit.initProgramCommunication();
		try {
			initProvidedInterfaces();
			while (true) {
				Thread.yield();
				if (this.programInstanceSensor != null) {
	                // indicate the program start point
	                this.programInstanceSensor.programStarts();					
				}
 
                // the processing of received messages method call is included
                // as it invokes the program's methods in its provided interfaces
                // that process the received messages (an RPC-style messaging)
				final int processedMessages = communicationToolkit.processReceivedMessages();

				// the execute method can indicate its invocation as empty (it is non-empty by default)
                this.lastExecuteMethodInvocationWasEmpty = false;
				execute();
                // indicate the program end point iff the program did something
                // (i.e., received any messages, or has its own execute() method with non-zero duration and non-empty invocation)

				if (this.programInstanceSensor != null) {
	                if ((processedMessages > 0) || (this.isExecuteMethodOverriden() && (this.programInstanceSensor.getCurrentDuration() > 0) && !this.lastExecuteMethodInvocationWasEmpty)) {
	                	this.programInstanceSensor.programEnds();
	                }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			juniperPlatform.shutdown();
		}
	}
}
