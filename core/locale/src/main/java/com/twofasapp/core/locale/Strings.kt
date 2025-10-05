/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.locale

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.math.abs
import kotlin.math.sign

@Stable
@Immutable
class Strings(private val c: Context) {
    private val timeUnits = TimeUnit.entries.reversed()

    // Common
    val commonOk = c.getString(R.string.common_ok)
    val commonYes = c.getString(R.string.common_yes)
    val commonNo = c.getString(R.string.common_no)
    val commonEdit = c.getString(R.string.common_edit)
    val commonError = c.getString(R.string.common_error)
    val commonDelete = c.getString(R.string.common_delete)
    val commonCancel = c.getString(R.string.common_cancel)
    val commonAdd = c.getString(R.string.common_add)
    val commonSearch = c.getString(R.string.common_search)
    val commonSave = c.getString(R.string.common_save)
    val commonContinue = c.getString(R.string.common_continue)
    val commonOther = c.getString(R.string.common_other)
    val commonTryAgain = c.getString(R.string.common_try_again)
    val commonCreated = c.getString(R.string.common_created)
    val commonClose = c.getString(R.string.common_close)
    val commonConfirm = c.getString(R.string.common_confirm)

    // Permissions
    val permissionCameraTitle = c.getString(R.string.permission_camera_title)
    val permissionCameraMsg = c.getString(R.string.permission_camera_msg)
    val permissionPushTitle = c.getString(R.string.permission_notifications_title)
    val permissionPushMsg = c.getString(R.string.permission_notifications_msg)

    val pushBrowserRequestTitle = c.getString(R.string.push_browser_request_title)
    val pushBrowserRequestMessage = c.getString(R.string.push_browser_request_message)

    // Onboarding
    val onboardingWelcome1Title = c.getString(R.string.onboarding_welcome1_title)
    val onboardingWelcome1Description = c.getString(R.string.onboarding_welcome1_description)
    val onboardingWelcome1Feature1 = c.getString(R.string.onboarding_welcome1_feature1)
    val onboardingWelcome1Feature2 = c.getString(R.string.onboarding_welcome1_feature2)
    val onboardingWelcome1Feature3 = c.getString(R.string.onboarding_welcome1_feature3)
    val onboardingWelcome2Title = c.getString(R.string.onboarding_welcome2_title)
    val onboardingWelcome2Description = c.getString(R.string.onboarding_welcome2_description)
    val onboardingWelcome2Feature1 = c.getString(R.string.onboarding_welcome2_feature1)
    val onboardingWelcome2Feature2 = c.getString(R.string.onboarding_welcome2_feature2)
    val onboardingWelcome2Feature3 = c.getString(R.string.onboarding_welcome2_feature3)
    val onboardingWelcome3Title = c.getString(R.string.onboarding_welcome3_title)
    val onboardingWelcome3Description = c.getString(R.string.onboarding_welcome3_description)
    val onboardingWelcome3Feature1 = c.getString(R.string.onboarding_welcome3_feature1)
    val onboardingWelcome3Feature2 = c.getString(R.string.onboarding_welcome3_feature2)
    val onboardingWelcome3Feature3 = c.getString(R.string.onboarding_welcome3_feature3)
    val onboardingWelcomeCtaStart = c.getString(R.string.onboarding_welcome_cta1)
    val onboardingWelcomeCtaRestore = c.getString(R.string.onboarding_welcome_cta2)

    val setupVaultStartTitle = c.getString(R.string.onboarding_progress_start_title)
    val setupVaultStartDescription = c.getString(R.string.onboarding_progress_start_description)
    val setupVaultHalfWayTitle = c.getString(R.string.onboarding_progress_halfway_title)
    val setupVaultHalfWayDescription = c.getString(R.string.onboarding_progress_halfway_description)
    val setupVaultCompletedTitle = c.getString(R.string.onboarding_progress_completed_title)
    val setupVaultCompletedDescription = c.getString(R.string.onboarding_progress_completed_description)
    val setupVaultCompletedCta = c.getString(R.string.onboarding_progress_completed_cta)
    val setupVaultHeader = c.getString(R.string.onboarding_progress_steps_header)
    val setupGenerateSecretKeyTitle = c.getString(R.string.onboarding_progress_step1_title)
    val setupGenerateSecretKeyDescription = c.getString(R.string.onboarding_progress_step1_description)
    val setupCreateMasterPasswordTitle = c.getString(R.string.onboarding_progress_step2_title)
    val setupCreateMasterPasswordDescription = c.getString(R.string.onboarding_create_master_password_description)

    val generateSecretKeyTitle = c.getString(R.string.onboarding_generate_secret_key_title)
    val generateSecretKeyDescription = c.getString(R.string.onboarding_generate_secret_key_description)
    val generateSecretKeyCta = c.getString(R.string.onboarding_generate_secret_key_cta)
    val generateSecretKeySuccessTitle = c.getString(R.string.onboarding_generate_secret_key_success_title)
    val generateSecretKeySuccessDescription = c.getString(R.string.onboarding_generate_secret_key_success_description)
    val generateMasterPasswordTitle = c.getString(R.string.onboarding_create_master_password_title)
    val generateMasterPasswordDescription = c.getString(R.string.onboarding_create_master_password_description)
    val decryptionKitTitle = c.getString(R.string.decryption_kit_title)
    val decryptionKitDescription = c.getString(R.string.decryption_kit_description)
    val decryptionKitStep1 = c.getString(R.string.decryption_kit_step1)
    val decryptionKitStep2 = c.getString(R.string.decryption_kit_step2)
    val decryptionKitNoticeTitle = c.getString(R.string.decryption_kit_notice_title)
    val decryptionKitNotice = c.getString(R.string.decryption_kit_notice_msg)
    val decryptionKitCta = c.getString(R.string.decryption_kit_cta)
    val decryptionKitConfirmTitle = c.getString(R.string.decryption_kit_confirm_title)
    val decryptionKitConfirmDescription = c.getString(R.string.decryption_kit_confirm_description)

