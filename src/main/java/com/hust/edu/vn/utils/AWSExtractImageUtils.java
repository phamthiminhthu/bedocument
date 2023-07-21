//package com.hust.edu.vn.utils;
//
//
//import com.amazonaws.services.textract.AmazonTextract;
//import com.amazonaws.services.textract.model.Block;
//import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
//import com.amazonaws.services.textract.model.DetectDocumentTextResult;
//import com.amazonaws.services.textract.model.Document;
//import com.amazonaws.util.IOUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.util.List;
//
//@Component
//@Slf4j
//public class AWSExtractImageUtils {
//    private final AmazonTextract client;
//
//    public AWSExtractImageUtils(AmazonTextract client) {
//        this.client = client;
//    }
//
//    public String extractImage(BufferedImage bufferedImage) {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        try {
//            ImageIO.write(bufferedImage, "jpeg", os);
//            ByteBuffer imageBytes;
//            try (InputStream inputStream = new ByteArrayInputStream(os.toByteArray())) {
//                imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
//            }
//            DetectDocumentTextRequest request = new DetectDocumentTextRequest()
//                    .withDocument(new Document().withBytes(imageBytes));
//            DetectDocumentTextResult result = client.detectDocumentText(request);
//            List<Block> blocks = result.getBlocks();
//            StringBuilder stringBuilder = new StringBuilder();
//            for (Block block : blocks) {
//                if(block.getText() != null) {
//                    stringBuilder.append(block.getText());
//                }
//            }
//            return stringBuilder.toString();
//        } catch (IOException e) {
//           e.printStackTrace();
//        }
//        return null;
//    }
//}
