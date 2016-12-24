/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.odnoklassniki.api.impl;

import java.net.URLEncoder;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.util.DigestUtils;

public abstract class AbstractOdnoklassnikiOperations {

    private static final String MAILRU_REST_URL = "http://api.odnoklassniki.ru/fb.do?";

    private final SortedMap<String, String> params = new TreeMap<String, String>(new Comparator<String>() {
        @Override
        public int compare(String str, String str2) {
            return str.compareTo(str2);
        }
    });

    private final boolean isAuthorized;

    private final String accessToken;

    private final String clientSecret;

    public AbstractOdnoklassnikiOperations(String applicationKey, String clientSecret, String accessToken, boolean isAuthorized) {

        this.isAuthorized = isAuthorized;
        this.accessToken = accessToken;
        this.clientSecret = clientSecret;

        params.put("application_key", applicationKey);
        params.put("access_token", this.accessToken);
        params.put("format", "json");
    }

    protected void requireAuthorization() {
        if (!isAuthorized) {
            throw new MissingAuthorizationException("odnoklassniki");
        }
    }

    protected String makeOperationURL(Map<String, String> params) {
        this.params.putAll(params);

        StringBuilder url = new StringBuilder(MAILRU_REST_URL);
        StringBuilder signature = new StringBuilder();

        for (String param : this.params.keySet()) {
            String value = this.params.get(param);
            if (!param.equals("access_token")) {
                signature.append(param).append("=").append(value);
            }
            url.append(param).append("=").append(URLEncoder.encode(value)).append("&");
        }
        signature.append(encodeSignarure(accessToken + clientSecret));
        url.append("sig=").append(encodeSignarure(signature.toString()));

        return url.toString();
    }

    private String encodeSignarure(String sign) {
        return DigestUtils.md5DigestAsHex(sign.getBytes()).toLowerCase();
    }
}