package cn.closeli.rtc.model.http;

public class UpgradeResp {
    private long versionCode;

    private String versionName;

    private String upgradeType;

    private String downloadUrl;

    public long getVersionCode() { return versionCode; }

    public void setVersionCode(long versionCode) { this.versionCode = versionCode; }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) { this.versionName = versionName; }

    public String getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(String upgradeType) {
        this.upgradeType = upgradeType;
    }

    public String getDownloadUrl() { return downloadUrl; }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "UpgradeResp{" +
                "versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", upgradeType='" + upgradeType + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
