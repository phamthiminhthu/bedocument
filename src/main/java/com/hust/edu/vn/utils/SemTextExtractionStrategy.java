package com.hust.edu.vn.utils;


import com.hust.edu.vn.model.TextData;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SemTextExtractionStrategy implements ITextExtractionStrategy {
    private List<TextData> listTextRenderData = new ArrayList<TextData>();
    @Override
    public String getResultantText() {
        return null;
    }

    @Override
    public void eventOccurred(IEventData iEventData, EventType eventType) {
        if (eventType.equals(EventType.RENDER_TEXT))
        {
            TextRenderInfo renderInfo = (TextRenderInfo) iEventData;
            String fontName = renderInfo.getFont().getFontProgram().getFontNames().getFontName();
            String text = renderInfo.getText();
//            log.info("Text" + text + " fontName: " + fontName);
            if((!fontName.contains("Symbol")) && (!fontName.contains("Wingdings")) && (!fontName.contains("New"))){
                String regex = "(?i).*bold.*";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(fontName);
                boolean isBold = matcher.find();
                float fontSize1 = renderInfo.getFontSize();
                float fontSizeTrue = renderInfo.getAscentLine().getStartPoint().get(1)
                        - renderInfo.getDescentLine().getStartPoint().get(1);
                float fontSize2 = (float) (Math.round(fontSizeTrue * 10.0) / 10.0);
                int index = checkMiddleText(fontSize1, fontSize2, isBold);
                if(index < listTextRenderData.size()) {
                    TextData data = listTextRenderData.get(index);
                    List<String> lines = data.getTextInfos();
                    lines.add(text);
                    data.setTextInfos(lines);
                }else{
                    List<String> newList = new ArrayList<>();
                    newList.add(text);
                    TextData newData = new TextData(fontSize1, fontSize2, newList, isBold);
                    listTextRenderData.add(newData);
                }
            }

        }
    }

    @Override
    public Set<EventType> getSupportedEvents() {
        return null;
    }

    public int checkMiddleText(float fontSize1, float fontSize2, boolean bold){
        for(int i = 0; i < listTextRenderData.size(); i++) {
            TextData data = listTextRenderData.get(i);
            if(data.getFontSize1() == fontSize1 && data.getFontSize2() == fontSize2 && bold == data.getIsBold()) {
                return i;
            }
        }
        return listTextRenderData.size();
    }

    public List<TextData> getResult(){
        return listTextRenderData;
    }
}
