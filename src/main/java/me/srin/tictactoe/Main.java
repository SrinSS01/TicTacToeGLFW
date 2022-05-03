package me.srin.tictactoe;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
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
import java.util.*;

import static java.lang.System.err;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Main {
    private static final int WIDTH = 400;
    public static final float BOARD_X = (WIDTH - 200f) / 2;
    private static final int HEIGHT = 400;
    public static final float BOARD_Y = (HEIGHT - 200f) / 2;
    private static final String TITLE = "TicTacToe";
    private static final long WINDOW;
    private static final ImGuiImplGlfw IM_GUI_GLFW = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 IM_GUI_GL3 = new ImGuiImplGl3();
    private static final int BOARD_TEXTURE;
    private static final int CROSS_TEXTURE;
    private static final int NOUGHT_TEXTURE;
    private static final int BLANK_TEXTURE;
    public static final int BOARD_WIDTH = 200;
    public static final int BOARD_HEIGHT = 200;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    private static final int HOVER_TEXTURE;

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
        IM_GUI_GLFW.init(WINDOW, true);
        IM_GUI_GL3.init(args.length == 0? "#version 460": args[0]);
        final float BUTTON_SIZE = 46.809f;
        final char[] player = new char[1];
        TicTacToeEngine engine = new TicTacToeEngine(player);

        LinkedList<Pair<ImVec2, Integer>> coords = new LinkedList<>(
                Arrays.asList(
                        Pair.of(new ImVec2(10.606f, 12.121f)  , BLANK_TEXTURE),
                        Pair.of(new ImVec2(72.727f, 12.121f)  , BLANK_TEXTURE),
                        Pair.of(new ImVec2(134.848f, 12.121f) , BLANK_TEXTURE),
                        Pair.of(new ImVec2(10.606f, 74.242f)  , BLANK_TEXTURE),
                        Pair.of(new ImVec2(72.727f, 74.242f)  , BLANK_TEXTURE),
                        Pair.of(new ImVec2(134.848f, 74.242f) , BLANK_TEXTURE),
                        Pair.of(new ImVec2(10.606f,  134.848f), BLANK_TEXTURE),
                        Pair.of(new ImVec2(72.727f,  134.848f), BLANK_TEXTURE),
                        Pair.of(new ImVec2(134.848f, 134.848f), BLANK_TEXTURE)
                )
        );
        boolean win = false, draw = false;
        char winner = 0;
        int noughtPoints = 0, crossPoints = 0;
        while (!glfwWindowShouldClose(WINDOW)) {
            glClear(GL_COLOR_BUFFER_BIT);
            imGuiStartFrame(); {
                ImGui.begin("background", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus); {
                    ImGui.setWindowSize(WIDTH, HEIGHT);
                    ImGui.setWindowPos(0, 0);
                    if (win) {
                        ImGui.setCursorPos(BOARD_X + 50, BOARD_Y - 70);
                        ImGui.text("winner is   !!");
                        ImGui.setCursorPos(BOARD_X + 120, BOARD_Y - 67);
                        ImGui.image(winner == 'x'? CROSS_TEXTURE: NOUGHT_TEXTURE, 10, 10);
                    } else if (draw) {
                        ImGui.setCursorPos((WIDTH - 25) / 2f, BOARD_Y - 70);
                        ImGui.text("draw!");
                    }
                    ImGui.setCursorPos((WIDTH - 50) / 2f, BOARD_Y + BOARD_HEIGHT + 10);
                    if ((win || draw) && ImGui.button("reset", 50, 25)) {
                        engine = new TicTacToeEngine(player);
                        win = false;
                        draw = false;
                        coords.forEach(p -> p.value = BLANK_TEXTURE);
                    }
                } ImGui.end();
                ImGui.begin("nought points", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration); {
                    ImGui.setWindowSize(80, 40);
                    ImGui.setWindowPos(205, 51);
                    ImGui.image(NOUGHT_TEXTURE, 40 - 16, 40 - 16);
                    ImGui.setCursorPos(48 + 14, ((40 - 10) / 2f));
                    ImGui.text(String.format("%d", noughtPoints));
                } ImGui.end();
                ImGui.begin("cross points", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration); {
                    ImGui.setWindowSize(80, 40);
                    ImGui.setWindowPos(119, 51);
                    ImGui.image(CROSS_TEXTURE, 40 - 16, 40 - 16);
                    ImGui.setCursorPos(48 + 14, ((40 - 10) / 2f));
                    ImGui.text(String.format("%d", crossPoints));
                } ImGui.end();
                ImGui.begin("board", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration); {
                    ImGui.setWindowSize(BOARD_WIDTH, BOARD_HEIGHT);
                    ImGui.setWindowPos(BOARD_X, BOARD_Y);
                    ImGui.image(BOARD_TEXTURE, 200 - 16, 200 - 16);
                    ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);

                    int size = coords.size();
                    for (int i = 0; i < size; i++) {
                        ImGui.pushID(i);
                        Pair<ImVec2, Integer> co_ord = coords.get(i);
                        ImVec2 pos = co_ord.key;
                        int texture = co_ord.value;
                        ImGui.setCursorPos(pos.x, pos.y);
                        boolean isButtonClicked = ImGui.imageButton(texture, BUTTON_SIZE, BUTTON_SIZE);
                        if (ImGui.isItemHovered()) {
                            ImGui.setCursorPos(pos.x, pos.y);
                            ImGui.image(HOVER_TEXTURE, BUTTON_SIZE + 7, BUTTON_SIZE + 5);
                        }
                        if (isButtonClicked && engine.place(8 - i) && !win && !draw) {
                            co_ord.value = (player[0] == 'x'? CROSS_TEXTURE: NOUGHT_TEXTURE);
                            winner = player[0];
                            if (engine.isWin()) {
                                win = true;
                                if (winner == 'x') {
                                    crossPoints++;
                                } else {
                                    noughtPoints++;
                                }
                            } else if (engine.isDraw()) {
                                draw = true;
                            }
                        }
                        ImGui.popID();
                    }
                    ImGui.popStyleColor(3);
                } ImGui.end();
            } imGuiEndFrame();
            glfwSwapBuffers(WINDOW);
            glfwPollEvents();
        }
        dispose();
    }

    private static void dispose() {
        IM_GUI_GL3.dispose();
        IM_GUI_GLFW.dispose();
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(WINDOW);
        glfwDestroyWindow(WINDOW);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
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
    private static class Pair<K, V> {
        private final K key;
        private V value;

        private Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public static <K, V> Pair<K, V> of(K key, V value) {
            return new Pair<>(key, value);
        }
    }
}
