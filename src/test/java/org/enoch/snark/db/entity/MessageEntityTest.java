package org.enoch.snark.db.entity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

public class MessageEntityTest {

    @Test
    public void getPlanet() {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.content = text;
        PlanetEntity planet = messageEntity.getPlanet();
        System.err.println(planet);
    }

    @Test
    public void getPlanet2() {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.content = text2;
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

    private static String text2 = "" +
            "\n" +
            "<ul class=\"pagination\"><li class=\"p_li last\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"15229348\" data-tabid=\"20\">|&lt;&lt;</a></li><li class=\"p_li\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"15221987\" data-tabid=\"20\">&lt;</a></li><li class=\"p_li active\"><a class=\"fright txt_link msg_action_link active\" data-messageid=\"15221975\" data-tabid=\"20\">4/32</a></li><li class=\"p_li\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"15221964\" data-tabid=\"20\">&gt;</a></li><li class=\"p_li last\"><a class=\"fright txt_link msg_action_link\" data-messageid=\"15048186\" data-tabid=\"20\">&gt;&gt;|</a></li></ul>\n" +
            "    <div class=\"detail_msg\" data-msg-id=\"15221975\" data-message-type=\"10\">\n" +
            "        <div class=\"detail_msg_head\">\n" +
            "            <div class=\"msg_status\"></div>\n" +
            "            <span class=\"msg_title new blue_txt\">Raport szpiegowski z <a href=\"https://s158-pl.ogame.gameforge.com/game/index.php?page=galaxy&amp;galaxy=3&amp;system=407&amp;position=8\" class=\"txt_link\"><figure class=\"planetIcon planet tooltip js_hideTipOnMobile\" title=\"Planeta\"></figure>Kwintesencja [3:407:8]</a></span>\n" +
            "            <span class=\"msg_date fright\">18.04.2019 21:17:30</span>\n" +
            "            <br/>\n" +
            "            <span class=\"msg_sender_label\">Od: </span>\n" +
            "            <span class=\"msg_sender\">dowódca floty</span>\n" +
            "\n" +
            "            <!-- only if comments are allowed (Only shared reports and broadcasts have comments): -->\n" +
            "            \n" +
            "            <div class=\"msg_actions clearfix\">\n" +
            "                                        <div class=\"icon_nf_link fleft\">\n" +
            "                            <span class=\"icon_nf icon_apikey tooltipCustom tooltip-width:400 fleft\"\n" +
            "                                  title=\"Te dane można wczytać z kompatybilnego symulatora walki:<br/><input value='sr-pl-158-d2f5d0527542d83c0ef1d7b5754e0ac06d5b6400' readonly onclick='select()' style='width:360px'></input>\"></span>\n" +
            "                        </div>\n" +
            "                                            <a href=\"https://s158-pl.ogame.gameforge.com/game/index.php?page=shareReportOverlay&messageId=15221975\"\n" +
            "                       data-overlay-title=\"udostępnij wiadomość\" title='udostępnij wiadomość'\n" +
            "                       class=\"icon_nf_link fleft overlay tooltip js_hideTipOnMobile\"\n" +
            "                    >\n" +
            "                        <span class=\"icon_nf icon_share tooltip js_hideTipOnMobile\"\n" +
            "                              title='udostępnij wiadomość'></span>\n" +
            "                    </a>\n" +
            "\n" +
            "                                        <a href=\"https://s158-pl.ogame.gameforge.com/game/index.php?page=fleet1&galaxy=3&system=407&position=8&type=1&mission=1\" class=\"icon_nf_link fleft\">\n" +
            "                        <span class=\"icon_nf icon_attack tooltip js_hideTipOnMobile\" title='Atakuj'>\n" +
            "                                                    </span>\n" +
            "                    </a>\n" +
            "                                        <a href=\"#\" onClick=\"sendShipsWithPopup(6,3,407,8,1,1); return false;\" class=\"icon_nf_link fleft\">\n" +
            "                        <span class=\"icon_nf icon_espionage tooltip js_hideTipOnMobile\"\n" +
            "                              title='Szpieguj'>\n" +
            "                                                    </span>\n" +
            "                    </a>\n" +
            "                                        <a href=\"javascript:void(0);\" class=\"icon_nf_link fright\">\n" +
            "                        <span class=\"icon_nf icon_refuse js_actionKillDetail tooltip js_hideTipOnMobile\"\n" +
            "                              title='usuń'></span>\n" +
            "                    </a>\n" +
            "                                </div>\n" +
            "        </div>\n" +
            "                    <div class=\"detail_msg_ctn\">\n" +
            "                \n" +
            "<div class=\"detail_txt\"><span>Gracz<span class=\"status_abbr_inactive\">&nbsp;&nbsp;SigmaPLK</span><span class=\"status_abbr_inactive\">(<span class='status_abbr_inactive'><span class=\"status_abbr_inactive tooltip js_hideTipOnMobile\" title=\"7 dni nieaktywności\">i</span></span>)</span></div>\n" +
            "<div class=\"detail_txt\">\n" +
            "    Szansa na przechwycenie sond: 0 %    <div class=\"\">\n" +
            "        Skan planety nie wykazał żadnych zaburzeń atmosfery. Istnieje duże prawdopodobieństwo, że w ciągu ostatniej godziny nie było aktywności na tej planecie.    </div>\n" +
            "</div>\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Surowce</span>\n" +
            "</div>\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"resources\">\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"405.433\">\n" +
            "        <div class=\"resourceIcon metal\"></div> \n" +
            "        <span class=\"res_value\">405.433</span>\n" +
            "    </li>\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"125.105\">\n" +
            "        <div class=\"resourceIcon crystal\"></div> \n" +
            "        <span class=\"res_value\">125.105</span>\n" +
            "    </li>\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"48.114\">\n" +
            "        <div class=\"resourceIcon deuterium\"></div>\n" +
            "        <span class=\"res_value\">48.114</span>\n" +
            "    </li>\n" +
            "    <li class=\"resource_list_el tooltipCustom\" title=\"2.324\">\n" +
            "        <div class=\"resourceIcon energy\"></div>\n" +
            "        <span class=\"res_value\">2.324</span>\n" +
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
            "    </ul>\n" +
            "\n" +
            "\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Obrona</span>\n" +
            "</div>\n" +
            "\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"defense\">\n" +
            "        <li class=\"detail_list_el\">\n" +
            "                       <div class=\"defense_image float_left\">\n" +
            "                <img class=\"defense401\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Wyrzutnia rakiet</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">76</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"defense_image float_left\">\n" +
            "                <img class=\"defense402\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Lekkie działo laserowe</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">260</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"defense_image float_left\">\n" +
            "                <img class=\"defense403\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Ciężkie działo laserowe</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">5</span>\n" +
            "        </li>\n" +
            "    </ul>   \n" +
            "\n" +
            "\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Budynek</span>\n" +
            "</div>\n" +
            "\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"buildings\">\n" +
            "        <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building1\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Kopalnia metalu</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">30</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building2\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Kopalnia kryształu</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">25</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building3\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Ekstraktor deuteru</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">20</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building4\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Elektrownia słoneczna</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">19</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building14\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Fabryka robotów</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">10</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building15\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Fabryka nanitów</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">1</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building21\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Stocznia</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">8</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building22\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Magazyn metalu</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">11</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building23\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Magazyn kryształu</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">9</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building24\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Zbiornik deuteru</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">8</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building31\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Laboratorium badawcze</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">10</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"building_image float_left\">\n" +
            "                <img class=\"building44\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                        <span class=\"detail_list_txt\">Silos rakietowy</span>\n" +
            "            <span class=\"fright\" style=\"margin-right: 10px;\">6</span>\n" +
            "        </li>\n" +
            "    </ul>\n" +
            "\n" +
            "<div class=\"section_title\">\n" +
            "    <div class=\"c-left\"></div>\n" +
            "    <div class=\"c-right\"></div>\n" +
            "    <span class=\"title_txt\">Badania</span>\n" +
            "</div>\n" +
            "\n" +
            "<ul class=\"detail_list clearfix\" data-type=\"research\">\n" +
            "        <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research106\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia szpiegowska</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">10</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research108\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia komputerowa</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">10</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research109\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia bojowa</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">12</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research110\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia ochronna</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">10</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research111\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Opancerzenie</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">12</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research113\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia energetyczna</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">8</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research114\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia nadprzestrzenna</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">8</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research115\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Napęd spalinowy</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">6</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research117\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Napęd impulsowy</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">4</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research118\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Napęd nadprzestrzenny</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">5</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research120\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia laserowa</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">12</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research121\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia jonowa</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">5</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research122\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Technologia plazmowa</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">7</span>\n" +
            "        </li>\n" +
            "            <li class=\"detail_list_el\">\n" +
            "                       <div class=\"research_image float_left\">\n" +
            "                <img class=\"research124\" width=\"28\" height=\"28\" src=\"https://gf2.geo.gfsrv.net/cdndf/3e567d6f16d040326c7a0ea29a4f41.gif\">\n" +
            "            </div>\n" +
            "                                        <span class=\"detail_list_txt\">Astrofizyka</span>\n" +
            "                        <span class=\"fright\" style=\"margin-right: 10px;\">8</span>\n" +
            "        </li>\n" +
            "    </ul>\n" +
            "\n" +
            "                                <br class=\"clearfloat\">\n" +
            "                <div class=\"section_title\">\n" +
            "                    <div class=\"c-left\"></div>\n" +
            "                    <div class=\"c-right\"></div>\n" +
            "                    <span class=\"title_txt\">Komentarze</span>\n" +
            "                </div>\n" +
            "\n" +
            "                <ul class=\"tab_inner ctn_with_new_msg clearfix\">\n" +
            "                    <li class=\"msg\">\n" +
            "                        <form id=\"newCommentForm\" class=\"clearfix\" action=\"index.php?page=messages\" method=\"POST\">\n" +
            "                            <input type=\"hidden\" name=\"action\" value=\"108\">\n" +
            "                            <input type=\"hidden\" name=\"messageId\" value=\"15221975\">\n" +
            "                            <link rel=\"stylesheet\" href=\"/cdn/css/select2.css\" type=\"text/css\">\n" +
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
            "    ";
}