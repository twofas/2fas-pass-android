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
    val appName = c.getString(R.string.app_name)
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
    val commonUseDefault = c.getString(R.string.common_use_default)
    val commonCreated = c.getString(R.string.common_created)
    val commonClose = c.getString(R.string.common_close)
    val commonConfirm = c.getString(R.string.common_confirm)
    val commonCopy = c.getString(R.string.common_copy)
    val commonStatus = c.getString(R.string.common_status)
    val commonOpen = c.getString(R.string.common_open)
    val commonOpenSystemSettings = c.getString(R.string.common_open_system_settings)
    val commonBack = c.getString(R.string.common_back)
    val commonHelp = c.getString(R.string.common_help)
    val commonDisabled = c.getString(R.string.common_disabled)
    val commonEnabled = c.getString(R.string.common_enabled)
    val commonOff = c.getString(R.string.common_off)
    val commonOn = c.getString(R.string.common_on)
    val commonModified = c.getString(R.string.common_modified)
    val commonCopied = c.getString(R.string.common_copied)
    val commonGeneralErrorTryAgain = c.getString(R.string.common_general_error_try_again)
    val commonSuggested = c.getString(R.string.common_suggested)
    val commonPasswords = c.getString(R.string.common_passwords)
    val commonDecrypting = c.getString(R.string.common_decrypting)
    val commonSettings = c.getString(R.string.common_settings)
    val commonDone = c.getString(R.string.common_done)
    val commonLoading = c.getString(R.string.common_loading)

    val generalNotAvailable = c.getString(R.string.general_not_available)
    val generalNetworkErrorDetails = c.getString(R.string.general_network_error_details)
    val generalServerErrorDetails = c.getString(R.string.general_server_error_details)
    val generalErrorNoLocalVault = c.getString(R.string.general_error_no_local_vault)

    // Permissions
    val permissionCameraTitle = c.getString(R.string.permission_camera_title)
    val permissionCameraMsg = c.getString(R.string.permission_camera_msg)
    val permissionPushTitle = c.getString(R.string.permission_notifications_title)
    val permissionPushMsg = c.getString(R.string.permission_notifications_msg)

    val pushBrowserRequestTitle = c.getString(R.string.push_browser_request_title)
    val pushBrowserRequestMessage = c.getString(R.string.push_browser_request_message)
    val pushBrowserRequestGenericMessage = c.getString(R.string.push_browser_request_generic_message)

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

    val onboardingProgressStep2Description = c.getString(R.string.onboarding_progress_step2_description)
    val onboardingCreateMasterPasswordGuideTitle =
        c.getString(R.string.onboarding_create_master_password_guide_title)
    val onboardingCreateMasterPasswordGuideDescription =
        c.getString(R.string.onboarding_create_master_password_guide_description)
    val onboardingGuide1 = c.getString(R.string.onboarding_guide_1)
    val onboardingGuide2 = c.getString(R.string.onboarding_guide_2)
    val onboardingGuide3 = c.getString(R.string.onboarding_guide_3)
    val onboardingGuide4 = c.getString(R.string.onboarding_guide_4)

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
    val decryptionKitSettingsSecretWordsIos =
        c.getString(R.string.decryption_kit_settings_secret_words_ios)
    val decryptionKitSettingsMasterKey = c.getString(R.string.decryption_kit_settings_master_key)
    val decryptionKitSettingsToggleTitle = c.getString(R.string.decryption_kit_settings_toggle_title)
    val decryptionKitSettingsToggleMsg = c.getString(R.string.decryption_kit_settings_toggle_msg)
    val decryptionKitSettingsCta = c.getString(R.string.decryption_kit_settings_cta)
    val decryptionKeyShareSheetTitle = c.getString(R.string.decryption_key_share_sheet_title)

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
    val importVaultErrorCorruptFile = c.getString(R.string.import_vault_error_corrupt_file)

    val restoreSuccessTitle = c.getString(R.string.restore_success_title)
    val restoreSuccessDescription = c.getString(R.string.restore_success_description)
    val restoreSuccessCta = c.getString(R.string.restore_success_cta)

    val restoreFailureTitle = c.getString(R.string.restore_failure_title)
    val restoreFailureDescription = c.getString(R.string.restore_failure_description)
    val restoreIcloudFilesTitle = c.getString(R.string.restore_icloud_files_title)
    val restoreIcloudFilesError = c.getString(R.string.restore_icloud_files_error)
    val restoreCloudFilesHeader = c.getString(R.string.restore_cloud_files_header)
    val restoreCloudFilesId = c.getString(R.string.restore_cloud_files_id__0025_0040)
    val restoreManualWord = c.getString(R.string.restore_manual_word)
    val restoreManualKeyIncorrectWords = c.getString(R.string.restore_manual_key_incorrect_words)
    val restoreEnterWordsTitle = c.getString(R.string.restore_enter_words_title)
    val restoreUseRecoveryKeyTitle = c.getString(R.string.restore_use_recovery_key_title)
    val restoreUseRecoveryKeyDescription = c.getString(R.string.restore_use_recovery_key_description)
    val restoreVaultTitle = c.getString(R.string.restore_vault_title)
    val restoreVaultMessage = c.getString(R.string.restore_vault_message)
    val restoreVaultWarning = c.getString(R.string.restore_vault_warning)
    val restoreDecryptVaultOptionTitle = c.getString(R.string.restore_decrypt_vault_option_title)
    val restoreVaultSourceOptionIcloud = c.getString(R.string.restore_vault_source_option_icloud)
    val restoreVaultSourceOptionIcloudDescription =
        c.getString(R.string.restore_vault_source_option_icloud_description)
    val restoreQrCodeIntroTitle = c.getString(R.string.restore_qr_code_intro_title)
    val restoreQrCodeIntroDescription = c.getString(R.string.restore_qr_code_intro_description)
    val restoreQrCodeIntroCta = c.getString(R.string.restore_qr_code_intro_cta)
    val restoreQrCodeCameraTitle = c.getString(R.string.restore_qr_code_camera_title)
    val restoreQrCodeCameraDescription = c.getString(R.string.restore_qr_code_camera_description)
    val restoreQrCodeError = c.getString(R.string.restore_qr_code_error)
    val restoreQrCodeErrorSystemSettings = c.getString(R.string.restore_qr_code_error_system_settings)
    val restoreUnencryptedFileTitle = c.getString(R.string.restore_unencrypted_file_title)
    val restoreUnencryptedFileDescription = c.getString(R.string.restore_unencrypted_file_description)
    val restoreUnencryptedFileCtaDescriptionIos =
        c.getString(R.string.restore_unencrypted_file_cta_description_ios)
    val restoreVaultVerifyMasterPasswordDescription =
        c.getString(R.string.restore_vault_verify_master_password_description)
    val restoreErrorIncorrectQrCode = c.getString(R.string.restore_error_incorrect_qr_code)
    val restoreErrorIncorrectWords = c.getString(R.string.restore_error_incorrect_words)
    val restoreErrorGeneral = c.getString(R.string.restore_error_general)

    val recoveryKitTitle = c.getString(R.string.recovery_kit_title)
    val recoveryKitAuthor = c.getString(R.string.recovery_kit_author)
    val recoveryKitCreator = c.getString(R.string.recovery_kit_creator)
    val recoveryKitHeader = c.getString(R.string.recovery_kit_header)
    val recoveryKitWriteDown = c.getString(R.string.recovery_kit_write_down)
    val recoveryErrorNothingToImport = c.getString(R.string.recovery_error_nothing_to_import)
    val recoveryErrorNewerVersion = c.getString(R.string.recovery_error_newer_version)
    val recoveryErrorIndexDamaged = c.getString(R.string.recovery_error_index_damaged)
    val recoveryErrorVaultDamaged = c.getString(R.string.recovery_error_vault_damaged)
    val recoveryErrorUnauthorized = c.getString(R.string.recovery_error_unauthorized)
    val recoveryErrorForbidden = c.getString(R.string.recovery_error_forbidden)
    val recoveryErrorIndexNotFound = c.getString(R.string.recovery_error_index_not_found)
    val recoveryErrorVaultNotFound = c.getString(R.string.recovery_error_vault_not_found)

    val vaultRecoveryDecrypting = c.getString(R.string.vault_recovery_decrypting)
    val vaultRecoveryErrorOpenFile = c.getString(R.string.vault_recovery_error_open_file)
    val vaultRecoveryErrorOpenFileDetails =
        c.getString(R.string.vault_recovery_error_open_file_details)
    val vaultRecoveryErrorGalleryAccess = c.getString(R.string.vault_recovery_error_gallery_access)
    val vaultRecoveryErrorScanningFile = c.getString(R.string.vault_recovery_error_scanning_file)
    val vaultRecoveryErrorFileCorrupted = c.getString(R.string.vault_recovery_error_file_corrupted)
    val vaultRecoveryErrorWrongMasterPasswordWords =
        c.getString(R.string.vault_recovery_error_wrong_master_password_words)
    val vaultRecoveryErrorOpenFileAccessExplain =
        c.getString(R.string.vault_recovery_error_open_file_access_explain)
    val vaultRecoveryWrongDecryptionKitTitle =
        c.getString(R.string.vault_recovery_wrong_decryption_kit_title)
    val vaultRecoveryWrongDecryptionKitDescription =
        c.getString(R.string.vault_recovery_wrong_decryption_kit_description)
    val vaultRecoveryWrongDecryptionKitAnotherBackupCta =
        c.getString(R.string.vault_recovery_wrong_decryption_kit_another_backup_cta)
    val vaultRecoveryWrongDecryptionKitAnotherDecryptionKitCta =
        c.getString(R.string.vault_recovery_wrong_decryption_kit_another_decryption_kit_cta)

    val openExternalFileErrorBody = c.getString(R.string.open_external_file_error_body)

    // Backup
    val backupImportHeader = c.getString(R.string.backup_import_header)
    val backupImportFooter = c.getString(R.string.backup_import_footer)
    val backupImportCta = c.getString(R.string.backup_import_cta)
    val backupImportingFileText = c.getString(R.string.backup_importing_file_text)
    val backupImportingSuccessTitle = c.getString(R.string.backup_importing_success_title)
    val backupImportingSuccessDescription =
        c.getString(R.string.backup_importing_success_description)
    val backupImportingFailureTitle = c.getString(R.string.backup_importing_failure_title)
    val backupImportingFailureDescription =
        c.getString(R.string.backup_importing_failure_description)
    val backupExportHeader = c.getString(R.string.backup_export_header)
    val backupExportFooter = c.getString(R.string.backup_export_footer)
    val backupExportCta = c.getString(R.string.backup_export_cta)
    val backupExportFailedTitle = c.getString(R.string.backup_export_failed_title)
    val backupExportFailedDescription = c.getString(R.string.backup_export_failed_description)
    val backupExportSuccessTitle = c.getString(R.string.backup_export_success_title)
    val backupExportSuccessDescription =
        c.getString(R.string.backup_export_success_description)
    val backupExportSaveTitle = c.getString(R.string.backup_export_save_title)
    val backupExportSaveSubtitle = c.getString(R.string.backup_export_save_subtitle)
    val backupExportSaveCta = c.getString(R.string.backup_export_save_cta)
    val backupExportSaveEncryptToggleTitle =
        c.getString(R.string.backup_export_save_encrypt_toggle_title)
    val backupExportSaveEncryptToggleDescription =
        c.getString(R.string.backup_export_save_encrypt_toggle_description)
    val exportVaultTitle = c.getString(R.string.export_vault_title)

    // Master Password / Authentication
    val masterPasswordLabel = c.getString(R.string.master_password_label)
    val masterPasswordConfirmLabel = c.getString(R.string.master_password_confirm_label)
    val masterPasswordDefine = c.getString(R.string.master_password_define)
    val masterPasswordCreateNew = c.getString(R.string.master_password_create_new)
    val masterPasswordMinLengthLabel = c.getString(R.string.master_password_min_length__0025lld)
    val masterPasswordNotMatch = c.getString(R.string.master_password_not_match)
    val passwordsMatchText = c.getString(R.string.passwords_match_text)
    val passwordLengthRequirement = c.getString(R.string.password_length_requirement)
    val setNewPasswordSuccessTitle = c.getString(R.string.set_new_password_success_title)

    val setNewPasswordConfirmBodyPart1Ios = c.getString(R.string.set_new_password_confirm_body_part1_ios)
    val setNewPasswordConfirmBodyPart2Ios = c.getString(R.string.set_new_password_confirm_body_part2_ios)
    val setNewPasswordConfirmBodyPart1Default =
        c.getString(R.string.set_new_password_confirm_body_part1)
    val setNewPasswordConfirmBodyPart2BoldDefault =
        c.getString(R.string.set_new_password_confirm_body_part2_bold)
    val setNewPasswordConfirmBodyPart3Default =
        c.getString(R.string.set_new_password_confirm_body_part3)
    val setNewPasswordConfirmBodyPart4BoldDefault =
        c.getString(R.string.set_new_password_confirm_body_part4_bold)
    val setNewPasswordConfirmBodyPart5Default =
        c.getString(R.string.set_new_password_confirm_body_part5)

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
    val homeDeleteConfirmTitle = c.getString(R.string.home_delete_confirm_title)
    val homeDeleteConfirmBody = c.getString(R.string.home_delete_confirm_body)
    val homeListOptionsModalTitle = c.getString(R.string.home_list_options_modal_title)
    val homeListMenuEdit = c.getString(R.string.home_list_menu_edit)
    val homeListMenuSort = c.getString(R.string.home_list_menu_sort)
    val homeListMenuFilter = c.getString(R.string.home_list_menu_filter)
    val homeListMenuClearFilters = c.getString(R.string.home_list_menu_clear_filters)
    val homeFilterTagWithCount = c.getString(R.string.home_filter_tag_with_count)
    val homeFilterSelectedTagCountSingle =
        c.getString(R.string.home_filter_selected_tag_count_single)
    val homeFilterSelectedTagCountPlural =
        c.getString(R.string.home_filter_selected_tag_count_plural)

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
    val usernameSuggestionsEmpty = c.getString(R.string.username_suggestions_empty)
    val loginSecurityTypeSecureTitle = c.getString(R.string.login_security_type_secure_title)
    val loginSecurityTypeUltraSecureTitle = c.getString(R.string.login_security_type_ultra_secure_title)
    val loginSecurityTypeTopSecretTitle = c.getString(R.string.login_security_type_top_secret_title)
    val loginSecurityTypeSecureDescription =
        c.getString(R.string.login_security_type_secure_description)
    val loginSecurityTypeUltraSecureDescription =
        c.getString(R.string.login_security_type_ultra_secure_description)
    val loginSecurityTypeTopSecretDescription =
        c.getString(R.string.login_security_type_top_secret_description)
    val loginEditIconCta = c.getString(R.string.login_edit_icon_cta)
    val loginErrorDeletedOtherDevice = c.getString(R.string.login_error_deleted_other_device)
    val loginErrorEditedOtherDevice = c.getString(R.string.login_error_edited_other_device)
    val loginErrorSave = c.getString(R.string.login_error_save)
    val loginDeleteCta = c.getString(R.string.login_delete_cta)
    val loginEdit = c.getString(R.string.login_edit)
    val loginPasswordAutogenerateCta = c.getString(R.string.login_password_autogenerate_cta)
    val loginPasswordGeneratorCta = c.getString(R.string.login_password_generator_cta)
    val loginPasswordPlaceholder = c.getString(R.string.login_password_placeholder)
    val loginSecurityLevelHeader = c.getString(R.string.login_security_level_header)
    val loginUriError = c.getString(R.string.login_uri_error__0025_0040)
    val loginUriHeader = c.getString(R.string.login_uri_header)
    val loginUriLabelFormatted = c.getString(R.string.login_uri_label__0025lld)
    val loginUsernameMostUsedHeader = c.getString(R.string.login_username_most_used_header)
    val loginUsernameMostUsedEmpty = c.getString(R.string.login_username_most_used_empty)
    val loginViewActionUrisTitle = c.getString(R.string.login_view_action_uris_title)
    val loginViewActionCommonCopy = c.getString(R.string.login_view_action_common_copy)
    val loginViewActionCopyUri = c.getString(R.string.login_view_action_copy_uri)
    val loginFilterModalTag = c.getString(R.string.login_filter_modal_tag)
    val loginFilterModalClear = c.getString(R.string.login_filter_modal_clear)
    val loginFilterModalTagAll = c.getString(R.string.login_filter_modal_tag_all)
    val loginFilterModalNoTags = c.getString(R.string.login_filter_modal_no_tags)
    val passwordErrorCopyUsername = c.getString(R.string.password_error_copy_username)
    val passwordErrorCopyPassword = c.getString(R.string.password_error_copy_password)

    // Password Generator
    val passwordGeneratorHeader = c.getString(R.string.password_generator_header)
    val passwordGeneratorDigits = c.getString(R.string.password_generator_digits)
    val passwordGeneratorCharacters = c.getString(R.string.password_generator_characters)
    val passwordGeneratorSpecialCharacters = c.getString(R.string.password_generator_special_characters)
    val passwordGeneratorUppercaseCharacters =
        c.getString(R.string.password_generator_uppercase_characters)
    val passwordGeneratorGenerateCta = c.getString(R.string.password_generator_generate_cta)
    val passwordGeneratorUseCta = c.getString(R.string.password_generator_use_cta)
    val passwordGeneratorCopyCta = c.getString(R.string.password_generator_copy_cta)

    // Item (Generic)
    val itemAddTitle = c.getString(R.string.item_add_title)
    val itemEditTitle = c.getString(R.string.item_edit_title)
    val noteItemLengthError = c.getString(R.string.note_item_length_error)

    // Content Types
    val contentTypeLoginName = c.getString(R.string.content_type_login_name)
    val contentTypeSecureNoteName = c.getString(R.string.content_type_secure_note_name)
    val contentTypeFilterSecureNoteName =
        c.getString(R.string.content_type_filter_secure_note_name)
    val contentTypeFilterLoginName = c.getString(R.string.content_type_filter_login_name)
    val contentTypeFilterAllName = c.getString(R.string.content_type_filter_all_name)
    val contentTypeCardName = c.getString(R.string.content_type_card_name)
    val contentTypeFilterCardName = c.getString(R.string.content_type_filter_card_name)

    // Secure Note
    val secureNoteAddTitle = c.getString(R.string.secure_note_add_title)
    val secureNoteEditTitle = c.getString(R.string.secure_note_edit_title)
    val secureNoteName = c.getString(R.string.secure_note_name_label)
    val secureNoteText = c.getString(R.string.secure_note_text_label)
    val secureNoteReveal = c.getString(R.string.secure_note_text_reveal_view_action)
    val secureNoteTextRevealEditAction = c.getString(R.string.secure_note_text_reveal_edit_action)
    val secureNoteTextMoreAction = c.getString(R.string.secure_note_text_more_action)
    val secureNoteViewActionCopy = c.getString(R.string.secure_note_view_action_copy)
    val secureNoteErrorCopy = c.getString(R.string.secure_note_error_copy)
    val secureNoteAdditionalInfoLabel = c.getString(R.string.secure_note_additional_info_label)

    // Credit Card
    val creditCardName = c.getString(R.string.credit_card_name_label)
    val creditCardCardholder = c.getString(R.string.credit_card_cardholder_label)
    val creditCardNumber = c.getString(R.string.credit_card_number_label)
    val creditCardExpiration = c.getString(R.string.credit_card_expiration_label)
    val creditCardCvv = c.getString(R.string.credit_card_cvv_label)
    val creditCardNotes = c.getString(R.string.credit_card_notes_label)
    val cardAddTitle = c.getString(R.string.card_add_title)
    val cardEditTitle = c.getString(R.string.card_edit_title)
    val cardNameLabel = c.getString(R.string.card_name_label)
    val cardHolderLabel = c.getString(R.string.card_holder_label)
    val cardNumberLabel = c.getString(R.string.card_number_label)
    val cardExpirationDateLabel = c.getString(R.string.card_expiration_date_label)
    val cardExpirationDatePlaceholder = c.getString(R.string.card_expiration_date_placeholder)
    val cardSecurityCodeLabel = c.getString(R.string.card_security_code_label)
    val cardNotesLabel = c.getString(R.string.card_notes_label)
    val cardDetailsHeader = c.getString(R.string.card_details_header)
    val cardViewActionCopyCardHolder = c.getString(R.string.card_view_action_copy_card_holder)
    val cardViewActionCopyCardNumber = c.getString(R.string.card_view_action_copy_card_number)
    val cardViewActionCopyExpirationDate =
        c.getString(R.string.card_view_action_copy_expiration_date)
    val cardViewActionCopySecurityCode =
        c.getString(R.string.card_view_action_copy_security_code)
    val cardErrorCopyNumber = c.getString(R.string.card_error_copy_number)
    val cardErrorCopySecurityCode = c.getString(R.string.card_error_copy_security_code)

    // Customize Icon
    val customizeIconHeader = c.getString(R.string.customize_icon_header)
    val customizeIconLabelHeader = c.getString(R.string.customize_icon_label_header)
    val customizeIconLabelKey = c.getString(R.string.customize_icon_label_key)
    val customizeIconLabelPlaceholder = c.getString(R.string.customize_icon_label_placeholder)
    val customizeIconLabelColor = c.getString(R.string.customize_icon_label_color)
    val customizeIconLabelReset = c.getString(R.string.customize_icon_label_reset)
    val customizeIconCustomHeader = c.getString(R.string.customize_icon_custom_header)
    val customizeIconCustomPlaceholder = c.getString(R.string.customize_icon_custom_placeholder)
    val customizeIcon = c.getString(R.string.customize_icon)
    val customizeIconIcon = c.getString(R.string.customize_icon_icon)
    val customizeIconCustom = c.getString(R.string.customize_icon_custom)
    val changeIconLabelHeader = c.getString(R.string.change_icon_label_header)
    val changeIconLabelPlaceholder = c.getString(R.string.change_icon_label_placeholder)
    val changeIconBackgroundHeader = c.getString(R.string.change_icon_background_header)
    val changeIconSelectIconHeader = c.getString(R.string.change_icon_select_icon_header)
    val changeIconNoUris = c.getString(R.string.change_icon_no_uris)
    val changeIconCustomImageHeader = c.getString(R.string.change_icon_custom_image_header)
    val changeIconCustomImagePlaceholder = c.getString(R.string.change_icon_custom_image_placeholder)
    val changeIconSegmentImageUrl = c.getString(R.string.change_icon_segment_image_url)

    // Connect
    val connectTitle = c.getString(R.string.connect_title)
    val connectConnectionHeader = c.getString(R.string.connect_connection_header)
    val connectConnectionConnecting = c.getString(R.string.connect_connection_connecting)
    val connectConnectionFailedTitle = c.getString(R.string.connect_connection_failed_title)
    val connectConnectionFailedDescription = c.getString(R.string.connect_connection_failed_description)
    val connectConnectionFailedCta = c.getString(R.string.connect_connection_failed_cta)
    val connectConnectionSecurityCheckTitle =
        c.getString(R.string.connect_connection_security_check_title)
    val connectConnectionSecurityCheckDescription =
        c.getString(R.string.connect_connection_security_check_description)
    val connectConnectionSecurityCheckAcceptCta =
        c.getString(R.string.connect_connection_security_check_accept_cta)
    val connectConnectionSuccessTitle = c.getString(R.string.connect_connection_success_title)
    val connectConnectionSuccessDescription =
        c.getString(R.string.connect_connection_success_description__0025_0040)
    val connectSetupStepsHeader = c.getString(R.string.connect_setup_steps_header)
    val connectSetupCameraCta = c.getString(R.string.connect_setup_camera_cta)
    val connectSetupCameraError = c.getString(R.string.connect_setup_camera_error)
    val connectSetupFinishCta = c.getString(R.string.connect_setup_finish_cta)
    val connectSetupPushCta = c.getString(R.string.connect_setup_push_cta)
    val connectSetupPushWarningIos = c.getString(R.string.connect_setup_push_warning_ios__0025_0040)
    val connectQrcodeCameraDescription = c.getString(R.string.connect_qrcode_camera_description)
    val knownBrowsersHeader = c.getString(R.string.known_browsers_header)

    val cameraQrCodeError = c.getString(R.string.camera_qr_code_error)
    val cameraErrorOtherAppUsesCamera = c.getString(R.string.camera_error_other_app_uses_camera)
    val cameraErrorSystemOverload = c.getString(R.string.camera_error_system_overload)
    val cameraErrorSplitMode = c.getString(R.string.camera_error_split_mode)
    val cameraErrorGeneral = c.getString(R.string.camera_error_general)

    // Settings
    val settingsTitle = c.getString(R.string.settings_title)
    val settingsHeaderPrefs = c.getString(R.string.settings_header_preferences)
    val settingsHeaderBrowserExtension = c.getString(R.string.settings_header_browser_extension)
    val settingsHeaderBackup = c.getString(R.string.settings_header_backup)
    val settingsHeaderAbout = c.getString(R.string.settings_header_about)
    val settingsHeaderMobileApp = c.getString(R.string.settings_header_mobile_app)
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
    val settingsAutofillToggle = c.getString(R.string.settings_autofill_toggle)
    val settingsAutofillToggleDescription =
        c.getString(R.string.settings_autofill_toggle_description)
    val settingsAutofillSystem = c.getString(R.string.settings_autofill_system)
    val settingsAutofillBrowsers = c.getString(R.string.settings_autofill_browsers)
    val settingsAutofillBrowsersDescription = c.getString(R.string.settings_autofill_browsers_description)
    val settingsAutofillOpenSystemSettingsDescription =
        c.getString(R.string.settings_autofill_open_system_settings_description)
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
    val settingsCloudSyncDescription = c.getString(R.string.settings_cloud_sync_description)
    val settingsCloudSyncTitle = c.getString(R.string.settings_cloud_sync_title)
    val settingsCloudSyncIcloudLabel = c.getString(R.string.settings_cloud_sync_icloud_label)
    val settingsCloudSyncWebdavLabel = c.getString(R.string.settings_cloud_sync_webdav_label)
    val settingsCloudSyncStatus = c.getString(R.string.settings_cloud_sync_status__0025_0040)
    val settingsCloudSyncLastSync = c.getString(R.string.settings_cloud_sync_last_sync__0025_0040)
    val cloudSyncActionSyncNow = c.getString(R.string.cloud_sync_action_sync_now)
    val cloudSyncWebdavTitle = c.getString(R.string.cloud_sync_webdav_title)
    val cloudSyncShowErrorDetails = c.getString(R.string.cloud_sync_show_error_details)
    val cloudSyncActionReplaceBackup = c.getString(R.string.cloud_sync_action_replace_backup)
    val cloudSyncActionChangePassword = c.getString(R.string.cloud_sync_action_change_password)
    val settingsEntrySyncInfo = c.getString(R.string.settings_entry_sync_info)
    val settingsEntrySyncAccount = c.getString(R.string.settings_entry_sync_account)
    val settingsEntrySyncLast = c.getString(R.string.settings_entry_sync_last)
    val settingsEntryWebDav = c.getString(R.string.settings_entry_webdav)
    val settingsEntryAppAccess = c.getString(R.string.settings_entry_app_access)
    val settingsEntryDataAccess = c.getString(R.string.settings_entry_data_access)
    val settingsEntryChangePassword = c.getString(R.string.settings_entry_change_password)
    val settingsEntryBiometrics = c.getString(R.string.settings_entry_biometrics)
    val settingsEntryBiometricsDesc = c.getString(R.string.settings_entry_biometrics_description)
    val settingsEntryBiometricsNotAvailable =
        c.getString(R.string.settings_entry_biometrics_not_available)
    val settingsEntryBiometricsToggle = c.getString(R.string.settings_entry_biometrics_toggle)
    val settingsEntryLockoutSettings = c.getString(R.string.settings_entry_lockout_settings)
    val settingsEntryLockoutSettingsDesc = c.getString(R.string.settings_entry_lockout_settings_description)
    val settingsEntryDecryptionKit = c.getString(R.string.settings_entry_decryption_kit)
    val settingsEntryDecryptionKitDescription =
        c.getString(R.string.settings_entry_decryption_kit_description)
    val settingsEntryAppLockTime = c.getString(R.string.settings_entry_app_lock_time)
    val settingsEntryAppLockTimeDesc = c.getString(R.string.settings_entry_app_lock_time_description)
    val settingsEntryAppLockAttempts = c.getString(R.string.settings_entry_app_lock_attempts)
    val settingsEntryAppLockAttemptsDesc = c.getString(R.string.settings_entry_app_lock_attempts_description)
    val settingsEntryAppLockAttemptsFooter =
        c.getString(R.string.settings_entry_app_lock_attempts_footer)
    val settingsEntryAutofillLockTime = c.getString(R.string.settings_entry_autofill_lock_time)
    val settingsEntryAutofillLockTimeDesc = c.getString(R.string.settings_entry_autofill_lock_time_description)
    val settingsEntrySecurityTier = c.getString(R.string.settings_entry_protection_level)
    val settingsEntrySecurityTierDesc = c.getString(R.string.settings_entry_protection_level_description)
    val settingsHeaderSecurityTier = c.getString(R.string.settings_header_protection_level)
    val settingsProtectionLevelHelp = c.getString(R.string.settings_protection_level_help)
    val settingsEntrySecurityTier1 = c.getString(R.string.settings_entry_protection_level0)
    val settingsEntrySecurityTier1Desc = c.getString(R.string.settings_entry_protection_level0_description)
    val settingsEntrySecurityTier2 = c.getString(R.string.settings_entry_protection_level1)
    val settingsEntrySecurityTier2Desc = c.getString(R.string.settings_entry_protection_level1_description)
    val settingsEntrySecurityTier3 = c.getString(R.string.settings_entry_protection_level2)
    val settingsEntrySecurityTier3Desc = c.getString(R.string.settings_entry_protection_level2_description)

    // Security Tiers Help
    val securityTiersHelpTitle = c.getString(R.string.security_tiers_help_title)
    val securityTiersHelpSubtitle = c.getString(R.string.security_tiers_help_subtitle)
    val securityTiersHelpLocalFirstSectionTitle =
        c.getString(R.string.security_tiers_help_local_first_section_title)
    val securityTiersHelpLocalFirstSectionSubtitle =
        c.getString(R.string.security_tiers_help_local_first_section_subtitle)
    val securityTiersHelpLocalFirstSectionFigureTitle =
        c.getString(R.string.security_tiers_help_local_first_section_figure_title)
    val securityTiersHelpTiersSectionTitle =
        c.getString(R.string.security_tiers_help_tiers_section_title)
    val securityTiersHelpTiersSectionSubtitle =
        c.getString(R.string.security_tiers_help_tiers_section_subtitle)
    val securityTiersHelpTiersSecretTitle =
        c.getString(R.string.security_tiers_help_tiers_secret_title)
    val securityTiersHelpTiersSecretSubtitle =
        c.getString(R.string.security_tiers_help_tiers_secret_subtitle)
    val securityTiersHelpTiersHighlySecretTitle =
        c.getString(R.string.security_tiers_help_tiers_highly_secret_title)
    val securityTiersHelpTiersHighlySecretSubtitle =
        c.getString(R.string.security_tiers_help_tiers_highly_secret_subtitle)
    val securityTiersHelpTiersTopSecretTitle =
        c.getString(R.string.security_tiers_help_tiers_top_secret_title)
    val securityTiersHelpTiersTopSecretSubtitle =
        c.getString(R.string.security_tiers_help_tiers_top_secret_subtitle)
    val securityTiersHelpLayersSectionTitle =
        c.getString(R.string.security_tiers_help_layers_section_title)
    val securityTiersHelpTiersLayersE2eeTitle =
        c.getString(R.string.security_tiers_help_tiers_layers_e2ee_title)
    val securityTiersHelpTiersLayersE2eeSubtitle =
        c.getString(R.string.security_tiers_help_tiers_layers_e2ee_subtitle)
    val securityTiersHelpTiersLayersSecureEnclaveTitle =
        c.getString(R.string.security_tiers_help_tiers_layers_secure_enclave_title)
    val securityTiersHelpTiersLayersSecureEnclaveSubtitle =
        c.getString(R.string.security_tiers_help_tiers_layers_secure_enclave_subtitle)
    val securityTiersHelpTiersLayersAdpTitle =
        c.getString(R.string.security_tiers_help_tiers_layers_adp_title)
    val securityTiersHelpTiersLayersAdpSubtitle =
        c.getString(R.string.security_tiers_help_tiers_layers_adp_subtitle)
    val settingsEntryScreenCapture = c.getString(R.string.settings_entry_screen_capture)
    val settingsEntryScreenCaptureDesc = c.getString(R.string.settings_entry_screen_capture_description)
    val settingsEntryScreenshotsConfirmTitle =
        c.getString(R.string.settings_entry_screenshots_confirm_title)
    val settingsEntryScreenshotsConfirmDesc =
        c.getString(R.string.settings_entry_screenshots_confirm_description)
    val settingsEntryKnownBrowsers = c.getString(R.string.settings_entry_known_browsers)
    val settingsEntryKnownBrowsersDesc = c.getString(R.string.settings_entry_known_browsers_description)
    val settingsEntryPushNotifications = c.getString(R.string.settings_entry_push_notifications)
    val settingsEntryPushNotificationsDesc = c.getString(R.string.settings_entry_push_notifications_description)
    val settingsPushNotificationsEnableCta =
        c.getString(R.string.settings_push_notifications_enable_cta)
    val settingsPushNotificationsDescription =
        c.getString(R.string.settings_push_notifications_description)
    val settingsPushNotificationsAllowTitle =
        c.getString(R.string.settings_push_notifications_allow_title)
    val settingsPushNotificationsAllowSubtitle =
        c.getString(R.string.settings_push_notifications_allow_subtitle)
    val settingsPushNotificationsOpenSystemSettingsDescription =
        c.getString(R.string.settings_push_notifications_open_system_settings_description)
    val settingsPushNotificationsStatusLabel =
        c.getString(R.string.settings_push_notifications_status_label)
    val settingsEntryHelpCenter = c.getString(R.string.settings_entry_help_center)
    val settingsEntryDiscord = c.getString(R.string.settings_entry_discord)
    val settingsEntryManageTags = c.getString(R.string.settings_entry_manage_tags)
    val settingsEntryManageTagsDescription = c.getString(R.string.settings_entry_manage_tags_description)
    val settingsEntrySubscriptionFreePlanSubtitle = c.getString(R.string.settings_entry_subscription_free_plan_subtitle)
    val settingsEntrySubscriptionPaidPlanSubtitle = c.getString(R.string.settings_entry_subscription_paid_plan_subtitle)
    val settingsManageTokensTitle = c.getString(R.string.settings_manage_tokens_title)
    val settings2fasOpen = c.getString(R.string.settings_2fas_open)
    val settings2fasGet = c.getString(R.string.settings_2fas_get)
    val settingsChangePasswordProcessingTitle =
        c.getString(R.string.settings_change_password_processing_title)
    val settingsChangePasswordProcessingDescription =
        c.getString(R.string.settings_change_password_processing_description)
    val settingsChangePasswordProcessingMessageLocal =
        c.getString(R.string.settings_change_password_processing_message_local)
    val settingsChangePasswordProcessingMessageCloud =
        c.getString(R.string.settings_change_password_processing_message_cloud)
    val settingsChangePasswordSuccessTitle =
        c.getString(R.string.settings_change_password_success_title)
    val settingsChangePasswordSuccessDescription =
        c.getString(R.string.settings_change_password_success_description)
    val settingsChangePasswordSuccessCardTitle =
        c.getString(R.string.settings_change_password_success_card_title)
    val settingsChangePasswordSuccessCardDescription =
        c.getString(R.string.settings_change_password_success_card_description)
    val settingsChangePasswordCtaProcessing =
        c.getString(R.string.settings_change_password_cta_processing)
    val settingsChangePasswordCtaSuccess =
        c.getString(R.string.settings_change_password_cta_success)
    val settingsKnownBrowserPlaceholderExtensionName =
        c.getString(R.string.settings_known_browser_placeholder_extension_name)
    val settingsKnownBrowserPlaceholderBrowserName =
        c.getString(R.string.settings_known_browser_placeholder_browser_name)
    val settingsCloudSyncDescriptionLong =
        c.getString(R.string.settings_cloud_sync_description_long)
    val settingsBackupSeedErrorMismatch =
        c.getString(R.string.settings_backup_seed_error_mismatch)
    val settingsBackupErrorReadingKit =
        c.getString(R.string.settings_backup_error_reading_kit)
    val settingsBackupErrorReadingKitDescription =
        c.getString(R.string.settings_backup_error_reading_kit_description)
    val settingsBackupErrorMismatchKit =
        c.getString(R.string.settings_backup_error_mismatch_kit)
    val settingsBackupErrorScanningKit =
        c.getString(R.string.settings_backup_error_scanning_kit)
    val settingsBackupErrorScanningKitDescription =
        c.getString(R.string.settings_backup_error_scanning_kit_description)
    val settingsBackupErrorScanningGeneric =
        c.getString(R.string.settings_backup_error_scanning_generic)
    val settingsBackupErrorWrongPassword =
        c.getString(R.string.settings_backup_error_wrong_password)
    val settingsBackupErrorDecryptionKitFile =
        c.getString(R.string.settings_backup_error_decryption_kit_file)

    val webdavServerUrl = c.getString(R.string.webdav_server_url)
    val webdavAllowUntrustedCertificates = c.getString(R.string.webdav_allow_untrusted_certificates)
    val webdavCredentials = c.getString(R.string.webdav_credentials)
    val webdavUsername = c.getString(R.string.webdav_username)
    val webdavPassword = c.getString(R.string.webdav_password)
    val webdavConnect = c.getString(R.string.webdav_connect)
    val webdavDisconnect = c.getString(R.string.webdav_disconnect)
    val webdavConnecting = c.getString(R.string.webdav_connecting)
    val webdavDisableIcloudConfirmTitle =
        c.getString(R.string.webdav_disable_icloud_confirm_title)
    val webdavDisableIcloudConfirmBody =
        c.getString(R.string.webdav_disable_icloud_confirm_body)
    val webdavDisableWebdavConfirmBody =
        c.getString(R.string.webdav_disable_webdav_confirm_body)

    // Sync
    val syncStatusSynced = c.getString(R.string.sync_status_synced)
    val syncStatusSyncing = c.getString(R.string.sync_status_syncing)
    val syncStatusIdle = c.getString(R.string.sync_status_idle)
    val syncStatusRetry = c.getString(R.string.sync_status_retry)
    val syncStatusRetrying = c.getString(R.string.sync_status_retrying)
    val syncStatusRetryingDetails = c.getString(R.string.sync_status_retrying_details)
    val syncStatusErrorForbidden = c.getString(R.string.sync_status_error_forbidden)
    val syncStatusErrorMethodNotAllowed = c.getString(R.string.sync_status_error_method_not_allowed)
    val syncStatusErrorNewerVersionNeededTitle =
        c.getString(R.string.sync_status_error_newer_version_needed_title)
    val syncStatusErrorNotConfigured = c.getString(R.string.sync_status_error_not_configured)
    val syncStatusErrorSslError = c.getString(R.string.sync_status_error_ssl_error)
    val syncStatusErrorUnauthorized = c.getString(R.string.sync_status_error_unauthorized)
    val syncStatusErrorLimitDevicesReached =
        c.getString(R.string.sync_status_error_limit_devices_reached)
    val syncStatusErrorWrongDirectoryUrl =
        c.getString(R.string.sync_status_error_wrong_directory_url)
    val syncStatusErrorNotAuthorized = c.getString(R.string.sync_status_error_not_authorized)
    val syncStatusErrorUserIsForbidden =
        c.getString(R.string.sync_status_error_user_is_forbidden)
    val syncStatusErrorGeneralReason = c.getString(R.string.sync_status_error_general_reason)
    val syncStatusErrorNewerVersionNeeded =
        c.getString(R.string.sync_status_error_newer_version_needed)
    val syncStatusErrorIncorrectUrl = c.getString(R.string.sync_status_error_incorrect_url)
    val syncStatusErrorTlsCertFailed = c.getString(R.string.sync_status_error_tls_cert_failed)
    val syncStatusErrorNoWebDavServer = c.getString(R.string.sync_status_error_no_web_dav_server)
    val syncStatusErrorPasswordChanged = c.getString(R.string.sync_status_error_password_changed)
    val syncSyncing = c.getString(R.string.sync_syncing)
    val syncSynced = c.getString(R.string.sync_synced)
    val syncChecking = c.getString(R.string.sync_checking)
    val syncNotAvailable = c.getString(R.string.sync_not_available)
    val syncDisabled = c.getString(R.string.sync_disabled)
    val syncErrorIcloudQuota = c.getString(R.string.sync_error_icloud_quota)
    val syncErrorIcloudDisabled = c.getString(R.string.sync_error_icloud_disabled)
    val syncErrorIcloudError = c.getString(R.string.sync_error_icloud_error)
    val syncErrorIcloudErrorDetails = c.getString(R.string.sync_error_icloud_error_details)
    val syncErrorIcloudErrorUserLoggedIn =
        c.getString(R.string.sync_error_icloud_error_user_logged_in)
    val syncErrorIcloudErrorReboot = c.getString(R.string.sync_error_icloud_error_reboot)
    val syncErrorIcloudErrorNewerVersion =
        c.getString(R.string.sync_error_icloud_error_newer_version)
    val syncErrorIcloudErrorDiffrentEncryption =
        c.getString(R.string.sync_error_icloud_error_diffrent_encryption)
    val syncErrorIcloudErrorNoAccount = c.getString(R.string.sync_error_icloud_error_no_account)
    val syncErrorIcloudErrorAccessRestricted =
        c.getString(R.string.sync_error_icloud_error_access_restricted)
    val syncErrorIcloudVaultEncryptionRestore =
        c.getString(R.string.sync_error_icloud_vault_encryption_restore)
    val syncErrorIcloudMergeError = c.getString(R.string.sync_error_icloud_merge_error)
    val syncErrorIcloudSyncNotAllowedTitle =
        c.getString(R.string.sync_error_icloud_sync_not_allowed_title)
    val syncErrorIcloudSyncNotAllowedDescription =
        c.getString(R.string.sync_error_icloud_sync_not_allowed_description)
    val cloudVaultDeleteConfirmTitle = c.getString(R.string.cloud_vault_delete_confirm_title)
    val cloudVaultDeleteConfirmBody = c.getString(R.string.cloud_vault_delete_confirm_body)
    val cloudVaultRemovingFailure = c.getString(R.string.cloud_vault_removing_failure)

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
    val aboutShareTitle = c.getString(R.string.about_share_title)
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
    val aboutLibrariesWeUse = c.getString(R.string.about_libraries_we_use)
    val aboutRateUsAppStore = c.getString(R.string.about_rate_us_app_store)
    val aboutSendLogsCta = c.getString(R.string.about_send_logs_cta)
    val aboutVersionIos = c.getString(R.string.about_version_ios__0025_0040)
    val shareLinkMessage = c.getString(R.string.share_link_message)
    val shareLinkSubject = c.getString(R.string.share_link_subject)

    // Authentication Form Strings
    val authUseBiometrics = c.getString(R.string.auth_use_biometrics)
    val authBiometricsModalTitle = c.getString(R.string.auth_biometrics_modal_title)
    val authBiometricsDisabledMessage = c.getString(R.string.auth_biometrics_disabled_message)
    val authPreviewTitle = c.getString(R.string.auth_preview_title)
    val authPreviewDescription = c.getString(R.string.auth_preview_description)
    val authPreviewCta = c.getString(R.string.auth_preview_cta)
    val biometricsMissingActivityError = c.getString(R.string.biometrics_missing_activity_error)
    val biometricsGenericError = c.getString(R.string.biometrics_generic_error)

    val autofillSaveLoginTitle = c.getString(R.string.autofill_save_login_title)
    val autofillSaveLoginToastSuccess = c.getString(R.string.autofill_save_login_toast_success)
    val autofillPickerEmptyState = c.getString(R.string.autofill_picker_empty_state)
    val autofillPromptTitle = c.getString(R.string.autofill_prompt_title)
    val autofillPromptDescription = c.getString(R.string.autofill_prompt_description)
    val autofillPromptCta = c.getString(R.string.autofill_prompt_cta)

    val autofillLoginDialogTitle = c.getString(R.string.autofill_login_dialog_title)
    val autofillLoginDialogBodyPrefix = c.getString(R.string.autofill_login_dialog_body_prefix)
    val autofillLoginDialogBodySuffix = c.getString(R.string.autofill_login_dialog_body_suffix)
    val autofillLoginDialogPositive = c.getString(R.string.autofill_login_dialog_positive)
    val autofillLoginDialogNeutral = c.getString(R.string.autofill_login_dialog_neutral)
    val autofillNoVaultMessage = c.getString(R.string.autofill_no_vault_message)

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

    // Lock Screen
    val lockScreenBiometricsPromptTitle = c.getString(R.string.lock_screen_biometrics_prompt_title)
    val lockScreenBiometricsPromptBody = c.getString(R.string.lock_screen_biometrics_prompt_body)
    val lockScreenBiometricsModalTitle = c.getString(R.string.lock_screen_biometrics_modal_title)
    val lockScreenBiometricsModalSubtitle = c.getString(R.string.lock_screen_biometrics_modal_subtitle)
    val lockScreenBiometricsErrorTooManyAttempts = c.getString(R.string.lock_screen_biometrics_error_too_many_attempts)
    val lockScreenBiometricsErrorTitle = c.getString(R.string.lock_screen_biometrics_error_title)
    val lockScreenUnlockTitle = c.getString(R.string.lock_screen_unlock_title)
    val lockScreenUnlockDescription = c.getString(R.string.lock_screen_unlock_description)
    val lockScreenUnlockCta = c.getString(R.string.lock_screen_unlock_cta)
    val lockScreenTooManyAttemptsDescription =
        c.getString(R.string.lock_screen_too_many_attempts_description)
    val lockScreenUnlockBiometricsError =
        c.getString(R.string.lock_screen_unlock_biometrics_error)
    val lockScreenUnlockBiometricsReason =
        c.getString(R.string.lock_screen_unlock_biometrics_reason)
    val lockScreenUnlockInvalidPassword =
        c.getString(R.string.lock_screen_unlock_invalid_password)
    val lockScreenUnlockTitleIos = c.getString(R.string.lock_screen_unlock_title_ios)
    val lockScreenUnlockUseFaceid = c.getString(R.string.lock_screen_unlock_use_faceid)
    val lockScreenUnlockUseTouchid = c.getString(R.string.lock_screen_unlock_use_touchid)
    val lockUsePassword = c.getString(R.string.lock_use_password)
    val lockCopyError = c.getString(R.string.lock_copy_error)
    val lockScreenTryAgainIn = c.getString(R.string.lock_screen_try_again__0025_0040)
    val lockScreenEnterMasterPassword = c.getString(R.string.lock_screen_enter_master_password)
    val lockScreenResetApp = c.getString(R.string.lock_screen_reset_app)
    val lockScreenResetAppTitle = c.getString(R.string.lock_screen_reset_app_title)
    val lockScreenBiometricsPromptFaceidTitle =
        c.getString(R.string.lock_screen_biometrics_prompt_faceid_title)
    val lockScreenBiometricsPromptTouchidTitle =
        c.getString(R.string.lock_screen_biometrics_prompt_touchid_title)
    val lockScreenBiometricsPromptAccept =
        c.getString(R.string.lock_screen_biometrics_prompt_accept)
    val lockScreenBiometricsPromptCancel =
        c.getString(R.string.lock_screen_biometrics_prompt_cancel)
    val biometryReason = c.getString(R.string.biometry_reason)
    val iosLockScreenUnlockTitle = c.getString(R.string.ios_lock_screen_unlock_title)

    val loginDeleteConfirmTitle = c.getString(R.string.login_delete_confirm_title)
    val loginDeleteConfirmBody = c.getString(R.string.login_delete_confirm_body)

    val loginSearchNoResultsTitle = c.getString(R.string.login_search_no_results_title)
    val loginSearchNoResultsDescription = c.getString(R.string.login_search_no_results_description)

    val scanDecryptionKitTitle = c.getString(R.string.scan_decryption_kit_title)
    val scanDecryptionKitDescription = c.getString(R.string.scan_decryption_kit_description)
    val qrScanCameraLaunchError = c.getString(R.string.qrscan_camera_launch_error)

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
    val connectModalErrorAppUpdateRequiredCtaDefault =
        c.getString(R.string.connect_modal_error_app_update_required_cta)

    val connectModalErrorBrowserExtensionUpdateRequiredTitle = c.getString(R.string.connect_modal_error_browser_extension_update_required_title)
    val connectModalErrorBrowserExtensionUpdateRequiredSubtitle = c.getString(R.string.connect_modal_error_browser_extension_update_required_subtitle)
    val connectModalErrorBrowserExtensionUpdateRequiredCta = c.getString(R.string.common_close)
    val connectModalErrorNoInternetTitle = c.getString(R.string.connect_modal_error_no_internet_title)
    val connectModalErrorNoInternetSubtitle = c.getString(R.string.connect_modal_error_no_internet_subtitle)

    // Request Modal
    val requestModalHeaderTitle = c.getString(R.string.request_modal_header_title)
    val requestModalLoading = c.getString(R.string.request_modal_loading)

    val requestModalPasswordRequestTitle = c.getString(R.string.request_modal_password_request_title)
    val requestModalPasswordRequestSubtitle = c.getString(R.string.request_modal_password_request_subtitle)
    val requestModalPasswordRequestCtaPositive = c.getString(R.string.request_modal_password_request_cta_positive)
    val requestModalPasswordRequestCtaNegative = c.getString(R.string.request_modal_password_request_cta_negative)

    val requestModalSecureNoteRequestTitle = c.getString(R.string.request_modal_secure_note_request_title)
    val requestModalSecureNoteRequestSubtitle = c.getString(R.string.request_modal_secure_note_request_subtitle)

    val requestModalCardRequestTitle = c.getString(R.string.request_modal_card_request_title)
    val requestModalCardRequestSubtitle = c.getString(R.string.request_modal_card_request_subtitle)

    val requestModalNewItemTitle = c.getString(R.string.request_modal_new_item_title)
    val requestModalNewItemSubtitle = c.getString(R.string.request_modal_new_item_subtitle)
    val requestModalNewItemCtaPositive = c.getString(R.string.request_modal_new_item_cta_positive)
    val requestModalNewItemCtaNegative = c.getString(R.string.request_modal_new_item_cta_negative)

    val requestModalNewLoginTitle = c.getString(R.string.request_modal_new_login_title)
    val requestModalNewSecureNoteTitle = c.getString(R.string.request_modal_new_secure_note_title)
    val requestModalNewCardTitle = c.getString(R.string.request_modal_new_card_title)

    val requestModalUpdateItemTitle = c.getString(R.string.request_modal_update_item_title)
    val requestModalUpdateItemSubtitle = c.getString(R.string.request_modal_update_item_subtitle)
    val requestModalUpdateItemCtaPositive = c.getString(R.string.request_modal_update_item_cta_positive)
    val requestModalUpdateItemCtaNegative = c.getString(R.string.request_modal_update_item_cta_negative)

    val requestModalUpdateLoginTitle = c.getString(R.string.request_modal_update_login_title)
    val requestModalUpdateSecureNoteTitle = c.getString(R.string.request_modal_update_secure_note_title)
    val requestModalUpdateCardTitle = c.getString(R.string.request_modal_update_card_title)

    val requestModalRemoveItemTitle = c.getString(R.string.request_modal_remove_item_title)
    val requestModalRemoveItemSubtitle = c.getString(R.string.request_modal_remove_item_subtitle)
    val requestModalRemoveItemCtaPositive = c.getString(R.string.request_modal_remove_item_cta_positive)
    val requestModalRemoveItemCtaNegative = c.getString(R.string.request_modal_remove_item_cta_negative)
    val requestModalRemoveLoginTitle = c.getString(R.string.request_modal_remove_login_title)
    val requestModalRemoveSecureNoteTitle = c.getString(R.string.request_modal_remove_secure_note_title)
    val requestModalRemoveCardTitle = c.getString(R.string.request_modal_remove_card_title)

    val requestModalFullSyncTitle = c.getString(R.string.request_modal_full_sync_title)
    val requestModalFullSyncSubtitle = c.getString(R.string.request_modal_full_sync_subtitle)
    val requestModalFullSyncCtaPositive = c.getString(R.string.request_modal_full_sync_cta_positive)
    val requestModalFullSyncCtaNegative = c.getString(R.string.request_modal_full_sync_cta_negative)

    val requestModalErrorGenericTitle = c.getString(R.string.request_modal_error_generic_title)
    val requestModalErrorGenericSubtitle = c.getString(R.string.request_modal_error_generic_subtitle)
    val requestModalErrorGenericCta = c.getString(R.string.request_modal_error_generic_cta)

    val requestModalErrorNoItemTitle = c.getString(R.string.request_modal_error_no_item_title)
    val requestModalErrorNoItemSubtitle = c.getString(R.string.request_modal_error_no_item_subtitle)
    val requestModalErrorNoItemCta = c.getString(R.string.request_modal_error_no_item_cta)

    val requestModalErrorItemsLimitTitle = c.getString(R.string.request_modal_error_items_limit_title)
    val requestModalErrorItemsLimitSubtitle = c.getString(R.string.request_modal_error_items_limit_subtitle)
    val requestModalErrorItemsLimitCta = c.getString(R.string.request_modal_error_items_limit_cta)
    val requestModalErrorSendDataTitle = c.getString(R.string.request_modal_error_send_data_title)
    val requestModalErrorSendDataSubtitle = c.getString(R.string.request_modal_error_send_data_subtitle)

    val requestModalToastCancel = c.getString(R.string.request_modal_toast_cancel)
    val requestModalToastAddLogin = c.getString(R.string.request_modal_toast_success_add_login)
    val requestModalToastUpdateLogin = c.getString(R.string.request_modal_toast_success_update_login)
    val requestModalToastDeleteItem = c.getString(R.string.request_modal_toast_success_delete_login)
    val requestModalToastPasswordRequest = c.getString(R.string.request_modal_toast_success_password_request)
    val requestModalToastSuccessSecureNoteRequest =
        c.getString(R.string.request_modal_toast_success_secure_note_request)
    val requestModalToastSuccessCardRequest =
        c.getString(R.string.request_modal_toast_success_card_request)
    val requestModalToastSuccessFullSync =
        c.getString(R.string.request_modal_toast_success_full_sync)

    val transferServicesDisclaimer = c.getString(R.string.transfer_services_list_footer)
    val transferResultDescription = c.getString(R.string.transfer_file_summary_description)
    val transferResultCta = c.getString(R.string.transfer_file_summary_cta)
    val transferResultUnknownItems = c.getString(R.string.transfer_file_summary_others_counter_description)
    val transferResultTagsItems = c.getString(R.string.transfer_file_summary_tags_counter_description)
    val transferResultLoginsDetected = c.getString(R.string.transfer_file_summary_logins_counter_description)
    val transferResultSecureNotesDetected = c.getString(R.string.transfer_file_summary_secure_notes_counter_description)
    val transferResultPaymentCardsDetected = c.getString(R.string.transfer_file_summary_payment_cards_counter_description)
    val transferResultTagsDetected = c.getString(R.string.transfer_file_summary_tags_counter_description)

    val subscriptionFreePlan = c.getString(R.string.subscription_free_plan)
    val subscriptionPaidPlan = c.getString(R.string.subscription_unlimited_plan)

    // Manage Subscription
    val manageSubscriptionTitle = c.getString(R.string.manage_subscription_title)
    val manageSubscriptionItemsTitle = c.getString(R.string.manage_subscription_items_title)
    val manageSubscriptionBrowsersTitle = c.getString(R.string.manage_subscription_browsers_title)
    val manageSubscriptionPricePrefix = c.getString(R.string.manage_subscription_price_prefix)
    val manageSubscriptionRenewDatePrefix = c.getString(R.string.manage_subscription_renew_date_prefix)
    val manageSubscriptionAppleInfo = c.getString(R.string.manage_subscription_apple_info)
    val manageSubscriptionEndDatePrefix = c.getString(R.string.manage_subscription_end_date_prefix)
    val manageSubscriptionUserIdentifierTitle =
        c.getString(R.string.manage_subscription_user_identifier_title)
    val manageSubscriptionMultiDeviceSyncTitle =
        c.getString(R.string.manage_subscription_multi_device_sync_title)
    val manageSubscriptionUnlimitedDescription = c.getString(R.string.manage_subscription_unlimited_description)
    val manageSubscriptionPlanTitle = c.getString(R.string.manage_subscription_plan_title)
    val manageSubscriptionPlanNamePrefix = c.getString(R.string.manage_subscription_plan_name_prefix)
    val manageSubscriptionPlanPricePrefix = c.getString(R.string.manage_subscription_plan_price_prefix)
    val manageSubscriptionRenewsAt = c.getString(R.string.manage_subscription_renews_at)
    val manageSubscriptionEndsAt = c.getString(R.string.manage_subscription_ends_at)
    val manageSubscriptionBenefitsHeader = c.getString(R.string.manage_subscription_benefits_header)
    val manageSubscriptionItemsInVaultTitle = c.getString(R.string.manage_subscription_items_in_vault_title)
    val manageSubscriptionItemsInVaultSubtitle = c.getString(R.string.manage_subscription_items_in_vault_subtitle)
    val manageSubscriptionTrustedExtensionsTitle = c.getString(R.string.manage_subscription_trusted_extensions_title)
    val manageSubscriptionTrustedExtensionsSubtitle = c.getString(R.string.manage_subscription_trusted_extensions_subtitle)
    val manageSubscriptionUnlimited = c.getString(R.string.manage_subscription_unlimited)

    // Transfer
    val transferServicesListHeader = c.getString(R.string.transfer_services_list_header)
    val transferInstructionsHeader = c.getString(R.string.transfer_instructions_header__0025_0040)
    val transferInstructionsCtaJson = c.getString(R.string.transfer_instructions_cta_json)
    val transferInstructionsCtaCsv = c.getString(R.string.transfer_instructions_cta_csv)
    val transferInstructionsCtaZip = c.getString(R.string.transfer_instructions_cta_zip)
    val transferInstructionsCtaGeneric = c.getString(R.string.transfer_instructions_cta_generic)
    val transferInstructionsCtaBitwarden = c.getString(R.string.transfer_instructions_cta_bitwarden)
    val transferInstructionsAdditionalInfoBitwarden =
        c.getString(R.string.transfer_instructions_additional_info_bitwarden)
    val transferInstructionsCtaOnepassword = c.getString(R.string.transfer_instructions_cta_onepassword)
    val transferInstructionsAdditionalInfoOnepassword =
        c.getString(R.string.transfer_instructions_additional_info_onepassword)
    val transferInstructionsCtaDashlaneMobile =
        c.getString(R.string.transfer_instructions_cta_dashlane_mobile)
    val transferInstructionsCtaDashlanePc =
        c.getString(R.string.transfer_instructions_dashlane_pc)
    val transferInstructionsCtaProtonPass = c.getString(R.string.transfer_instructions_cta_proton_pass)
    val transferInstructionsAdditionalInfoProtonPass =
        c.getString(R.string.transfer_instructions_additional_info_proton_pass)
    val transferInstructionsBitwarden = c.getString(R.string.transfer_instructions_bitwarden)
    val transferInstructionsOnepassword = c.getString(R.string.transfer_instructions_onepassword)
    val transferInstructionsProtonpass = c.getString(R.string.transfer_instructions_protonpass)
    val transferInstructionsChrome = c.getString(R.string.transfer_instructions_chrome)
    val transferInstructionsFirefox = c.getString(R.string.transfer_instructions_firefox)
    val transferInstructionsLastpass = c.getString(R.string.transfer_instructions_lastpass)
    val transferInstructionsDashlaneMobile = c.getString(R.string.transfer_instructions_dashlane_mobile)
    val transferInstructionsApplePasswordsPc =
        c.getString(R.string.transfer_instructions_apple_passwords_pc)
    val transferInstructionsApplePasswordsMobile =
        c.getString(R.string.transfer_instructions_apple_passwords_mobile)
    val transferInstructionsKeepass = c.getString(R.string.transfer_instructions_keepass)
    val transferInstructionsKeepassxc = c.getString(R.string.transfer_instructions_keepassxc)
    val transferInstructionsMicrosoftEdge = c.getString(R.string.transfer_instructions_microsoft_edge)
    val transferInstructionsEnpass = c.getString(R.string.transfer_instructions_enpass)
    val transferInstructionsKeeper = c.getString(R.string.transfer_instructions_keeper)
    val transferImportingFileText = c.getString(R.string.transfer_importing_file_text)
    val transferImportingSuccessTitle = c.getString(R.string.transfer_importing_success_title)
    val transferImportingSuccessDescription =
        c.getString(R.string.transfer_importing_success_description)
    val transferImportingFailureTitle = c.getString(R.string.transfer_importing_failure_title)
    val transferImportingFailureDescription =
        c.getString(R.string.transfer_importing_failure_description)
    val externalImportTransferTitle = c.getString(R.string.external_import_transfer_title)
    val externalImportSuccessTitle = c.getString(R.string.external_import_success_title)
    val externalImportErrorGeneric = c.getString(R.string.external_import_error_generic)
    val externalImportErrorPrefix = c.getString(R.string.external_import_error_prefix)
    val externalImportErrorTitle = c.getString(R.string.external_import_error_title)

    val setupConnectIntroTitle = c.getString(R.string.connect_intro_header)
    val setupConnectIntroDescription = c.getString(R.string.connect_intro_description)
    val setupConnectLearnMore = c.getString(R.string.connect_intro_learn_more_cta)
    val connectEnterBrowserQrTitle = c.getString(R.string.connect_enter_browser_qr_title)
    val connectPermissionsEnable = c.getString(R.string.connect_permissions_enable)

    val setupConnectTitle = c.getString(R.string.connect_setup_header)
    val setupConnectDescription = c.getString(R.string.connect_setup_description)
    val setupConnectStepCameraTitle = c.getString(R.string.connect_setup_camera_step_title)
    val setupConnectStepCameraDescription = c.getString(R.string.connect_setup_camera_step_description)
    val setupConnectStepNotificationsTitle = c.getString(R.string.connect_setup_push_step_title)
    val setupConnectStepNotificationsDescription = c.getString(R.string.connect_setup_push_step_description)

    // Paywall
    val paywallNoticeCta = c.getString(R.string.paywall_notice_cta)
    val paywallNoticeItemsLimitReachedTitle = c.getString(R.string.paywall_notice_items_limit_reached_title)
    val paywallNoticeItemsLimitReachedMsg = c.getString(R.string.paywall_notice_items_limit_reached_msg)
    val paywallNoticeItemsLimitImportTitle = c.getString(R.string.paywall_notice_items_limit_import_title)
    val paywallNoticeItemsLimitImportMsg = c.getString(R.string.paywall_notice_items_limit_import_msg)
    val paywallNoticeItemsLimitTransferTitle = c.getString(R.string.paywall_notice_items_limit_transfer_title)
    val paywallNoticeItemsLimitTransferMsg = c.getString(R.string.paywall_notice_items_limit_transfer_msg)
    val paywallNoticeItemsLimitRestoreTitle = c.getString(R.string.paywall_notice_items_limit_restore_title)
    val paywallNoticeItemsLimitRestoreMsg = c.getString(R.string.paywall_notice_items_limit_restore_msg)
    val paywallNoticeMultiDeviceMsg = c.getString(R.string.paywall_notice_multi_device_msg)
    val paywallNoticeBrowsersLimitTitle = c.getString(R.string.paywall_notice_browsers_limit_title)
    val paywallNoticeBrowsersLimitMsg = c.getString(R.string.paywall_notice_browsers_limit_msg)

    val migrationErrorTitle = c.getString(R.string.migration_error_title)
    val migrationErrorBody = c.getString(R.string.migration_error_body)

    // Quick Setup
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
    val quickSetupIcloudSyncTitle = c.getString(R.string.quick_setup_icloud_sync_title)
    val quickSetupIcloudSyncDescription = c.getString(R.string.quick_setup_icloud_sync_description)
    val quickSetupIcloudSyncFailure = c.getString(R.string.quick_setup_icloud_sync_failure)

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
    val filterTagBannerIos = c.getString(R.string.filter_tag_banner_ios)

    // Customization
    val customizationDeviceNameErrorEmpty = c.getString(R.string.customization_device_name_error_empty)
    val customizationDeviceNameErrorMaxLength = c.getString(R.string.customization_device_name_error_max_length)

    // Import/Export
    val importExportBackupSavedToast = c.getString(R.string.import_export_backup_saved_toast)
    val importExportBackupFileTitle = c.getString(R.string.import_export_backup_file_title)
    val importExportImportSuccessfulToast = c.getString(R.string.import_export_import_successful_toast)
    val importExportSubtitle = c.getString(R.string.import_export_subtitle)
    val importExportAuthDescription = c.getString(R.string.import_export_auth_description)
    val cloudSyncInvalidSchemaErrorCta = c.getString(R.string.cloud_sync_invalid_schema_error_cta)
    val cloudSyncInvalidSchemaErrorMsg = c.getString(R.string.cloud_sync_invalid_schema_error_msg)
    val importInvalidSchemaErrorCta = c.getString(R.string.import_invalid_schema_error_cta)
    val importInvalidSchemaErrorMsg = c.getString(R.string.import_invalid_schema_error_msg)

    // Trash
    val trashDeletedAt = c.getString(R.string.trash_deleted_at__0025_0040)
    val trashEmpty = c.getString(R.string.trash_empty)
    val trashRemovePermanently = c.getString(R.string.trash_remove_permanently)
    val trashRestore = c.getString(R.string.trash_restore)
    val trashDeleteConfirmTitleIos = c.getString(R.string.trash_delete_confirm_title_ios)
    val trashDeleteConfirmBodyIos = c.getString(R.string.trash_delete_confirm_body_ios)
    val trashSelectedItems = c.getString(R.string.trash_selected_items)
    val trashDeleteConfirmTitle = c.getString(R.string.trash_delete_confirm_title)
    val trashDeleteConfirmBody = c.getString(R.string.trash_delete_confirm_body)

    val appUpdateModalTitle = c.getString(R.string.app_update_modal_title)
    val appUpdateModalSubtitle = c.getString(R.string.app_update_modal_subtitle)
    val appUpdateModalCtaPositive = c.getString(R.string.app_update_modal_cta_positive)
    val appUpdateModalCtaNegative = c.getString(R.string.app_update_modal_cta_negative)

    // Toast Messages
    val toastPasswordCopied = c.getString(R.string.toast_password_copied)
    val toastUsernameCopied = c.getString(R.string.toast_username_copied)
    val toastSecureNoteCopied = c.getString(R.string.toast_secure_note_copied)
    val toastCardNumberCopied = c.getString(R.string.toast_card_number_copied)
    val toastCardSecurityCodeCopied = c.getString(R.string.toast_card_security_code_copied)

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

    fun homeSelectionCount(count: Int): String {
        if (count == 0) return ""
        return c.resources.getQuantityString(R.plurals.home_selection_count, count, count)
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