    val decryptionKitSaveModalTitle = c.getString(R.string.decryption_kit_save_modal_title)
    val decryptionKitSaveModalDescription = c.getString(R.string.decryption_kit_save_modal_description)
    val decryptionKitSaveModalCta1 = c.getString(R.string.decryption_kit_save_modal_cta1)
    val decryptionKitSaveModalCta2 = c.getString(R.string.decryption_kit_save_modal_cta2)
    val decryptionKitSaveToast = c.getString(R.string.decryption_kit_save_toast)

    val decryptionKitSettingsTitle = c.getString(R.string.decryption_kit_settings_title)
    val decryptionKitSettingsDescription = c.getString(R.string.decryption_kit_settings_description)
    val decryptionKitSettingsQrLabel = c.getString(R.string.decryption_kit_settings_qr_label)
    val decryptionKitSettingsSecretWords = c.getString(R.string.decryption_kit_settings_secret_words)
    val decryptionKitSettingsMasterKey = c.getString(R.string.decryption_kit_settings_master_key)
    val decryptionKitSettingsToggleTitle = c.getString(R.string.decryption_kit_settings_toggle_title)
    val decryptionKitSettingsToggleMsg = c.getString(R.string.decryption_kit_settings_toggle_msg)
    val decryptionKitSettingsCta = c.getString(R.string.decryption_kit_settings_cta)

    // Restore
    val restoreVaultSourceTitle = c.getString(R.string.restore_vault_source_title)
    val restoreVaultSourceDescription = c.getString(R.string.restore_vault_source_description)
    val restoreVaultSourceOptionGoogleDrive = c.getString(R.string.restore_vault_source_option_google_drive)
    val restoreVaultSourceOptionGoogleDriveDescription = c.getString(R.string.restore_vault_source_option_google_drive_description)
    val restoreVaultSourceOptionWebdav = c.getString(R.string.restore_vault_source_option_webdav)
    val restoreVaultSourceOptionWebdavDescription = c.getString(R.string.restore_vault_source_option_webdav_description)
    val restoreVaultSourceOptionFile = c.getString(R.string.restore_vault_source_option_file)
    val restoreVaultSourceOptionFileDescription = c.getString(R.string.restore_vault_source_option_file_description)

    val restoreWebdavTitle = c.getString(R.string.restore_webdav_title)
    val restoreWebdavDescription = c.getString(R.string.restore_webdav_description)

    val restoreCloudFilesTitle = c.getString(R.string.restore_cloud_files_title)
    val restoreCloudFilesDescription = c.getString(R.string.restore_cloud_files_description)
    val restoreCloudFilesEmptyDescription = c.getString(R.string.restore_cloud_files_empty_description)
    val restoreCloudFilesUpdatedAt = c.getString(R.string.restore_cloud_files_updated_at__0025_0040)

    val restoreDecryptVaultTitle = c.getString(R.string.restore_decrypt_vault_title)
    val restoreDecryptVaultDescription = c.getString(R.string.restore_decrypt_vault_description)
    val restoreDecryptVaultOptionFile = c.getString(R.string.restore_decrypt_vault_option_file)
    val restoreDecryptVaultOptionFileDescription = c.getString(R.string.restore_decrypt_vault_option_file_description)
    val restoreDecryptVaultOptionScanQr = c.getString(R.string.restore_decrypt_vault_option_scan_qr)
    val restoreDecryptVaultOptionScanQrDescription = c.getString(R.string.restore_decrypt_vault_option_scan_qr_description)
    val restoreDecryptVaultOptionManual = c.getString(R.string.restore_decrypt_vault_option_manual)
    val restoreDecryptVaultOptionManualDescription = c.getString(R.string.restore_decrypt_vault_option_manual_description)
    val restoreManualKeyInputTitle = c.getString(R.string.restore_manual_key_input_title)
    val restoreManualKeyInputDescription = c.getString(R.string.restore_manual_key_input_description)
    val restoreMasterPasswordTitle = c.getString(R.string.restore_master_password_title)
    val restoreMasterPasswordDescription = c.getString(R.string.restore_master_password_description)
    val restoreMasterPasswordLabel = c.getString(R.string.restore_master_password_label)
    val restoreReadingFileText = c.getString(R.string.restore_reading_file_text)
    val restoreImportingFileText = c.getString(R.string.restore_importing_file_text)
    val restoreImportingFileErrorText = c.getString(R.string.restore_importing_file_error_text)

    val restoreSuccessTitle = c.getString(R.string.restore_success_title)
    val restoreSuccessDescription = c.getString(R.string.restore_success_description)
    val restoreSuccessCta = c.getString(R.string.restore_success_cta)

    // Master Password / Authentication
    val masterPasswordLabel = c.getString(R.string.master_password_label)
    val masterPasswordConfirmLabel = c.getString(R.string.master_password_confirm_label)

    // Main
    val bottomBarPasswords = c.getString(R.string.bottom_bar_passwords)
    val bottomBarConnect = c.getString(R.string.bottom_bar_connect)
    val bottomBarSettings = c.getString(R.string.bottom_bar_settings)

    // Home
    val homeTitle = c.getString(R.string.home_title)
    val homeItemView = c.getString(R.string.login_view_action_view_details)
    val homeItemEdit = c.getString(R.string.common_edit)
    val homeItemCopyUsername = c.getString(R.string.login_view_action_copy_username)
    val homeItemCopyPassword = c.getString(R.string.login_view_action_copy_password)
    val homeItemOpenUri = c.getString(R.string.login_view_action_open_uri)
    val homeItemDelete = c.getString(R.string.login_view_action_delete)
    val homeEmptyTitle = c.getString(R.string.home_empty_title)
    val homeEmptyImportCta = c.getString(R.string.home_empty_import_cta)
    val homeListOptionsModalTitle = c.getString(R.string.home_list_options_modal_title)

