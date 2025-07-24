package net.prizowo.vectorientation.main.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SimpleConfig {

    private static final Logger LOGGER = LogManager.getLogger("SimpleConfig");
    private final HashMap<String, String> config = new HashMap<>();
    private final ConfigRequest request;
    private Set<String> edited;
    private boolean broken = false;

    public interface DefaultConfig {
        String get( String namespace );

        static String empty( String namespace ) {
            return "";
        }
    }

    public static class ConfigRequest {

        private File file;
        private final String filename;
        private DefaultConfig provider;

        private ConfigRequest(File file, String filename ) {
            this.file = file;
            this.filename = filename;
            this.provider = DefaultConfig::empty;
        }

        /**
         * Sets the default config provider, used to generate the
         * config if it's missing.
         *
         * @param provider default config provider
         * @return current config request object
         * @see DefaultConfig
         */
        public ConfigRequest provider( DefaultConfig provider ) {
            this.provider = provider;
            return this;
        }

        /**
         * Loads the config from the filesystem.
         *
         * @return config object
         * @see SimpleConfig
         */
        public SimpleConfig request() {
            return new SimpleConfig( this );
        }

        private String getConfig() {
            return provider.get( filename ) + "\n";
        }

    }

    /**
     * Creates new config request object, ideally `namespace`
     * should be the name of the mod id of the requesting mod
     *
     * @param filename - name of the config file
     * @return new config request object
     */
    public static ConfigRequest of( String filename ) {
        Path path = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        return new ConfigRequest( path.resolve( filename + ".properties" ).toFile(), filename );
    }

    private void createConfig() throws IOException {

        // try creating missing files
        request.file.getParentFile().mkdirs();
        Files.createFile( request.file.toPath() );

        // write default config data
        PrintWriter writer = new PrintWriter(request.file, "UTF-8");
        writer.write( request.getConfig() );
        writer.close();

    }

    private void loadConfig() throws IOException {
        Scanner reader = new Scanner( request.file );
        for( int line = 1; reader.hasNextLine(); line ++ ) {
            parseConfigEntry( reader.nextLine(), line );
        }
        reader.close();
    }

    private void parseConfigEntry( String entry, int line ) {
        if( !entry.isEmpty() && !entry.startsWith( "#" ) ) {
            String[] parts = entry.split("=", 2);
            if( parts.length == 2 ) {
                config.put( parts[0], parts[1] );
            }else{
                throw new RuntimeException("Syntax error in config file on line " + line + "!");
            }
        }
    }

    private void writeConfig() throws IOException {
        Scanner reader = new Scanner( request.file );
        String newConfig = "";

        while(reader.hasNextLine()) {
            String entry = reader.nextLine();
            if(!entry.isEmpty() && !entry.startsWith("#")){
                String[] parts = entry.split("=", 2);
                if( parts.length == 2 && edited.contains(parts[0])) {
                    newConfig += (parts[0]+"="+config.get(parts[0])+"\n");
                    edited.remove(parts[0]);
                } else {
                    newConfig += (entry) + "\n";
                }
            } else {
                newConfig += (entry) + "\n";
            }
        }
        reader.close();

        request.file.delete();
        request.file.getParentFile().mkdirs();
        Files.createFile( request.file.toPath() );
        PrintWriter writer = new PrintWriter(request.file, "UTF-8");
        writer.write( newConfig );
        writer.close();
    }

    private SimpleConfig( ConfigRequest request ) {
        this.edited = new HashSet<>();
        this.request = request;
        String identifier = "Config '" + request.filename + "'";

        if( !request.file.exists() ) {
            LOGGER.info( identifier + " is missing, generating default one..." );

            try {
                createConfig();
            } catch (IOException e) {
                LOGGER.error( identifier + " failed to generate!" );
                LOGGER.trace( e );
                broken = true;
            }
        }

        if( !broken ) {
            try {
                loadConfig();
            } catch (Exception e) {
                LOGGER.error( identifier + " failed to load!" );
                LOGGER.trace( e );
                broken = true;
            }
        }

    }

    /**
     * Queries a value from config, returns `null` if the
     * key does not exist.
     *
     * @return  value corresponding to the given key
     * @see     SimpleConfig#getOrDefault
     */
    @Deprecated
    public String get( String key ) {
        return config.get( key );
    }

    public void set(String key, String value){
        config.replace(key, value);
        edited.add(key);
    }

    public void writeToFile(){
        try {
            writeConfig();
        } catch (IOException e){
            System.out.println("Could not write to config file!");
            e.printStackTrace();
        }
    }

    public void reconstructFile(){
        if (!broken) edited.addAll(config.keySet());
        try {
            Files.deleteIfExists(request.file.toPath());
            createConfig();
            writeToFile();
            loadConfig();
            broken = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns string value from config corresponding to the given
     * key, or the default string if the key is missing.
     *
     * @return  value corresponding to the given key, or the default value
     */
    public String getOrDefault( String key, String def ) {
        String val = get(key);
        if (val == null) broken = true;
        return val == null ? def : val;
    }

    /**
     * Returns integer value from config corresponding to the given
     * key, or the default integer if the key is missing or invalid.
     *
     * @return  value corresponding to the given key, or the default value
     */
    public int getOrDefault( String key, int def ) {
        try {
            return Integer.parseInt( get(key) );
        } catch (Exception e) {
            broken = true;
            return def;
        }
    }

    /**
     * Returns boolean value from config corresponding to the given
     * key, or the default boolean if the key is missing.
     *
     * @return  value corresponding to the given key, or the default value
     */
    public boolean getOrDefault( String key, boolean def ) {
        String val = get(key);
        if( val != null ) {
            return val.equalsIgnoreCase("true");
        }
        broken = true;

        return def;
    }

    /**
     * Returns double value from config corresponding to the given
     * key, or the default string if the key is missing or invalid.
     *
     * @return  value corresponding to the given key, or the default value
     */
    public double getOrDefault( String key, double def ) {
        try {
            return Double.parseDouble( get(key) );
        } catch (Exception e) {
            broken = true;
            return def;
        }
    }

    /**
     * If any error occurred during loading or reading from the config
     * a 'broken' flag is set, indicating that the config's state
     * is undefined and should be discarded using `delete()`
     *
     * @return the 'broken' flag of the configuration
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * deletes the config file from the filesystem
     *
     * @return true if the operation was successful
     */
    public boolean delete() {
        LOGGER.warn( "Config '" + request.filename + "' was removed from existence! Restart the game to regenerate it." );
        return request.file.delete();
    }

}