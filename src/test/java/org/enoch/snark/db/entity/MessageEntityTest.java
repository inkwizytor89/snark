package org.enoch.snark.db.entity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

public class MessageEntityTest {

    @Test
    public void getPlanet() {
//        Document doc = Jsoup.parse(text);
//        Elements title = doc.getElementsByClass("msg_title new blue_txt");
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.content = text;
        PlanetEntity planet = messageEntity.getPlanet();
        System.err.println(planet);
    }


    public static String text = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body><ul class=\"pagination\"><li class=\"p_li last\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"12355401\" data-tabid=\"20\">|&lt;&lt;</a></li><li class=\"p_li\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"12355401\" data-tabid=\"20\">&lt;</a></li><li class=\"p_li active\"><a class=\"fright txt_link msg_action_link active\" data-messageid=\"12355383\" data-tabid=\"20\">2/218</a></li><li class=\"p_li\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"12355363\" data-tabid=\"20\">&gt;</a></li><li class=\"p_li last\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"12156895\" data-tabid=\"20\">&gt;&gt;|</a></li></ul>\n" +
            "    <div class=\"detail_msg\" data-msg-id=\"12355383\" data-message-type=\"10\">\n" +
            "        <div class=\"detail_msg_head\">\n" +
            "            <div class=\"msg_status\"></div>\n" +
            "            <span class=\"msg_title new blue_txt\">Raport szpiegowski z <a href=\"https://s158-pl.ogame.gameforge.com/game/index.php?page=galaxy&amp;galaxy=2&amp;system=257&amp;position=13\" class=\"txt_link\"><figure class=\"planetIcon planet tooltip js_hideTipOnMobile\" title=\"Planeta\"></figure>Hella [2:257:13]</a></span>\n" +
            "            <span class=\"msg_date fright\">25.03.2019 14:05:16</span>\n" +
            "            <br />\n" +
            "            <span class=\"msg_sender_label\">Od: </span>\n" +
            "            <span class=\"msg_sender\">dowódca floty</span>\n" +
            "\n" +
            "            <!-- only if comments are allowed (Only shared reports and broadcasts have comments): -->\n" +
            "            \n" +
            "            <div class=\"msg_actions clearfix\">\n" +
            "                                        <div class=\"icon_nf_link fleft\">\n" +
            "                            <span class=\"icon_nf icon_apikey tooltipCustom tooltip-width:400 fleft\" title=\"Te dane można wczytać z kompatybilnego symulatora walki:&lt;br/&gt;&lt;input value='sr-pl-158-f0bdfc50c3a3489a16baa5c039a5cf808ab59c6c' readonly onclick='select()' style='width:360px'&gt;&lt;/input&gt;\"></span>\n" +
            "                        </div>\n" +
            "                                            <a href=\"https://s158-pl.ogame.gameforge.com/game/index.php?page=shareReportOverlay&amp;messageId=12355383\" data-overlay-title=\"udostępnij wiadomość\" title=\"udostępnij wiadomość\" class=\"icon_nf_link fleft overlay tooltip js_hideTipOnMobile\">\n" +
            "                        <span class=\"icon_nf icon_share tooltip js_hideTipOnMobile\" title=\"udostępnij wiadomość\"></span>\n" +
            "                    </a>\n" +
            "\n" +
            "                                        <a href=\"https://s158-pl.ogame.gameforge.com/game/index.php?page=fleet1&amp;galaxy=2&amp;system=257&amp;position=13&amp;type=1&amp;mission=1\" class=\"icon_nf_link fleft\">\n" +
            "                        <span class=\"icon_nf icon_attack tooltip js_hideTipOnMobile\" title=\"Atakuj\">\n" +
            "                                                    </span>\n" +
            "                    </a>\n" +
            "                                        <a href=\"#\" onclick=\"sendShipsWithPopup(6,2,257,13,1,1); return false;\" class=\"icon_nf_link fleft\">\n" +
            "                        <span class=\"icon_nf icon_espionage tooltip js_hideTipOnMobile\" title=\"Szpieguj\">\n" +
            "                                                    </span>\n" +
            "                    </a>\n" +
            "                                        <a href=\"javascript:void(0);\" class=\"icon_nf_link fright\">\n" +
            "                        <span class=\"icon_nf icon_refuse js_actionKillDetail tooltip js_hideTipOnMobile\" title=\"usuń\"></span>\n" +
            "                    </a>\n" +
            "                                </div>\n" +
            "        </div>\n" +
            "                    <div class=\"detail_msg_ctn\">\n" +
            "                \n" +
            "<div class=\"detail_txt\"><span>Gracz<span class=\"status_abbr_longinactive\">  Pandora</span><span class=\"status_abbr_inactive\">(<span class=\"status_abbr_inactive\"><span class=\"status_abbr_inactive tooltip js_hideTipOnMobile\" title=\"7 dni nieaktywności\">i</span></span>)</span></span></div>\n" +
            "<div class=\"detail_txt\">\n" +
            "    Szansa na przechwycenie sond: 0 %    <div class=\"\">\n" +
            "        Skan planety wykazał zaburzenia atmosfery, co sugeruje aktywność w ciągu ostatnich <font color=\"red\">15</font> minut.    </div>\n" +
            "</div>\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Surowce</span>\n" +
            "</div>\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"resources\">\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"129.750\">\n" +
            "        <div class=\"resourceIcon metal\"></div> \n" +
            "        <span class=\"res_value\">129.750</span>\n" +
            "    </li>\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"73.110\">\n" +
            "        <div class=\"resourceIcon crystal\"></div> \n" +
            "        <span class=\"res_value\">73.110</span>\n" +
            "    </li>\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"38.081\">\n" +
            "        <div class=\"resourceIcon deuterium\"></div>\n" +
            "        <span class=\"res_value\">38.081</span>\n" +
            "    </li>\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"5.472\">\n" +
            "        <div class=\"resourceIcon energy\"></div>\n" +
            "        <span class=\"res_value\">5.472</span>\n" +
            "    </li>\n" +
            "</ul>\n" +
            "\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Floty</span>\n" +
            "</div>\n" +
            "\n" +
            "    <ul class=\"detail_list clearfix\" data-type=\"ships\">\n" +
            "            <li class=\"detail_list_fail\">Nie stwierdziliśmy żadnych wiarygodnych danych do tego typu przy skanowaniu.</li>\n" +
            "    </ul>\n" +
            "\n" +
            "    <div class=\"section_title\">\n" +
            "        <div class=\"c-left\"></div>\n" +
            "        <div class=\"c-right\"></div>\n" +
            "        <span class=\"title_txt\">Bieżące zlecenia napraw</span>\n" +
            "    </div>\n" +
            "\n" +
            "    <ul class=\"detail_list clearfix\" data-type=\"repairorders\">\n" +
            "            <li class=\"detail_list_fail\">Nie stwierdziliśmy żadnych wiarygodnych danych do tego typu przy skanowaniu.</li>\n" +
            "        </ul>\n" +
            "\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Obrona</span>\n" +
            "</div>\n" +
            "\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"defense\">\n" +
            "    <li class=\"detail_list_fail\">Nie stwierdziliśmy żadnych wiarygodnych danych do tego typu przy skanowaniu.</li>\n" +
            "</ul>   \n" +
            "\n" +
            "\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Budynek</span>\n" +
            "</div>\n" +
            "\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"buildings\">\n" +
            "    <li class=\"detail_list_fail\">Nie stwierdziliśmy żadnych wiarygodnych danych do tego typu przy skanowaniu.</li>\n" +
            "</ul>\n" +
            "\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Badania</span>\n" +
            "</div>\n" +
            "\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"research\">\n" +
            "    <li class=\"detail_list_fail\">Nie stwierdziliśmy żadnych wiarygodnych danych do tego typu przy skanowaniu.</li>\n" +
            "</ul>\n" +
            "\n" +
            "                                <br class=\"clearfloat\" />\n" +
            "                <div class=\"section_title\">\n" +
            "                    <div class=\"c-left\"></div>\n" +
            "                    <div class=\"c-right\"></div>\n" +
            "                    <span class=\"title_txt\">Komentarze</span>\n" +
            "                </div>\n" +
            "\n" +
            "                <ul class=\"tab_inner ctn_with_new_msg clearfix\">\n" +
            "                    <li class=\"msg\">\n" +
            "                        <form id=\"newCommentForm\" class=\"clearfix\" action=\"index.php?page=messages\" method=\"POST\">\n" +
            "                            <input type=\"hidden\" name=\"action\" value=\"108\" />\n" +
            "                            <input type=\"hidden\" name=\"messageId\" value=\"12355383\" />\n" +
            "                            <link rel=\"stylesheet\" href=\"/cdn/css/select2.css\" type=\"text/css\" />\n" +
            "<div>\n" +
            "    <button class=\"btn_blue js_send_comment fright ally_send_button\" onclick=\"return false;\">Wyślij</button>\n" +
            "    <div class=\"editor_wrap\">\n" +
            "   \t\t<textarea name=\"text\" class=\"new_msg_textarea\"></textarea>\n" +
            "    </div>\n" +
            "</div>\n" +
            "<script language=\"javascript\">\n" +
            "initBBCodeEditor(locaKeys, itemNames, false, '.new_msg_textarea', 2000, true);\n" +
            "</script>\n" +
            "                        </form>\n" +
            "                    </li>\n" +
            "                                    </ul>\n" +
            "                <script language=\"javascript\">\n" +
            "                    ogame.messages.initCommentForm();\n" +
            "                </script>\n" +
            "            </div>\n" +
            "                </div>\n" +
            "    <script type=\"text/javascript\">\n" +
            "                        ogame.messages.initDetailMessages(true);\n" +
            "                ogame.messagecounter.resetCounterByType(ogame.messagecounter.type_message);\n" +
            "                    var elem, messageId, senderId, receiverId, associationId;\n" +
            "        function reportMessageConfirmation(_elem, _messageId, _senderId, _receiverId, _question)\n" +
            "        {\n" +
            "            elem       = _elem;\n" +
            "            messageId  = _messageId;\n" +
            "            senderId   = _senderId;\n" +
            "            receiverId = _receiverId;\n" +
            "\n" +
            "            errorBoxDecision(\n" +
            "                \"Uwaga\",\n" +
            "                _question,\n" +
            "                \"Tak\",\n" +
            "                \"Nie\",\n" +
            "                reportMessageCallback\n" +
            "            );\n" +
            "        }\n" +
            "\n" +
            "        function reportMessageCallback()\n" +
            "        {\n" +
            "            elem.hide();\n" +
            "            reportMessage(messageId, senderId, receiverId);\n" +
            "        }\n" +
            "    </script>\n" +
            "    </body></html>";
}