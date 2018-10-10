package SnoopDogServer;

import java.net.URL;

final public class ResourceLoader {

    public static URL load(String path) {
        URL resourceURL =  ResourceLoader.class.getResource(path);
        if (resourceURL == null) {
             resourceURL = ResourceLoader.class.getResource("/" + path);
        }
        return resourceURL;
    }

}
