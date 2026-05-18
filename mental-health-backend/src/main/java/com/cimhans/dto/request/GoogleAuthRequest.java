package com.cimhans.dto.request;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    /** The Google OAuth access_token (or id_token) from the frontend */
    private String credential;
}
