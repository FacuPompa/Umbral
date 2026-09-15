import LoadingIndicator from './LoadingIndicator';
import { Button } from './ui/button';

export default function BackendWakeupNotice({ onRetry, retrying = false }) {
  return (
    <div className="grid max-w-[420px] justify-items-start gap-4" aria-live="polite">
      <LoadingIndicator label="Iniciando servidor" />
      <div className="grid gap-3">
        <h2 className="text-2xl leading-[30px] font-semibold tracking-normal">Iniciando servidor…</h2>
        <p className="text-base leading-6 text-muted-foreground">
          El servidor gratuito puede tardar unos segundos en despertar. Esto suele pasar
          después de un período de inactividad.
        </p>
      </div>
      {onRetry && (
        <Button variant="outline" type="button" onClick={onRetry} disabled={retrying}>
          {retrying ? 'Reintentando…' : 'Reintentar'}
        </Button>
      )}
    </div>
  );
}
