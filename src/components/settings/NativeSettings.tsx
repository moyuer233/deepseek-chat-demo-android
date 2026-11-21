import { useRef, useState } from 'react'
import { useTranslate } from '../../i18n/index.js'
import { useSettings, setAutoCompact, setLocale, setModel, setThemePreference, setTtsVoice } from '../../services/settings.js'
import { nativeShell, readArchiveFile, requestChatArchive } from '../../services/deepseek/shell.js'
import { showToast } from '../base/toastState.js'
import { applyImport, BackupError, clearHistory, discardImport, exportHistory, exportHistoryToFile, prepareImport, type ImportMode, type ImportPreflight } from '../../services/chat/backup.js'
import { getSession, useChatStore } from '../../services/chat/store.js'
import { DshIcon } from '../chatInput/StatusIcons.js'
import { contextPressure, formatTokens, sessionUsage, totalUsage } from '../../services/chat/usage.js'
import { shareBlob } from '../../services/deepseek/share.js'
import { AccountPane } from './AccountPane.js'
import { SettingsRow as Row } from './SettingsRow.js'
import { Modal } from '../base/Modal.js'
import { ConfirmDialog } from '../base/ConfirmDialog.js'
import { Button } from '../base/Button.js'
import styles from './NativeSettings.module.css'
import {
  IconDataOutline16,
  IconDarkOutline16,
  IconLinkOutline16,
  IconPlayMediaOutline16,
  IconSettingsOutline16,
  IconTrashOutline16,
  IconUploadOutline14,
  IconDownloadOutline16,
  IconGlobeLanguageOutline14,
  IconWarningOutline16,
} from '../base/icons.js'

const Shell = ({ children }: { children: React.ReactNode }) => <div className={styles.overlay}>{children}</div>

