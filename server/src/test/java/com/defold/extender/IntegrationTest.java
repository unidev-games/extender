package com.defold.extender;

import com.defold.extender.client.*;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntegrationTest {

    private static final int EXTENDER_PORT = 9000;

    @BeforeClass
    public static void before() throws IOException, InterruptedException {
        ProcessExecutor processExecutor = new ProcessExecutor();
        processExecutor.execute("scripts/start-test-server.sh");
        System.out.println(processExecutor.getOutput());

        // Wait for server to start in container.
        Thread.sleep(7000);
    }

    @AfterClass
    public static void after() throws IOException, InterruptedException {
        ProcessExecutor processExecutor = new ProcessExecutor();
        processExecutor.execute("scripts/stop-test-server.sh");
        System.out.println(processExecutor.getOutput());
    }

    @Test
    public void buildingRemoteShouldReturnEngine() throws IOException, ExtenderClientException {
        File cacheDir = new File("build");
        ExtenderClient extenderClient = new ExtenderClient("http://localhost:" + EXTENDER_PORT, cacheDir);
        List<ExtenderResource> sourceFiles = Lists.newArrayList(
                new FileExtenderResource("test-data/ext/ext.manifest"),
                new FileExtenderResource("test-data/ext/src/test_ext.cpp"),
                new FileExtenderResource("test-data/ext/include/test_ext.h"),
                new FileExtenderResource("test-data/ext/lib/x86-osx/libalib.a"));
        File destination = Files.createTempFile("dmengine", ".zip").toFile();
        File log = Files.createTempFile("dmengine", ".log").toFile();

        String platform = "x86-osx";
        String sdkVersion = "a";

        try {
            extenderClient.build(
                    platform,
                    sdkVersion,
                    sourceFiles,
                    destination,
                    log
            );
        } catch (ExtenderClientException e) {
            System.out.println(new String(Files.readAllBytes(log.toPath())));
            throw e;
        }

        assertTrue("Resulting engine should be of a size greater than zero.", destination.length() > 0);
        assertEquals("Log should be of size zero if successful.", 0, log.length());

        ExtenderClientCache cache = new ExtenderClientCache(cacheDir);
        assertTrue(cache.getCachedBuildFile(platform).exists());

        FileUtils.deleteDirectory(new File("build" + File.separator + sdkVersion));
    }
}