package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import objects.ContainerObject;
import org.apache.log4j.Logger;


import java.util.List;

public class DockerController {
    private final DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock");
    private static org.apache.log4j.Logger log = Logger.getLogger(DockerController.class);

    /**
     * TODO
     * How to add containers to SS and DB?
     * Get list of container ID's from the DB
     * For each container ID return
     * - Status
     * - IP address
     * - Last refresh
     * - Link to console/logs
     */


    public String getContainerData(String containerID) {
        // create json object for a container
        ContainerObject container = new ContainerObject();
        container.setId(containerID);
        try {
            final ContainerInfo info = docker.inspectContainer(containerID);
            info.networkSettings().ipAddress();
            List<Container> listOfContainers = docker.listContainers(DockerClient.ListContainersParam.allContainers());
            for (Container temp : listOfContainers) {
                if (temp.id().equalsIgnoreCase(containerID)) {
                    container.setName(temp.names().get(0).substring(1));
                    container.setStatus(temp.state());
                    // IP address is set for bridge - wont be when it comes to deployment
                    container.setIpAddress(temp.networkSettings().networks().get("bridge").ipAddress());
                    container.setLastReset(temp.status());
                    container.setLevelName("Level Name from DB");
                }
            }
        } catch (DockerException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }

        return new Gson().toJson(container);
    }

    public String getContainerData() {
        // Create a json object for all containers
        // Get data from DB
        // for each in resultset
        // Example execution below
        // Parser converts Json String back to Json Object to be consumed correctly (prevents \" in output)
        JsonArray data = new JsonArray();
        JsonParser parser = new JsonParser();
        data.add(parser.parse(getContainerData("058afa9b1566f31b8ebae22ec2d48476156be229c15eecbd3f175d57834d4c35")));
        data.add(parser.parse(getContainerData("058afa9b1566f31b8ebae22ec2d48476156be229c15eecbd3f175d57834d4c35")));
        return new Gson().toJson(data);
    }


    public String getContainerLogs(String containerID) {
        try {
            String logs = "";
            LogStream stream = docker.logs(containerID, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr());
            logs = stream.readFully();

            return logs;
        }
        catch (DockerException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
        return "An error occurred";
    }

    public void restartContainer(String containerID) {
        try {
            log.info("Restarting container: " + containerID);
            docker.restartContainer(containerID);
        } catch (DockerException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void startContainer(String containerID) {
        try {
            log.info("Starting container: " + containerID);
            docker.startContainer(containerID);
        } catch (DockerException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void stopContainer(String containerID) {
        try {
            log.info("Stopping container: " + containerID);
            docker.stopContainer(containerID, 10);
        } catch (DockerException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
