package com.adorsys.webank.obs.serviceimpl;

import com.adorsys.webank.obs.config.PhoneNumberCache;
import com.adorsys.webank.obs.service.ObsOtpServiceApi;
import com.adorsys.webank.service.OtpServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObsOtpServiceImpl implements ObsOtpServiceApi {

    private static final Logger logger = LoggerFactory.getLogger(ObsOtpServiceImpl.class);

    @Autowired
    private OtpServiceApi otpServiceApi;

    @Autowired
    private PhoneNumberCache phoneNumberCache;

    @Override
    public String sendOtp(String phoneNumber, String publicKey) {
        // Check cache for phone number
        if (phoneNumberCache.isInCache(phoneNumber)) {
            logger.warn("Phone number {} is already in cache. Rejecting request.", phoneNumber);
            throw new IllegalArgumentException("OTP already sent recently for this phone number.");
        }

        // Add phone number to cache
        phoneNumberCache.addToCache(phoneNumber);

        // Process the OTP request
        String otpResponse = otpServiceApi.sendOtp(phoneNumber, publicKey);


        return otpResponse;
    }

    @Override
    public boolean validateOtp(String phoneNumber, String publicKey, String otpInput, String otpHash) {
        // Perform OTP validation
        boolean isValid = otpServiceApi.validateOtp(phoneNumber, publicKey, otpInput, otpHash);

        // If validation is successful, clear the phone number from the cache
        if (isValid) {
            logger.info("OTP validation successful for phone number: {}. Clearing from cache.", phoneNumber);
            phoneNumberCache.removeFromCache(phoneNumber);
        } else {
            logger.warn("OTP validation failed for phone number: {}.", phoneNumber);
        }

        return isValid;
    }
}
