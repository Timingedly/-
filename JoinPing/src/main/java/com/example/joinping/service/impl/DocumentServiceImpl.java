package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.joinping.constant.DocumentConstants;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.pojo.Document;
import com.example.joinping.entity.pojo.User;
import com.example.joinping.enums.ErrorCodeEnum;
import com.example.joinping.mapper.DocumentMapper;
import com.example.joinping.mapper.UserMapper;
import com.example.joinping.service.DocumentService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.RabbitMQUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentServiceImpl implements DocumentService {
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RabbitMQUtils rabbitMQUtils;
    
    @Override
    @Transactional
    public Document uploadImage(MultipartFile file, String path) {
        //获取当前年月作为文件夹
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DocumentConstants.YEAR_MONTH_FORMATTER)) + "/";
        String uploadDir = DocumentConstants.PATH_UPLOAD_IMAGE + path + datePath;
        String fileUrl = DocumentConstants.REQUEST_STATIC_MAPPING_PATH + path + datePath;
        Long fileId = null;
        // 开始异常处理
        try {
            // 检查上传的文件是否为空
            if (file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }
            if (!StrUtil.startWith(file.getContentType(), "image/")) {
                throw new IllegalArgumentException("只能上传图片文件");
            }
            createFilePathIfNotExist(uploadDir);
            // 获取原始文件名
            String originalFileName = file.getOriginalFilename();
            // 初始化文件扩展名为空字符串
            String fileExtension = "";
            // 检查原始文件名不为空且包含扩展名分隔符"."
            if (originalFileName != null && originalFileName.contains(".")) {
                // 提取文件扩展名（从最后一个"."开始到字符串结尾）
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            Document document = new Document();
            CommonUtils.initPojoCommonProperties(document);
            // 将文件id作为文件名前缀，拼接文件扩展名
            String fileName = document.getId() + fileExtension;
            // 构建完整的文件保存路径（目录路径 + 文件名）
            Path filePath = Paths.get(uploadDir, fileName);
            // 将上传的文件流复制到目标路径
            Files.copy(file.getInputStream(), filePath);
            // 构建文件访问URL（因为配置了静态资源映射）
            String mappingUrl = fileUrl + fileName;
            document.initProperties(mappingUrl, filePath.toAbsolutePath().toString(), fileName, fileExtension, StpUtil.getLoginIdAsLong());
            documentMapper.insert(document);
            fileId = document.getId();
            return document;
        } catch (IOException e) {
            //将已上传的图片标记为删除，方便日后集中清理
            if (fileId != null) {
                documentMapper.deleteById(fileId);
            }
            // 打印异常堆栈信息（生产环境应使用日志框架）
            e.printStackTrace();
            throw new BusinessException(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    @Transactional
    public Document changeUserImage(MultipartFile file, String path) {
        Document document = uploadImage(file, path);
        User user = userMapper.selectById(StpUtil.getLoginIdAsLong());
        //将原有头像删除(如果不是默认头像的话)
        if (user.getPhotoId() != null) {
            documentMapper.delete(new LambdaQueryWrapper<Document>().eq(Document::getId, user.getPhotoId()));
        }
        //更新用户头像
        user.setPhotoId(document.getId());
        user.setPhotoUrl(document.getUrl());
        userMapper.updateById(user);
        return document;
    }
    
    @Override
    public void uploadMultipleImages(MultipartFile[] files, String path) {
        // 遍历文件数组中的每个文件
        for (MultipartFile file : files) {
            // 检查当前文件是否非空
            if (!file.isEmpty()) {
                // 调用单个文件上传方法处理当前文件
                uploadImage(file, path);
            }
        }
    }
    
    /**
     * 创建文件目录
     *
     * @param path
     */
    private void createFilePathIfNotExist(String path) {
        File directory = new File(path);
        // 检查目录是否已存在
        if (!directory.exists()) {
            // 创建目录（包括所有不存在的父目录）
            boolean created = directory.mkdirs();
            // 检查目录是否创建成功
            if (!created) {
                throw new BusinessException("文件目录创建失败: " + path);
            }
        }
    }
    
    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "传入的id有误");
        rabbitMQUtils.sendPhotoPathToPhotoPathFanoutExchange(id);
    }
}
