package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //截取后四位文件名.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新生成文件名,防止文件名覆盖
        String fileName = UUID.randomUUID().toString()+suffix;

        //创建一个目录对象并判断是否存在
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 浏览器下载图片
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));
            ServletOutputStream servletOutputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                servletOutputStream.write(bytes,0,len);
                servletOutputStream.flush();
            }
            fileInputStream.close();
            servletOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
