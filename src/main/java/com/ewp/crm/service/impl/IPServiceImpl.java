package com.ewp.crm.service.impl;

import com.ewp.crm.configs.inteface.IPConfig;
import com.ewp.crm.service.interfaces.IPService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class IPServiceImpl implements IPService {

	private String voximplantApiKey;

	private String voximplantAccountId;

	private String voximplantRuleId;

	private String voximplantLoginForWebCall;

	private String voximplantPasswordForWebCall;

	private String voximplantCodeToSetRecord;

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(IPServiceImpl.class);


	@Autowired
	public IPServiceImpl(IPConfig ipConfig) {
		voximplantApiKey = ipConfig.getVoximplantApiKey();
		voximplantAccountId = ipConfig.getVoximplantAccountId();
		voximplantRuleId = ipConfig.getVoximplantRuleId();
		voximplantLoginForWebCall = ipConfig.getVoximplantLoginForWebCall();
		voximplantPasswordForWebCall = ipConfig.getVoximplantPasswordForWebCall();
		voximplantCodeToSetRecord = ipConfig.getVoximplantCodeToSetRecord();
	}

	public void call(String from, String to, Long callId) {

		String callVoximplant = "https://api.voximplant.com/platform_api/StartScenarios/" +
				"?account_id=" +
				voximplantAccountId +
				"&api_key=" +
				voximplantApiKey +
				"&rule_id=" +
				voximplantRuleId +
				"&script_custom_data=" +
				from +
				"%3A" +
				to +
				"%3A" +
				callId;

		try {
			HttpGet callVox = new HttpGet(callVoximplant);
			HttpClient httpClient = HttpClients.custom()
					.setDefaultRequestConfig(RequestConfig.custom()
							.setCookieSpec(CookieSpecs.STANDARD).build())
					.build();
			httpClient.execute(callVox);
		} catch (IOException e) {
			logger.error("Failed to connect to Voximplant server ", e);
		}
	}

	public Optional<String> getVoximplantLoginForWebCall() {
		return Optional.ofNullable(voximplantLoginForWebCall);
	}

	public Optional<String> getVoximplantPasswordForWebCall() {
		return Optional.ofNullable(voximplantPasswordForWebCall);
	}

    public Optional<String> getVoximplantUserLogin(String fullLogin) {
        return Optional.of(fullLogin.replaceAll("@.+", ""));
    }

	public Optional<String> getVoximplantCodeToSetRecord() {
		return Optional.ofNullable(voximplantCodeToSetRecord);
	}
}


