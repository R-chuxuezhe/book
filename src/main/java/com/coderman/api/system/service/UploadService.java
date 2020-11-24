package com.coderman.api.system.service;

import com.coderman.api.common.pojo.system.ImageAttachment;
import com.coderman.api.system.vo.ImageAttachmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Author
 * @Date 2020/8/19 14:57
 * @Version 1.0
 **/
public interface UploadService {

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    String getUploadFilePath(MultipartFile file)throws IOException;

    /**
     * FDFS上传文件
     * @param file
     * @return
     * @throws IOException
     */
    String uploadImage(MultipartFile file) throws IOException; //图片上传
    List<ImageAttachment> findImageList(ImageAttachmentVO imageAttachmentVO); //图片列表
    void delete(Long id); //删除图片
}
