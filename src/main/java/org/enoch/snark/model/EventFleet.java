package org.enoch.snark.model;

public class EventFleet {
    public String countDown;
    public String arrivalTime;
    public String missionFleet;
    public String originFleet;
    public String coordsOrigin;
    public String detailsFleet;
    public String iconMovement;
    public String destFleet;
    public String destCoords;
    public String sendProbe;
    public String sendMail;
    public boolean isForeign = false;

    @Override
    public String toString() {
        return "EventFleet{" +
                " isForeign=" + isForeign +
                ", countDown='" + countDown + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", missionFleet='" + missionFleet + '\'' +
                ", originFleet='" + originFleet + '\'' +
                ", coordsOrigin='" + coordsOrigin + '\'' +
                ", detailsFleet='" + detailsFleet + '\'' +
                ", iconMovement='" + iconMovement + '\'' +
                ", destFleet='" + destFleet + '\'' +
                ", destCoords='" + destCoords + '\'' +
                ", sendProbe='" + sendProbe + '\'' +
                ", sendMail='" + sendMail + '\'' +
                '}';
    }
}