    // Login
    val loginPassword = c.getString(R.string.login_password_label)
    val loginAddTitle = c.getString(R.string.login_add_title)
    val loginEditTitle = c.getString(R.string.login_edit_title)
    val loginName = c.getString(R.string.login_name_label)
    val loginUsername = c.getString(R.string.login_username_label)
    val loginNotes = c.getString(R.string.login_notes_label)
    val loginSecurityLevel = c.getString(R.string.login_security_level_label)
    val loginUri = c.getString(R.string.login_uri_label)
    val loginAddUri = c.getString(R.string.login_add_uri_cta)
    val loginUnsavedChangesDialogTitle = c.getString(R.string.login_unsaved_changes_dialog_title)
    val loginUnsavedChangesDialogDescription = c.getString(R.string.login_unsaved_changes_dialog_description)
    val loginNoItemName = c.getString(R.string.login_no_item_name)

    // Connect
    val connectTitle = c.getString(R.string.connect_title)

    // Settings
    val settingsTitle = c.getString(R.string.settings_title)
    val settingsHeaderPrefs = c.getString(R.string.settings_header_preferences)
    val settingsHeaderBrowserExtension = c.getString(R.string.settings_header_browser_extension)
    val settingsHeaderBackup = c.getString(R.string.settings_header_backup)
    val settingsHeaderAbout = c.getString(R.string.settings_header_about)
    val settingsEntryCustomization = c.getString(R.string.settings_entry_customization)
    val settingsEntryCustomizationDesc = c.getString(R.string.settings_entry_customization_description)
    val settingsEntryAutofill = c.getString(R.string.settings_entry_autofill)
    val settingsEntryAutofillDesc = c.getString(R.string.settings_entry_autofill_description)
    val settingsEntrySecurity = c.getString(R.string.settings_entry_security)
    val settingsEntrySecurityDesc = c.getString(R.string.settings_entry_security_description)
    val settingsEntryTheme = c.getString(R.string.settings_entry_theme)
    val settingsEntryConvenience = c.getString(R.string.settings_entry_convenience)
    val settingsEntryDynamicColors = c.getString(R.string.settings_entry_dynamic_colors)
    val settingsEntryDynamicColorsDesc = c.getString(R.string.settings_entry_dynamic_colors_description)
    val settingsEntryLoginClickAction = c.getString(R.string.settings_entry_login_click_action)
    val settingsEntryLoginClickActionDesc = c.getString(R.string.settings_entry_login_click_action_description)
    val settingsEntryDeviceNickname = c.getString(R.string.settings_entry_device_nickname)
    val settingsEntryDeviceNicknameDesc = c.getString(R.string.settings_entry_device_nickname_description)
    val settingsAutofillService = c.getString(R.string.settings_autofill_service)
    val settingsAutofillServiceDesc = c.getString(R.string.settings_autofill_service_description)
    val settingsAutofillKeyboard = c.getString(R.string.settings_autofill_keyboard)
    val settingsAutofillKeyboardDesc = c.getString(R.string.settings_autofill_keyboard_description)
    val settingsEntryCloudSync = c.getString(R.string.settings_entry_cloud_sync)
    val settingsEntryCloudSyncDesc = c.getString(R.string.settings_entry_cloud_sync_description)
    val settingsEntryImportExport = c.getString(R.string.settings_entry_import_export)
    val settingsEntryImportExportDesc = c.getString(R.string.settings_entry_import_export_description)
    val settingsEntryTransferFromOtherApps = c.getString(R.string.settings_entry_transfer_from_other_apps)
    val settingsEntryTransferFromOtherAppsDesc = c.getString(R.string.settings_entry_transfer_from_other_apps_description)
    val settingsEntryTrash = c.getString(R.string.settings_entry_trash)
    val settingsEntryTrashDesc = c.getString(R.string.settings_entry_trash_description)
    val settingsEntryAbout = c.getString(R.string.settings_entry_about)
    val settingsEntrySubscription = c.getString(R.string.settings_entry_subscription)
    val settingsAbout = c.getString(R.string.settings_about)
    val settingsEntryImportExport2Pass = c.getString(R.string.settings_entry_import_export_2pass)
    val settingsEntryImport2Pass = c.getString(R.string.settings_entry_import_2pass)
    val settingsEntryExport2Pass = c.getString(R.string.settings_entry_export_2pass)
    val settingsEntryImportOtherApps = c.getString(R.string.settings_entry_import_other_apps)
    val settingsEntryCloudSyncProvider = c.getString(R.string.settings_entry_cloud_sync_provider)
    val settingsEntryGoogleDrive = c.getString(R.string.settings_entry_google_drive)
    val settingsEntryGoogleDriveSync = c.getString(R.string.settings_entry_google_drive_sync)
    val settingsEntryGoogleDriveSyncDesc = c.getString(R.string.settings_entry_google_drive_sync_description)
    val settingsEntryGoogleDriveSyncExplanation =
        c.getString(R.string.settings_entry_google_drive_sync_explanation)
    val settingsEntrySyncInfo = c.getString(R.string.settings_entry_sync_info)
    val settingsEntrySyncAccount = c.getString(R.string.settings_entry_sync_account)
    val settingsEntrySyncLast = c.getString(R.string.settings_entry_sync_last)
    val settingsEntryWebDav = c.getString(R.string.settings_entry_webdav)
    val settingsEntryAppAccess = c.getString(R.string.settings_entry_app_access)
    val settingsEntryDataAccess = c.getString(R.string.settings_entry_data_access)
    val settingsEntryChangePassword = c.getString(R.string.settings_entry_change_password)
    val settingsEntryBiometrics = c.getString(R.string.settings_entry_biometrics)
    val settingsEntryBiometricsDesc = c.getString(R.string.settings_entry_biometrics_description)
    val settingsEntryLockoutSettings = c.getString(R.string.settings_entry_lockout_settings)
    val settingsEntryLockoutSettingsDesc = c.getString(R.string.settings_entry_lockout_settings_description)
    val settingsEntryDecryptionKit = c.getString(R.string.settings_entry_decryption_kit)
    val settingsEntryAppLockTime = c.getString(R.string.settings_entry_app_lock_time)
    val settingsEntryAppLockTimeDesc = c.getString(R.string.settings_entry_app_lock_time_description)
    val settingsEntryAppLockAttempts = c.getString(R.string.settings_entry_app_lock_attempts)
    val settingsEntryAppLockAttemptsDesc = c.getString(R.string.settings_entry_app_lock_attempts_description)
    val settingsEntryAutofillLockTime = c.getString(R.string.settings_entry_autofill_lock_time)
    val settingsEntryAutofillLockTimeDesc = c.getString(R.string.settings_entry_autofill_lock_time_description)
    val settingsEntrySecurityTier = c.getString(R.string.settings_entry_protection_level)
    val settingsEntrySecurityTierDesc = c.getString(R.string.settings_entry_protection_level_description)
    val settingsHeaderSecurityTier = c.getString(R.string.settings_header_protection_level)
    val settingsEntrySecurityTier1 = c.getString(R.string.settings_entry_protection_level0)
    val settingsEntrySecurityTier1Desc = c.getString(R.string.settings_entry_protection_level0_description)
    val settingsEntrySecurityTier2 = c.getString(R.string.settings_entry_protection_level1)
    val settingsEntrySecurityTier2Desc = c.getString(R.string.settings_entry_protection_level1_description)
    val settingsEntrySecurityTier3 = c.getString(R.string.settings_entry_protection_level2)
    val settingsEntrySecurityTier3Desc = c.getString(R.string.settings_entry_protection_level2_description)
    val settingsEntryScreenCapture = c.getString(R.string.settings_entry_screen_capture)
    val settingsEntryScreenCaptureDesc = c.getString(R.string.settings_entry_screen_capture_description)
    val settingsEntryScreenshotsConfirmTitle =
        c.getString(R.string.settings_entry_screenshots_confirm_title)
    val settingsEntryScreenshotsConfirmDesc =
        c.getString(R.string.settings_entry_screenshots_confirm_description)
    val settingsEntryKnownBrowsers = c.getString(R.string.settings_entry_known_browsers)
    val settingsEntryKnownBrowsersDesc = c.getString(R.string.settings_entry_known_browsers_description)
    val settingsEntryPushNotification = c.getString(R.string.settings_entry_push_notifications)
    val settingsEntryPushNotificationDesc = c.getString(R.string.settings_entry_push_notifications_description)
    val settingsEntryHelpCenter = c.getString(R.string.settings_entry_help_center)
    val settingsEntryDiscord = c.getString(R.string.settings_entry_discord)
    val settingsEntryManageTags = c.getString(R.string.settings_entry_manage_tags)
    val settingsEntryManageTagsDescription = c.getString(R.string.settings_entry_manage_tags_description)

