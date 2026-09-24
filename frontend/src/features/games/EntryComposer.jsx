import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/field';
import LoadingIndicator from '@/components/LoadingIndicator';

import { entryTypeLabels } from './journalEntryTypes';

export default function EntryComposer({ checkpoints, checkpointId, type, content, saving, onCheckpointChange, onTypeChange, onContentChange, onSubmit }) {
  return (
    <form className="grid gap-5" onSubmit={onSubmit} aria-busy={saving}>
      <fieldset disabled={saving} className="grid min-w-0 gap-5">
        <legend className="sr-only">Escribir una publicación</legend>
        <fieldset className="min-w-0">
          <legend className="mb-2 text-base leading-6 font-medium">Tipo de publicación</legend>
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
            {Object.entries(entryTypeLabels).map(([value, label]) => (
              <label key={value} className="relative min-w-0 cursor-pointer">
                <input className="peer sr-only" type="radio" name="entry-type" value={value} checked={type === value}
                  onChange={() => onTypeChange(value)} required />
                <span className="flex min-h-11 items-center justify-center rounded-md border border-input bg-popover px-2 py-2 text-sm font-medium text-muted-foreground peer-checked:border-primary peer-checked:bg-primary peer-checked:text-primary-foreground peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-ring peer-disabled:opacity-60">{label}</span>
              </label>
            ))}
          </div>
        </fieldset>
        <label className="grid gap-2 text-base leading-6 font-medium" htmlFor="entry-checkpoint">
          Esta entrada habla hasta
          <select id="entry-checkpoint" aria-describedby="entry-boundary-hint" required
            className="min-h-11 w-full min-w-0 rounded-md border border-input bg-popover px-3 text-base font-normal text-foreground disabled:opacity-60"
            value={checkpointId} onChange={(event) => onCheckpointChange(event.target.value)}>
            {checkpoints.map((checkpoint) => <option key={checkpoint.id} value={checkpoint.id}>{checkpoint.label}</option>)}
          </select>
        </label>
        <p id="entry-boundary-hint" className="text-sm leading-5 text-muted-foreground">Elegí el último tramo del que hablás. Solo pueden leerla quienes llegaron hasta ahí.</p>
        <div className="grid gap-2">
          <label className="text-base leading-6 font-medium" htmlFor="entry-content">Tu entrada</label>
          <Textarea id="entry-content" className="min-h-44" value={content} maxLength={5000} required
            aria-describedby="entry-character-count" onChange={(event) => onContentChange(event.target.value)} />
          <p id="entry-character-count" className="text-right text-sm tabular-nums text-muted-foreground">
            {content.length.toLocaleString('es-AR')} / 5.000<span className="sr-only"> caracteres</span>
          </p>
        </div>
      </fieldset>
      <div className="flex justify-end">
        <Button disabled={saving || !checkpointId || !type || !content.trim()} type="submit">
          {saving ? <LoadingIndicator label="Publicando entrada" showLabel /> : 'Publicar'}
        </Button>
      </div>
    </form>
  );
}
