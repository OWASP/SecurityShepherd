package objects;

public class ContainerObject {
    private String name;
    private String id;
    private String ipAddress;
    private String levelName;
    private String status;
    private String lastReset;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastReset() {
        return lastReset;
    }

    public void setLastReset(String lastReset) {
        this.lastReset = lastReset;
    }

}
