package com.axelor.event.registration.db.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.event.registration.db.report.ITranslation;
import com.axelor.event.registration.db.service.EventRegistrationService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventRegistrationController {

	@Inject
	private EventRegistrationService eventRegistrationService;

	public void validation(ActionRequest request, ActionResponse response) {
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		if (eventRegistration.getRegistrationDate() != null && eventRegistration.getEvent() != null) {
			Event event = eventRegistration.getEvent();
			LocalDate registrationDate = eventRegistration.getRegistrationDate().toLocalDate();
			if (event.getCapacity() < (event.getTotalEntry() + 1)) {
				response.setError(I18n.get(ITranslation.CAPACITY_EXCEED));
			} else {
				LocalDate registrationOpen = event.getRegistrationOpen();
				LocalDate registrationClose = event.getRegistrationClose();
				if (registrationOpen != null && registrationClose != null && registrationDate != null
						&& registrationDate.isBefore(registrationOpen) || registrationDate.isAfter(registrationClose)) {
					response.setError(I18n.get(ITranslation.DATE_BETWEEN));
				}
			}
		} else {
			response.setError(I18n.get(ITranslation.MISSING_FIELD));
		}
	}

	public void calculation(ActionRequest request, ActionResponse response) {
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		Event event = request.getContext().getParent().asType(Event.class);

		LocalDate registrationOpen = event.getRegistrationOpen();
		LocalDate registrationClose = event.getRegistrationClose();
		LocalDateTime registrationDateTime = eventRegistration.getRegistrationDate();
		if (registrationDateTime != null) {
			LocalDate registrationDate = registrationDateTime.toLocalDate();
			if (registrationOpen != null && registrationClose != null && registrationDate != null
					&& registrationDate.isBefore(registrationClose) && registrationDate.isAfter(registrationOpen)) {
				eventRegistrationService.calculation(eventRegistration, event);
				response.setValue("amount", eventRegistration.getAmount());
			} else {
				response.setError(I18n.get(ITranslation.DATE_BETWEEN));
			}
		} else {
			response.setError(I18n.get(ITranslation.MISSING_REGISTRATION_DATE));
		}
	}

	public void calculationNoParent(ActionRequest request, ActionResponse response) {
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		LocalDateTime registrationDateTime = eventRegistration.getRegistrationDate();

		if (registrationDateTime != null && eventRegistration.getEvent() != null) {
			Event event = eventRegistration.getEvent();
			LocalDate registrationOpen = event.getRegistrationOpen();
			LocalDate registrationClose = event.getRegistrationClose();
			LocalDate registrationDate = registrationDateTime.toLocalDate();

			if (registrationOpen != null && registrationClose != null) {
				if ((registrationDate.isAfter(registrationOpen) && registrationDate.isBefore(registrationClose))
						|| registrationDate.isEqual(registrationOpen) || registrationDate.isEqual(registrationClose)) {
					eventRegistrationService.calculation(eventRegistration, event);
					response.setValue("amount", eventRegistration.getAmount());
				} else {
					response.setError(I18n.get(ITranslation.DATE_BETWEEN));
				}
			} else {
				response.setError("Please fill registration open and close date for event");
			}
		}
	}
}
