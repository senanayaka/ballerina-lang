/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.services.dispatchers.http;

import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.dispatchers.session.Session;
import org.ballerinalang.services.dispatchers.session.SessionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.services.dispatchers.http.Constants.PATH;
import static org.ballerinalang.services.dispatchers.http.Constants.RESPONSE_COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.SESSION_ID;

/**
 * HTTPSession represents a session
 */
public class HTTPSession implements Session {

    private final String sessionPath;
    private String id;
    private Long createTime;
    private Long lastAccessedTime;
    private int maxInactiveInterval;
    private Map<String, BValue> attributeMap = new ConcurrentHashMap<>();
    private SessionManager sessionManager;
    private boolean isValid = true;
    private boolean isNew = true;

    public HTTPSession(String id, int maxInactiveInterval, String path) {
        this.id = id;
        this.maxInactiveInterval = maxInactiveInterval;
        createTime = System.currentTimeMillis();
        lastAccessedTime = createTime;
        this.sessionPath = path;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setAttribute(String attributeKey, BValue attributeValue) {
        checkValidity();
        attributeMap.put(attributeKey, attributeValue);
    }

    @Override
    public BValue getAttributeValue(String attributeKey) {
        checkValidity();
        return attributeMap.get(attributeKey);
    }

    @Override
    public Long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void invalidate() {
        sessionManager.invalidateSession(this);
        attributeMap.clear();
        isValid = false;
    }

    public void setManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Session setAccessed() {
        checkValidity();
        lastAccessedTime = System.currentTimeMillis();
        return this;
    }

    private void checkValidity() {
        if (!isValid) {
            throw new IllegalStateException("Session is invalid");
        }
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void generateSessionHeader(BMessage message) {
        //Add set Cookie only for the first response after the creation
        if (this.isNew()) {
            message.value().setHeader(RESPONSE_COOKIE_HEADER, SESSION_ID + this.getId() + "; "
                    + PATH + this.getPath() + ";");
        }
        //set other headers (path)
    }

    public boolean isNew() {
        return this.isNew;
    }

    @Override
    public Session setNew(boolean isNew) {
        this.isNew = isNew;
        return this;
    }


    @Override
    public String getPath() {
        return sessionPath;
    }
}
