package com.itheima.controller;

import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 文件上传和下载
 */
@Slf4j
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
    public R<String> upload(MultipartFile file){//叫file是因为前端参数叫file

        //如果目录不存在就创建一个
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdir();
        }

        //原始文件名
        String originalFilename = file.getOriginalFilename();

        //获得后缀(.jpg or .jpeg之类的
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用uuid生成文件名
        String fileName = UUID.randomUUID().toString() + suffix;

        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //把文件名返回给前端（因为文件名要存到数据库表
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){//不需要返回值，用二进制输出流写数据

        try {
            //输入流，读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));

            //输出流，写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            byte[] bytes = new byte[1024];
            int length = 0;
            while((length = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0 , length);
                outputStream.flush();
            }

            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
