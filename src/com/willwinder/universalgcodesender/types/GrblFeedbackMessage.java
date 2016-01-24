package com.willwinder.universalgcodesender.types;

/**
 * Created by Phil on 1/15/2016.
 */
public class GrblFeedbackMessage {
    final String message;

    String distanceMode = null;
    String units = null;

    public GrblFeedbackMessage(String message) {
        this.message = message;
        parse();
    }

    private void parse() {
        String substring = message.substring(1, message.length() - 1);

        String[] parts = substring.split(" ");
        for (String part : parts) {
            switch(part) {
                case "G90":
                case "G91":
                    distanceMode = part;
                    break;
                case "G20":
                case "G21":
                    units = part;
                    break;
                default:
                    //ignore
            }
        }
    }

    public String getDistanceMode() {
        return distanceMode;
    }

    public String getUnits() {
        return units;
    }

    @Override
    public String toString() {
        return "GrblFeedbackMessage{" +
                "message='" + message + '\'' +
                ", distanceMode='" + distanceMode + '\'' +
                ", units='" + units + '\'' +
                '}';
    }
}
