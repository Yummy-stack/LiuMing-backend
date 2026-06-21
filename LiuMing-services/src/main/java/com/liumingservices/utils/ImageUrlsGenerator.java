package com.liumingservices.utils;

import java.util.ArrayList;
import java.util.List;

public class ImageUrlsGenerator {

    /**
     * 生成 100 条“在国内相对好访问”的图片 HTTP/HTTPS 路径
     */
    public static List<String> genImageUrls(int total) {
        List<String> urls = new ArrayList<>(total);

        // ✅ 你只需要在这里切换你选中的 BASE
        // 候选1：temp.im（注意是 http，部分环境可能需要允许 http 资源）
        // 候选2：placehold.co（https，更省心）
        final String mode = "temp.im"; // 或 "placehold.co"

        for (int i = 1; i <= total; i++) {
            // 让每张“看起来不一样”，同时避免因为完全相同 URL 被缓存成一张
            int w = 360 + (i % 5) * 20; // 360~440
            int h = 240 + (i % 3) * 20; // 240~280

            String url;
            switch (mode) {
                case "temp.im":
                    // temp.im 支持 /WxH，也支持 /WxH/BG/FG
                    url = String.format("http://temp.im/%dx%d", w, h);
                    break;

                case "placehold.co":
                    // placehold.co 支持 ?text= 做区分
                    url = String.format(
                            "https://placehold.co/%dx%d/EEE/31343F?text=img-%03d",
                            w, h, i
                    );
                    break;

                default:
                    throw new IllegalStateException("unknown mode");
            }

            urls.add(url);
        }
        return urls;
    }
}