    val webdavServerUrl = c.getString(R.string.webdav_server_url)
    val webdavAllowUntrustedCertificates = c.getString(R.string.webdav_allow_untrusted_certificates)
    val webdavCredentials = c.getString(R.string.webdav_credentials)
    val webdavUsername = c.getString(R.string.webdav_username)
    val webdavPassword = c.getString(R.string.webdav_password)
    val webdavConnect = c.getString(R.string.webdav_connect)

    // About
    val aboutTagline = c.getString(R.string.about_tagline)
    val aboutVersionPrefix = c.getString(R.string.about_version_prefix)
    val aboutSectionGeneral = c.getString(R.string.about_section_general)
    val aboutRateUs = c.getString(R.string.about_rate_us)
    val aboutPrivacyPolicy = c.getString(R.string.about_privacy_policy)
    val aboutTermsOfUse = c.getString(R.string.about_terms_of_use)
    val aboutOpenSourceLicenses = c.getString(R.string.about_open_source_licenses)
    val aboutSectionShare = c.getString(R.string.about_section_share)
    val aboutInviteFriends = c.getString(R.string.about_invite_friends)
    val aboutInviteFriendsShareText = c.getString(R.string.about_invite_friends_share_text)
    val aboutSectionConnect = c.getString(R.string.about_section_connect)
    val aboutDiscord = c.getString(R.string.about_discord)
    val aboutGithub = c.getString(R.string.about_github)
    val aboutX = c.getString(R.string.about_x)
    val aboutYoutube = c.getString(R.string.about_youtube)
    val aboutLinkedin = c.getString(R.string.about_linkedin)
    val aboutReddit = c.getString(R.string.about_reddit)
    val aboutFacebook = c.getString(R.string.about_facebook)
    val aboutSectionCrashReporting = c.getString(R.string.about_section_crash_reporting)
    val aboutSendCrashReports = c.getString(R.string.about_send_crash_reports)
    val aboutCrashReportsDescription = c.getString(R.string.about_crash_reports_description)

    // Authentication Form Strings
    val authUseBiometrics = c.getString(R.string.auth_use_biometrics)
    val authBiometricsModalTitle = c.getString(R.string.auth_biometrics_modal_title)
    val authBiometricsDisabledMessage = c.getString(R.string.auth_biometrics_disabled_message)
    val authPreviewTitle = c.getString(R.string.auth_preview_title)
    val authPreviewDescription = c.getString(R.string.auth_preview_description)
    val authPreviewCta = c.getString(R.string.auth_preview_cta)

