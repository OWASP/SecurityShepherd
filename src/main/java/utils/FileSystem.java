package utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Class is used to interact with the Filesystem
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>.
 * @author ismisepaul
 *
 */

public class FileSystem {

    private static org.apache.log4j.Logger log = Logger.getLogger(FileSystem.class);

    /**
     * Creates a file on the filesystem
     * @param filename the file to be created
     */
    public static void createFile(String filename) throws IOException{
        File file = new File(filename);
        file.createNewFile();
    }

    /**
     * Writes content to a file on the filesystem
     * @param filename the file to be written to
     * @param data the data to write to the file
     */
    public static void writeFile(String filename, String data) throws IOException{

        FileWriter writer;
        writer = new FileWriter(filename);
        writer.write(data);
        writer.close();
    }

    /**
     * Read a properties file and return the string result from the property specified
     * @param filename the file to be written to
     * @param property the property value to return
     */
    public static String readPropertiesFile(String filename, String property) throws IOException{

        InputStream input = FileSystem.class.getClassLoader().getResourceAsStream(filename);
        Properties prop = new Properties();
        prop.load(input);

        return prop.getProperty(property);
    }
}
