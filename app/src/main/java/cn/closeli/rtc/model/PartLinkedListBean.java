package cn.closeli.rtc.model;

public class PartLinkedListBean {
    private String connectionId = "";        //需要注意 取得时候可能需要 判断 streamType
    private String streamType = "";          //客户端为双屏显示，且该类型是sharing时，忽略该排序
    private int left = 0;
    private int top = 0;
    private int width = 0;
    private int height = 0;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