    val autofillPromptTitle = c.getString(R.string.autofill_prompt_title)
    val autofillPromptDescription = c.getString(R.string.autofill_prompt_description)
    val autofillPromptCta = c.getString(R.string.autofill_prompt_cta)

    val autofillLoginDialogTitle = c.getString(R.string.autofill_login_dialog_title)
    val autofillLoginDialogBodyPrefix = c.getString(R.string.autofill_login_dialog_body_prefix)
    val autofillLoginDialogBodySuffix = c.getString(R.string.autofill_login_dialog_body_suffix)
    val autofillLoginDialogPositive = c.getString(R.string.autofill_login_dialog_positive)
    val autofillLoginDialogNeutral = c.getString(R.string.autofill_login_dialog_neutral)

    val connectQrInstruction = c.getString(R.string.connect_qr_instruction)
    val connectInvalidSignatureTitle = c.getString(R.string.connect_invalid_signature_title)
    val connectInvalidSignatureMessage = c.getString(R.string.connect_invalid_signature_message)
    val connectConfirmTitle = c.getString(R.string.connect_confirm_title)
    val connectConfirmMessage = c.getString(R.string.connect_confirm_message)

    val enterCurrentPasswordTitle = c.getString(R.string.enter_current_password_title)
    val enterCurrentPasswordDescription = c.getString(R.string.enter_current_password_description)

    val exportBackupModalTitle = c.getString(R.string.export_backup_modal_title)
    val exportBackupModalEncryptedTitle = c.getString(R.string.export_backup_modal_encrypted_title)
    val exportBackupModalEncryptedDescription = c.getString(R.string.export_backup_modal_encrypted_description)
    val exportBackupModalShare = c.getString(R.string.export_backup_modal_share)
    val exportBackupModalSaveToFile = c.getString(R.string.export_backup_modal_save_to_file)

    val loginFilterModalTitle = c.getString(R.string.login_filter_modal_title)
    val loginFilterModalSortNameAsc = c.getString(R.string.login_filter_modal_sort_name_asc)
    val loginFilterModalSortNameDesc = c.getString(R.string.login_filter_modal_sort_name_desc)
    val loginFilterModalSortCreationDateAsc = c.getString(R.string.login_filter_modal_sort_creation_date_asc)
    val loginFilterModalSortCreationDateDesc = c.getString(R.string.login_filter_modal_sort_creation_date_desc)

    val knownBrowserLastConnectionPrefix = c.getString(R.string.known_browser_last_connection_prefix)
    val knownBrowserFirstConnectionPrefix = c.getString(R.string.known_browser_first_connection_prefix)
    val knownBrowserDeleteButton = c.getString(R.string.known_browser_delete_button)
    val knownBrowserDeleteDialogTitle = c.getString(R.string.known_browser_delete_dialog_title)
    val knownBrowserDeleteDialogBody = c.getString(R.string.known_browser_delete_dialog_body)
    val knownBrowsersTitle = c.getString(R.string.known_browsers_title)
    val knownBrowsersDescription = c.getString(R.string.known_browsers_description)
    val knownBrowsersEmpty = c.getString(R.string.known_browsers_empty)

    val lockoutSettingsAppLockoutHeader = c.getString(R.string.lockout_settings_app_lockout_header)
    val lockoutSettingsAutofillLockoutHeader = c.getString(R.string.lockout_settings_autofill_lockout_header)
    val lockoutSettingsAppLockTimeImmediately = c.getString(R.string.lockout_settings_app_lock_time_immediately)
    val lockoutSettingsAppLockTimeSeconds30 = c.getString(R.string.lockout_settings_app_lock_time_seconds_30)
    val lockoutSettingsAppLockTimeMinute1 = c.getString(R.string.lockout_settings_app_lock_time_minute_1)
    val lockoutSettingsAppLockTimeMinutes5 = c.getString(R.string.lockout_settings_app_lock_time_minutes_5)
    val lockoutSettingsAppLockTimeHour1 = c.getString(R.string.lockout_settings_app_lock_time_hour_1)
    val lockoutSettingsAppLockAttemptsCount3 = c.getString(R.string.lockout_settings_app_lock_attempts_count_3)
    val lockoutSettingsAppLockAttemptsCount5 = c.getString(R.string.lockout_settings_app_lock_attempts_count_5)
    val lockoutSettingsAppLockAttemptsCount10 = c.getString(R.string.lockout_settings_app_lock_attempts_count_10)
    val lockoutSettingsAppLockAttemptsNoLimit = c.getString(R.string.lockout_settings_app_lock_attempts_no_limit)
    val lockoutSettingsAutofillLockTimeMinutes5 = c.getString(R.string.lockout_settings_autofill_lock_time_minutes_5)
    val lockoutSettingsAutofillLockTimeMinutes15 = c.getString(R.string.lockout_settings_autofill_lock_time_minutes_15)
    val lockoutSettingsAutofillLockTimeMinutes30 = c.getString(R.string.lockout_settings_autofill_lock_time_minutes_30)
    val lockoutSettingsAutofillLockTimeHour1 = c.getString(R.string.lockout_settings_autofill_lock_time_hour_1)
    val lockoutSettingsAutofillLockTimeDay1 = c.getString(R.string.lockout_settings_autofill_lock_time_day_1)
    val lockoutSettingsAutofillLockTimeNever = c.getString(R.string.lockout_settings_autofill_lock_time_never)

