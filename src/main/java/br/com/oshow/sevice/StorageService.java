package br.com.oshow.sevice;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketname;

    @Autowired
    private AmazonS3 s3Client;



    public String uploadFile(MultipartFile file) {
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketname, fileName, fileObj));
        fileObj.delete();
        return "File uploaded : " + fileName;

    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketname, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketname, fileName);
        return fileName + " removed!";

    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

    public String listObjects(){

        ObjectListing objectListing = s3Client.listObjects(bucketname);

        JSONObject outputJsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            jsonArray.put( objectSummary.getKey());
        }

        outputJsonObj.put("arquivos" , jsonArray);

        return outputJsonObj.toString();

    }


}
