package com.example.joinping.service;

import com.example.joinping.entity.pojo.Document;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    /**
     * 上传图片文件
     *
     * @param file
     * @param path
     * @return
     */
    Document uploadImage(MultipartFile file, String path);
    
    /**
     * 用户更换头像
     *
     * @param file
     * @param path
     * @return
     */
    Document changeUserImage(MultipartFile file, String path);
    
    /**
     * 批量上传图片文件
     *
     * @param files
     * @param path
     */
    void uploadMultipleImages(MultipartFile[] files, String path);
    
    /**
     * 根据id删除已上传图片
     *
     * @param id
     */
    void deleteById(Long id);
}
