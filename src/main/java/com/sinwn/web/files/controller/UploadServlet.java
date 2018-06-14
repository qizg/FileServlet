package com.sinwn.web.files.controller;

import com.google.gson.Gson;
import com.sinwn.web.files.bean.ResponseBean;
import com.sinwn.web.files.constant.Defs;
import com.sinwn.web.files.constant.ResConstant;
import com.sinwn.web.files.id.ObjectId;
import com.sinwn.web.files.utils.FileUtil;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/upload")
@MultipartConfig(maxRequestSize = 1024L * 10000)
public class UploadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.reset();
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter writer = resp.getWriter();
        ResponseBean bean = new ResponseBean(ResConstant.RES_REQ_ERROR, ResConstant.RES_REQ_GET_MSG);
        writer.write(new Gson().toJson(bean));
        writer.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Collection<Part> parts = null;
        resp.reset();
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter writer = resp.getWriter();
        // 获取上传的文件列表，Part对象就是Servlet3对文件上传支持中对文件数据的抽象结构
        try {
            parts = req.getParts();
        } catch (Exception e) {
            System.out.println("======>get file part error");
            e.printStackTrace();

            ResponseBean bean = new ResponseBean(ResConstant.RES_REQ_ERROR, ResConstant.RES_UPDATE_ERROR + e.getMessage());
            writer.write(new Gson().toJson(bean));
            writer.flush();
            return;
        }
        if (parts == null || parts.isEmpty()) {
            ResponseBean bean = new ResponseBean(ResConstant.RES_REQ_ERROR, ResConstant.RES_UPDATE_EMPTY);
            writer.write(new Gson().toJson(bean));
            writer.flush();
            return;
        }
        List<String> fileNameList = new ArrayList<>();
        for (Part part : parts) {
            if (part == null) {
                continue;
            }
            FileUtil.logPartInfo(part);
            String fileName = FileUtil.getFileName(part);
            String ext = FileUtil.getFileExt(fileName);
            InputStream is = part.getInputStream();
            //创建全局唯一的文件名
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
            String newName = format.format(new Date()) + File.separator + new ObjectId().toString() + ext;
            String newFileName = Defs.getFilePath() + newName;
            try {
                // 将文件保存指硬盘
                FileUtils.copyInputStreamToFile(is, new File(newFileName));
                fileNameList.add(newName);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        ResponseBean<List<String>> bean
                = new ResponseBean<>(ResConstant.RES_SUCCESS, ResConstant.RES_UPDATE_MSG);
        bean.setData(fileNameList);
        writer.write(new Gson().toJson(bean));

        writer.flush();
    }

}
