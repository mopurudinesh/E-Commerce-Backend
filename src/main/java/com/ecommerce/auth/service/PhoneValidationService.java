package com.ecommerce.auth.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.springframework.stereotype.Service;

/**
 * Validates and normalizes international phone numbers according to E.164.
 */
@Service
public class PhoneValidationService {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public boolean isValid(String rawNumber, String defaultRegion) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) {
            return false;
        }

        try {
            String region = rawNumber.trim().startsWith("+") ? null : (defaultRegion != null ? defaultRegion : "IN");
            PhoneNumber number = phoneUtil.parse(rawNumber.trim(), region);
            return phoneUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public String formatToE164(String rawNumber, String defaultRegion) {
        try {
            String region = rawNumber.trim().startsWith("+") ? null : (defaultRegion != null ? defaultRegion : "IN");
            PhoneNumber number = phoneUtil.parse(rawNumber.trim(), region);
            return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            return rawNumber.trim();
        }
    }
}
