package com.cloud.storage_service.service.storage;

import com.cloud.storage_service.config.minio.MinioConfiguration;
import com.cloud.storage_service.constants.GeneralConstant;
import com.cloud.storage_service.util.common.StringUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.cloud.storage_service.constants.GeneralConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({
        "PMD.UnnecessaryFullyQualifiedName",
        "PMD.AvoidInstantiatingObjectsInLoops",
        "PMD.AvoidReassigningParameters",
        "PMD.SignatureDeclareThrowsException",
        "PMD.CloseResource",
        "PMD.CyclomaticComplexity",
        "PMD.AssignmentInOperand",
        "PMD.PrematureDeclaration",
        "PMD.NPathComplexity",
        "PMD.CognitiveComplexity"})
public class MinioService {
    private final MinioClient minioClient;
    private final MinioConfiguration minioConfig;

    public List<String> uploadFileFromPath(String filePath, String title)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        // Validate that the file path is not null or empty
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        List<String> uploadedFileNames = new ArrayList<>();
        Path path = Paths.get(filePath);

        // Check if the file exists at the given path
        if (!Files.exists(path)) {
            log.warn("File not found at path: {}", filePath);
            throw new IOException("File not found at the specified path: " + filePath);
        }

        String fileName = path.getFileName().toString();
        String contentType = Files.probeContentType(path); // Automatically detect content type

        // If content type is null, set default content type to octet-stream
        if (contentType == null) {
            contentType = OCTET_STREAM_CONTENT_TYPE;
        }

        // Handle zip files explicitly if necessary
        if (fileName.endsWith(DOT + ZIP_EXTENSION)) {
            contentType = ZIP_CONTENT_TYPE;
        }

        // Object name in MinIO (path within the bucket)
        String objectName = (title != null)
                ? WEBTOONS + SLASH + title + SLASH + fileName
                : fileName;

