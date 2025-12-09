package com.cloud.storage_service.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelConstant {
    public static final String TBL_USER = "user";
    public static final String TBL_WEBTOONS = "webtoons";
    public static final String TBL_WEBTOON_CHAPTER = "webtoon_chapters";

    public static final String ID = "id";
    public static final String PLAN_ID = "plan_id";
    public static final String ALIAS = "alias";
    public static final String IMAGE = "image";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String GENRE = "genre";
    public static final String DESCRIPTION = "description";
    public static final String RATING = "rating";
    public static final String ARTIST = "artist";
    public static final String CHAPTER_COUNT = "chapter_count";
    public static final String SOURCE = "source";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String CREATED_DATE = "created_date";
    public static final String UPDATED_DATE = "updated_date";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String BANNER_IMAGE = "banner_image";
    public static final String COVER_IMAGE = "cover_image";

    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String GOOGLE_ID = "googleId";
    public static final String AVATAR_URL = "avatarUrl";

    public static final String CHAPTER = "chapter";

    public static final String HIVETOON = "hivetoon";
    public static final String ASURA = "asura";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PortfolioTable {
        public static final String CERTIFICATIONS = "certifications";
        public static final String EDUCATIONS = "educations";
        public static final String EXPERIENCES = "experiences";
        public static final String EXPERIENCE_CATEGORIES = "experience_categories";
        public static final String EXPERIENCE_ITEMS = "experience_items";
        public static final String PROJECTS = "projects";
        public static final String SKILLS = "skills";
        public static final String SKILL_CATEGORIES = "skill_categories";
        public static final String SOCIAL_LINKS = "social_links";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MosqueTable {
        public static final String USER_DONATIONS = "user_donations";
        public static final String LOGISTIC_INVENTORY = "logistics_inventory";
        public static final String PRAYER_TIMES = "prayer_times";
        public static final String EVENTS = "events";
        public static final String SPONSORS = "sponsors";
        public static final String ANNOUNCEMENTS = "announcements";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InsuranceTable {
        public static final String PLAN = "plan";
        public static final String RULE_SET = "ruleset";
        public static final String NOTIFICATION = "notification";
        public static final String CLAIM = "claim";
        public static final String CLAIM_DOCUMENT = "claim_document";
        public static final String CLAIM_TYPE = "claim_type";
        public static final String BENEFICIARY = "beneficiary";
        public static final String DOCUMENT_TYPE = "document_type";
        public static final String PAYMENT = "payment";
        public static final String POLICY = "policy";
        public static final String QUOTATION_APPLICATION = "quotation_application";
        public static final String TERMS_DECLARATION = "terms_declaration";
    }
}
