package org.enoch.snark.instance.model.action.find;

import lombok.Data;

@Data
public class QueryParam {
    String name;
    String value;

    public static QueryParam parse(String term) {
        String[] split = term.split("=");
        QueryParam param = new QueryParam();
        param.setName(split[0]);
        if(split.length > 1) param.setValue(split[1]);
        return param;
    }
}