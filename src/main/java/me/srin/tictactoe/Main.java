package me.srin.tictactoe;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import me.srin.tictactoe.engine.TicTacToeEngine;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.System.err;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Main {
    static final int WIDTH = 400;
    private static final int HEIGHT = 400;
    private static final String TITLE = "TicTacToe";
    private static final long WINDOW;
    private static final ImGuiImplGlfw IM_GUI_GLFW = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 IM_GUI_GL3 = new ImGuiImplGl3();
    static final int BOARD_TEXTURE;
    static final int CROSS_TEXTURE;
    static final int NOUGHT_TEXTURE;
    static final int BLANK_TEXTURE;
    static final int HOVER_TEXTURE;
    static final int PC_TEXTURE;
    static final int HUMAN_TEXTURE;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    static GameState gameState = GameState.PLAYING;
    static Player.PlayerType winner = null;
    static int noughtPoints = 0, crossPoints = 0;
    static Player player = null;
    static TicTacToeEngine engine = null;
    static {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
//        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
//        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
//        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        WINDOW = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if (WINDOW == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window");
        }
        GLFWVidMode vidMode = Objects.requireNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));
        glfwSetWindowPos(WINDOW, (vidMode.width() - WIDTH) / 2, (vidMode.height() - HEIGHT) / 2);
        glfwMakeContextCurrent(WINDOW);
        glfwSwapInterval(1);
        glfwShowWindow(WINDOW);
        GL.createCapabilities();
        System.out.println("OpenGL version: " + glGetString(GL_VERSION));
        glViewport(0, 0, WIDTH, HEIGHT);
        GLFWErrorCallback.createPrint(err).set();
        GLFWKeyCallback.create(Main::keyCallback).set(WINDOW);
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setIniFilename(null);
        ImGui.styleColorsDark();
        try {
            BOARD_TEXTURE = loadTexture("textures/board.png");
            CROSS_TEXTURE = loadTexture("textures/cross.png");
            NOUGHT_TEXTURE = loadTexture("textures/circle.png");
            BLANK_TEXTURE = loadTexture("textures/blank.png");
            HOVER_TEXTURE = loadTexture("textures/hover.png");
            PC_TEXTURE = loadTexture("textures/pc.png");
            HUMAN_TEXTURE = loadTexture("textures/person.png");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try (GLFWImage.Buffer image = GLFWImage.malloc(2)) {
            ByteBuffer appIcon32x32 = loadResourcesAsBuffer("textures/tictactoe32x32.png");
            ByteBuffer appIcon16x16 = loadResourcesAsBuffer("textures/tictactoe16x16.png");

            int[] width = { 0 };
            int[] height = { 0 };
            int[] channels = { 0 };
            stbi_set_flip_vertically_on_load(true);

            ByteBuffer pixels16 = stbi_load_from_memory(appIcon16x16, width, height, channels, 4);
            if (pixels16 == null) {
                throw new RuntimeException("Failed to load icon");
            }
            image.position(0).width(width[0]).height(height[0]).pixels(pixels16);

            ByteBuffer pixels32 = stbi_load_from_memory(appIcon32x32, width, height, channels, 4);
            if (pixels32 == null) {
                throw new RuntimeException("Failed to load icon");
            }
            image.position(1).width(width[0]).height(height[0]).pixels(pixels32);

            glfwSetWindowIcon(WINDOW, image.position(0));
            stbi_image_free(pixels16);
            stbi_image_free(pixels32);
            MemoryUtil.memFree(appIcon32x32);
            MemoryUtil.memFree(appIcon16x16);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static final Board BOARD = new Board((WIDTH - 200f) / 2, (HEIGHT - 200f) / 2, 200, 200);
    private static final HUD HUD = new HUD(0, 0, WIDTH, HEIGHT);
    private static final NoughtPoints NOUGHT_POINTS = new NoughtPoints((WIDTH - (80 * 2 + 6)) * .5f, 51, 80, 40);
    private static final CrossPoints CROSS_POINTS = new CrossPoints((WIDTH - (80 * 2 + 6)) * .5f + 80 + 6, 51, 80, 40);
    private static int loadTexture(String name) throws URISyntaxException, IOException {
        int texture = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        final int[] width = { 0 };
        final int[] height = { 0 };
        final int[] channel = { 0 };
        byte[] data = loadResources(name);
        ByteBuffer buffer = MemoryUtil.memCalloc(data.length);
        buffer.put(data);
        buffer.flip();
        final ByteBuffer image = stbi_load_from_memory(buffer, width, height, channel, 4);
        try {
            if (image != null) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                glGenerateMipmap(GL_TEXTURE_2D);
                stbi_image_free(image);
            } else throw new RuntimeException("Failed to load image!!!");
        } finally {
            MemoryUtil.memFree(buffer);
        }
        return texture;
    }
    private static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true);
        }
    }
    public static byte[] loadResources(String filename) throws IOException {
        try (InputStream in = Main.class.getResourceAsStream(filename)) {
            if (in == null) throw new FileNotFoundException(filename);
            return readNBytes(in, Integer.MAX_VALUE);
        }
    }
    public static ByteBuffer loadResourcesAsBuffer(String filename) throws IOException {
        try (InputStream in = Main.class.getResourceAsStream(filename)) {
            if (in == null) throw new FileNotFoundException(filename);
            byte[] bytes = readNBytes(in, Integer.MAX_VALUE);
            ByteBuffer buffer = MemoryUtil.memCalloc(bytes.length);
            buffer.put(bytes).flip();
            return buffer;
        }
    }
    private static void imGuiStartFrame() {
        IM_GUI_GLFW.newFrame();
        ImGui.newFrame();
    }
    private static void imGuiEndFrame() {
        ImGui.render();
        IM_GUI_GL3.renderDrawData(ImGui.getDrawData());
    }
    public static void main(String[] args) {
        start(args, () -> {
            while (!glfwWindowShouldClose(WINDOW)) {
                glClear(GL_COLOR_BUFFER_BIT);
                imGuiStartFrame(); {
                    HUD.render();
                    NOUGHT_POINTS.render();
                    CROSS_POINTS.render();
                    BOARD.render();
                } imGuiEndFrame();
                glfwSwapBuffers(WINDOW);
                glfwPollEvents();
            }
        });
    }
    private static void start(String[] args, Runnable game) {
        // initialising ImGui stuff
        IM_GUI_GLFW.init(WINDOW, true);
        IM_GUI_GL3.init(args.length == 0? "#version 460": args[0]);

        // running the game
        game.run();

        // disposing of ImGui and opengl stuff
        IM_GUI_GL3.dispose();
        IM_GUI_GLFW.dispose();
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(WINDOW);
        glfwDestroyWindow(WINDOW);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
    static Pair<Float, Float> getBoardXY() {
        return Pair.of(BOARD.x, BOARD.y);
    }
    static void reset() {
        player = null;
        engine = null;
        gameState = GameState.PLAYING;
        BOARD.reset();
    }
    public static Player.PlayerType getWinner() {
        return winner;
    }
    public static int getNoughtPoints() {
        return noughtPoints;
    }
    public static int getCrossPoints() {
        return crossPoints;
    }
    public static byte[] readNBytes(InputStream is, int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, DEFAULT_BUFFER_SIZE)];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = is.read(buf, nread,
                    Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;
            }

            if (nread > 0) {
                if (MAX_BUFFER_SIZE - total < nread) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ?
                    result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }
    static class Pair<K, V> {
        private final K key;
        private V value;

        private Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public static <K, V> Pair<K, V> of(K key, V value) {
            return new Pair<>(key, value);
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
    enum GameState { WIN, DRAW, PLAYING }
}
