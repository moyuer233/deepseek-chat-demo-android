import { App as NativeApp } from '@capacitor/app'
import { Capacitor } from '@capacitor/core'
import { useEffect } from 'react'
import { useTranslate } from '../i18n/index.js'
import { useSettings } from '../services/settings.js'
import { useIsMobile, useMobileViewport, usePreventPageScroll } from './viewport.js'
import {
  bindChatPersistence,
  initChat,
  retrySaveSessions,
  newChat,
  selectSession,
  useChatStore,
} from '../services/chat/store.js'
import { loadAccountToken } from '../services/deepseek/auth.js'
import { runBackHandler } from '../services/backHandlers.js'
import { nativeShell } from '../services/deepseek/shell.js'
import { SearchPanel } from '../components/search/SearchPanel.js'
import { closeSearchPanel, useSearchPanel } from '../components/search/searchPanelState.js'
import { SessionView } from '../components/session/SessionView.js'
import { MobileHeader } from '../components/session/MobileHeader.js'
import { Welcome } from '../components/session/Welcome.js'
import {
  closeSettings,
  ensureApiKey,
  useSettingsDialog,
} from '../components/settings/settingsDialogState.js'
import { SettingsDialog } from '../components/settings/SettingsDialog.js'
import { Toast } from '../components/base/Toast.js'
import { showToast } from '../components/base/toastState.js'
import { onUploadNotice } from '../services/attachments.js'
import { registerShellAuthCallbacks } from '../services/deepseek/shell.js'
import { installNativeSiderGesture } from '../services/nativeSiderGesture.js'
import { importPendingArchive } from '../services/chatImport.js'
import { Button } from '../components/base/Button.js'
import { stopReadAloud } from '../services/speech/readAloud.js'
import { CollapsedSider } from '../components/sider/CollapsedSider.js'
import { closeMobileSider, useMobileSider } from '../components/sider/mobileSiderState.js'
import { MobileSider, Sider } from '../components/sider/Sider.js'
import styles from './App.module.css'

const isNativeApp = Capacitor.isNativePlatform()

