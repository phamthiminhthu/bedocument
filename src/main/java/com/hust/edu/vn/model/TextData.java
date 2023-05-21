package com.hust.edu.vn.model;


import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextData {
    private Float fontSize1;
    private Float fontSize2;
    private List<String> textInfos;
    private boolean isBold;

    public boolean getIsBold() {
        return isBold;
    }
}
