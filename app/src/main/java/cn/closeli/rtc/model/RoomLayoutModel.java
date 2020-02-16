package cn.closeli.rtc.model;

import java.util.ArrayList;

public class RoomLayoutModel {
    private int mode = 0;
    private String type = "";
    private ArrayList<String> layout = new ArrayList<>();
    private int moderatorIndex = -1;                //断线重连 主持人位置

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public ArrayList<String> getLayout() {
        return layout;
    }

    public void setLayout(ArrayList<String> layout) {
        this.layout = layout;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getModeratorIndex() {
        return moderatorIndex;
    }

    public void setModeratorIndex(int moderatorIndex) {
        this.moderatorIndex = moderatorIndex;
    }
}
