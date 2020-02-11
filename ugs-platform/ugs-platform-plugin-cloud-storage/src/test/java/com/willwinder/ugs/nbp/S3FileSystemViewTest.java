/*
    Copyright 2020 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.findify.s3mock.S3Mock;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.assertj.core.api.Assertions;

/**
 *
 * @author will
 */
public class S3FileSystemViewTest {
    private String s3url = "http://127.0.0.1:8001";
    static S3Mock api = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
    S3FileSystemView instance;
    
    public S3FileSystemViewTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        api.start();
    }
    
    @AfterClass
    public static void tearDownClass() {
        api.stop();
    }
    
    @Before
    public void setUp() throws Exception {
        refreshInstance();
    }
    
    @After
    public void tearDown() {
    }

    // The buckets are cached, so this will probably be needed.
    private void refreshInstance() throws Exception {
        instance = new S3FileSystemView(s3url, "test", "test");
    }
    
    private File createFileWith(byte[] contents) throws IOException {
        File f = File.createTempFile("foo", "bar");
        f.deleteOnExit();
        if (contents != null) {
            Files.write(f.toPath(), contents);
        }
        return f;
    }
    
    @Test
    public void testUploadDownloadFile() throws Exception {
        System.out.println("downloadFile");
        byte[] fileContents = "Contents of file".getBytes();
        File originalFile = createFileWith(fileContents);
        
        instance.createBucket("test-files");
        File s3File = new S3VirtualFile("s3:/test-files/testfile.nc", 0);
        File localFile = createFileWith(null);

        // Upload the file.
        instance.uploadFile(originalFile, s3File.toString());
        
        // Download to new file.
        instance.downloadFile(s3File.toString(), localFile);
        
        // Compare result to original file.
        byte[] results = Files.readAllBytes(localFile.toPath());
        Assert.assertArrayEquals(fileContents, results);
    }
    
    @Test
    public void testGetFiles_buckets() throws Exception {
        System.out.println("getFiles_buckets");
        
        List<String> buckets = ImmutableList.of("one", "two", "three", "four");
        buckets.forEach((b) -> instance.createBucket(b));

        refreshInstance();
        File root = new S3VirtualFile("s3:", 0);
         File[] results = instance.getFiles(root, true);

        // Convert Files to Strings, and format like "s3:/one/" to "one".
        List<String> resultList = Arrays.stream(results)
                .map((f) -> f.toString().substring(4, f.toString().length()))
                .collect(Collectors.toList());
        
        Assertions.assertThat(resultList).containsAll(buckets);
    }

    @Test
    public void testGetFiles() throws Exception {
        System.out.println("getFiles");
        
        instance.createBucket("test-files");
        refreshInstance();
        
        List<String> files = ImmutableList.of(
                "s3:/test-files/rootfile.nc",
                "s3:/test-files/dir1/dir2/nested_file1.nc",
                "s3:/test-files/dir1/dir2/nested_file2.nc",
                "s3:/test-files/dir1/dir2/nested_file3.nc"
        );
        
        for (String uri : files) {
            instance.uploadFile(createFileWith("some-data".getBytes()), uri);
        }
        
        File[] rootFiles = instance.getFiles(new S3VirtualFile("s3:/test-files/", 0), true);
        Assertions.assertThat(rootFiles).hasSize(2);
        for (File f : rootFiles) {
            if (f.isDirectory()) {
                Assertions.assertThat(f.getName()).isEqualTo("dir1");
            } else {
                Assertions.assertThat(f.getName()).isEqualTo("rootfile.nc");
            }
        }
        
        File[] middleDir = instance.getFiles(new S3VirtualFile("s3:/test-files/dir1/", 0), true);
        Assertions.assertThat(middleDir).hasSize(1);
        for (File f : middleDir) {
            Assertions.assertThat(f.isDirectory()).isTrue();
            Assertions.assertThat(f.getName()).isEqualTo("dir2");
        }
        
        File[] leaves = instance.getFiles(new S3VirtualFile("s3:/test-files/dir1/dir2", 0), true);
        Assertions.assertThat(leaves).hasSize(3);
        for (File f : leaves) {
            Assertions.assertThat(f.isDirectory()).isFalse();
            Assertions.assertThat(f.getName()).matches("nested_file\\d\\.nc");
        }
    }
    
    @Test
    public void testS3VirtualFileDir() {
        System.out.println("S3 Virtual File Dir");
        
        Map<String,Boolean> testCases = ImmutableMap.of(
                "s3:/bucket/", true,
                "s3:/bucket/file.nc", false,
                "s3:/bucket/dir1/", true,
                "s3:/bucket/dir1/dir2/", true,
                "s3:/bucket/dir1/dir2/file.nc", false);
        
        testCases.forEach((path, isDir) -> {
            Assertions.assertThat(new S3VirtualFile(path, 0).isDirectory()).isEqualTo(isDir);
        });
    }
}
