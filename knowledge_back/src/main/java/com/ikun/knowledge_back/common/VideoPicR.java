package com.ikun.knowledge_back.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//此类是一个图片视频结果类,
@Data
public class VideoPicR implements Serializable {

    private Integer errno;

    private String message; //错误信息

    private ImageData data; //数据
    @Data
    public static class ImageData {
        private String url;
    }


    //响应成功返回R对象
    public static VideoPicR success(String url) {
        VideoPicR r = new VideoPicR();
        r.data=new ImageData();
        r.data.setUrl(url);
        r.errno = 0;
        return r;
    }

    //响应失败也返回R对象
    public static VideoPicR error(String msg) {
        VideoPicR r = new VideoPicR();
        r.message = msg;
        r.errno = 1;
        return r;
    }

}
