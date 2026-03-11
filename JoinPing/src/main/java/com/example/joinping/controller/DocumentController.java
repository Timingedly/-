package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.DocumentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.joinping.constant.DocumentConstants.*;

/**
 * 文件相关请求接口类
 */
@RestController
@RequestMapping("/document")
public class DocumentController {
    @Resource
    private DocumentService documentService;
    
    /**
     * 上传话题图片
     *
     * @param file
     * @return
     */
    @PutMapping("/topic")
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    public Result uploadTopicImage(@RequestParam("file") MultipartFile file) {
        return Result.success(documentService.uploadImage(file, UPLOAD_TOPIC_IMAGE_PATH));
    }
    
    /**
     * 上传主体图片
     *
     * @param file
     * @return
     */
    @PutMapping("/thing")
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    public Result uploadThingImage(@RequestParam("file") MultipartFile file) {
        return Result.success(documentService.uploadImage(file, UPLOAD_THING_IMAGE_PATH));
    }
    
    /**
     * 上传用户头像
     *
     * @param file
     * @return
     */
    @PutMapping("/user")
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    public Result uploadUserImage(@RequestParam("file") MultipartFile file) {
        return Result.success(documentService.changeUserImage(file, UPLOAD_USER_IMAGE_PATH));
    }
    
    /**
     * 批量上传图片
     *
     * @param files
     * @return
     */
    @PutMapping("/upload-multiple")
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    public Result uploadMultipleImages(@RequestParam("files") MultipartFile[] files) {
        documentService.uploadMultipleImages(files, "");
        return Result.success();
    }
    
    /**
     * 根据id删除已上传图片
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    public Result deleteById(@PathVariable("id") Long id) {
        documentService.deleteById(id);
        return Result.success();
    }
}




