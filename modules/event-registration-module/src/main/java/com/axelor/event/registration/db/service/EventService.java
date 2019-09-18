package com.axelor.event.registration.db.service;

import java.io.IOException;

import javax.mail.MessagingException;

import com.axelor.event.registration.db.Event;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;

public interface EventService {

	public void eventCalculation(Event event);
	
	public void sendEmail(Event event) throws MessagingException, IOException, AxelorException;
	
	public void importRegistrationData(Integer event_id, MetaFile dataFile) throws IOException;
}
