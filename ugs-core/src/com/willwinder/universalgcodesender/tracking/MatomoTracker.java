/*
    Copyright 2018 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.tracking;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Version;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A tracker implementation to use a Matomo API
 *
 * @author Joacim Breiler
 */
public class MatomoTracker implements ITracker {
    private static final Integer SITE_ID = 2;
    private static final String SESSION_ID = RandomStringUtils.random(16, "01234567890abcdefABCDEF");
    private static final Logger LOGGER = Logger.getLogger(MatomoTracker.class.getName());
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final String MATOMO_URL = "http://ugs.willwinder.com/analytics/piwik.php";

    private final BackendAPI backendAPI;
    private final Client client;
    private final String userAgent;

    public MatomoTracker(BackendAPI backendAPI, Client client) {
        this.backendAPI = backendAPI;
        this.client = client;
        this.userAgent = "UniversalGcodeSender/" + Version.getVersionString() +
                " (Language=Java/" + Runtime.class.getPackage().getImplementationVersion() +
                " Platform=" + System.getProperty("os.name") + " " + System.getProperty("os.version") + " ; " +
                Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry() + ") ";
    }

    @Override
    public void report(Class module, String action) {
        report(module, action, false, null, 0);
    }

    @Override
    public void report(Class module, String action, boolean newVisit) {
        report(module, action, newVisit, null, 0);
    }

    @Override
    public void report(Class module, String action, boolean newVisit, String resourceName, int resourceValue) {
        // Run in a worker thread
        EXECUTOR_SERVICE.submit(() -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(MATOMO_URL);
                uriBuilder.addParameter("rand", RandomStringUtils.randomAlphanumeric(10));
                uriBuilder.addParameter("idsite", String.valueOf(SITE_ID));
                uriBuilder.addParameter("rec", "1");
                uriBuilder.addParameter("apiv", "1");
                uriBuilder.addParameter("send_image", "0");
                uriBuilder.addParameter("uid", backendAPI.getSettings().getClientId());
                uriBuilder.addParameter("cid", SESSION_ID);
                uriBuilder.addParameter("action_name", client.name() + " / " + module.getSimpleName() + " / " + action);

                uriBuilder.addParameter("_cvar", "{\"1\":[\"Version\",\"" + Version.getVersionString() + "\"]}");

                uriBuilder.addParameter("e_c", module.getSimpleName());
                uriBuilder.addParameter("e_a", module.getSimpleName() + " - " + action);

                if (StringUtils.isNotEmpty(resourceName)) {
                    uriBuilder.addParameter("e_n", resourceName);
                    uriBuilder.addParameter("e_v", String.valueOf(resourceValue));
                }

                // Creates a new session
                if (newVisit) {
                    uriBuilder.addParameter("new_visit", "1");
                }

                uriBuilder.addParameter("lang", Locale.getDefault().getLanguage());

                String baseURL = "http://ugs/" + client.name().toLowerCase();
                uriBuilder.addParameter("url", baseURL);
                uriBuilder.addParameter("ua", userAgent);


                HttpClient client = getHttpClient();
                HttpGet get = new HttpGet(uriBuilder.build());
                client.execute(get);
            } catch (URISyntaxException | IOException e) {
                LOGGER.log(Level.WARNING, "Couldn't report usage statistics for '" + module.getSimpleName() + " - " + action + "'", e);
            }
        });
    }

    private HttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }
}
