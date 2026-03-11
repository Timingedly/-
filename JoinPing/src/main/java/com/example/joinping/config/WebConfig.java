package com.example.joinping.config;

import com.example.joinping.constant.DocumentConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SpringMVC配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将物理路径映射到URL路径，在拦截器之后执行
        // 匹配所有以 /images/ 开头的URL请求,"file:" 前缀表示这是文件系统路径（不是classpath）,如果是Windows系统，路径可能是 "file:C:/uploads/images/"
        registry.addResourceHandler(DocumentConstants.REQUEST_STATIC_MAPPING_PATH + "**").addResourceLocations("file:" + DocumentConstants.PATH_UPLOAD_IMAGE);
    }
}

