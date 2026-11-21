import { useRef, useState } from 'react'
import { useTranslate } from '../../i18n/index.js'
import { useSettings, setLocale, setThemePreference, setTtsVoice } from '../../services/settings.js'
import { openRecharge } from '../../services/deepseek/account.js'
import { nativeShell, readArchiveBytes, requestChatArchive } from '../../services/deepseek/shell.js'
import { showToast } from '../base/toastState.js'
import { clearHistory, exportHistory, importHistory } from '../../services/chat/backup.js'
import { shareBlob } from '../../services/deepseek/share.js'
import { AccountPane } from './AccountPane.js'
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

const Row = ({
  icon,
  title,
  value,
  danger,
  onClick,
}: {
  icon: React.ReactNode
  title: string
  value?: string
  danger?: boolean
  onClick?: () => void
}) => (
  <button type="button" className={styles.row} onClick={onClick} style={danger ? { color: '#d93025' } : undefined}>
    <span className={styles.rowIcon} aria-hidden>{icon}</span>
    <span className={styles.rowTitle}>{title}</span>
    {value ? <span className={styles.rowValue}>{value}</span> : null}
    {onClick ? <span className={styles.chevron}>›</span> : null}
  </button>
)

/** 移动端原生风格设置页：账户 / 应用 / 语音 / 关于。 */
export const NativeSettings = ({ onClose }: { onClose: () => void }) => {
  const t = useTranslate()
  const locale = useSettings((s) => s.locale)
  const theme = useSettings((s) => s.themePreference)
  const voice = useSettings((s) => s.ttsVoice)
  const [page, setPage] = useState<'main' | 'account' | 'data'>('main')
  const fileRef = useRef<HTMLInputElement>(null)
  const [chatImportBusy, setChatImportBusy] = useState(false)

  const cycleLocale = () => setLocale(locale === 'zh-CN' ? 'en-US' : 'zh-CN')
  const cycleTheme = () => setThemePreference(theme === 'system' ? 'light' : theme === 'light' ? 'dark' : 'system')
  const cycleVoice = () => setTtsVoice(voice === 'stella' ? 'tide' : 'stella')
  const themeLabel =
    theme === 'system'
      ? t('avatarMenuSettingsDialogThemeSelectSystemOption')
      : theme === 'light'
        ? t('avatarMenuSettingsDialogThemeSelectLightOption')
        : t('avatarMenuSettingsDialogThemeSelectDarkOption')

  const onImport = async (file: File) => {
    // oxlint-disable-next-line no-alert -- WebView 通过 onJsConfirm 显示原生确认框
    if (!window.confirm(t('historyImportConfirmTitle') + '\n' + t('historyImportConfirmDescription'))) {
      return
    }
    try {
      const count = await importHistory(file)
      showToast({ key: count === 1 ? 'historyImportedOne' : 'historyImported', params: { count } })
    } catch {
      showToast({ key: 'historyImportFailed', type: 'error' })
    }
  }

  const onExport = async () => {
    try {
      const { name, blob } = await exportHistory()
      await shareBlob(name, blob)
    } catch {
      showToast({ key: 'historyExportFailed', type: 'error' })
    }
  }

  const onChatImport = async () => {
    // oxlint-disable-next-line no-alert -- WebView 通过 onJsConfirm 显示原生确认框
    if (!window.confirm(t('historyImportConfirmTitle') + '\n' + t('historyImportConfirmDescription'))) {
      return
    }
    setChatImportBusy(true)
    try {
      const archive = await requestChatArchive()
      const bytes = readArchiveBytes(archive.path, archive.bytes)
      const file = new File([bytes], archive.name, { type: 'application/zip' })
      const count = await importHistory(file)
      nativeShell()?.deleteArchive?.(archive.path)
      showToast({ key: count === 1 ? 'historyImportedOne' : 'historyImported', params: { count } })
    } catch (error) {
      showToast(error instanceof Error ? error.message : String(error))
    } finally {
      setChatImportBusy(false)
    }
  }

  const onDelete = async () => {
    // oxlint-disable-next-line no-alert -- WebView 通过 onJsConfirm 显示原生确认框
    if (!window.confirm(t('deleteAllSessionsModalDescription'))) {
      return
    }
    try {
      await clearHistory()
      showToast({ key: 'historyDeleted' })
    } catch {
      showToast({ key: 'historyDeleteFailed', type: 'error' })
    }
  }

  const header = (title: string, onBack: () => void) => (
    <div className={styles.header}>
      <button type="button" className={styles.back} onClick={onBack} aria-label="返回">‹</button>
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
  if (page === 'data') {
    return (
      <Shell>
        {header(t('settingDialogAsideDataOptionLabel'), () => setPage('main'))}
        <div className={styles.body}>
          <div className={styles.card}>
            <Row icon={<IconUploadOutline14 />} title={t('upload')} onClick={() => fileRef.current?.click()} />
            <Row
              icon={<IconLinkOutline16 />}
              title={(locale === 'zh-CN' ? '从 chat.deepseek.com 导入会话' : 'Import chats from chat.deepseek.com') + (chatImportBusy ? '…' : '')}
              onClick={() => {
                if (!chatImportBusy) {
                  void onChatImport()
                }
              }}
            />
            <Row icon={<IconDownloadOutline16 />} title={t('exportAllDownload')} onClick={() => void onExport()} />
            <Row icon={<IconTrashOutline16 />} title={t('deleteAllSessions')} danger onClick={() => void onDelete()} />
          </div>
          <input
            ref={fileRef}
            type="file"
            accept=".zip,application/zip"
            hidden
            onChange={(event) => {
              const file = event.target.files?.[0]
              event.target.value = ''
              if (file) {
                void onImport(file)
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
        <div className={styles.section}>{locale === 'zh-CN' ? '账户' : 'Account'}</div>
        <div className={styles.card}>
          <Row icon={<IconSettingsOutline16 />} title={t('accountTab')} onClick={() => setPage('account')} />
          <Row icon={<IconDataOutline16 />} title={t('settingDialogAsideDataOptionLabel')} onClick={() => setPage('data')} />
          <Row icon={<IconLinkOutline16 />} title={t('accountRecharge')} onClick={() => void openRecharge()} />
        </div>

        <div className={styles.section}>{locale === 'zh-CN' ? '应用' : 'App'}</div>
        <div className={styles.card}>
          <Row icon={<IconGlobeLanguageOutline14 />} title={t('settingsLocalePreference')} value={locale === 'zh-CN' ? '中文' : 'English'} onClick={cycleLocale} />
          <Row icon={<IconDarkOutline16 />} title={t('avatarMenuSettingsDialogThemeItemLabel')} value={themeLabel} onClick={cycleTheme} />
        </div>

        <div className={styles.section}>{locale === 'zh-CN' ? '语音' : 'Voice'}</div>
        <div className={styles.card}>
          <Row icon={<IconPlayMediaOutline16 />} title={t('settingsTtsVoice')} value={voice} onClick={cycleVoice} />
        </div>

        <div className={styles.section}>{locale === 'zh-CN' ? '关于' : 'About'}</div>
        <div className={styles.card}>
          <Row icon={<IconWarningOutline16 />} title={t('aboutVersion')} value="0.3.0" />
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
