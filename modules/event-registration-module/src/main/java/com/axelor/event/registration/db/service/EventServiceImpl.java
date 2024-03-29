package com.axelor.event.registration.db.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;

import com.axelor.app.AppSettings;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.data.csv.CSVImporter;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.Inject;

public class EventServiceImpl implements EventService {

	@Inject
	private MessageService messageService;

	@Override
	public void eventCalculation(Event event) {
		BigDecimal amountCollected = BigDecimal.ZERO;
		BigDecimal totalDiscount = BigDecimal.ZERO;
		int temp;

		if (event.getEventRegistrationList() != null) {
			List<EventRegistration> eventRegistrationsList = event.getEventRegistrationList();
			for (EventRegistration eventRegistration : eventRegistrationsList) {
				amountCollected = amountCollected.add(eventRegistration.getAmount());
			}

			temp = event.getEventRegistrationList().size();
			totalDiscount = (event.getEventFees().multiply(BigDecimal.valueOf(temp))).subtract(amountCollected);
			event.setAmountCollected(amountCollected);
			event.setTotalDiscount(totalDiscount);
		}
	}

	@Override
	public Boolean sendEmail(Event event) throws MessagingException, IOException, AxelorException {
		Set<EmailAddress> emailAddressSet = new HashSet<EmailAddress>();
		if (event.getEventRegistrationList() != null) {
			for (EventRegistration eventRegistration : event.getEventRegistrationList()) {
				if (eventRegistration.getEmail() != null && !(eventRegistration.getIsSendEmail())) {
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setAddress(eventRegistration.getEmail());
					emailAddressSet.add(emailAddress);
					eventRegistration.setIsSendEmail(true);
				}
			}
		}
		Boolean checkEmailList = false;
		if (!emailAddressSet.isEmpty()) {
			checkEmailList = true;
			Message message = new Message();
			message.setMailAccount(
					Beans.get(EmailAccountRepository.class).all().filter("self.isValid = ?1", true).fetchOne());
			message.setContent("This is Regisgration Information mail");
			message.setToEmailAddressSet(emailAddressSet);
			message.setSubject("Registration Regarding");
			messageService.sendByEmail(message);
		}
		return checkEmailList;
	}
}
