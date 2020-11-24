package com.coderman.api.system.service.impl;

import com.coderman.api.common.config.web.FdfsConfig;
import com.coderman.api.common.exception.ServiceException;
import com.coderman.api.common.pojo.system.ImageAttachment;
import com.coderman.api.common.utils.FdfsUtil;
import com.coderman.api.system.mapper.ImageAttachmentMapper;
import com.coderman.api.system.service.UploadService;
import com.coderman.api.system.vo.ImageAttachmentVO;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author
 * @Date 2020/8/19 14:59
 * @Version 1.0
 **/
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private ImageAttachmentMapper attachmentMapper;

//    @Autowired
//    private FdfsConfig config;

    @Autowired
    private FdfsUtil fdfsUtil;


    //图片存放根路径
    @Value("${file.rootPath}")
    private String ROOT_PATH;
    //图片存放根目录下的子目录
    @Value("${file.sonPath}")
    private String SON_PATH;

    @Value("${server.port}")
    //获取主机端口
    private String POST;
    //获取IP
    @Value("${file.host}")
    private String HOST;


    @Override
    public String getUploadFilePath(MultipartFile file) {
        //返回上传的文件是否为空，即没有选择任何文件，或者所选文件没有内容。
        //防止上传空文件导致奔溃
        if (file.isEmpty()) {
            throw new NullPointerException("文件为空");
        }
        // 设置文件上传后的路径
        String filePath = ROOT_PATH + SON_PATH;
        // 获取文件名后缀名
        String suffix = file.getOriginalFilename();
        String prefix = suffix.substring(suffix.lastIndexOf(".")+1);
        //为防止文件重名被覆盖，文件名取名为：当前日期 + 1-1000内随机数
        Random random = new Random();
        Integer randomFileName = random.nextInt(1000);
        String fileName = timestamp2string(System.currentTimeMillis(),"yyyyMMddHHmmss") + randomFileName +"." +  prefix;
        //创建文件路径
        File dest = new File(filePath + fileName);
        // 解决中文问题，liunx下中文路径，图片显示问题
        // fileName = UUID.randomUUID() + suffixName;
        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            //假如文件不存在即重新创建新的文件已防止异常发生
            dest.getParentFile().mkdirs();
        }
        try {
            //transferTo（dest）方法将上传文件写到服务器上指定的文件
            file.transferTo(dest);
            //保存t_upload_file表中
            String filePathNew = SON_PATH + fileName;
            BufferedImage image = ImageIO.read(file.getInputStream());
            String profilePhoto = HOST + ":" + POST + filePathNew;
            if (image != null) {//如果image=null 表示上传的不是图片格式
                ImageAttachment imageAttachment = new ImageAttachment();
                imageAttachment.setCreateTime(new Date());
                imageAttachment.setHeight(image.getHeight());
                imageAttachment.setWidth(image.getWidth());
                imageAttachment.setMediaType(file.getContentType());
                imageAttachment.setPath(HOST + ":" + POST + filePathNew);
                attachmentMapper.insert(imageAttachment);
                System.out.println(profilePhoto);
            }
            return profilePhoto;
        } catch (Exception e) {
            return dest.toString();
        }
    }


    public static String timestamp2string(long time, String pattern) {
        Date d = new Date(time);

        if (pattern == null) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return DateFormatUtils.format(d, pattern);
    }



    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ServiceException("上传的文件不能为空");
        }
        InputStream inputStream = file.getInputStream();
        //文件的原名称
        long size = file.getSize();
        String originalFilename = file.getOriginalFilename();
        String fileExtName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String path = fdfsUtil.upfileImage(inputStream, size, fileExtName.toUpperCase(), null);
        //保存图片信息到数据库
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image != null) {//如果image=null 表示上传的不是图片格式
            ImageAttachment imageAttachment = new ImageAttachment();
            imageAttachment.setCreateTime(new Date());
            imageAttachment.setHeight(image.getHeight());
            imageAttachment.setWidth(image.getWidth());
            imageAttachment.setMediaType(fileExtName);
            imageAttachment.setMediaType(file.getContentType());
            imageAttachment.setPath(path);
            attachmentMapper.insert(imageAttachment);
        }
        //TODO
        return  path;
    }






    @Override
    public List<ImageAttachment> findImageList(ImageAttachmentVO imageAttachmentVO) {
        Example o = new Example(ImageAttachment.class);
        Example.Criteria criteria = o.createCriteria();
        o.setOrderByClause("create_time desc");
        if (imageAttachmentVO.getMediaType() != null && !"".equals(imageAttachmentVO.getMediaType())) {
            criteria.andEqualTo("mediaType", imageAttachmentVO.getMediaType());
        }
        if (imageAttachmentVO.getPath() != null && !"".equals(imageAttachmentVO.getPath())) {
            criteria.andLike("path", "%" + imageAttachmentVO.getPath() + "%");
        }
        //拼装图片真实路径
        List<ImageAttachment> attachments = attachmentMapper.selectByExample(o);
//        for (ImageAttachment attachment : attachments) {
//            attachment.setPath(config.getResHost()+attachment.getPath());
//        }
        return attachments;
    }

    @Override
    public void delete(Long id) {
        ImageAttachment image = attachmentMapper.selectByPrimaryKey(id);
        if(image==null){
            throw new ServiceException("图片不存在");
        }else {
            attachmentMapper.deleteByPrimaryKey(id);
           // fdfsUtil.deleteFile(image.getPath());
            deleteFileImg(image.getPath().replace((HOST + ":" + POST),""));
        }
    }

    private static boolean running = false;
    public void deleteFileImg(String IMG_PATH){
        if (!running ) {
            boolean isRunning = true;
            File file = new File(IMG_PATH);
            //判断文件是否存在
            if (file.exists() == true){
                System.out.println("图片存在，可执行删除操作");
                Boolean flag = false;
                flag = file.delete();
                if (flag){
                    System.out.println("成功删除图片"+file.getName());
                }else {
                    System.out.println("删除失败");
                }
            }else {
                System.out.println("图片不存在，终止操作");
            }
        }
    }
}