        // Upload the file to MinIO
        try (InputStream inputStream = Files.newInputStream(path)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .contentType(contentType)
                            .stream(inputStream, Files.size(path), -1) // Upload file as a stream
                            .build());
            uploadedFileNames.add(fileName); // Add the file name to the list of uploaded files
        } catch (MinioException e) {
            log.error("MinIO error while uploading file {}: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("MinIO file upload failed", e);
        } catch (IOException e) {
            log.error("IO error while uploading file {}: {}", fileName, e.getMessage(), e);
            throw new IOException("IO error during file upload", e);
        } catch (Exception e) {
            log.error("Unexpected error while uploading file {}: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during file upload", e);
        }

        return uploadedFileNames;
    }


    public List<String> uploadFiles(List<MultipartFile> files, String title)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        if (files.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileName = file.getOriginalFilename();

                // Determine content type for zip files
                String contentType = file.getContentType();
                if (contentType == null) {
                    contentType = OCTET_STREAM_CONTENT_TYPE;
                }

                // Handle zip files explicitly (optional, if you want to check the content type)
                if (fileName != null && fileName.endsWith(DOT + ZIP_EXTENSION)) {
                    contentType = ZIP_CONTENT_TYPE;
                }

                String objectName = (title != null)
                        ? WEBTOONS + SLASH + title + SLASH + fileName
                        : fileName;

                try (InputStream inputStream = file.getInputStream()) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(minioConfig.getBucketName())
                                    .object(objectName)
                                    .contentType(contentType)
                                    .stream(inputStream, file.getSize(), -1)
                                    .build());
                    uploadedFileNames.add(fileName);
                } catch (IOException | MinioException e) {
                    log.error("Error uploading file {}: {}", fileName, e.getMessage(), e);
                    throw new RuntimeException("File upload failed", e);
                }
            }
        }
        return uploadedFileNames;
    }

    public void deleteFile(String fileName) throws NoSuchAlgorithmException, InvalidKeyException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build());
        } catch (MinioException | IOException e) {
            log.error("Error deleting file {}: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("File deletion failed", e);
        }
    }

    public String getFileInfo(String fileName) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(fileName)
                        .build()).toString();
    }

    public void downloadFile(String fileName, HttpServletResponse response) {
        try {
            InputStream fileInputStream =  minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build());

            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType(FORCE_DOWNLOAD_CONTENT_TYPE);
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(fileInputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("Error downloading file {}: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Error downloading file", e);
        }
    }

    public InputStream downloadZipFile(String fileName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(fileName)
                .build());
    }

    public List<String> listFiles(String prefix) {
        List<String> fileList = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .prefix(prefix) // Use prefix to filter files inside a folder
                            .delimiter(SLASH) // Delimiter to distinguish folders from files
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                fileList.add(item.objectName());
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error retrieving files from MinIO", e);
        }

        return fileList;
    }

    public List<Map<String, Object>> processFiles(List<String> fileNames, String prefix) {
        List<Map<String, Object>> alias = new ArrayList<>();

        if (prefix != null && !prefix.isEmpty()) {
            prefix = prefix
                    .replaceAll(FORWARD_SLASH_PREFIX_REGEX, EMPTY_STRING)
                    .replaceAll(FORWARD_SLASH_SUFFIX_REGEX, EMPTY_STRING);
        }

        for (String fileName : fileNames) {
            Map<String, Object> entry = new HashMap<>();
            entry.put(GeneralConstant.ORIGINAL_NAME_KEY, fileName);

            String sanitizedName = fileName;
            if (prefix != null && fileName.startsWith(prefix)) {
                sanitizedName = fileName.substring(prefix.length());
            }

            if (sanitizedName.endsWith(GeneralConstant.SLASH)) {
                entry.put(GeneralConstant.LABEL_KEY, sanitizedName
                        .replaceAll(FORWARD_SLASH_SINGLE_SUFFIX_REGEX, EMPTY_STRING));
                entry.put(GeneralConstant.ALIAS_KEY, StringUtils
                        .capitalizeWords(sanitizedName
                        .replaceAll(FORWARD_SLASH_SINGLE_SUFFIX_REGEX, EMPTY_STRING)
                        .replaceAll(DASH, SINGLE_SPACE)));
                entry.put(GeneralConstant.TYPE_KEY, GeneralConstant.TYPE_FOLDER);
                entry.put(GeneralConstant.EXTENSION_KEY, null);
            } else {
                String[] baseNameAndExtension = StringUtils.extractBaseNameAndExtension(sanitizedName);
                entry.put(GeneralConstant.LABEL_KEY, sanitizedName);
                entry.put(GeneralConstant.ALIAS_KEY, StringUtils.capitalizeWords(baseNameAndExtension[0]));
                entry.put(GeneralConstant.TYPE_KEY, GeneralConstant.TYPE_FILE);
                entry.put(GeneralConstant.EXTENSION_KEY, baseNameAndExtension[1]);
            }

            alias.add(entry);
        }

        return alias;
    }

    // Step 2: Extract images from the ZIP file
    public List<Path> extractZipFile(Path zipFilePath) throws IOException {
        List<Path> extractedFiles = new ArrayList<>();
        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path extractedFile = Paths.get("extracted", entry.getName());
                    Files.createDirectories(extractedFile.getParent());
                    try (OutputStream out = Files.newOutputStream(extractedFile)) {
                        zipIn.transferTo(out);
                    }
                    extractedFiles.add(extractedFile);
                }
                zipIn.closeEntry();
            }
        }
        return extractedFiles;
    }

    // Step 3: Convert the extracted images to URLs (or Base64)
    public List<String> convertPathsToUrls(List<Path> extractedFiles) throws IOException {
        List<String> urls = new ArrayList<>();
        for (Path path : extractedFiles) {
            // Assuming these files are served from a static directory
            String fileUrl = path.toUri().toString(); // Alternatively, use base64 encoding
            urls.add(fileUrl);
        }
        return urls;
    }
}