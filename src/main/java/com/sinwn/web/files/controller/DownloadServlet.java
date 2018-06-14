package com.sinwn.web.files.controller;

import com.google.gson.Gson;
import com.sinwn.web.files.bean.ResponseBean;
import com.sinwn.web.files.constant.Defs;
import com.sinwn.web.files.constant.ResConstant;
import com.sinwn.web.files.utils.FileUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet("/download/*")
public class DownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String fileName = null;
        String requestUri = req.getRequestURI();
        int lastIndex = requestUri.lastIndexOf("download/");
        if (lastIndex > 0) {
            fileName = requestUri.substring(lastIndex + 9);
        }
        if (fileName == null || fileName.trim().length() == 0) {
            resp.reset();
            resp.setContentType("application/json; charset=utf-8");
            PrintWriter writer = resp.getWriter();
            ResponseBean bean = new ResponseBean(ResConstant.RES_REQ_ERROR, ResConstant.RES_REQ_EMPTY);
            writer.write(new Gson().toJson(bean));
            writer.flush();
            return;
        }
        fileName = Defs.getFilePath() + fileName;
        File file = new File(fileName);
        if (!file.exists()) {
            resp.reset();
            resp.setContentType("application/json; charset=utf-8");
            PrintWriter writer = resp.getWriter();
            ResponseBean bean = new ResponseBean(ResConstant.RES_REQ_ERROR, ResConstant.RES_DOWNLOAD_EMPTY);
            writer.write(new Gson().toJson(bean));
            writer.flush();
            return;
        }

        long length = file.length();
        long start = 0;
        resp.reset();
        resp.setHeader("Accept-Ranges", "byte");
        //断点续传的信息就存储在这个Header属性里面： range:bytes=3-100;200 （从3开始，读取长度为100，总长度为200）
        String range = req.getHeader("Range");
        if (range != null) {
            //SC_PARTIAL_CONTENT 206 表示服务器已经成功处理了部分 GET 请求。
            // 类似于 FlashGet 或者迅雷这类的 HTTP下载工具都是使用此类响应实现断点续传或者将一个大文档分解为多个下载段同时下载。
            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            range = range.substring("bytes=".length());
            String[] rangeInfo = range.split("-");
            start = new Long(rangeInfo[0]);
            if (start > file.length()) {
                resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }
            if (rangeInfo.length > 1) {
                length = Long.parseLong(rangeInfo[1]) - start + 1;
            } else {
                length = length - start;
            }
            if (length + start > file.length()) {
                length = file.length() - start;
            }
        }

        resp.setHeader("Content-Type", FileUtil.getMime(fileName));
        resp.setHeader("Content-Length", new Long(length).toString());
        if (range != null) {
            resp.setHeader("Content-Range",
                    "bytes " + new Long(start).toString()
                            + "-" + new Long(start + length - 1).toString()
                            + "/" + new Long(file.length()).toString());
        }
        resp.setContentType(FileUtil.getMime(fileName));
        resp.setHeader("Content-Disposition",
                "attachment;filename=" + new String(file.getName().getBytes(), "utf-8"));
        long k = 0;
        int ibuffer = 65536;
        byte[] bytes = new byte[ibuffer];
        FileInputStream fileinputstream = new FileInputStream(file);
        try {
            if (start != 0) {
                fileinputstream.skip(start);
            }
            OutputStream os = resp.getOutputStream();
            while (k < length) {
                int j = fileinputstream.read(bytes, 0, (int) (length - k < ibuffer ? length - k : ibuffer));
                if (j < 1) {
                    break;
                }
                os.write(bytes, 0, j);
                k += j;
            }
            os.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileinputstream.close();
        }
    }
}