/** 移动端原生风格设置页：账户 / 应用 / 语音 / 关于。 */
export const NativeSettings = ({ onClose }: { onClose: () => void }) => {
  const t = useTranslate()
  const locale = useSettings((s) => s.locale)
  const theme = useSettings((s) => s.themePreference)
  const voice = useSettings((s) => s.ttsVoice)
  const autoCompact = useSettings((s) => s.autoCompact)
  const model = useSettings((s) => s.model)
  const activeId = useChatStore((state) => state.activeId)
  const [page, setPage] = useState<'main' | 'account' | 'data' | 'chat'>('main')
  const fileRef = useRef<HTMLInputElement>(null)
  const [chatImportBusy, setChatImportBusy] = useState(false)
  const [pendingPreflight, setPendingPreflight] = useState<ImportPreflight | null>(null)
  const [importBusy, setImportBusy] = useState(false)

  const cycleLocale = () => setLocale(locale === 'zh-CN' ? 'en-US' : 'zh-CN')
  const cycleTheme = () => setThemePreference(theme === 'system' ? 'light' : theme === 'light' ? 'dark' : 'system')
  const cycleVoice = () => setTtsVoice(voice === 'stella' ? 'tide' : 'stella')
  const themeLabel =
    theme === 'system'
      ? t('avatarMenuSettingsDialogThemeSelectSystemOption')
      : theme === 'light'
        ? t('avatarMenuSettingsDialogThemeSelectLightOption')
        : t('avatarMenuSettingsDialogThemeSelectDarkOption')

  const startPrepare = async (file: File) => {
    if (importBusy) {
      return
    }
    setImportBusy(true)
    try {
      const diff = await prepareImport(file)
      if (diff.added === 0 && diff.updated === 0) {
        await discardImport()
        showToast({ key: 'importUpToDate' })
        return
      }
      setPendingPreflight(diff)
    } catch (error) {
      showToast({ key: error instanceof BackupError ? error.key : 'historyImportFailed', type: 'error' })
    } finally {
      setImportBusy(false)
    }
  }

  const runImport = async (mode: ImportMode) => {
    if (!pendingPreflight || importBusy) {
      return
    }
    setImportBusy(true)
    try {
      const count = await applyImport(mode)
      showToast({ key: count === 1 ? 'historyImportedOne' : 'historyImported', params: { count } })
      setPendingPreflight(null)
    } catch (error) {
      showToast({ key: error instanceof BackupError ? error.key : 'historyImportFailed', type: 'error' })
    } finally {
      setImportBusy(false)
    }
  }

  const cancelImport = () => {
    setPendingPreflight(null)
    void discardImport()
  }

  const [exporting, setExporting] = useState(false)

  const onExport = async () => {
    if (exporting) {
      return
    }
    setExporting(true)
    try {
      // 原生端边生成边落盘，避免整包 ZIP 驻留内存；保存完成的提示由原生弹出
      if (await exportHistoryToFile()) {
        return
      }
      const { name, blob } = await exportHistory()
      await shareBlob(name, blob)
    } catch (error) {
      showToast({ key: error instanceof BackupError ? error.key : 'historyExportFailed', type: 'error' })
    } finally {
      setExporting(false)
    }
  }

  const onChatImport = async () => {
    setChatImportBusy(true)
    try {
      const archive = await requestChatArchive()
      const file = await readArchiveFile(archive.path, archive.bytes, archive.name)
      nativeShell()?.deleteArchive?.(archive.path)
      await startPrepare(file)
    } catch (error) {
      showToast(error instanceof Error ? error.message : String(error))
    } finally {
      setChatImportBusy(false)
    }
  }

  const checkUpdate = async () => {
    try {
      const response = await fetch('https://api.github.com/repos/moyuer233/deepseek-chat-demo-android/releases/latest', {
        headers: { Accept: 'application/vnd.github+json' },
      })
      if (!response.ok) {
        throw new Error('HTTP ' + response.status)
      }
      const data = (await response.json()) as { tag_name?: string; html_url?: string }
      const latest = String(data.tag_name ?? '').replace(/^v/, '')
      if (latest && latest !== __APP_VERSION__) {
        showToast(t('updateAvailable') + ' ' + latest)
        if (data.html_url) {
          nativeShell()?.openExternal?.(data.html_url)
        }
      } else {
        showToast(t('updateLatest'))
      }
    } catch {
      showToast(t('updateCheckFailed'))
    }
  }

  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteBusy, setDeleteBusy] = useState(false)

  const onDelete = async () => {
    setDeleteBusy(true)
    try {
      await clearHistory()
      showToast({ key: 'historyDeleted' })
      setDeleteOpen(false)
    } catch {
      showToast({ key: 'historyDeleteFailed', type: 'error' })
    } finally {
      setDeleteBusy(false)
    }
  }

  const header = (title: string, onBack: () => void) => (
    <div className={styles.header}>
      <button type="button" className={styles.back} onClick={onBack} aria-label={t('back')}>‹</button>
      <div className={styles.title}>{title}</div>
      <span className={styles.spacer40} />
    </div>
  )

  if (page === 'account') {
    return (
      <Shell>
        {header(t('accountTab'), () => setPage('main'))}
        <div className={styles.body}>
          <AccountPane />
        </div>
      </Shell>
    )
  }
  if (page === 'chat') {
    const session = activeId ? (getSession(activeId) ?? null) : null
    const usage = session ? sessionUsage(session) : null
    const total = totalUsage()
    const pressure = contextPressure(session)
    const row = (glyph: 'checklist-outline' | 'data-outline' | 'layers-outline' | 'paper-plane-outline' | 'context-injection-outline', title: string, value: string) => (
      <Row icon={<DshIcon name={glyph} size={16} />} title={title} value={value} />
    )
    return (
      <Shell>
        {header(t('chatSettings'), () => setPage('main'))}
        <div className={styles.body}>
          <div className={styles.card}>
            <Row
              icon={<IconDataOutline16 />}
              title={t('modelSetting')}
              value={model === 'deepseek-v4-pro' ? 'deepseek-v4-pro' : 'deepseek-flash'}
              onClick={() => setModel(model === 'deepseek-flash' ? 'deepseek-v4-pro' : 'deepseek-flash')}
            />
            <Row
              icon={<DshIcon name="compact-outline" size={16} />}
              title={t('contextCompression')}
              value={autoCompact ? t('enabled') : t('disabled')}
              onClick={() => setAutoCompact(!autoCompact)}
            />
          </div>
          <div className={styles.section}>{t('usageSession')}</div>
          <div className={styles.card}>
            {usage ? (
              <>
                {row('checklist-outline', t('usageRequests'), String(usage.requests))}
                {row('data-outline', t('usageInput'), formatTokens(usage.input + usage.cacheWrite))}
                {row('layers-outline', t('usageCacheRead'), formatTokens(usage.cacheRead))}
                {row('paper-plane-outline', t('usageOutput'), formatTokens(usage.output))}
              </>
            ) : (
              <Row icon={<IconDataOutline16 />} title={t('usageRequests')} value="0" />
            )}
            {row('context-injection-outline', t('usageContext'), formatTokens(pressure.tokens) + ' / ' + formatTokens(pressure.window))}
          </div>
          <div className={styles.section}>{t('usageTotal')}</div>
          <div className={styles.card}>
            {row('checklist-outline', t('usageRequests'), String(total.requests))}
            {row('data-outline', t('usageInput'), formatTokens(total.input + total.cacheWrite))}
            {row('layers-outline', t('usageCacheRead'), formatTokens(total.cacheRead))}
            {row('paper-plane-outline', t('usageOutput'), formatTokens(total.output))}
          </div>
        </div>
      </Shell>
    )
  }
  if (page === 'data') {
    return (
      <Shell>
        {header(t('settingDialogAsideDataOptionLabel'), () => setPage('main'))}
        <div className={styles.body}>
          <div className={styles.card}>
            <Row icon={<IconUploadOutline14 />} title={t('historyImport')} onClick={() => fileRef.current?.click()} />
            <Row
              icon={<IconLinkOutline16 />}
              title={t('nativeSettingsImportChats') + (chatImportBusy ? '…' : '')}
              onClick={() => {
                if (!chatImportBusy) {
                  void onChatImport()
                }
              }}
            />
            <Row
              icon={<IconDownloadOutline16 />}
              title={t('exportAllLabel')}
              value={exporting ? '…' : undefined}
              onClick={() => {
                if (!exporting) {
                  void onExport()
                }
              }}
            />
            <Row
              icon={<IconTrashOutline16 />}
              title={t('deleteAllSessions')}
              danger
              onClick={() => {
                if (!deleteBusy) {
                  setDeleteOpen(true)
                }
              }}
            />
          </div>
          {deleteOpen ? (
            <ConfirmDialog
              title={t('deleteAllSessionsModalTitle')}
              lead={t('deleteAllSessionsModalDescription')}
              icon={<IconTrashOutline16 />}
              tone="danger"
              confirmText={t('delete')}
              confirmVariant="danger"
              busy={deleteBusy}
              onConfirm={() => void onDelete()}
              onClose={() => setDeleteOpen(false)}
            />
          ) : null}
          {pendingPreflight ? (
            <Modal
              title={t('importModeTitle')}
              onClose={cancelImport}
              footer={
                <Button variant="outlined" size="m" onClick={cancelImport}>
                  {t('cancel')}
                </Button>
              }
            >
              <div className={styles.importModes}>
                <p className={styles.importStats}>{t('importStats', { added: pendingPreflight.added, updated: pendingPreflight.updated, localOnly: pendingPreflight.localOnly })}</p>
                <p className={styles.importHint}>{t('importModeHint')}</p>
                <button type="button" className={styles.importMode} onClick={() => void runImport('merge')}>
                  <span className={styles.importModeTitle}>{t('importModeMerge')}</span>
                  <span className={styles.importModeDesc}>{t('importModeMergeDesc')}</span>
                </button>
                <button type="button" className={styles.importMode} onClick={() => void runImport('add')}>
                  <span className={styles.importModeTitle}>{t('importModeAdd')}</span>
                  <span className={styles.importModeDesc}>{t('importModeAddDesc')}</span>
                </button>
                <button type="button" className={styles.importModeDanger} onClick={() => void runImport('overwrite')}>
                  <span className={styles.importModeTitle}>{t('importModeOverwrite')}</span>
                  <span className={styles.importModeDesc}>{t('importModeOverwriteDesc')}</span>
                </button>
              </div>
            </Modal>
          ) : null}
          <input
            ref={fileRef}
            type="file"
            accept=".zip,application/zip"
            hidden
            onChange={(event) => {
              const file = event.target.files?.[0]
              event.target.value = ''
              if (file) {
                void startPrepare(file)
              }
            }}
          />
        </div>
      </Shell>
    )
  }

  return (
    <Shell>
      {header(t('settings'), onClose)}
      <div className={styles.body}>
        <div className={styles.section}>{t('nativeSettingsAccount')}</div>
        <div className={styles.card}>
          <Row icon={<IconSettingsOutline16 />} title={t('accountTab')} onClick={() => setPage('account')} />
          <Row icon={<IconDataOutline16 />} title={t('settingDialogAsideDataOptionLabel')} onClick={() => setPage('data')} />
        </div>

        <div className={styles.section}>{t('nativeSettingsApp')}</div>
        <div className={styles.card}>
          <Row icon={<DshIcon name="sparkle" size={16} />} title={t('chatSettings')} onClick={() => setPage('chat')} />
          <Row icon={<IconGlobeLanguageOutline14 />} title={t('settingsLocalePreference')} value={locale === 'zh-CN' ? '中文' : 'English'} onClick={cycleLocale} />
          <Row icon={<IconDarkOutline16 />} title={t('avatarMenuSettingsDialogThemeItemLabel')} value={themeLabel} onClick={cycleTheme} />
        </div>

        <div className={styles.section}>{t('nativeSettingsVoice')}</div>
        <div className={styles.card}>
          <Row icon={<IconPlayMediaOutline16 />} title={t('settingsTtsVoice')} value={voice} onClick={cycleVoice} />
        </div>

        <div className={styles.section}>{t('nativeSettingsAbout')}</div>
        <div className={styles.card}>
          <Row icon={<IconWarningOutline16 />} title={t('aboutVersion')} value={__APP_VERSION__} />
          <Row icon={<IconDownloadOutline16 />} title={t('checkUpdate')} onClick={checkUpdate} />
          <Row
            icon={(
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
                <path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4" />
                <path d="M9 18c-4.51 2-5-2-7-2" />
              </svg>
            )}
            title={t('aboutSource')} onClick={() => nativeShell()?.openExternal('https://github.com/moyuer233/deepseek-chat-demo-android')} />
        </div>
        <div className={styles.about}>{t('aboutDisclaimer')}</div>
      </div>
    </Shell>
  )
}
