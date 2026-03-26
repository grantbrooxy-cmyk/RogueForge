package com.rogueforge.game.support;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import java.io.File;

/**
 * Boots a minimal LibGDX runtime for tests that load real asset JSON.
 */
public final class GdxTestSupport {
    private static boolean initialized;

    private GdxTestSupport() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        File assetsDir = findAssetsDir();
        Gdx.files = new TestFiles(assetsDir);
        initialized = Gdx.files != null;
    }

    private static File findAssetsDir() {
        File current = new File("").getAbsoluteFile();
        while (current != null) {
            File candidate = new File(current, "assets");
            if (new File(candidate, "data/abilities.json").isFile()) {
                return candidate;
            }
            current = current.getParentFile();
        }
        throw new IllegalStateException("Could not locate assets directory for tests.");
    }

    private static final class TestFiles implements Files {
        private final File assetsDir;

        private TestFiles(File assetsDir) {
            this.assetsDir = assetsDir;
        }

        @Override
        public FileHandle getFileHandle(String path, FileType type) {
            switch (type) {
                case Classpath:
                case Internal:
                    return internal(path);
                case External:
                    return external(path);
                case Absolute:
                    return absolute(path);
                case Local:
                default:
                    return local(path);
            }
        }

        @Override
        public FileHandle classpath(String path) {
            return internal(path);
        }

        @Override
        public FileHandle internal(String path) {
            return new FileHandle(new File(assetsDir, path));
        }

        @Override
        public FileHandle external(String path) {
            return new FileHandle(new File(getExternalStoragePath(), path));
        }

        @Override
        public FileHandle absolute(String path) {
            return new FileHandle(new File(path));
        }

        @Override
        public FileHandle local(String path) {
            return new FileHandle(new File(getLocalStoragePath(), path));
        }

        @Override
        public String getExternalStoragePath() {
            return assetsDir.getAbsolutePath();
        }

        @Override
        public boolean isExternalStorageAvailable() {
            return true;
        }

        @Override
        public String getLocalStoragePath() {
            return assetsDir.getAbsolutePath();
        }

        @Override
        public boolean isLocalStorageAvailable() {
            return true;
        }
    }
}