    val lockScreenBiometricsPromptTitle = c.getString(R.string.lock_screen_biometrics_prompt_title)
    val lockScreenBiometricsPromptBody = c.getString(R.string.lock_screen_biometrics_prompt_body)
    val lockScreenBiometricsModalTitle = c.getString(R.string.lock_screen_biometrics_modal_title)
    val lockScreenBiometricsModalSubtitle = c.getString(R.string.lock_screen_biometrics_modal_subtitle)
    val lockScreenBiometricsErrorTooManyAttempts = c.getString(R.string.lock_screen_biometrics_error_too_many_attempts)
    val lockScreenBiometricsErrorTitle = c.getString(R.string.lock_screen_biometrics_error_title)
    val lockScreenUnlockTitle = c.getString(R.string.lock_screen_unlock_title)
    val lockScreenUnlockDescription = c.getString(R.string.lock_screen_unlock_description)
    val lockScreenUnlockCta = c.getString(R.string.lock_screen_unlock_cta)

    val loginDeleteConfirmTitle = c.getString(R.string.login_delete_confirm_title)
    val loginDeleteConfirmBody = c.getString(R.string.login_delete_confirm_body)

    val loginSearchNoResultsTitle = c.getString(R.string.login_search_no_results_title)
    val loginSearchNoResultsDescription = c.getString(R.string.login_search_no_results_description)

    val scanDecryptionKitTitle = c.getString(R.string.scan_decryption_kit_title)
    val scanDecryptionKitDescription = c.getString(R.string.scan_decryption_kit_description)

    val securityBiometricsEnableTitle = c.getString(R.string.security_biometrics_enable_title)
    val securityBiometricsEnableDescription = c.getString(R.string.security_biometrics_enable_description)
    val securityBiometricsEnableCta = c.getString(R.string.security_biometrics_enable_cta)

    val securityLockoutSettingsTitle = c.getString(R.string.security_lockout_settings_title)
    val securityLockoutSettingsDescription = c.getString(R.string.security_lockout_settings_description)
    val securityLockoutSettingsCta = c.getString(R.string.security_lockout_settings_cta)

    val securityScreenCaptureEnableTitle = c.getString(R.string.security_screen_capture_enable_title)
    val securityScreenCaptureEnableDescription = c.getString(R.string.security_screen_capture_enable_description)
    val securityScreenCaptureEnableCta = c.getString(R.string.security_screen_capture_enable_cta)

    val securityDecryptionKitAccessTitle = c.getString(R.string.security_decryption_kit_access_title)
    val securityDecryptionKitAccessDescription = c.getString(R.string.security_decryption_kit_access_description)
    val securityDecryptionKitAccessCta = c.getString(R.string.security_decryption_kit_access_cta)
    val biometricsModalTitle = c.getString(R.string.biometrics_modal_title)
    val biometricsModalSubtitleEnable = c.getString(R.string.biometrics_modal_subtitle_enable)
    val biometricsModalErrorTooManyAttempts = c.getString(R.string.biometrics_modal_error_too_many_attempts)
    val biometricsErrorDialogTitle = c.getString(R.string.biometrics_error_dialog_title)

    val securityTypeModalHeader = c.getString(R.string.security_type_modal_header)
    val securityTypeModalDescription = c.getString(R.string.security_type_modal_description)

    val setNewPasswordScreenTitle = c.getString(R.string.set_new_password_screen_title)
    val setNewPasswordScreenDescription = c.getString(R.string.set_new_password_screen_description)
    val setNewPasswordConfirmTitle = c.getString(R.string.set_new_password_confirm_title)
    val setNewPasswordConfirmBodyPart1 = c.getString(R.string.set_new_password_confirm_body_part1_ios)
    val setNewPasswordConfirmBodyPart2 = c.getString(R.string.set_new_password_confirm_body_part2_ios)

    val uriSettingsModalHeader = c.getString(R.string.uri_settings_modal_header)
    val uriSettingsMatchingRuleHeader = c.getString(R.string.uri_settings_matching_rule_header)
    val uriSettingsModalDescription = c.getString(R.string.uri_settings_modal_description)
    val loginUriMatcherDomainTitle = c.getString(R.string.login_uri_matcher_domain_title)
    val loginUriMatcherHostTitle = c.getString(R.string.login_uri_matcher_host_title)
    val loginUriMatcherStartsWithTitle = c.getString(R.string.login_uri_matcher_starts_with_title)
    val loginUriMatcherExactTitle = c.getString(R.string.login_uri_matcher_exact_title)
    val loginUriMatcherDomainDescription = c.getString(R.string.login_uri_matcher_domain_description)
    val loginUriMatcherHostDescription = c.getString(R.string.login_uri_matcher_host_description)
    val loginUriMatcherStartsWithDescription = c.getString(R.string.login_uri_matcher_starts_with_description)
    val loginUriMatcherExactDescription = c.getString(R.string.login_uri_matcher_exact_description)

    // Connect Modal
    val connectModalHeaderTitle = c.getString(R.string.connect_modal_header_title)
    val connectModalLoading = c.getString(R.string.connect_modal_loading)

    val connectModalUnknownBrowserTitle = c.getString(R.string.connect_modal_unknown_browser_title)
    val connectModalUnknownBrowserSubtitle = c.getString(R.string.connect_modal_unknown_browser_subtitle)
    val connectModalUnknownBrowserCtaPositive = c.getString(R.string.connect_modal_unknown_browser_cta_positive)
    val connectModalUnknownBrowserCtaNegative = c.getString(R.string.connect_modal_unknown_browser_cta_negative)

    val connectModalSuccessTitle = c.getString(R.string.connect_modal_success_title)
    val connectModalSuccessSubtitle = c.getString(R.string.connect_modal_success_subtitle)
    val connectModalSuccessCta = c.getString(R.string.connect_modal_success_cta)
    val connectModalSuccessToast = c.getString(R.string.connect_modal_success_toast)

    val connectModalErrorGenericTitle = c.getString(R.string.connect_modal_error_generic_title)
    val connectModalErrorGenericSubtitle = c.getString(R.string.connect_modal_error_generic_subtitle)
    val connectModalErrorGenericCta = c.getString(R.string.connect_modal_error_generic_cta)

