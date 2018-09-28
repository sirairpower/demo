package com.cacafly.exam.demo.front.controller;

import com.cacafly.exam.demo.middle.service.FacebookService;
import com.cacafly.exam.demo.middle.service.StorageFileNotFoundException;
import com.cacafly.exam.demo.middle.service.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class UpLoadFacebookController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    @Inject
    private FacebookService facebookService;
    @Inject
    private StorageService storageService;

    @GetMapping("/uploadFacebook")
    public String uploadFacebook(Model model) {
        storageService.init();
        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(UpLoadFacebookController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadFacebook";
    }

    @GetMapping("/file/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        LOGGER.info("file.getFilename:{}",file.getFilename());

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/uploadPhotos")
    public String handleFileUpload(HttpServletRequest request,
                                   RedirectAttributes redirectAttributes,
                                   @RequestParam("file") MultipartFile... files)throws Exception {


        LOGGER.info("upload photo to Facebook...");
        String accessToken = (String)request.getSession().getAttribute("accessToken");
        if(StringUtils.isBlank(accessToken)){
            throw new Exception("Without login in fb!");
        }
        Facebook facebook = facebookService.createFacebook(accessToken);
        storageService.deleteAll();
        storageService.init();
        storageService.storeFiles(files);
        AtomicInteger countSucceed = new AtomicInteger(0);
        storageService.loadAll().forEach(p->{
            try {
                Resource photo = new UrlResource(p.toUri());
                LOGGER.info("upload photo :{} to facebook",photo.getFilename());
                facebook.mediaOperations().postPhoto(photo);
                countSucceed.getAndIncrement();
            } catch (MalformedURLException e) {
                LOGGER.error(e.getMessage(),e);
            }
        });


        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded for :" + countSucceed.get()+"/"+files.length + " file!");

        return "uploadFacebook";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
