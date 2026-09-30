package com.attendance.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.attendance.config.BaiduFaceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class BaiduFaceService {

    private static final Logger logger = LoggerFactory.getLogger(BaiduFaceService.class);

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String ADD_FACE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
    private static final String SEARCH_FACE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/search";
    private static final String DELETE_FACE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/delete";

    @Autowired
    private BaiduFaceConfig baiduFaceConfig;

    private RestTemplate restTemplate;
    private String accessToken;
    private long tokenExpireTime;

    public BaiduFaceService() {
        this.restTemplate = new RestTemplate();
    }

    private synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < tokenExpireTime) {
            return accessToken;
        }

        String apiKey = baiduFaceConfig.getApiKey();
        String secretKey = baiduFaceConfig.getSecretKey();

        if ("your-api-key".equals(apiKey) || "your-secret-key".equals(secretKey)) {
            logger.warn("百度AI API Key未配置，将使用本地比对");
            return null;
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "client_credentials");
            params.add("client_id", apiKey);
            params.add("client_secret", secretKey);

            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, params, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                accessToken = jsonObject.getString("access_token");
                int expiresIn = jsonObject.getIntValue("expires_in");
                tokenExpireTime = now + (expiresIn - 60) * 1000L;
                logger.info("获取百度AI access_token成功，有效期: {}秒", expiresIn);
                return accessToken;
            } else {
                logger.error("获取access_token失败: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("获取access_token异常", e);
            return null;
        }
    }

    public boolean addFace(String userId, String faceImage) {
        String token = getAccessToken();
        if (token == null) {
            return false;
        }

        String url = ADD_FACE_URL + "?access_token=" + token;

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("image", faceImage);
            requestBody.put("image_type", "BASE64");
            requestBody.put("group_id", baiduFaceConfig.getGroupId());
            requestBody.put("user_id", userId);
            requestBody.put("user_info", "考勤系统用户");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                int errorCode = jsonObject.getIntValue("error_code");
                
                if (errorCode == 0) {
                    logger.info("百度AI人脸注册成功，用户ID: {}", userId);
                    return true;
                } else if (errorCode == 222207) {
                    logger.warn("人脸已存在，用户ID: {}", userId);
                    deleteFace(userId);
                    return addFace(userId, faceImage);
                } else {
                    logger.error("百度AI人脸注册失败，错误码: {}, 错误信息: {}", 
                            errorCode, jsonObject.getString("error_msg"));
                    return false;
                }
            } else {
                logger.error("百度AI人脸注册HTTP失败: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("百度AI人脸注册异常", e);
            return false;
        }
    }

    public String searchFace(String faceImage) {
        String token = getAccessToken();
        if (token == null) {
            return null;
        }

        String url = SEARCH_FACE_URL + "?access_token=" + token;

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("image", faceImage);
            requestBody.put("image_type", "BASE64");
            requestBody.put("group_id_list", baiduFaceConfig.getGroupId());
            requestBody.put("max_user_num", 1);
            requestBody.put("match_threshold", 70);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                int errorCode = jsonObject.getIntValue("error_code");
                
                if (errorCode == 0) {
                    JSONObject result = jsonObject.getJSONObject("result");
                    if (result != null && result.containsKey("user_list")) {
                        com.alibaba.fastjson.JSONArray userList = result.getJSONArray("user_list");
                        if (userList != null && userList.size() > 0) {
                            JSONObject user = userList.getJSONObject(0);
                            String userId = user.getString("user_id");
                            double score = user.getDoubleValue("score");
                            logger.info("百度AI人脸搜索成功，用户ID: {}, 相似度: {:.2f}%", userId, score);
                            return score >= 70 ? userId : null;
                        }
                    }
                    logger.info("百度AI人脸搜索未找到匹配用户");
                    return null;
                } else {
                    logger.error("百度AI人脸搜索失败，错误码: {}, 错误信息: {}", 
                            errorCode, jsonObject.getString("error_msg"));
                    return null;
                }
            } else {
                logger.error("百度AI人脸搜索HTTP失败: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("百度AI人脸搜索异常", e);
            return null;
        }
    }

    public boolean deleteFace(String userId) {
        String token = getAccessToken();
        if (token == null) {
            return false;
        }

        String url = DELETE_FACE_URL + "?access_token=" + token;

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("group_id", baiduFaceConfig.getGroupId());
            requestBody.put("user_id", userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                int errorCode = jsonObject.getIntValue("error_code");
                
                if (errorCode == 0) {
                    logger.info("百度AI人脸删除成功，用户ID: {}", userId);
                    return true;
                } else {
                    logger.warn("百度AI人脸删除失败，错误码: {}", errorCode);
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("百度AI人脸删除异常", e);
            return false;
        }
    }
}