    val connectModalErrorExtensionsLimitTitle = c.getString(R.string.connect_modal_error_extensions_limit_title)
    val connectModalErrorExtensionsLimitSubtitle = c.getString(R.string.connect_modal_error_extensions_limit_subtitle)
    val connectModalErrorExtensionsLimitCta = c.getString(R.string.connect_modal_error_extensions_limit_cta)

    val connectModalErrorAppUpdateRequiredTitle = c.getString(R.string.connect_modal_error_app_update_required_title)
    val connectModalErrorAppUpdateRequiredSubtitle = c.getString(R.string.connect_modal_error_app_update_required_subtitle)
    val connectModalErrorAppUpdateRequiredCta = c.getString(R.string.connect_modal_error_app_update_required_cta_android)

    val connectModalErrorBrowserExtensionUpdateRequiredTitle = c.getString(R.string.connect_modal_error_browser_extension_update_required_title)
    val connectModalErrorBrowserExtensionUpdateRequiredSubtitle = c.getString(R.string.connect_modal_error_browser_extension_update_required_subtitle)
    val connectModalErrorBrowserExtensionUpdateRequiredCta = c.getString(R.string.common_close)

    // Request Modal
    val requestModalHeaderTitle = c.getString(R.string.request_modal_header_title)
    val requestModalLoading = c.getString(R.string.request_modal_loading)

    val requestModalPasswordRequestTitle = c.getString(R.string.request_modal_password_request_title)
    val requestModalPasswordRequestSubtitle = c.getString(R.string.request_modal_password_request_subtitle)
    val requestModalPasswordRequestCtaPositive = c.getString(R.string.request_modal_password_request_cta_positive)
    val requestModalPasswordRequestCtaNegative = c.getString(R.string.request_modal_password_request_cta_negative)

    val requestModalNewItemTitle = c.getString(R.string.request_modal_new_item_title)
    val requestModalNewItemSubtitle = c.getString(R.string.request_modal_new_item_subtitle)
    val requestModalNewItemCtaPositive = c.getString(R.string.request_modal_new_item_cta_positive)
    val requestModalNewItemCtaNegative = c.getString(R.string.request_modal_new_item_cta_negative)

    val requestModalUpdateItemTitle = c.getString(R.string.request_modal_update_item_title)
    val requestModalUpdateItemSubtitle = c.getString(R.string.request_modal_update_item_subtitle)
    val requestModalUpdateItemCtaPositive = c.getString(R.string.request_modal_update_item_cta_positive)
    val requestModalUpdateItemCtaNegative = c.getString(R.string.request_modal_update_item_cta_negative)

    val requestModalRemoveItemTitle = c.getString(R.string.request_modal_remove_item_title)
    val requestModalRemoveItemSubtitle = c.getString(R.string.request_modal_remove_item_subtitle)
    val requestModalRemoveItemCtaPositive = c.getString(R.string.request_modal_remove_item_cta_positive)
    val requestModalRemoveItemCtaNegative = c.getString(R.string.request_modal_remove_item_cta_negative)

    val requestModalErrorGenericTitle = c.getString(R.string.request_modal_error_generic_title)
    val requestModalErrorGenericSubtitle = c.getString(R.string.request_modal_error_generic_subtitle)
    val requestModalErrorGenericCta = c.getString(R.string.request_modal_error_generic_cta)

    val requestModalErrorNoItemTitle = c.getString(R.string.request_modal_error_no_item_title)
    val requestModalErrorNoItemSubtitle = c.getString(R.string.request_modal_error_no_item_subtitle)
    val requestModalErrorNoItemCta = c.getString(R.string.request_modal_error_no_item_cta)

    val requestModalErrorItemsLimitTitle = c.getString(R.string.request_modal_error_items_limit_title)
    val requestModalErrorItemsLimitSubtitle = c.getString(R.string.request_modal_error_items_limit_subtitle)
    val requestModalErrorItemsLimitCta = c.getString(R.string.request_modal_error_items_limit_cta)

    val requestModalToastCancel = c.getString(R.string.request_modal_toast_cancel)
    val requestModalToastAddLogin = c.getString(R.string.request_modal_toast_success_add_login)
    val requestModalToastUpdateLogin = c.getString(R.string.request_modal_toast_success_update_login)
    val requestModalToastDeleteItem = c.getString(R.string.request_modal_toast_success_delete_login)
    val requestModalToastPasswordRequest = c.getString(R.string.request_modal_toast_success_password_request)
    val lockScreenTryAgainIn = c.getString(R.string.lock_screen_try_again__0025_0040)

    val transferServicesDisclaimer = c.getString(R.string.transfer_services_list_footer)

    val subscriptionFreePlan = c.getString(R.string.subscription_free_plan)
    val subscriptionPaidPlan = "Unlimited"

    val setupConnectIntroTitle = c.getString(R.string.connect_intro_header)
    val setupConnectIntroDescription = c.getString(R.string.connect_intro_description)
    val setupConnectLearnMore = c.getString(R.string.connect_intro_learn_more_cta)

    val setupConnectTitle = c.getString(R.string.connect_setup_header)
    val setupConnectDescription = c.getString(R.string.connect_setup_description)
    val setupConnectStepCameraTitle = c.getString(R.string.connect_setup_camera_step_title)
    val setupConnectStepCameraDescription = c.getString(R.string.connect_setup_camera_step_description)
    val setupConnectStepNotificationsTitle = c.getString(R.string.connect_setup_push_step_title)
    val setupConnectStepNotificationsDescription = c.getString(R.string.connect_setup_push_step_description)

