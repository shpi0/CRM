package com.ewp.crm.service.slack;

import com.ewp.crm.models.Client;
import com.ewp.crm.models.ProjectProperties;
import com.ewp.crm.models.SlackProfile;
import com.ewp.crm.models.Status;
import com.ewp.crm.service.impl.StudentServiceImpl;
import com.ewp.crm.service.interfaces.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Service
@PropertySources(value = {
        @PropertySource("classpath:application.properties"),
        @PropertySource("file:./slack.properties")
})
public class SlackServiceImpl implements SlackService {

    private static Logger logger = LoggerFactory.getLogger(SlackServiceImpl.class);

    private final ClientService clientService;
    private final StudentServiceImpl studentService;
    private final StatusService statusService;
    private final ClientHistoryService clientHistoryService;
    private final ProjectPropertiesService propertiesService;

    // get it for you Workspace from
    // https://api.slack.com/custom-integrations/legacy-tokens
    // and put in to slack.properties
    private String LEGACY_TOKEN;

    @Autowired
    public SlackServiceImpl(Environment environment,
                            ClientService clientService,
                            StudentServiceImpl studentService,
                            StatusService statusService,
                            ClientHistoryService clientHistoryService,
                            ProjectPropertiesService propertiesService) {
        try {
            this.LEGACY_TOKEN = environment.getRequiredProperty("slack.legacyToken");
            if (LEGACY_TOKEN.isEmpty()) {
                throw new NullPointerException();
            }
        } catch (NullPointerException npe) {
            logger.error("Can't get slack.legacyToken get it from https://api.slack.com/custom-integrations/legacy-tokens", npe);
        }
        this.clientService = clientService;
        this.studentService = studentService;
        this.statusService = statusService;
        this.clientHistoryService = clientHistoryService;
        this.propertiesService = propertiesService;
    }

    @Override
    public SlackProfile receiveClientSlackProfileBySlackHashName(String slackHashName) {
        try {
            String url = "https://slack.com/api/users.info?token=" + LEGACY_TOKEN + "&user=" + slackHashName;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode actualObj = objectMapper.readTree(response.toString()).get("user").get("profile");
            SlackProfile clientSlackProfile = objectMapper.treeToValue(actualObj, SlackProfile.class);
            clientSlackProfile.setHashName(slackHashName);
            return clientSlackProfile;
        } catch (IOException | RuntimeException e) {
            logger.warn("Can't receive Client Slack profile", e);
        }
        return new SlackProfile();
    }

    @Override
    public String getEmailListFromJson(String json) {
        try {
            StringBuilder result = new StringBuilder();
            JSONObject jsonObj = new JSONObject(json);
            JSONArray jsonData = jsonObj.getJSONArray("members");
            for (int i = 0; i < jsonData.length(); i++) {
                JSONObject userProfile = jsonData.getJSONObject(i).optJSONObject("profile");
                if (userProfile == null) {
                    continue;
                }
                String mail = userProfile.optString("email");
                if (mail != null && !mail.isEmpty()) {
                    result.append(mail);
                    if (i != jsonData.length() - 1) {
                        result.append("\n");
                    }
                }
            }
            return result.toString();
        } catch (JSONException e) {
            logger.warn("Can't parse emails from slack", e);
        }
        return "Error";
    }

    @Override
    public void memberJoinSlack(SlackProfile slackProfile) {
        Optional<Client> client = clientService.getClientByEmail(slackProfile.getEmail());
        if (client.isPresent()) {
            client.get().setSlackProfile(slackProfile);
            slackProfile.setClient(client.get());
            ProjectProperties projectProperties = propertiesService.get();
            if (projectProperties == null || projectProperties.getDefaultStatusId() == null) {
                logger.warn("Don't have projectProperties yet! Create it.");
            } else {
                if (statusService.get(projectProperties.getDefaultStatusId()).isPresent()) {
                    Status newClientStatus = statusService.get(projectProperties.getDefaultStatusId()).get();
                    client.get().setStatus(newClientStatus);
                    newClientStatus.addClient(client.get());
                    statusService.update(newClientStatus);
                }
            }
            if (client.get().getStudent() == null) {
                studentService.addStudentForClient(client.get());
            }
            clientHistoryService.createHistory("Slack, nickname: " + slackProfile.getDisplayName()
                    + ". " + client.get().getName() + " " + client.get().getLastName() + " теперь студент").ifPresent(client.get()::addHistory);
            clientService.updateClient(client.get());
            logger.info("New member " + slackProfile.getDisplayName() + " "
                    + slackProfile.getEmail() + " joined to general channel");
        } else {
            logger.info(slackProfile.getDisplayName() + " not a client joined to Slack general channel!");
        }
    }
}
