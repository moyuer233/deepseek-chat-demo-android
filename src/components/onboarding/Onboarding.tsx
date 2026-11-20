import { useEffect, useState } from 'react'
import { useTranslate } from '../../i18n/index.js'
import {
  cancelPreview,
  confirmImportAndCreateKey,
  resetAccountFlow,
  startLoginAndImport,
  startPlatformOAuth,
  useAccount,
} from '../../services/deepseek/account.js'
import { useSettings } from '../../services/settings.js'
import { Button } from '../base/Button.js'
import styles from './Onboarding.module.css'

type Step = 'welcome' | 'import' | 'auth' | 'done'

const progressKeys = {
  chatLogin: 'accountProgressChatLogin',
  exporting: 'accountProgressExporting',
  importing: 'accountProgressImporting',
  platformLogin: 'accountProgressPlatformLogin',
  creatingKey: 'accountProgressCreatingKey',
} as const

/** 首次启动引导：一步一步走完「登录导入（可跳过）→ 账号授权 → 主页面」。 */
export const Onboarding = ({ onFinish }: { onFinish: () => void }) => {
  const t = useTranslate()
  const phase = useAccount((state) => state.phase)
  const preview = useAccount((state) => state.preview)
  const error = useAccount((state) => state.error)
  const importedCount = useAccount((state) => state.importedCount)
  const accountToken = useSettings((state) => state.accountToken)
  const [step, setStep] = useState<Step>('welcome')
  const [authBusy, setAuthBusy] = useState(false)
  const [authError, setAuthError] = useState<string | null>(null)

  // 导入流程自己会接着做授权；完成或已有授权时直接进入下一步
  useEffect(() => {
    if (step === 'import' && phase === 'done') {
      setStep('auth')
    }
  }, [phase, step])

  useEffect(() => {
    if (step === 'auth' && accountToken) {
      setStep('done')
    }
  }, [accountToken, step])

  const busy = phase in progressKeys
  const progressKey = progressKeys[phase as keyof typeof progressKeys]

  const finish = () => {
    resetAccountFlow()
    try {
      localStorage.setItem('chat-demo:onboarded', '1')
    } catch {
      // 存储不可用时只是每次启动都展示引导
    }
    onFinish()
  }

  return (
    <div className={styles.mask} role="dialog" aria-modal="true" aria-labelledby="onboarding-title">
      <div className={styles.card}>
        <div className={styles.stepBar} aria-hidden="true">
          {(['welcome', 'import', 'auth', 'done'] as Step[]).map((item) => (
            <span
              key={item}
              className={item === step ? styles.dotActive : styles.dot}
            />
          ))}
        </div>

        {step === 'welcome' ? (
          <>
            <h1 id="onboarding-title" className={styles.title}>
              {t('onboardingTitle')}
            </h1>
            <p className={styles.pitch}>{t('onboardingPitch')}</p>
            <ol className={styles.steps}>
              <li>{t('onboardingStep1')}</li>
              <li>{t('onboardingStep2')}</li>
              <li>{t('onboardingStep3')}</li>
            </ol>
            <div className={styles.actions}>
              <Button size="m" autoFocus onClick={() => setStep('import')}>
                {t('onboardingStart')}
              </Button>
            </div>
            <p className={styles.note}>{t('onboardingNote')}</p>
          </>
        ) : null}

        {step === 'import' ? (
          <>
            <h1 id="onboarding-title" className={styles.title}>
              {t('onboardingImportTitle')}
            </h1>
            <p className={styles.pitch}>{t('onboardingImportPitch')}</p>
            {progressKey ? <p className={styles.status}>{t(progressKey)}</p> : null}
            {phase === 'error' ? (
              <p className={styles.error}>{error ?? t('unknownError')}</p>
            ) : null}
            {phase === 'preview' && preview ? (
              <div className={styles.previewBox}>
                <p className={styles.status}>
                  {t('accountPreviewTitle', { count: preview.sessions.length })}
                </p>
                <p className={styles.note}>{t('accountPreviewWarning')}</p>
              </div>
            ) : null}
            <div className={styles.actions}>
              {phase === 'preview' ? (
                <>
                  <Button size="m" variant="outlined" autoFocus onClick={cancelPreview}>
                    {t('cancel')}
                  </Button>
                  <Button size="m" onClick={() => void confirmImportAndCreateKey()}>
                    {t('accountPreviewConfirm')}
                  </Button>
                </>
              ) : phase === 'error' ? (
                <>
                  <Button size="m" autoFocus onClick={() => void startLoginAndImport()}>
                    {t('retry')}
                  </Button>
                  <Button size="m" variant="outlined" onClick={() => setStep('auth')}>
                    {t('onboardingSkip')}
                  </Button>
                </>
              ) : (
                <>
                  <Button size="m" autoFocus disabled={busy} onClick={() => void startLoginAndImport()}>
                    {t('onboardingLoginImport')}
                  </Button>
                  <Button size="m" variant="outlined" disabled={busy} onClick={() => setStep('auth')}>
                    {t('onboardingSkip')}
                  </Button>
                </>
              )}
            </div>
            <p className={styles.note}>{t('onboardingNote')}</p>
          </>
        ) : null}

        {step === 'auth' ? (
          <>
            <h1 id="onboarding-title" className={styles.title}>
              {t('onboardingAuthTitle')}
            </h1>
            <p className={styles.pitch}>{t('onboardingAuthPitch')}</p>
            {accountToken ? <p className={styles.status}>{t('accountLoggedInAccount')}</p> : null}
            {authError ? <p className={styles.error}>{authError}</p> : null}
            <div className={styles.actions}>
              {accountToken ? (
                <Button size="m" autoFocus onClick={() => setStep('done')}>
                  {t('onboardingNext')}
                </Button>
              ) : (
                <>
                  <Button
                    size="m"
                    autoFocus
                    disabled={authBusy}
                    onClick={() => {
                      setAuthBusy(true)
                      setAuthError(null)
                      void startPlatformOAuth()
                        .then(() => setStep('done'))
                        .catch((error: unknown) =>
                          setAuthError(error instanceof Error ? error.message : String(error))
                        )
                        .finally(() => setAuthBusy(false))
                    }}
                  >
                    {t('onboardingAuthButton')}
                  </Button>
                  <Button
                    size="m"
                    variant="outlined"
                    disabled={authBusy}
                    onClick={() => setStep('done')}
                  >
                    {t('onboardingSkip')}
                  </Button>
                </>
              )}
            </div>
            <p className={styles.note}>{t('onboardingNote')}</p>
          </>
        ) : null}

        {step === 'done' ? (
          <>
            <h1 id="onboarding-title" className={styles.title}>
              {t('onboardingDoneTitle')}
            </h1>
            <p className={styles.pitch}>
              {importedCount !== null
                ? t('accountImported', { count: importedCount })
                : t('onboardingDonePitch')}
            </p>
            <div className={styles.actions}>
              <Button size="m" autoFocus onClick={finish}>
                {t('onboardingEnter')}
              </Button>
            </div>
          </>
        ) : null}
      </div>
    </div>
  )
}
