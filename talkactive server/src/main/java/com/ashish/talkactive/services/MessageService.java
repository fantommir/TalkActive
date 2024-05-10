package com.ashish.talkactive.services;

import com.ashish.talkactive.cloudinary.CloudinaryImageService;
import com.ashish.talkactive.models.Message;
import com.ashish.talkactive.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.Map;


@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CloudinaryImageService cloudinaryImageService;

    public Message save(Message message) {
        messageRepository.save(message);
        return message;
    }

    public String processImage(String base64, String folder) {
        byte[] data = DatatypeConverter.parseBase64Binary(base64.split(",")[1]);
        Map<String, String> imageData = cloudinaryImageService.upload(data, "TalkActive/"+folder);
        return imageData.get("secure_url");
    }

}
