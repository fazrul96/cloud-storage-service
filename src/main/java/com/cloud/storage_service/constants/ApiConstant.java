package com.cloud.storage_service.constants;

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

    public static class PORTFOLIO {
        public static final String CERTIFICATIONS = "certifications";
        public static final String CERTIFICATION_ID = CERTIFICATIONS + ID_PATH;

        public static final String PROJECTS = "projects";
        public static final String PROJECT_ID = PROJECTS + ID_PATH;

        public static final String EDUCATIONS = "educations";
        public static final String EDUCATION_ID = EDUCATIONS + ID_PATH;

        public static final String SKILLS = "skills";
        public static final String SKILL_ID = SKILLS + ID_PATH;

        public static final String SOCIAL_LINKS = "social-links";
        public static final String SOCIAL_LINK_ID = SOCIAL_LINKS + ID_PATH;

        public static final String EXPERIENCES = "experiences";
        public static final String EXPERIENCE_ID = EXPERIENCES + ID_PATH;

        public static final String EXPERIENCE_CATEGORIES = "experience-categories";
        public static final String EXPERIENCE_CATEGORY_ID = EXPERIENCE_CATEGORIES + ID_PATH;
    }

    public static class MOSQUE {
        public static final String BASE = "mosque";
        public static final String USER_DONATIONS = BASE + "-user-donations";
        public static final String USER_DONATIONS_ID = BASE + "-user-donations/{id}";
        public static final String USER_DONATIONS_NRIC = BASE + "-user-donations/{nric}";

        public static final String INVENTORIES = BASE + "-inventories";
        public static final String INVENTORIES_STATUS = BASE + "-inventories/{status}";
        public static final String INVENTORIES_ID = BASE + "-inventories/{id}";
    }

    public static class INSURANCE {
        public static final String QUOTATION_PLAN = "plan/get-quotation-plan";
        public static final String GET_TERMS = "terms";
        public static final String GET_POLICIES = "policy/getAll";
        public static final String GET_POLICY_WITH_ID = "policy/{id}";
        public static final String CREATE_APPLICATION = "policy/create-application";
        public static final String CREATE_BENEFICIARIES = "policy/beneficiary";
        public static final String PAYMENT = "payment/handle-payment";
        public static final String GET_NOTIFICATIONS = "notifications/getAll";

        public static final String BASE_AUTH = "auth";
        public static final String SIGNUP = BASE_AUTH + "/signup";
        public static final String LOGIN = BASE_AUTH + "/login";
        public static final String LOGIN_AUTH0 = BASE_AUTH + "/loginAuth0";

        public static final String BASE_CLAIM = "claim";
        public static final String CLAIM_LIST = BASE_CLAIM + "/list";
        public static final String CLAIM_DETAIL = BASE_CLAIM + "/detail/{claimId}";
        public static final String CLAIM_POLICY_DOC = BASE_CLAIM + "/claimpolicydocument";
        public static final String CLAIM_SUBMIT = BASE_CLAIM + "/submit";
        public static final String CLAIM_DOWNLOAD = BASE_CLAIM + "/download";
    }

    public static class STRIPE {
        public static final String BASE = "stripe";
        public static final String PAYMENT_INTENT = BASE + "/payment-intent";
    }

    public static class AUTH0 {
        public static final String AUTHORIZE_URL = "authorize";
        public static final String DEVICE_AUTHORIZATION_URL = "oauth/device/code";
        public static final String TOKEN_URL = "oauth/token";
        public static final String USER_INFO_URL = "userinfo";
        public static final String OPENID_CONFIG = ".well-known/openid-configuration";
        public static final String JWKS_JSON = ".well-known/jwks.json";
    }

    public static class MINIO {
        public static final String BASE = "minio";
        public static final String UPLOAD_FILES = BASE + "/uploadFiles";
        public static final String DELETE_FILE = BASE + "/deleteFile";
        public static final String GET_FILE_INFO = BASE + "/getFileInfo";
        public static final String DOWNLOAD_FILE = BASE + "/downloadFile";
        public static final String LIST_FILES = BASE + "/listFiles";
    }

    public static class BILLING {
        public static final String BASE = "billing";
        public static final String GET_CURRENT = BASE + "/getCurrent";
        public static final String GET_PREVIOUS = BASE + "/getPrevious";
        public static final String GET_FORECAST = BASE + "/getForecast";
        public static final String GET_BY_SERVICE = BASE + "/getByService";
        public static final String GET_BY_SERVICE_PREVIOUS = BASE + "/getByServicePrevious";
    }

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