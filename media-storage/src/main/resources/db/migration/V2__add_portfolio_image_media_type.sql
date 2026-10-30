ALTER TABLE media_files
DROP CONSTRAINT media_files_media_type_check;

ALTER TABLE media_files
    ADD CONSTRAINT media_files_media_type_check
        CHECK (
            media_type IN (
                           'PROFILE_PHOTO',
                           'PORTFOLIO_IMAGE',
                           'DOCUMENT'
                )
            );