package com.cacafly.exam.demo.front.controller;




import com.cacafly.exam.demo.middle.service.FacebookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.social.facebook.api.*;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    @Inject
    FacebookService facebookService;

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @PostMapping ("/showUserInfo")
    public String showUserInfo(@RequestParam("accessToken") String accessToken,
                               @RequestParam("userID") String userID,
                               Model model,
                               HttpServletRequest request) throws Exception{


        LOGGER.info("accessToken:{}",accessToken);
        LOGGER.info("userID:{}",userID);
        request.getSession().setAttribute("accessToken",accessToken);
        Facebook facebook = facebookService.createFacebook(accessToken);
        LikeOperations userLikes = facebook.likeOperations();
        PagedList<Reference> userLikePages = fetchUserLikes(model, facebook, userID);
        userLikePages.forEach(p->{
            String ss = ToStringBuilder.reflectionToString(p,ToStringStyle.SIMPLE_STYLE);
            LOGGER.info("info:{}",ss);
        });
        model.addAttribute("userLikes",userLikePages);

//        PagedList<Reference> userLikes = fetchUserLikes(model,facebook,userID);
//        userLikes.stream().forEach(r->{
//            LOGGER.info("name:{}",r.getName());
//        });
//        LOGGER.info("getTotalCount:{}",userLikes.getTotalCount());
//        model.addAttribute("userLikes",userLikes);
//        userLikes.getNextPage().toMap().entrySet().stream().forEach(e->{
//            LOGGER.info("{},{}",e.getKey(),e.getValue());
//        });
//        model.addAttribute("totalCount",userLikes.getNextPage());
        return "userInfo";
    }

    private PagedList<Reference> fetchUserLikes(Model model, Facebook facebook, String userID) {
//        model.addAttribute("userLikes",facebook.fetchObject("me/likes", String.class));
//        LOGGER.info("likes:{}",facebook.fetchObject("me/likes", String.class));
        return facebook.likeOperations().getLikes(userID);
    }

}
