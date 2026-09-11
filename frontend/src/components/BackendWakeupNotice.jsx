import LoadingIndicator from './LoadingIndicator';

export default function BackendWakeupNotice({ onRetry, retrying = false }) {
  return (
    <div className="backend-wakeup-notice" aria-live="polite">
      <LoadingIndicator label="Iniciando servidor" />
      <div>
        <h2>Iniciando servidor…</h2>
        <p>
          El servidor gratuito puede tardar unos segundos en despertar. Esto suele pasar
          después de un período de inactividad.
        </p>
      </div>
      {onRetry && (
        <button className="button-secondary" type="button" onClick={onRetry} disabled={retrying}>
          {retrying ? 'Reintentando…' : 'Reintentar'}
        </button>
      )}
    </div>
  );
}
