package org.enoch.snark.gi;


import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.technology.Technology;
import org.enoch.snark.instance.model.uc.TechnologyUC;
import org.enoch.snark.instance.service.TechnologyService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TechnologyGIR extends GraphicalInterfaceReader {

    public Long updateQueue(ColonyEntity colony, String queueType) {
        List<WebElement> elements = wd.findElements(By.id(queueType));
        if(elements.isEmpty()) {
            return null;
        }
        WebElement queueElement = elements.get(0);
        List<WebElement> dataDetails = queueElement.findElements(By.className("data"));
        if (dataDetails.isEmpty()) {
            TechnologyService.getInstance().clean(colony, queueType);
        } else {
            String durationInput = queueElement.findElement(By.tagName("time")).getAttribute("datetime");
            long durationSeconds = Duration.parse(durationInput).getSeconds();
            LocalDateTime endTime = LocalDateTime.now().plusSeconds(durationSeconds);

            String textWithBuildingEnum = dataDetails.get(0).findElement(By.tagName("a")).getAttribute("onclick");
            Long technologyId = extractBuildingEnumId(textWithBuildingEnum);
            Technology technology = TechnologyUC.parse(technologyId);
            TechnologyService.getInstance().update(colony, queueType, endTime, technology);
            return durationSeconds;
        }
        return null;
    }

    private Long extractBuildingEnumId(String input) {
        Pattern firstNumberPattern = Pattern.compile("\\D+(\\d+).+");
        Matcher m = firstNumberPattern.matcher(input);
        if (m.find()) {
            long id = Long.parseLong(m.group(1));
            return id;
        }
        return null;
    }

}
