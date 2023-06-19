package com.hust.edu.vn.utils;

import com.hust.edu.vn.model.TextData;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Component
@Slf4j
public class ExtractDataFileUtils {

    private final AWSExtractImageUtils extractImageUtils;

    public ExtractDataFileUtils(AWSExtractImageUtils extractImageUtils) {
        this.extractImageUtils = extractImageUtils;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

//    private String readFile(File file) throws IOException {
//        try (InputStream input = new FileInputStream(file)) {
//            AutoDetectParser parser = new AutoDetectParser();
//            BodyContentHandler handler = new BodyContentHandler(-1);
//            Metadata metadata = new Metadata();
//            ParseContext context = new ParseContext();
//            parser.parse(input, handler, metadata, context);
//            String content = handler.toString();
//            String s_content = content.substring(0, Math.min(content.length(), 1000));
//            return s_content;
//        } catch (IOException | TikaException | SAXException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    private Document openPdfFile(File pdfFile) throws IOException {
//        return PDF.open(pdfFile);
//    }

//    private String getTextPdxStream(File file) throws IOException {
//        try {
//            Document pdfDoc = openPdfFile(file);
////            StringWriter writer = new StringWriter();
////            pdfDoc.pipe(new OutputTarget(writer));
////            pdfDoc.close();
////            log.info(writer.toString());
////            return writer.toString();
//            String textFromImage = null;
//            int totalPage = pdfDoc.getPageCnt();
//            int end = 3;
//            float fontSizeMax = 0;
//            int idxBlock = 0;
//            int idxPage = 0;
//            if(totalPage < 3) end = totalPage;
//
//            for( int i = 0; i < end; i++ ){
//                Page page = pdfDoc.getPage(i);
//                List<Image> images = (List<Image>) page.getImages();
//                if(images.size() > 0){
//                    BufferedImage bufferedImage = images.get(0).bitmap();
//                    textFromImage = extractImageUtils.extractImage(bufferedImage) + " ";
//                }
//                BlockParent blockParent = page.getTextContent();
//                int countBlocks = blockParent.getChildCnt();
//                for(int j = 0; j < countBlocks; j++){
//                    Block block = blockParent.getChild(j);
//                    StringWriter writer = new StringWriter();
//                    block.pipe(new OutputTarget(writer));
//                    if(block != null){
//                        for(int k = 0; k < block.getLineCnt(); k++){
//                            Line line = block.getLine(k);
//                            if(line.getTextUnitCnt() > 0){
//                                for(int n = 0; n < line.getTextUnitCnt(); n++){
//                                    TextUnit unit = line.getTextUnit(n);
//                                    if(unit != null){
//                                        float textSize= unit.getFontSize();
//                                        boolean textBold = unit.getFont().isBold();
//                                        if (textSize >= fontSizeMax && textBold){
//                                            fontSizeMax = textSize;
//                                            idxBlock = j;
//                                            idxPage = i;
//                                            break;
//                                        }
//                                        if(textSize > fontSizeMax){
//                                            fontSizeMax = textSize;
//                                            idxBlock = j;
//                                            idxPage = i;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            Page page = pdfDoc.getPage(idxPage);
//            BlockParent blockParent = page.getTextContent();
//            Block block = blockParent.getChild(idxBlock);
//            StringWriter writer = new StringWriter();
//            block.pipe(new OutputTarget(writer));
//            pdfDoc.close();
//            return writer.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    private String regexResult(String input){
        return input.replaceAll("(?<!^)(?=[A-Z](?![A-Z])|[A-Z][a-z]|\\\\d+\\\\s)\\\\s*", " ");
    }
    private String getTextItext(File file){
        try {
            List<TextData> listResults= new ArrayList<TextData>();
            TreeMap<Float, String> result = new TreeMap<>();
            //Create PdfReader instance.
            PdfReader pdfReader = new PdfReader(new FileInputStream(file));
            //Get the number of pages in pdf.
            PdfDocument pdfDoc = new PdfDocument(pdfReader);
            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            String titleAvailable = info.getTitle();
            //Iterate the pdf through pages.

            PdfDictionary infoDictionary = pdfDoc.getTrailer().getAsDictionary(PdfName.Info);
//            for (PdfName key: infoDictionary.keySet()) {
//                log.info("key: " + key + " value: " + infoDictionary.getAsDictionary(key));
//            }

            for(int i=1; i<=3;  i++) {
                SemTextExtractionStrategy strategy = new SemTextExtractionStrategy();
                PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), strategy);
                List<TextData> results = strategy.getResult();
                listResults.addAll(results);
            }
            if(listResults.size() > 0){
//                List<TextData> filteredList = listResults.stream()
//                        .filter(data -> data.getFontSize1() == 1.0)
//                        .collect(Collectors.toList());
                Collections.sort(listResults, Comparator.comparing(TextData::getFontSize1)
                        .thenComparing(TextData::getFontSize2).reversed());
//                listResults.addAll(0, filteredList);
//                for (TextData data : listResults) {
//                    log.info("text data: " + data.toString());
//                }
                StringBuffer title = new StringBuffer();
                int count = 0;
                log.info("size" + listResults.size());
                while(listResults.size() > 0 && title.length() < 30 && count < listResults.size()){
                    List<String> listTextFirst = listResults.get(count).getTextInfos();
                    StringBuffer fTitle = new StringBuffer();
                    boolean isBold = listResults.get(count).getIsBold();
                    for (String itext : listTextFirst) {
                        fTitle.append(itext);
                    }
                    if(fTitle.length() < 5 || fTitle.toString().trim().matches("[^a-zA-Z]*")){
                        ++count;
                    }else{
                        boolean check = true;
                        ++count;
                        while(count < listResults.size() && check){
                            List<String> listTextSecond = listResults.get(count).getTextInfos();
                            StringBuffer sTitle = new StringBuffer();
                            boolean isBoldSecond = listResults.get(count).getIsBold();
                            if(isBold && fTitle.toString().trim().length() > 15 && !isBoldSecond){
                                return title.append(fTitle.toString()).toString();
                            }
                            isBold = false;
                            for (String itext : listTextSecond) {
                                sTitle.append(itext);
                            }
                            if(fTitle.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").trim().equals(sTitle.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").trim())){
                                check = true;
                                ++count;
                            }else{
                                check = false;
                                title.append(fTitle);
                            }
                        }
                        title.toString().trim();
                        if(title.toString().matches("\\s*")){
                            title.setLength(0);
                        }else{
                            title.append(" ");
                        }
                        if(title.length() > 250){
                            break;
                        }
                    }

                }

                String resultSearch = title.toString().replaceAll("\\s+", " ");
                String secondTitleFound = regexResult(resultSearch.replaceAll(".*:(?=\\s+[A-Z])", ""));
                if(secondTitleFound.length() > 0){
                    return secondTitleFound;
                }
                if(titleAvailable != null){
                    return titleAvailable;
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String extractData(MultipartFile file) {
        try {
            File convFile = convertMultiPartToFile(file);
            String contentItext = getTextItext(convFile);
            return contentItext;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}