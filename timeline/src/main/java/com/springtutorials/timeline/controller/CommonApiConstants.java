package com.springtutorials.timeline.controller;

public class CommonApiConstants {
    public static final String VERSION_1 = "/v1";
    public static final String OK_RESPONSE_CODE = "200";
    public static final String CREATED_RESPONSE_CODE = "201";
    public static final String ACCEPTED_RESPONSE_CODE = "202";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String CONFLICT = "409";
    public static final String REQUEST_ID_HEADER = "Request-Id";

    public static final String HAS_ANY_AUTHORITY_EXPRESSION = "hasAnyAuthority";

    public static final String FRONTOFFICE_ROLE_EXPR = "@paymentProperties.security.retailBusinessDepRole";
    public static final String CONTACT_CENTER_ROLE_EXPR = "@paymentProperties.security.contactCenterRole";
    public static final String DEC_ROLE_EXPR = "@paymentProperties.security.ecommerceDepRole";
    public static final String BACKOFFICE_ROLE_EXPR = "@paymentProperties.security.backofficeRole";
    public static final String BUSINESS_ADMIN_ROLE_EXPR = "@paymentProperties.security.businessAdminRole";
    public static final String SUPERVISOR_ROLE_EXPR = "@paymentProperties.security.itSupportRole";

    public static final String SERVICE_PROVIDER_ROLE_EXPR = "@paymentProperties.security.providerRole";

    public static final String USER_DEFINED_ERRORS = "USER_DEFINED_ERRORS";
    public static final String INVALID_REGISTRY_STATUS = "Invalid Registry";

    private CommonApiConstants() {
    }
}
