package com.ikun.knowledge_back.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface OssService {
    //上传头像到oss
    String uploadFileAvatar(MultipartFile file);
}