export const App = () => {
  const t = useTranslate()
  const isMobile = useIsMobile()
  const locale = useSettings((state) => state.locale)
  // 移动端侧边栏以独立抽屉管理开合状态
  const siderCollapsed = useSettings((state) => state.siderCollapsed) && !isMobile
  const ready = useChatStore((state) => state.ready)
  const initError = useChatStore((state) => state.initError)
  const saveError = useChatStore((state) => Object.values(state.sessionSaveErrors).some(Boolean))
  const importingHistory = useChatStore((state) => state.importingHistory)
  const session = useChatStore((state) =>
    state.activeId !== null && Object.hasOwn(state.sessionCache, state.activeId)
      ? state.sessionCache[state.activeId]
      : undefined
  )
  const sessionLoadError = useChatStore((state) =>
    state.activeId === null ? false : state.sessionLoadErrors[state.activeId] === true
  )
  const frameRef = useMobileViewport(isMobile)
  const streaming = useChatStore((state) =>
    state.activeId === null ? false : Object.hasOwn(state.streaming, state.activeId)
  )
  const activeId = useChatStore((state) => state.activeId)
  usePreventPageScroll(isMobile)

  useEffect(() => {
    document.documentElement.lang = locale
    // 原生文案跟随应用内语言，而不是只看系统语言
    nativeShell()?.setAppLocale?.(locale)
  }, [locale])

  // 文件数、存储空间达到上限时，附件上只显示「上传失败」，原因用 toast 说明
  useEffect(() => onUploadNotice(showToast), [])
  useEffect(bindChatPersistence, [])

  // 搜索结果面板只属于当前会话
  useEffect(closeSearchPanel, [activeId])
  // 切换会话时停止朗读
  useEffect(stopReadAloud, [activeId])
  // 移动端选中会话、切换布局后收起抽屉
  useEffect(closeMobileSider, [activeId, isMobile])

  // Android 返回键：按「弹窗 → 抽屉 → 搜索面板 → 会话 → 退出」逐级后退，而不是直接杀 App
  useEffect(() => {
    if (!Capacitor.isNativePlatform()) {
      return
    }
    let listener: { remove: () => void } | null = null
    let cancelled = false
    void NativeApp.addListener('backButton', () => {
      if (useSettingsDialog.getState().open) {
        closeSettings()
        return
      }
      if (useMobileSider.getState().open) {
        closeMobileSider()
        return
      }
      if (useSearchPanel.getState().entry !== null) {
        closeSearchPanel()
        return
      }
      if (useChatStore.getState().activeId !== null) {
        newChat()
        return
      }
      void NativeApp.exitApp()
    })
      .then((handle) => {
        if (cancelled) {
          handle.remove()
        } else {
          listener = handle
        }
      })
      .catch(() => {
        // 注册失败时不阻塞应用
      })
    return () => {
      cancelled = true
      listener?.remove()
    }
  }, [])

  useEffect(() => {
    // 原生壳返回键：网页按「预览 → 设置 → 抽屉 → 搜索面板 → 会话」逐级后退，返回 false 才退出 App
    const handler = () => {
      // 先让设置子页、弹窗这些页面内状态消费返回
      if (runBackHandler()) {
        return true
      }
      if (useSettingsDialog.getState().open) {
        closeSettings()
        return true
      }
      if (useMobileSider.getState().open) {
        closeMobileSider()
        return true
      }
      if (useSearchPanel.getState().entry !== null) {
        closeSearchPanel()
        return true
      }
      if (useChatStore.getState().activeId !== null) {
        newChat()
        return true
      }
      return false
    }
    ;(window as unknown as { __onBack?: () => boolean }).__onBack = handler
    return () => {
      delete (window as unknown as { __onBack?: () => boolean }).__onBack
    }
  }, [])

  useEffect(() => {
    registerShellAuthCallbacks()
    const disposeGesture = installNativeSiderGesture()
    void importPendingArchive()
    return () => disposeGesture?.()
  }, [])

  useEffect(() => {
    // 先读原生保存的账号 grant，再决定是否要提示填 Key
    void (async () => {
      await loadAccountToken()
      await initChat()
      // 原生端不再强制弹设置（欢迎页/引导页都有入口）；Web 端保持上游行为
      if (!isNativeApp) {
        ensureApiKey()
      }
    })()
  }, [])

  return (
    <>
      <div ref={frameRef} className={styles.frame}>
        {isMobile ? <MobileSider /> : siderCollapsed ? null : <Sider />}
        <main className={styles.main}>
          {saveError ? (
            <div className={styles.saveError} role="alert">
              <span>{t('sessionSaveFailed')}</span>
              <Button
                variant="outlined"
                size="s"
                disabled={importingHistory}
                onClick={retrySaveSessions}
              >
                {t('retry')}
              </Button>
            </div>
          ) : null}
          <div className={styles.content}>
            {siderCollapsed ? <CollapsedSider /> : null}
            {!ready || (activeId !== null && !session) ? (
              <div className={styles.loadingView}>
                {isMobile ? <MobileHeader /> : null}
                <div className={styles.loadingState} role="status" aria-live="polite">
                  <span>
                    {(!ready ? initError : sessionLoadError)
                      ? t(!ready ? 'sessionListLoadFailed' : 'sessionLoadFailed')
                      : t('sessionLoading')}
                  </span>
                  {(!ready ? initError : sessionLoadError) ? (
                    <Button
                      variant="outlined"
                      size="m"
                      onClick={() => {
                        if (!ready) {
                          void initChat()
                        } else if (activeId !== null) {
                          selectSession(activeId)
                        }
                      }}
                    >
                      {t('retry')}
                    </Button>
                  ) : null}
                </div>
              </div>
            ) : session ? (
              <SessionView
                session={session}
                streaming={streaming}
                siderCollapsed={siderCollapsed}
              />
            ) : (
              <Welcome />
            )}
          </div>
        </main>
        <SearchPanel />
      </div>
      <SettingsDialog />
      <Toast />
    </>
  )
}
