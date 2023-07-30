package com.hust.edu.vn.utils;

import com.hust.edu.vn.model.TextData;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ExtractDataFileUtils {

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
    private String regexResult(String input){
        return input.replaceAll("(?<!^)(?=[A-Z](?![A-Z])|[A-Z][a-z]|\\\\d+\\\\s)\\\\s*", " ");
    }
    private HashMap<String, String> getTextItext(File file){
        HashMap<String, String> resultAll = new HashMap<>();
        try {
            List<TextData> listResults= new ArrayList<>();
            //Create PdfReader instance.
            PdfReader pdfReader = new PdfReader(new FileInputStream(file));
            //Get the number of pages in pdf.
            PdfDocument pdfDoc = new PdfDocument(pdfReader);
            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            String titleAvailable = info.getTitle();
            String authorAvailable = info.getAuthor();
            resultAll.put("author", authorAvailable);

            for(int i=1; i<=3;  i++) {
                SemTextExtractionStrategy strategy = new SemTextExtractionStrategy();
                PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), strategy);
                List<TextData> results = strategy.getResult();
                listResults.addAll(results);
            }
            if(listResults.size() > 0){
                listResults.sort(Comparator.comparing(TextData::getFontSize1)
                        .thenComparing(TextData::getFontSize2).reversed());
                StringBuffer title = new StringBuffer();
                int count = 0;
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
                        boolean stop = false;
                        ++count;
                        while(count < listResults.size() && check){
                            List<String> listTextSecond = listResults.get(count).getTextInfos();
                            StringBuffer sTitle = new StringBuffer();
                            boolean isBoldSecond = listResults.get(count).getIsBold();
                            if(isBold && fTitle.toString().trim().length() > 15 && !isBoldSecond){
                                title.append(fTitle).toString();
                                check = false;
                                stop = true;
                            }
                           if(!stop){
                               isBold = false;
                               for (String itext : listTextSecond) {
                                   sTitle.append(itext);
                               }
                               if(fTitle.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").trim().equals(sTitle.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").trim())){
                                   ++count;
                               }else{
                                   check = false;
                                   title.append(fTitle);
                               }
                           }
                        }
                        title.toString().trim();
                        if(!check && stop) {
                            break;
                        }
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
                String secondTitleFound = regexResult(resultSearch.replaceAll("(?i).*\\b(?:paper|whitepaper):\\s*", ""));
                StringBuffer resultSecondTitleFound = new StringBuffer();
                if(secondTitleFound.length() > 0) {
                    Pattern pattern = Pattern.compile("(?<!^)(?=[A-Z])");
                    Matcher matcher = pattern.matcher(secondTitleFound);
                    while (matcher.find()) {
                        matcher.appendReplacement(resultSecondTitleFound, " ");
                    }
                    matcher.appendTail(resultSecondTitleFound);
                }
                if(resultSecondTitleFound.toString().length() > 0 && titleAvailable != null){
                    resultAll.put("title", resultSecondTitleFound.toString());
                    resultAll.put("title2", titleAvailable);
                    return resultAll;
                }
                if(resultSecondTitleFound.toString().length() > 0){
                    resultAll.put("title", resultSecondTitleFound.toString());
                    return resultAll;
                }
            }
            if(titleAvailable != null){
                resultAll.put("title", titleAvailable);
                return resultAll;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public HashMap<String, String> extractData(MultipartFile file) {
        try {
            File convFile = convertMultiPartToFile(file);
            return getTextItext(convFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}