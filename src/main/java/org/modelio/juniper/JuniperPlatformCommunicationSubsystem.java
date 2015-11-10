package org.modelio.juniper;

import eu.juniper.sa.monitoring.sensor.DataConnectionSensorInterface;

import java.io.Serializable;
import java.util.ArrayList;

import org.modelio.juniper.platform.JuniperProgram;

public class JuniperPlatformCommunicationSubsystem implements
		CommunicationSubsystem<String> {
	
	private boolean monitoringLibEnabled;

	public JuniperPlatformCommunicationSubsystem(JuniperProgram juniperProgram) {
		super();
		this.juniperProgram = juniperProgram;
		this.monitoringLibEnabled = juniperProgram.getMonitoringLib() != null;
	}

	private JuniperProgram juniperProgram;

	@Override
	public void sendObject(String dst, Serializable obj)
			throws CommunicationException {
		ArrayList<Object> data = new ArrayList<Object>(1);
		data.add(obj);
		juniperProgram.transferData(dst, data);
	}

	@Override
	public void sendObjects(String dst, ArrayList<Serializable> obj)
			throws CommunicationException {
		ArrayList<Object> data = new ArrayList<Object>(obj);
		juniperProgram.transferData(dst, data);		
	}
	
	@Override
	public Serializable receiveReturnValue(String src)
			throws CommunicationException {
		String channel = src+"_return";
		if (monitoringLibEnabled) {			
			try {
				juniperProgram.getMonitoringLib().start(juniperProgram.getApplicationModel().getName(), ""+juniperProgram.getCommunicationID(), juniperProgram.getConnectionName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
		final DataConnectionSensorInterface dataConnectionSensor = this.juniperProgram.getDataConnectionSensor(src);
		if (dataConnectionSensor != null) {
	        dataConnectionSensor.receiveStarts(); // indicate the receive start point			
		}

        ArrayList<Object> ret =  (ArrayList<Object>) juniperProgram.transferData(channel);
		
        try {
			// indicate the receive end point
			if (this.juniperProgram.getProgramInstanceSensor() != null) {        	
				this.juniperProgram.getProgramInstanceSensor().subtract(dataConnectionSensor.receiveEnds());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (monitoringLibEnabled) {			
			try {
				juniperProgram.getMonitoringLib().stop(juniperProgram.getApplicationModel().getName(), ""+juniperProgram.getCommunicationID(), juniperProgram.getConnectionName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return (Serializable) ret.get(0);
	}

	@Override
	public Serializable receiveObject(String src)
			throws CommunicationException {
		if (monitoringLibEnabled) {			
			try {
				juniperProgram.getMonitoringLib().start(juniperProgram.getApplicationModel().getName(), ""+juniperProgram.getCommunicationID(), juniperProgram.getConnectionName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
		final DataConnectionSensorInterface dataConnectionSensor = this.juniperProgram.getDataConnectionSensor(src);
		
		if (dataConnectionSensor !=  null) {
	        dataConnectionSensor.receiveStarts(); // indicate the receive start point			
		}
		
        ArrayList<Object> ret =  (ArrayList<Object>) juniperProgram.transferData(src);
		
		try {
			// indicate the receive end point
			if (this.juniperProgram.getProgramInstanceSensor() != null) {
	            this.juniperProgram.getProgramInstanceSensor().subtract(dataConnectionSensor.receiveEnds());				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (monitoringLibEnabled) {			
			try {
				juniperProgram.getMonitoringLib().stop(juniperProgram.getApplicationModel().getName(), ""+juniperProgram.getCommunicationID(), juniperProgram.getConnectionName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (ret.size() == 1) {
			return (Serializable) ret.get(0);
		} else {
			return ret;
		}
	}

	@Override
	public void sendReturnValue(String dst, Serializable obj)
			throws CommunicationException {
		ArrayList<Object> data = new ArrayList<Object>(1);
		data.add(obj);
		juniperProgram.transferData(dst+"_return", data);
	}

	@Override
	public boolean hasObject(String src) throws CommunicationException {
        return juniperProgram.hasData(src);
	}

}
