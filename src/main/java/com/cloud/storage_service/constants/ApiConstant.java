package com.cloud.storage_service.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiConstant {
    public static final String WEBTOONS = "webtoons";
    public static final String WEBTOONS_TITLE = "webtoons/{title}";
    public static final String WEBTOONS_TITLE_CHAPTERS = "webtoons/{title}/chapters";
    public static final String WEBTOONS_SYNC = "webtoon-sync";
    public static final String DOWNLOAD_WEBTOON = "download-webtoon";
    public static final String DOWNLOAD_WEBTOON_CHAPTERS = "download-webtoon-chapters";
    public static final String UPLOAD_WEBTOON = "upload-webtoon";
    public static final String LATEST_CHAPTER_WEBTOON = "latest-chapter-webtoon";
    public static final String ID_PATH = "/{id}";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AUTH0 {
        public static final String AUTHORIZE_URL = "authorize";
        public static final String DEVICE_AUTHORIZATION_URL = "oauth/device/code";
        public static final String TOKEN_URL = "oauth/token";
        public static final String USER_INFO_URL = "userinfo";
        public static final String OPENID_CONFIG = ".well-known/openid-configuration";
        public static final String JWKS_JSON = ".well-known/jwks.json";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MINIO {
        public static final String BASE = "minio";
        public static final String UPLOAD_FILES = BASE + "/uploadFiles";
        public static final String DELETE_FILE = BASE + "/deleteFile";
        public static final String GET_FILE_INFO = BASE + "/getFileInfo";
        public static final String DOWNLOAD_FILE = BASE + "/downloadFile";
        public static final String LIST_FILES = BASE + "/listFiles";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class S3 {
        public static final String BASE = "s3";
        public static final String UPLOAD_FILES = BASE + "/uploadFiles";
        public static final String DELETE_FILE = BASE + "/deleteFile";
        public static final String DELETE_FOLDER = BASE + "/deleteFolder";
        public static final String GET_FILE_INFO = BASE + "/getFileInfo";
        public static final String GET_FILE_EXISTS = BASE + "/fileExists";
        public static final String DOWNLOAD_FILE = BASE + "/downloadFile";
        public static final String DOWNLOAD_FOLDER = BASE + "/downloadFolder";
        public static final String VIEW_FILE = BASE + "/viewFile";
        public static final String LIST_FILES = BASE + "/listFiles";
        public static final String PRESIGN_URL = BASE + "/presignUrl";
    }
}