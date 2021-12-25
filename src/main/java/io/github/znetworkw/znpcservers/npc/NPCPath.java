package io.github.znetworkw.znpcservers.npc;

import io.github.znetworkw.znpcservers.ServersNPC;
import io.github.znetworkw.znpcservers.configuration.Configuration;
import io.github.znetworkw.znpcservers.configuration.ConfigurationValue;
import io.github.znetworkw.znpcservers.user.ZUser;
import io.github.znetworkw.znpcservers.utility.location.ZLocation;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface NPCPath {
    /**
     * Reads all the path attributes.
     *
     * @throws IOException If an I/O error occurs.
     */
    void initialize(DataInputStream dataInputStream) throws IOException;

    /**
     * Writes all the path attributes.
     *
     * @throws IOException If an I/O error occurs.
     */
    void write(DataOutputStream dataOutputStream) throws IOException;

    /**
     * Initializes the task for the path.
     */
    void start();

    /**
     * Returns a new path handler for the specified npc.
     *
     * @param npc The npc.
     * @return A new path the current type.
     */
    PathInitializer getPath(NPC npc);

    interface PathInitializer {
        /**
         * Handles the path for the npc.
         */
        void handle();

        /**
         * Returns the current path location for the npc.
         *
         * @return The current path location for the npc.
         */
        ZLocation getLocation();

        /**
         * An abstract implementation of a {@link PathInitializer}.
         */
        abstract class AbstractPath implements PathInitializer {
            /**
             * The npc in which the path will be handled.
             */
            private final NPC npc;

            /**
             * The path type.
             */
            private final AbstractTypeWriter typeWriter;

            /**
             * The current path location.
             */
            private ZLocation location;

            /**
             * Creates a new path handler for an npc.
             *
             * @param npc The npc.
             * @param typeWriter The path type.
             */
            public AbstractPath(NPC npc,
                                AbstractTypeWriter typeWriter) {
                this.npc = npc;
                this.typeWriter = typeWriter;
            }

            /**
             * Returns the npc in which the path will be handled.
             *
             * @return The npc in which the path will be handled.
             */
            public NPC getNpc() {
                return npc;
            }

            /**
             * Returns the path type.
             *
             * @return The path type.
             */
            public AbstractTypeWriter getPath() {
                return typeWriter;
            }

            /**
             * Sets the new path location.
             *
             * @param location The location to set.
             */
            public void setLocation(ZLocation location) {
                this.location = location;
            }

            @Override
            public ZLocation getLocation() {
                return location;
            }
        }
    }

    class ZNPCPathDelegator {
        /**
         * The path file.
         */
        private final File file;

        /**
         * Creates a new delegator for a path file.
         *
         * @param file The path file.
         */
        protected ZNPCPathDelegator(File file) {
            this.file = file;
        }

        /**
         * Returns an output stream to write a path.
         *
         * @throws IOException If an I/O error occurs.
         */
        public DataOutputStream getOutputStream() throws IOException {
            return new DataOutputStream(new FileOutputStream(file));
        }

        /**
         * Returns an input stream to read a path.
         *
         * @throws IOException If an I/O error occurs.
         */
        public DataInputStream getInputStream() throws IOException {
            return new DataInputStream(new FileInputStream(file));
        }

        /**
         * Resolves a path delegator for the path file.
         *
         * @param file The path file.
         * @return A path delegator for the path file.
         */
        public static ZNPCPathDelegator forFile(File file) {
            return new ZNPCPathDelegator(file);
        }

        /**
         * Resolves a path delegator for the path.
         *
         * @param pathAbstract The path file.
         * @return A path delegator for the path.
         */
        public static ZNPCPathDelegator forPath(AbstractTypeWriter pathAbstract) {
            return new ZNPCPathDelegator(pathAbstract.getFile());
        }
    }

    /**
     * An abstract implementation of a {@link NPCPath}
     */
    abstract class AbstractTypeWriter implements NPCPath {
        /**
         * The class logger.
         */
        private static final Logger LOGGER = Logger.getLogger(AbstractTypeWriter.class.getName());

        /**
         * A map for storing & identifying a path by its name.
         */
        private static final ConcurrentMap<String, AbstractTypeWriter> PATH_TYPES = new ConcurrentHashMap<>();

        /**
         * Represents how often the locations will be saved.
         */
        private static final int PATH_DELAY = 1;

        /**
         * The path type.
         */
        private final TypeWriter typeWriter;

        /**
         * The path file.
         */
        private final File file;

        /**
         * The recorded path locations.
         */
        private final List<ZLocation> locationList;

        /**
         * Creates a new type path for the given path file.
         *
         * @param file The path File.
         */
        public AbstractTypeWriter(TypeWriter typeWriter, File file) {
            this.typeWriter = typeWriter;
            this.file = file;
            this.locationList = new ArrayList<>();
        }

        /**
         * Creates a new type path for the given path name.
         *
         * @param typeWriter The path type.
         * @param pathName The path name.
         */
        public AbstractTypeWriter(TypeWriter typeWriter, String pathName) {
            this(typeWriter, new File(ServersNPC.PATH_FOLDER, pathName + ".path"));
        }

        /**
         * Registers the path and load it.
         */
        public void load() {
            try (DataInputStream reader = ZNPCPathDelegator.forFile(file).getInputStream()) {
                initialize(reader);
                // register path..
                register(this);
            } catch (IOException e) {
                // the path could not be initialized...
                LOGGER.log(Level.WARNING, String.format("The path %s could not be loaded", file.getName()));
            }
        }

        /**
         * Writes the path attributes.
         */
        public void write() {
            try (DataOutputStream writer = ZNPCPathDelegator.forFile(getFile()).getOutputStream()) {
                write(writer);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, String.format("Path %s could not be created", getName()), e);
            }
        }

        public static AbstractTypeWriter forCreation(String pathName, ZUser user, TypeWriter typeWriter) {
            if (typeWriter == TypeWriter.MOVEMENT) {
                return new TypeMovement(pathName, user);
            } else {
                throw new IllegalStateException("can't find type writer for: " + typeWriter.name());
            }
        }

        public static AbstractTypeWriter forFile(File file, TypeWriter typeWriter) {
            if (typeWriter == TypeWriter.MOVEMENT) {
                return new TypeMovement(file);
            } else {
                throw new IllegalStateException("can't find type writer for: " + typeWriter.name());
            }
        }

        /**
         * Returns the path file.
         *
         * @return The path file
         */
        public File getFile() {
            return file;
        }

        /**
         * Returns the saved path locations.
         *
         * @return The saved path locations.
         */
        public List<ZLocation> getLocationList() {
            return locationList;
        }

        /**
         * Returns the path name.
         *
         * @return The path name.
         */
        public String getName() {
            return file.getName().substring(0, file.getName().lastIndexOf('.'));
        }

        /**
         * Registers a new path.
         */
        public static void register(AbstractTypeWriter abstractZNPCPath) {
            PATH_TYPES.put(abstractZNPCPath.getName(), abstractZNPCPath);
        }

        /**
         * Locates a path by its name.
         *
         * @param name The path name.
         * @return The path or {@code null} if no path was found.
         */
        public static AbstractTypeWriter find(String name) {
            return PATH_TYPES.get(name);
        }

        /**
         * A collection of all registered paths.
         *
         * @return A collection of all registered paths.
         */
        public static Collection<AbstractTypeWriter> getPaths() {
            return PATH_TYPES.values();
        }

        public enum TypeWriter {
            MOVEMENT
        }

        private static class TypeMovement extends AbstractTypeWriter {
            /**
             * The maximum locations that the path can have.
             */
            private static final int MAX_LOCATIONS = Configuration.CONFIGURATION.getValue(ConfigurationValue.MAX_PATH_LOCATIONS);

            /**
             * The player who is creating the path.
             */
            private ZUser npcUser;

            /**
             * The path task.
             */
            private BukkitTask bukkitTask;

            /**
             * Creates a new path for the given file.
             *
             * @param file The file.
             */
            public TypeMovement(File file) {
                super(TypeWriter.MOVEMENT, file);
            }

            /**
             * Creates a new type path for the given file name.
             *
             * @param fileName The file name.
             * @param npcUser  The player that is creating the path.
             */
            public TypeMovement(String fileName,
                                ZUser npcUser) {
                super(TypeWriter.MOVEMENT, fileName);
                this.npcUser = npcUser;
                // start path creation task
                start();
            }

            @Override
            public void initialize(DataInputStream dataInputStream) throws IOException {
                while (dataInputStream.available() > 0) {
                    String worldName = dataInputStream.readUTF();
                    double x = dataInputStream.readDouble();
                    double y = dataInputStream.readDouble();
                    double z = dataInputStream.readDouble();
                    float yaw = dataInputStream.readFloat();
                    float pitch = dataInputStream.readFloat();
                    // add path location
                    getLocationList().add(new ZLocation(worldName, x, y, z, yaw, pitch));
                }
            }

            @Override
            public void write(DataOutputStream dataOutputStream) throws IOException {
                if (getLocationList().isEmpty()) {
                    return;
                }
                Iterator<ZLocation> locationIterator = getLocationList().iterator();
                while (locationIterator.hasNext()) {
                    final ZLocation location = locationIterator.next();
                    // location world name
                    dataOutputStream.writeUTF(location.getWorldName());
                    // location x,y,z,yaw,pitch
                    dataOutputStream.writeDouble(location.getX());
                    dataOutputStream.writeDouble(location.getY());
                    dataOutputStream.writeDouble(location.getZ());
                    dataOutputStream.writeFloat(location.getYaw());
                    dataOutputStream.writeFloat(location.getPitch());

                    if (!locationIterator.hasNext()) {
                        // register the path...
                        register(this);
                    }
                }
            }

            @Override
            public void start() {
                npcUser.setHasPath(true);
                // start creation task for path
                bukkitTask = ServersNPC.SCHEDULER.runTaskTimerAsynchronously(() -> {
                    // check if the player who is creating the path is online and
                    // the current saved path locations haven't exceed the limit
                    if (npcUser.toPlayer() != null && npcUser.isHasPath() && MAX_LOCATIONS > getLocationList().size()) {
                        final Location location = npcUser.toPlayer().getLocation();
                        // check if location is valid
                        if (isValid(location)) {
                            // add new location to path ..
                            getLocationList().add(new ZLocation(location));
                        }
                    } else {
                        bukkitTask.cancel();
                        // set user creation path to none
                        npcUser.setHasPath(false);
                        write();
                    }
                }, PATH_DELAY, PATH_DELAY);
            }

            @Override
            public MovementPath getPath(NPC npc) {
                return new MovementPath(npc, this);
            }

            /**
             * Returns {@code true} if the location can be added to the path.
             *
             * @param location The location to add.
             * @return {@code true} If location can be added.
             */
            protected boolean isValid(Location location) {
                if (getLocationList().isEmpty()) {
                    return true;
                }

                ZLocation last = getLocationList().get(getLocationList().size() - 1);

                double xDiff = Math.abs(last.getX() - location.getX());
                double yDiff = Math.abs(last.getY() - location.getY());
                double zDiff = Math.abs(last.getZ() - location.getZ());

                return (xDiff + yDiff + zDiff) > 0.01;
            }

            protected static class MovementPath extends PathInitializer.AbstractPath {
                /**
                 * The current path location index.
                 */
                private int currentEntryPath = 0;

                /**
                 * Determines if the path is running backwards or forwards.
                 */
                private boolean pathReverse = false;

                /**
                 * Creates a new path for an npc.
                 *
                 * @param npc The npc that will be handled.
                 * @param path The path that will handle the npc.
                 */
                public MovementPath(NPC npc,
                                    TypeMovement path) {
                    super(npc, path);
                }

                @Override
                public void handle() {
                    updatePathLocation(getPath().getLocationList().get(currentEntryPath = getNextLocation()));
                    int nextIndex = getNextLocation();
                    if (nextIndex < 1)  {
                        pathReverse = false;
                    } else if (nextIndex >= getPath().getLocationList().size() - 1) {
                        pathReverse = true;
                    }
                }

                private int getNextLocation() {
                    return pathReverse ? currentEntryPath - 1 : currentEntryPath + 1;
                }

                /**
                 * Updates the new npc location according to current path index.
                 *
                 * @param location The npc path location.
                 */
                protected void updatePathLocation(ZLocation location) {
                    setLocation(location);
                    final ZLocation next = getPath().getLocationList().get(getNextLocation());
                    // add y diff (elevation)
                    Vector vector = next.toVector().add(new Vector(0, location.getY() - next.getY(), 0));
                    Location direction = next.bukkitLocation().clone().setDirection(location.toVector().subtract(vector).
                            multiply(new Vector(-1, 0, -1))); // Reverse
                    getNpc().setLocation(direction, false);
                    // look at next location
                    getNpc().lookAt(null, direction, true);
                }
            }
        }
    }
}
