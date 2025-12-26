package com.lab.recruitment.controller;

import com.lab.recruitment.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("application/pdf") && 
                 !contentType.equals("application/msword") && 
                 !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") && 
                 !contentType.equals("application/zip") && 
                 !contentType.equals("application/x-rar-compressed") &&
                 !contentType.startsWith("image/") &&
                 !contentType.equals("text/plain"))) {
                return Result.error("只支持PDF、Word文档、压缩包、图片和文本文件");
            }

            // 创建上传目录
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String dirPath = uploadPath + datePath;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(dirPath, newFilename);
            Files.write(filePath, file.getBytes());

            // 构建返回信息
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", originalFilename);
            result.put("url", "/uploads/" + datePath + "/" + newFilename);
            result.put("path", "/uploads/" + datePath + "/" + newFilename);
            result.put("fileSize", file.getSize());

            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}