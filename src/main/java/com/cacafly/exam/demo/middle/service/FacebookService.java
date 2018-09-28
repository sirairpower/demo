package com.cacafly.exam.demo.middle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Service;

@Service
public class FacebookService {

    @Value("${spring.social.facebook.appId}")
    String facebookAppId;
    @Value("${spring.social.facebook.appSecret}")
    String facebookSecret;
    @Value("${app.config.oauth.facebook.scope}")
    String scope;
    @Value("${app.config.oauth.facebook.callback}")
    String callbackURL;

    public Facebook createFacebook(){
        FacebookConnectionFactory connectionFactory = new FacebookConnectionFactory(facebookAppId, facebookSecret);
        OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();
        OAuth2Parameters params = new OAuth2Parameters();
        params.setRedirectUri(callbackURL);
        params.setScope(scope);
        return new FacebookTemplate(oauthOperations.authenticateClient(scope).getAccessToken());
    }

    public Facebook createFacebook(String accessToken){
        return new FacebookTemplate(accessToken);
    }


}