    val paywallNoticeCta = c.getString(R.string.paywall_notice_cta)
    val paywallNoticeItemsLimitReachedTitle = c.getString(R.string.paywall_notice_items_limit_reached_title)
    val paywallNoticeItemsLimitReachedMsg = c.getString(R.string.paywall_notice_items_limit_reached_msg)
    val paywallNoticeItemsLimitImportTitle = c.getString(R.string.paywall_notice_items_limit_import_title)
    val paywallNoticeItemsLimitImportMsg = c.getString(R.string.paywall_notice_items_limit_import_msg)
    val paywallNoticeItemsLimitTransferTitle = c.getString(R.string.paywall_notice_items_limit_transfer_title)
    val paywallNoticeItemsLimitTransferMsg = c.getString(R.string.paywall_notice_items_limit_transfer_msg)
    val paywallNoticeItemsLimitRestoreTitle = c.getString(R.string.paywall_notice_items_limit_restore_title)
    val paywallNoticeItemsLimitRestoreMsg = c.getString(R.string.paywall_notice_items_limit_restore_msg)

    val migrationErrorTitle = c.getString(R.string.migration_error_title)
    val migrationErrorBody = c.getString(R.string.migration_error_body)

    val quickSetupTitle = c.getString(R.string.quick_setup_title)
    val quickSetupDescription = c.getString(R.string.quick_setup_subtitle)
    val quickSetupRecommended = c.getString(R.string.quick_setup_recommended)
    val quickSetupAutofillTitle = c.getString(R.string.quick_setup_autofill_title)
    val quickSetupAutofillDescription = c.getString(R.string.quick_setup_autofill_description)
    val quickSetupSyncTitle = c.getString(R.string.quick_setup_drive_sync_title)
    val quickSetupSyncDescription = c.getString(R.string.quick_setup_drive_sync_description)
    val quickSetupSecurityTierTitle = c.getString(R.string.quick_setup_security_tier_title)
    val quickSetupSecurityTierDescription = c.getString(R.string.quick_setup_security_tier_description)
    val quickSetupSecurityTierDefault = c.getString(R.string.quick_setup_security_tier_default_label)
    val quickSetupImportItemsCta = c.getString(R.string.quick_setup_import_items_cta)
    val quickSetupTransferItemsCta = c.getString(R.string.quick_setup_transfer_items_cta)

    // Tags
    val tagDeleteCta = c.getString(R.string.tag_delete_cta)
    val tagDescription = c.getString(R.string.tag_description)
    val tagDeleteConfirmTitle = c.getString(R.string.tag_delete_confirm_title)
    val tagDeleteConfirmDescription = c.getString(R.string.tag_delete_confirm_description)
    val tagsEmptyList = c.getString(R.string.tags_empty_list)
    val tagsTitle = c.getString(R.string.tags_title)
    val selectTagsTitle = c.getString(R.string.select_tags_title)
    val tagsAddNewCta = c.getString(R.string.tags_add_new_cta)
    val tagEditorPlaceholder = c.getString(R.string.tag_editor_placeholder)
    val tagEditorDescription = c.getString(R.string.tag_editor_description)
    val tagEditorEditTitle = c.getString(R.string.tag_editor_edit_title)
    val tagEditorNewTitle = c.getString(R.string.tag_editor_new_title)
    val loginTagsHeader = c.getString(R.string.login_tags_header)
    val loginTags = c.getString(R.string.login_tags)
    val loginTagsDescription = c.getString(R.string.login_tags_description)
    val loginSelectedTags = c.getString(R.string.login_selected_tags)
    val cloudSyncInvalidSchemaErrorCta = c.getString(R.string.cloud_sync_invalid_schema_error_cta)
    val cloudSyncInvalidSchemaErrorMsg = c.getString(R.string.cloud_sync_invalid_schema_error_msg)
    val importInvalidSchemaErrorCta = c.getString(R.string.import_invalid_schema_error_cta)
    val importInvalidSchemaErrorMsg = c.getString(R.string.import_invalid_schema_error_msg)

    val appUpdateModalTitle = c.getString(R.string.app_update_modal_title)
    val appUpdateModalSubtitle = c.getString(R.string.app_update_modal_subtitle)
    val appUpdateModalCtaPositive = c.getString(R.string.app_update_modal_cta_positive)
    val appUpdateModalCtaNegative = c.getString(R.string.app_update_modal_cta_negative)

    fun formatDuration(millis: Long): String {
        val diff = System.currentTimeMillis() - millis
        val diffSign = diff.sign
        val diffAbs = abs(diff)

        timeUnits.forEach { timeUnit ->
            val diffInUnit = diffAbs / timeUnit.millis
            val diffInUnitModulo = diffAbs % timeUnit.millis
            val timeUnitHalf = timeUnit.millis / 2

            if (diffInUnit >= 1 && diffInUnitModulo >= timeUnitHalf) {
                return format(c, diffInUnit + 1, diffSign, timeUnit)
            }

            if (diffInUnit >= 1 && diffInUnitModulo < timeUnitHalf) {
                return format(c, diffInUnit, diffSign, timeUnit)
            }
        }

        return format(c, diff, diffSign, timeUnits.last())
    }

    private fun format(context: Context, quantity: Long, sign: Int, timeUnit: TimeUnit): String {
        return c.resources.getQuantityString(
            if (sign > 0) timeUnit.pastStringRes else timeUnit.pastStringRes, // Handle future values
            quantity.toInt(),
            quantity.toInt(),
        )
    }

    private enum class TimeUnit(val millis: Long, val pastStringRes: Int) {
        Second(1_000L, R.plurals.past_duration_seconds),
        Minute(60 * 1_000L, R.plurals.past_duration_minutes),
        Hour(60 * 60 * 1_000L, R.plurals.past_duration_hours),
        Day(24 * 60 * 60 * 1_000L, R.plurals.past_duration_days),
        Week(7 * 24 * 60 * 60 * 1_000L, R.plurals.past_duration_weeks),
        Month(4 * 7 * 24 * 60 * 60 * 1_000L, R.plurals.past_duration_months),
    }
}