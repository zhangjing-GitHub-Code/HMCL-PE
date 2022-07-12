package com.tungsten.hmclpe.launcher.mod.server;

import static com.tungsten.hmclpe.launcher.download.LibraryAnalyzer.LibraryType.MINECRAFT;

import android.os.AsyncTask;

import com.google.gson.JsonParseException;
import com.tungsten.hmclpe.launcher.mod.Modpack;
import com.tungsten.hmclpe.launcher.mod.ModpackConfiguration;
import com.tungsten.hmclpe.launcher.mod.ModpackManifest;
import com.tungsten.hmclpe.launcher.mod.ModpackProvider;
import com.tungsten.hmclpe.utils.gson.tools.TolerableValidationException;
import com.tungsten.hmclpe.utils.gson.tools.Validation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

public class ServerModpackManifest implements ModpackManifest, Validation {
    private final String name;
    private final String author;
    private final String version;
    private final String description;
    private final String fileApi;
    private final List<ModpackConfiguration.FileInformation> files;
    private final List<Addon> addons;

    public ServerModpackManifest() {
        this("", "", "", "", "", Collections.emptyList(), Collections.emptyList());
    }

    public ServerModpackManifest(String name, String author, String version, String description, String fileApi, List<ModpackConfiguration.FileInformation> files, List<Addon> addons) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.description = description;
        this.fileApi = fileApi;
        this.files = files;
        this.addons = addons;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getFileApi() {
        return fileApi;
    }

    public List<ModpackConfiguration.FileInformation> getFiles() {
        return files;
    }

    public List<Addon> getAddons() {
        return addons;
    }

    @Override
    public ModpackProvider getProvider() {
        return ServerModpackProvider.INSTANCE;
    }

    @Override
    public void validate() throws JsonParseException, TolerableValidationException {
        if (fileApi == null)
            throw new JsonParseException("ServerModpackManifest.fileApi cannot be blank");
        if (files == null)
            throw new JsonParseException("ServerModpackManifest.files cannot be null");
    }

    public static final class Addon {
        private final String id;
        private final String version;

        public Addon() {
            this("", "");
        }

        public Addon(String id, String version) {
            this.id = id;
            this.version = version;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }
    }

    public Modpack toModpack(Charset encoding) throws IOException {
        String gameVersion = addons.stream().filter(x -> MINECRAFT.getPatchId().equals(x.id)).findAny()
                .orElseThrow(() -> new IOException("Cannot find game version")).getVersion();
        return new Modpack(name, author, version, gameVersion, description, encoding, this) {
            @Override
            public AsyncTask getInstallTask(File zipFile, String name) {
                return new ServerModpackLocalInstallTask(zipFile, this, ServerModpackManifest.this, name);
            }
        };
    }

}