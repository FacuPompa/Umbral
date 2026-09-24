import { Check, LockKeyhole } from 'lucide-react';
import { cn } from '@/lib/utils';
import StatusMessage from '@/components/StatusMessage';

export default function CheckpointRail({ checkpoints, progress, saving, onSelect }) {
  if (!checkpoints.length) {
    return <StatusMessage>Todavía no hay checkpoints aprobados para este juego.</StatusMessage>;
  }

  const ordered = [...checkpoints].sort((a, b) => a.position - b.position);

  return (
    <ol aria-label="Recorrido de checkpoints" aria-busy={saving}>
      {ordered.map((checkpoint, index) => {
        const current = progress?.checkpointId === checkpoint.id;
        const reached = progress && checkpoint.position <= progress.position;
        const upcoming = progress && checkpoint.position > progress.position;

        return (
          <li key={checkpoint.id} className="relative pb-3 last:pb-0">
            {index < ordered.length - 1 && (
              <span aria-hidden="true" className={cn('absolute bottom-0 left-[22px] top-6 border-l', reached && !current ? 'border-primary' : 'border-border')} />
            )}
            <button type="button" onClick={() => onSelect(checkpoint)} disabled={saving}
              aria-current={current ? 'step' : undefined}
              className={cn('relative grid min-h-16 w-full grid-cols-[44px_minmax(0,1fr)] items-start gap-3 rounded-md p-1 pl-0 text-left transition-colors hover:bg-accent disabled:opacity-60', current && 'bg-surface-subtle')}>
              <span aria-hidden="true" className={cn('flex size-11 items-center justify-center rounded-full border bg-background text-sm tabular-nums', current ? 'border-primary bg-primary text-primary-foreground' : reached ? 'border-primary text-primary' : 'border-border text-muted-foreground')}>
                {String(checkpoint.position).padStart(2, '0')}
              </span>
              <span className="grid min-w-0 gap-1 py-2 pr-2">
                <span className="break-words text-base leading-6 font-medium">{checkpoint.label}</span>
                <span className={cn('flex items-center gap-1 text-sm leading-5', current ? 'text-primary' : 'text-muted-foreground')}>
                  {current ? <><Check aria-hidden="true" className="size-4" />Tu avance actual</> : reached ? 'Tramo alcanzado' : upcoming ? 'Por alcanzar' : 'Marcar como mi avance'}
                </span>
              </span>
            </button>
            {current && (
              <p className="relative mb-3 ml-14 mt-2 grid gap-2 border-b border-primary pb-3 text-sm leading-5 text-muted-foreground">
                <span className="font-medium text-foreground">Podés leer hasta acá</span>
                <span className="flex items-start gap-2"><LockKeyhole aria-hidden="true" className="mt-0.5 size-4 shrink-0" />Las publicaciones posteriores quedan fuera de tu lectura.</span>
              </p>
            )}
          </li>
        );
      })}
    </ol>
  );
}
