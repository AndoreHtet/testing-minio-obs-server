package org.example.miniodemo.controller;

import org.example.miniodemo.service.StorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
public class MinioController {
    private final StorageService storageService;

    public MinioController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/minio")
    public String getMinioPage(Model model){
        List<String> objectNames = storageService.listObjectNames("sale-bucket");
        model.addAttribute("objectNames", objectNames);
        return "minio";
    }
    @PostMapping("/delete-photo/{objectName}")
    public String deletePhoto(@PathVariable("objectName")String objectName, RedirectAttributes redirectAttributes) {
        try {
            storageService.deleteObject("sale-bucket",objectName);
            return "redirect:/minio";
        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",e.getMessage());
            return "redirect:/minio";
        }
    }
    @PostMapping("/edit-photo/{objectName}")
    public String editPhoto(@PathVariable("objectName") String objectName, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            return "redirect:/uploadFailure";
        }
        try {
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            storageService.editImage("sale-bucket", objectName, file, contentType, inputStream, file.getOriginalFilename());
            return "redirect:/minio";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/minio";
        }
    }


    @PostMapping("/upload-photos")
    public String handleFileUpload(@RequestParam("file") MultipartFile file
                                   ,RedirectAttributes redirectAttributes) {
        if (file.isEmpty()){
            return "redirect:/uploadFailure";
        }
        try {

            storageService.putFile("sale-bucket",file,file.getOriginalFilename(),file.getInputStream(),file.getContentType());
//            storageService.uploadFIle("sale-bucket",file.getOriginalFilename(),file,file.getContentType(),file.getInputStream(),file.getName());
            System.out.println(file.getOriginalFilename());
            return "redirect:/minio";
        }catch (Exception e){
            System.out.println("-----" + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/uploadFailure";
        }
    }
}
