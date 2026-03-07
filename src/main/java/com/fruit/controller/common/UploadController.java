package com.fruit.controller.common;

import com.fruit.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/common/upload")
public class UploadController {

    @Value("${upload.url-prefix:/uploads/images/}")
    private String urlPrefix;

    // 使用项目根目录下的 uploads/images
    private String getUploadPath() {
        String projectDir = System.getProperty("user.dir");
        return Paths.get(projectDir, "uploads", "images").toString();
    }

    @ApiOperation("上传图片")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.failed("请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.failed("只能上传图片文件");
        }

        // 检查文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.failed("图片大小不能超过5MB");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 确保目录存在
            String uploadPath = getUploadPath();
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            File destFile = new File(uploadDir, newFilename);
            file.transferTo(destFile);

            // 返回访问URL
            String imageUrl = urlPrefix + newFilename;
            return Result.success(imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.failed("文件上传失败：" + e.getMessage());
        }
    }
}
