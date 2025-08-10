package com.cloud.storage_service.constants;

public class MessageConstants {
    public static final class ResponseMessages {
        public static final String SUCCESS = "Success";
        public static final String ERROR = "Error";
        public static final String FAILURE = "Failure";
        public static final String GET_SUCCESS = "Get successfully!";
        public static final String CREATE_SUCCESS = "Created successfully!";
        public static final String UPDATE_SUCCESS = "Updated successfully!";
        public static final String DELETE_SUCCESS = "Deleted successfully!";
    }

    public static final class HttpCodes {
        public static final String OK = "200";
        public static final String CREATED = "201";
        public static final String NO_CONTENT = "204";
        public static final String BAD_REQUEST = "400";
        public static final String UNAUTHORIZED = "401";
        public static final String FORBIDDEN = "403";
        public static final String NOT_FOUND = "404";
        public static final String CONFLICT = "409";
        public static final String UNPROCESSABLE_ENTITY = "422";
        public static final String INTERNAL_SERVER_ERROR = "500";
    }

    public static final class HttpDescription {
        public static final String OK_DESC = "Request successful";
        public static final String CREATED_DESC = "Successfully created";
        public static final String NO_CONTENT_DESC = "No content to return";
        public static final String BAD_REQUEST_DESC = "Invalid request input";
        public static final String UNAUTHORIZED_DESC = "Unauthorized access";
        public static final String FORBIDDEN_DESC = "Access forbidden";
        public static final String NOT_FOUND_DESC = "Resource not found";
        public static final String CONFLICT_DESC = "Conflict occurred";
        public static final String UNPROCESSABLE_ENTITY_DESC = "Unprocessable input";
        public static final String INTERNAL_ERROR_DESC = "Unexpected error occurred";
    }
}
