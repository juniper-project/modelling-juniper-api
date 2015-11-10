package org.modelio.juniper;

import java.io.Serializable;
import java.util.ArrayList;

public interface CommunicationSubsystem<ProgramIdentifierType> {
	void sendObjects(ProgramIdentifierType dst, ArrayList<Serializable> obj) throws CommunicationException;
	void sendObject(ProgramIdentifierType dst, Serializable obj) throws CommunicationException;
	Serializable receiveReturnValue(ProgramIdentifierType src) throws CommunicationException;	
	Serializable receiveObject(ProgramIdentifierType src) throws CommunicationException;
	void sendReturnValue(ProgramIdentifierType dst, Serializable obj) throws CommunicationException;
	boolean hasObject(ProgramIdentifierType src) throws CommunicationException;
}
