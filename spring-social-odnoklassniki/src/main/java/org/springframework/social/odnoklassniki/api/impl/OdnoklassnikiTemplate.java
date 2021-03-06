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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.odnoklassniki.api.FriendsOperations;
import org.springframework.social.odnoklassniki.api.Odnoklassniki;
import org.springframework.social.odnoklassniki.api.OdnoklassnikiErrorHandler;
import org.springframework.social.odnoklassniki.api.UsersOperations;
import org.springframework.social.odnoklassniki.api.json.OdnoklassnikiModule;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the central class for interacting with Odnoklassniki.
 *
 * @author vkolodrevskiy
 */
public class OdnoklassnikiTemplate extends AbstractOAuth2ApiBinding implements Odnoklassniki {

    private FriendsOperations friendsOperations;
    private UsersOperations usersOperations;

    private final String accessToken;
    private final String applicationKey;
    private final String applicationSecretKey;

    private ObjectMapper objectMapper;

    public OdnoklassnikiTemplate() {
        this.applicationKey = null;
        this.applicationSecretKey = null;
        this.accessToken = null;
        initialize();
    }

    public OdnoklassnikiTemplate(String applicationKey, String applicationSecretKey, String accessToken) {
        super(accessToken);
        Assert.hasLength(accessToken, "Access token cannot be null or empty.");
        this.applicationKey = applicationKey;
        this.applicationSecretKey = applicationSecretKey;
        this.accessToken = accessToken;
        initialize();
    }

    private void initialize() {
        registerJsonModule();
        getRestTemplate().setErrorHandler(new OdnoklassnikiErrorHandler());
        initSubApis();
    }

    private void registerJsonModule() {
        List<HttpMessageConverter<?>> converters = getRestTemplate().getMessageConverters();
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;

                List<MediaType> mTypes = new LinkedList<>(jsonConverter.getSupportedMediaTypes());
                mTypes.add(new MediaType("text", "javascript", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
                jsonConverter.setSupportedMediaTypes(mTypes);

                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new OdnoklassnikiModule());
                jsonConverter.setObjectMapper(objectMapper);
            }
        }
    }

    private void initSubApis() {
        usersOperations = new UsersTemplate(getRestTemplate(), accessToken, applicationKey, applicationSecretKey, isAuthorized());
        friendsOperations = new FriendsTemplate(getRestTemplate(), accessToken, applicationKey, applicationSecretKey, isAuthorized());
    }

    @Override
    public FriendsOperations friendsOperations() {
        return friendsOperations;
    }

    @Override
    public UsersOperations usersOperations() {
        return usersOperations;
    }

    @Override
    public RestOperations restOperations() {
        return getRestTemplate();
    }
}
