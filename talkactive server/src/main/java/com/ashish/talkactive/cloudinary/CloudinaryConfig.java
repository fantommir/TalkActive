package com.ashish.talkactive.cloudinary;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${spring.cloudinary.cloudinary_name}")
    private String cloudName;

    @Value("${spring.cloudinary.cloudinary_api_key}")
    private String apiKey;

    @Value("${spring.cloudinary.cloudinary_api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary getCloudinary() {
        Cloudinary cloudinary = null;
        Map config = new HashMap();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", true);
        cloudinary = new Cloudinary(config);
        return cloudinary;
    }
}
