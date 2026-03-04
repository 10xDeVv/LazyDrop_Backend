package com.lazydrop.config;

/**
 * Bucket CORS is no longer configured programmatically — the Spaces access key
 * doesn't have permission for PutBucketCors.  File uploads now go through the
 * backend proxy endpoint (DropFileController#uploadFile) so browser → Spaces
 * CORS is not needed.
 */
