package org.enoch.snark.gi.types;

import org.enoch.snark.common.HashDecoder;
import org.enoch.snark.gi.GI;
import org.enoch.snark.instance.Instance;

import java.util.HashMap;

import static org.enoch.snark.instance.si.module.ConfigMap.HASH;
import static org.enoch.snark.instance.si.module.ConfigMap.URL;

public class UrlBuilder {

    static final String PAGE_TERM = "page=";
    static final String COMPONENT_TERM = "component=";

    static final String CP_PARAM = "cp";
    static final String GALAXY_PARAM = "galaxy";
    static final String SYSTEM_PARAM = "system";
    static final String POSITION_PARAM = "position";
    static final String TYPE_PARAM = "type";
    static final String MISSION_PARAM = "mission";
    static final String SITE_PARAM = "site";

    private final UrlPage page;
    private UrlComponent component;
    private final HashMap<String,Object> params = new HashMap<>();

    UrlBuilder(UrlPage page) {
        this.page = page;
    }

    UrlBuilder(UrlComponent component) {
        this.page = UrlPage.INGAME;
        this.component = component;
    }

    UrlBuilder param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    String get() {
        StringBuilder builder = new StringBuilder(getLink());

        if (page == null) throw new RuntimeException("Missing term to build url");
        builder.append(PAGE_TERM).append(page.name().toLowerCase());

        if (component != null) builder.append("&").append(COMPONENT_TERM).append(component.name().toLowerCase());

        params.forEach((name, value) -> {
            if (value != null) builder.append("&").append(name).append("=").append(value);
        });

        GI.getInstance().getWebDriver().get(builder.toString());
        return builder.toString();
    }

    private static String getLink() {
        String config = Instance.getGlobalMainConfigMap().getConfig(URL, null);
        if(config == null ) {
            String hash = Instance.getGlobalMainConfigMap().getConfig(HASH);
            config = HashDecoder.parseLink(hash);
        }
        return config + "?";
    }

}
